package com.example.data.api

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object DrugRepository {

    private const val TAG = "DrugRepository"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val rxNavService: RxNavApiService = Retrofit.Builder()
        .baseUrl("https://rxnav.nlm.nih.gov/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(RxNavApiService::class.java)

    private val openFdaService: OpenFdaApiService = Retrofit.Builder()
        .baseUrl("https://api.fda.gov/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OpenFdaApiService::class.java)

    /**
     * Search medications by name for auto-complete using RxNav and OpenFDA.
     */
    suspend fun searchDrugs(query: String): List<DrugSearchResult> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext emptyList()
        val cleanQuery = query.trim()
        val results = mutableListOf<DrugSearchResult>()

        try {
            // 1. Query RxNav Drugs API
            val rxDrugs = rxNavService.getDrugsByName(cleanQuery)
            rxDrugs.drugGroup?.conceptGroup?.forEach { group ->
                group.conceptProperties?.forEach { prop ->
                    if (!prop.name.isNullOrBlank()) {
                        results.add(
                            DrugSearchResult(
                                rxcui = prop.rxcui,
                                name = prop.name,
                                brandName = if (group.tty == "SBD" || group.tty == "BN") prop.name else null,
                                genericName = if (group.tty == "SCD" || group.tty == "IN") prop.name else null,
                                dosageForm = group.tty
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "RxNav search error: ${e.message}")
        }

        try {
            // 2. Query RxNav approximate terms if direct drugs yielded few results
            if (results.size < 5) {
                val approx = rxNavService.getApproximateTerms(cleanQuery, maxEntries = 10)
                approx.approximateGroup?.candidate?.forEach { cand ->
                    if (!cand.rxcui.isNullOrEmpty() && results.none { it.rxcui == cand.rxcui }) {
                        results.add(
                            DrugSearchResult(
                                rxcui = cand.rxcui,
                                name = "$cleanQuery (RxCUI ${cand.rxcui})"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "RxNav approximate search error: ${e.message}")
        }

        try {
            // 3. Query OpenFDA Drug Label
            val fdaResponse = openFdaService.searchDrugLabel("openfda.brand_name:\"$cleanQuery\"+OR+openfda.generic_name:\"$cleanQuery\"", limit = 5)
            fdaResponse.results?.forEach { item ->
                val bName = item.openfda?.brandName?.firstOrNull()
                val gName = item.openfda?.genericName?.firstOrNull()
                val active = item.activeIngredient?.firstOrNull() ?: item.openfda?.genericName?.joinToString(", ")
                val rxcui = item.openfda?.rxcui?.firstOrNull()
                val displayName = bName ?: gName ?: cleanQuery

                if (results.none { it.name.equals(displayName, ignoreCase = true) }) {
                    results.add(
                        0, DrugSearchResult(
                            rxcui = rxcui,
                            name = displayName,
                            brandName = bName,
                            genericName = gName,
                            activeIngredient = active
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "OpenFDA label search error: ${e.message}")
        }

        // Deduplicate and return top 15
        results.distinctBy { it.name.lowercase() }.take(15)
    }

    /**
     * Search drug details by UPC or NDC barcode using OpenFDA.
     */
    suspend fun searchByBarcodeOrUpc(rawCode: String): PillDetailInfo? = withContext(Dispatchers.IO) {
        val cleanCode = rawCode.trim().replace("-", "").replace(" ", "")
        if (cleanCode.length < 5) return@withContext null

        try {
            val query = "openfda.upc:\"$cleanCode\"+OR+openfda.package_ndc:\"$cleanCode\"+OR+package_ndc:\"$cleanCode\""
            val fdaResp = openFdaService.searchDrugLabel(query, limit = 1)
            val result = fdaResp.results?.firstOrNull()

            if (result != null) {
                return@withContext parseOpenFdaToPillDetail(result, cleanCode)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Barcode OpenFDA lookup error: ${e.message}")
        }

        // Fallback: try searching openFDA general label with code
        try {
            val fdaResp = openFdaService.searchDrugLabel("\"$cleanCode\"", limit = 1)
            val result = fdaResp.results?.firstOrNull()
            if (result != null) {
                return@withContext parseOpenFdaToPillDetail(result, cleanCode)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Barcode fallback lookup error: ${e.message}")
        }

        null
    }

    /**
     * Fetch comprehensive Pill Detail Info (Visual description, Food Warnings, Warnings) for a medication name or RxCUI.
     */
    suspend fun getPillDetailInfo(medName: String, rxcui: String? = null, upc: String? = null): PillDetailInfo = withContext(Dispatchers.IO) {
        val cleanName = medName.trim()
        var fdaResult: OpenFdaLabelResult? = null

        // 1. Search OpenFDA by RxCUI if available
        if (!rxcui.isNullOrEmpty()) {
            try {
                val resp = openFdaService.searchDrugLabel("openfda.rxcui:\"$rxcui\"", limit = 1)
                fdaResult = resp.results?.firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "OpenFDA search by RxCUI error: ${e.message}")
            }
        }

        // 2. Search OpenFDA by UPC if available
        if (fdaResult == null && !upc.isNullOrEmpty()) {
            try {
                val resp = openFdaService.searchDrugLabel("openfda.upc:\"$upc\"", limit = 1)
                fdaResult = resp.results?.firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "OpenFDA search by UPC error: ${e.message}")
            }
        }

        // 3. Search OpenFDA by medication name
        if (fdaResult == null && cleanName.isNotEmpty()) {
            try {
                val sanitized = cleanName.replace(Regex("[^a-zA-Z0-9 ]"), "")
                val resp = openFdaService.searchDrugLabel("openfda.brand_name:\"$sanitized\"+OR+openfda.generic_name:\"$sanitized\"", limit = 1)
                fdaResult = resp.results?.firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "OpenFDA search by name error: ${e.message}")
            }
        }

        if (fdaResult != null) {
            return@withContext parseOpenFdaToPillDetail(fdaResult, upc, cleanName, rxcui)
        }

        // Fallback default pill detail if API fails or drug label not found
        PillDetailInfo(
            name = cleanName,
            brandName = cleanName,
            genericName = null,
            rxcui = rxcui,
            upcOrNdc = upc,
            activeIngredients = null,
            visualDescription = "Standard tablet or capsule. Verify dosage instructions on the official prescription box.",
            foodWarnings = parseFoodWarningsFromText(cleanName, emptyList(), emptyList()),
            warnings = listOf("Always take medications exactly as prescribed by your physician or pharmacist."),
            dosageGuidance = "Consult official package label for administration instructions.",
            manufacturer = null
        )
    }

    /**
     * Check for Drug-to-Drug interactions between a list of RxCUIs or drug names using RxNav API.
     */
    suspend fun checkDrugInteractions(medications: List<Pair<String, String?>>): List<DrugInteractionResult> = withContext(Dispatchers.IO) {
        // Pair<MedicationName, Rxcui?>
        if (medications.size < 2) return@withContext emptyList()
        val interactions = mutableListOf<DrugInteractionResult>()

        // Extract valid RxCUIs or look them up
        val rxcuiMap = mutableMapOf<String, String>() // rxcui -> medName
        medications.forEach { (name, rxcui) ->
            if (!rxcui.isNullOrEmpty()) {
                rxcuiMap[rxcui] = name
            } else {
                // Try resolving RxCUI on the fly
                try {
                    val resp = rxNavService.getRxcuiByName(name)
                    val foundId = resp.idGroup?.rxnormId?.firstOrNull()
                    if (foundId != null) {
                        rxcuiMap[foundId] = name
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed resolving RxCUI for $name: ${e.message}")
                }
            }
        }

        if (rxcuiMap.size >= 2) {
            try {
                val rxCuiQuery = rxcuiMap.keys.joinToString("+")
                val resp = rxNavService.getInteractionList(rxCuiQuery)

                resp.fullInteractionTypeGroup?.forEach { group ->
                    group.fullInteractionType?.forEach { fit ->
                        fit.interactionPair?.forEach { pair ->
                            val concepts = pair.interactionConcept
                            if (concepts != null && concepts.size >= 2) {
                                val c1 = concepts[0].minConceptItem
                                val c2 = concepts[1].minConceptItem
                                val name1 = rxcuiMap[c1?.rxcui] ?: c1?.name ?: "Medication"
                                val name2 = rxcuiMap[c2?.rxcui] ?: c2?.name ?: "Medication"

                                val severityEnum = when (pair.severity?.lowercase()) {
                                    "high" -> WarningSeverity.HIGH
                                    "moderate" -> WarningSeverity.MODERATE
                                    else -> WarningSeverity.INFO
                                }

                                interactions.add(
                                    DrugInteractionResult(
                                        drug1Name = name1,
                                        drug1Rxcui = c1?.rxcui,
                                        drug2Name = name2,
                                        drug2Rxcui = c2?.rxcui,
                                        severity = severityEnum,
                                        description = pair.description ?: "Potential interaction detected between $name1 and $name2."
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "RxNav Interaction API call failed: ${e.message}")
            }
        }

        // Heuristic fallback for common high-profile drug interactions
        val namesLower = medications.map { it.first.lowercase() }
        checkHeuristicInteractions(namesLower, medications, interactions)

        interactions.distinctBy { "${it.drug1Name.lowercase()}_${it.drug2Name.lowercase()}" }
    }

    private fun parseOpenFdaToPillDetail(
        item: OpenFdaLabelResult,
        fallbackUpc: String? = null,
        fallbackName: String? = null,
        fallbackRxcui: String? = null
    ): PillDetailInfo {
        val brand = item.openfda?.brandName?.firstOrNull()
        val generic = item.openfda?.genericName?.firstOrNull()
        val name = brand ?: generic ?: fallbackName ?: "Medication"
        val rxcui = item.openfda?.rxcui?.firstOrNull() ?: fallbackRxcui
        val upc = item.openfda?.upc?.firstOrNull() ?: item.openfda?.packageNdc?.firstOrNull() ?: fallbackUpc

        val active = item.activeIngredient?.joinToString(", ") ?: item.openfda?.genericName?.joinToString(", ")

        // Extract Visual Description
        val descList = item.description ?: item.splProductDataElements ?: emptyList()
        val descText = descList.joinToString(" ")
        val visualDesc = if (descText.length > 300) descText.take(300) + "..." else descText.ifEmpty {
            "Verified FDA medication listing ($name). Check package insert for physical tablet markings."
        }

        // Extract Food Warnings
        val foodWarnings = parseFoodWarningsFromText(
            name = name,
            foodInteractions = item.foodInteractions ?: emptyList(),
            drugInteractions = (item.drugInteractions ?: emptyList()) + (item.warnings ?: emptyList())
        )

        // Extract General Warnings
        val warningsList = (item.warnings ?: item.precautions ?: emptyList())
            .map { if (it.length > 200) it.take(200) + "..." else it }
            .take(3)

        val dosageGuidance = item.dosageAndAdministration?.firstOrNull()?.let {
            if (it.length > 250) it.take(250) + "..." else it
        }

        val manufacturer = item.openfda?.manufacturerName?.firstOrNull()

        return PillDetailInfo(
            name = name,
            brandName = brand,
            genericName = generic,
            rxcui = rxcui,
            upcOrNdc = upc,
            activeIngredients = active,
            visualDescription = visualDesc,
            foodWarnings = foodWarnings,
            warnings = warningsList.ifEmpty { listOf("Take with water as directed by your physician.") },
            dosageGuidance = dosageGuidance,
            manufacturer = manufacturer
        )
    }

    private fun parseFoodWarningsFromText(
        name: String,
        foodInteractions: List<String>,
        drugInteractions: List<String>
    ): List<FoodWarningItem> {
        val combinedText = (foodInteractions + drugInteractions).joinToString(" ").lowercase() + " " + name.lowercase()
        val items = mutableListOf<FoodWarningItem>()

        // 1. Grapefruit Interaction
        if (combinedText.contains("grapefruit") || combinedText.contains("citrus juice")) {
            items.add(
                FoodWarningItem(
                    title = "Grapefruit Interaction Alert",
                    icon = "grapefruit",
                    warningText = "Do not consume grapefruit or grapefruit juice. Grapefruit blocks enzymes that break down this drug, causing dangerous blood levels.",
                    severity = WarningSeverity.HIGH
                )
            )
        }

        // 2. Dairy & Calcium
        if (combinedText.contains("dairy") || combinedText.contains("milk") || combinedText.contains("calcium") || combinedText.contains("antacid")) {
            items.add(
                FoodWarningItem(
                    title = "Dairy & Calcium Restriction",
                    icon = "dairy",
                    warningText = "Avoid taking within 2 hours of milk, cheese, yogurt, or calcium supplements as calcium binds to the drug and reduces absorption.",
                    severity = WarningSeverity.HIGH
                )
            )
        }

        // 3. Alcohol Warning
        if (combinedText.contains("alcohol") || combinedText.contains("ethanol") || combinedText.contains("liquor") || combinedText.contains("drowsiness")) {
            items.add(
                FoodWarningItem(
                    title = "Strict Alcohol Warning",
                    icon = "alcohol",
                    warningText = "Avoid alcohol. Alcohol increases central nervous system depression, dizziness, or risk of liver and stomach toxicity.",
                    severity = WarningSeverity.HIGH
                )
            )
        }

        // 4. Potassium / Salt Substitutes
        if (combinedText.contains("potassium") || combinedText.contains("salt substitute")) {
            items.add(
                FoodWarningItem(
                    title = "Potassium Caution",
                    icon = "potassium",
                    warningText = "Limit high-potassium foods (bananas, spinach) or salt substitutes, as this medication can elevate blood potassium levels.",
                    severity = WarningSeverity.MODERATE
                )
            )
        }

        // 5. Take with food / empty stomach
        if (combinedText.contains("take with food") || combinedText.contains("with meals") || combinedText.contains("stomach upset")) {
            items.add(
                FoodWarningItem(
                    title = "Take With Food",
                    icon = "food",
                    warningText = "Administer with a meal or light snack to minimize stomach irritation and maximize drug absorption.",
                    severity = WarningSeverity.INFO
                )
            )
        } else if (combinedText.contains("empty stomach") || combinedText.contains("1 hour before meal")) {
            items.add(
                FoodWarningItem(
                    title = "Take On Empty Stomach",
                    icon = "food",
                    warningText = "Take on an empty stomach (at least 1 hour before or 2 hours after meals) with a full glass of water.",
                    severity = WarningSeverity.INFO
                )
            )
        }

        return items
    }

    private fun checkHeuristicInteractions(
        namesLower: List<String>,
        medications: List<Pair<String, String?>>,
        interactions: MutableList<DrugInteractionResult>
    ) {
        fun containsMed(query: String) = namesLower.any { it.contains(query) }
        fun getRealName(query: String) = medications.firstOrNull { it.first.lowercase().contains(query) }?.first
            ?: query.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }

        // Aspirin + Warfarin / Blood Thinners
        if ((containsMed("aspirin") || containsMed("ibuprofen") || containsMed("naproxen")) &&
            (containsMed("warfarin") || containsMed("coumadin") || containsMed("heparin") || containsMed("eliquis") || containsMed("plavix"))
        ) {
            val d1 = if (containsMed("aspirin")) getRealName("aspirin") else getRealName("ibuprofen")
            val d2 = if (containsMed("warfarin")) getRealName("warfarin") else getRealName("eliquis")
            interactions.add(
                DrugInteractionResult(
                    drug1Name = d1,
                    drug1Rxcui = null,
                    drug2Name = d2,
                    drug2Rxcui = null,
                    severity = WarningSeverity.HIGH,
                    description = "Combining NSAIDs/Aspirin with anticoagulant blood thinners severely increases risk of internal gastrointestinal bleeding."
                )
            )
        }

        // ACE Inhibitors (Lisinopril) + Potassium / Spironolactone
        if ((containsMed("lisinopril") || containsMed("enalapril") || containsMed("losartan")) &&
            (containsMed("potassium") || containsMed("spironolactone"))
        ) {
            val d1 = getRealName("lisinopril")
            val d2 = getRealName("potassium")
            interactions.add(
                DrugInteractionResult(
                    drug1Name = d1,
                    drug1Rxcui = null,
                    drug2Name = d2,
                    drug2Rxcui = null,
                    severity = WarningSeverity.HIGH,
                    description = "Combining ACE inhibitors/ARBs with potassium supplements can cause dangerous hyperkalemia (high blood potassium)."
                )
            )
        }

        // Metformin + Alcohol
        if (containsMed("metformin") && containsMed("alcohol")) {
            interactions.add(
                DrugInteractionResult(
                    drug1Name = getRealName("metformin"),
                    drug1Rxcui = null,
                    drug2Name = "Alcohol",
                    drug2Rxcui = null,
                    severity = WarningSeverity.HIGH,
                    description = "Metformin combined with excessive alcohol increases the risk of acute lactic acidosis."
                )
            )
        }
    }
}
