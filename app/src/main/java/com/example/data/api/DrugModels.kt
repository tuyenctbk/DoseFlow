package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- RxNav Models ---
@JsonClass(generateAdapter = true)
data class RxNavSpellingResponse(
    @param:Json(name = "suggestionGroup") val suggestionGroup: SuggestionGroup? = null
)

@JsonClass(generateAdapter = true)
data class SuggestionGroup(
    @param:Json(name = "suggestionList") val suggestionList: SuggestionList? = null
)

@JsonClass(generateAdapter = true)
data class SuggestionList(
    @param:Json(name = "suggestion") val suggestion: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class RxNavApproximateResponse(
    @param:Json(name = "approximateGroup") val approximateGroup: ApproximateGroup? = null
)

@JsonClass(generateAdapter = true)
data class ApproximateGroup(
    @param:Json(name = "candidate") val candidate: List<RxCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class RxCandidate(
    @param:Json(name = "rxcui") val rxcui: String? = null,
    @param:Json(name = "score") val score: String? = null,
    @param:Json(name = "rank") val rank: String? = null
)

@JsonClass(generateAdapter = true)
data class RxNavDrugsResponse(
    @param:Json(name = "drugGroup") val drugGroup: DrugGroup? = null
)

@JsonClass(generateAdapter = true)
data class DrugGroup(
    @param:Json(name = "conceptGroup") val conceptGroup: List<ConceptGroup>? = null
)

@JsonClass(generateAdapter = true)
data class ConceptGroup(
    @param:Json(name = "tty") val tty: String? = null,
    @param:Json(name = "conceptProperties") val conceptProperties: List<ConceptProperty>? = null
)

@JsonClass(generateAdapter = true)
data class ConceptProperty(
    @param:Json(name = "rxcui") val rxcui: String? = null,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "synonym") val synonym: String? = null,
    @param:Json(name = "tty") val tty: String? = null
)

@JsonClass(generateAdapter = true)
data class RxNavRxcuiResponse(
    @param:Json(name = "idGroup") val idGroup: IdGroup? = null
)

@JsonClass(generateAdapter = true)
data class IdGroup(
    @param:Json(name = "rxnormId") val rxnormId: List<String>? = null
)

// --- RxNav Interactions ---
@JsonClass(generateAdapter = true)
data class RxNavInteractionListResponse(
    @param:Json(name = "fullInteractionTypeGroup") val fullInteractionTypeGroup: List<FullInteractionTypeGroup>? = null,
    @param:Json(name = "nlmDisclaimer") val nlmDisclaimer: String? = null
)

@JsonClass(generateAdapter = true)
data class FullInteractionTypeGroup(
    @param:Json(name = "sourceName") val sourceName: String? = null,
    @param:Json(name = "fullInteractionType") val fullInteractionType: List<FullInteractionType>? = null
)

@JsonClass(generateAdapter = true)
data class FullInteractionType(
    @param:Json(name = "comment") val comment: String? = null,
    @param:Json(name = "interactionPair") val interactionPair: List<InteractionPair>? = null
)

@JsonClass(generateAdapter = true)
data class InteractionPair(
    @param:Json(name = "interactionConcept") val interactionConcept: List<InteractionConcept>? = null,
    @param:Json(name = "severity") val severity: String? = null,
    @param:Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class InteractionConcept(
    @param:Json(name = "minConceptItem") val minConceptItem: MinConceptItem? = null,
    @param:Json(name = "sourceConceptItem") val sourceConceptItem: SourceConceptItem? = null
)

@JsonClass(generateAdapter = true)
data class MinConceptItem(
    @param:Json(name = "rxcui") val rxcui: String? = null,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "tty") val tty: String? = null
)

@JsonClass(generateAdapter = true)
data class SourceConceptItem(
    @param:Json(name = "id") val id: String? = null,
    @param:Json(name = "name") val name: String? = null
)

// --- OpenFDA Models ---
@JsonClass(generateAdapter = true)
data class OpenFdaLabelResponse(
    @param:Json(name = "results") val results: List<OpenFdaLabelResult>? = null
)

@JsonClass(generateAdapter = true)
data class OpenFdaLabelResult(
    @param:Json(name = "openfda") val openfda: OpenFdaField? = null,
    @param:Json(name = "description") val description: List<String>? = null,
    @param:Json(name = "active_ingredient") val activeIngredient: List<String>? = null,
    @param:Json(name = "drug_interactions") val drugInteractions: List<String>? = null,
    @param:Json(name = "food_interactions") val foodInteractions: List<String>? = null,
    @param:Json(name = "warnings") val warnings: List<String>? = null,
    @param:Json(name = "precautions") val precautions: List<String>? = null,
    @param:Json(name = "dosage_and_administration") val dosageAndAdministration: List<String>? = null,
    @param:Json(name = "indications_and_usage") val indicationsAndUsage: List<String>? = null,
    @param:Json(name = "spl_product_data_elements") val splProductDataElements: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class OpenFdaField(
    @param:Json(name = "brand_name") val brandName: List<String>? = null,
    @param:Json(name = "generic_name") val genericName: List<String>? = null,
    @param:Json(name = "manufacturer_name") val manufacturerName: List<String>? = null,
    @param:Json(name = "rxcui") val rxcui: List<String>? = null,
    @param:Json(name = "package_ndc") val packageNdc: List<String>? = null,
    @param:Json(name = "upc") val upc: List<String>? = null
)

// --- Domain Models for UI & App State ---
data class DrugSearchResult(
    val rxcui: String?,
    val name: String,
    val brandName: String? = null,
    val genericName: String? = null,
    val activeIngredient: String? = null,
    val dosageForm: String? = null,
    val upcOrNdc: String? = null
)

data class PillDetailInfo(
    val name: String,
    val brandName: String? = null,
    val genericName: String? = null,
    val rxcui: String? = null,
    val upcOrNdc: String? = null,
    val activeIngredients: String? = null,
    val visualDescription: String? = null,
    val foodWarnings: List<FoodWarningItem> = emptyList(),
    val warnings: List<String> = emptyList(),
    val dosageGuidance: String? = null,
    val manufacturer: String? = null
)

data class FoodWarningItem(
    val title: String,
    val icon: String, // "grapefruit", "dairy", "alcohol", "potassium", "food"
    val warningText: String,
    val severity: WarningSeverity
)

data class DrugInteractionResult(
    val drug1Name: String,
    val drug1Rxcui: String?,
    val drug2Name: String,
    val drug2Rxcui: String?,
    val severity: WarningSeverity,
    val description: String
)

enum class WarningSeverity {
    HIGH,
    MODERATE,
    INFO
}
