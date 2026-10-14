package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- RxNav Models ---
@JsonClass(generateAdapter = true)
data class RxNavSpellingResponse(
    @Json(name = "suggestionGroup") val suggestionGroup: SuggestionGroup? = null
)

@JsonClass(generateAdapter = true)
data class SuggestionGroup(
    @Json(name = "suggestionList") val suggestionList: SuggestionList? = null
)

@JsonClass(generateAdapter = true)
data class SuggestionList(
    @Json(name = "suggestion") val suggestion: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class RxNavApproximateResponse(
    @Json(name = "approximateGroup") val approximateGroup: ApproximateGroup? = null
)

@JsonClass(generateAdapter = true)
data class ApproximateGroup(
    @Json(name = "candidate") val candidate: List<RxCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class RxCandidate(
    @Json(name = "rxcui") val rxcui: String? = null,
    @Json(name = "score") val score: String? = null,
    @Json(name = "rank") val rank: String? = null
)

@JsonClass(generateAdapter = true)
data class RxNavDrugsResponse(
    @Json(name = "drugGroup") val drugGroup: DrugGroup? = null
)

@JsonClass(generateAdapter = true)
data class DrugGroup(
    @Json(name = "conceptGroup") val conceptGroup: List<ConceptGroup>? = null
)

@JsonClass(generateAdapter = true)
data class ConceptGroup(
    @Json(name = "tty") val tty: String? = null,
    @Json(name = "conceptProperties") val conceptProperties: List<ConceptProperty>? = null
)

@JsonClass(generateAdapter = true)
data class ConceptProperty(
    @Json(name = "rxcui") val rxcui: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "synonym") val synonym: String? = null,
    @Json(name = "tty") val tty: String? = null
)

@JsonClass(generateAdapter = true)
data class RxNavRxcuiResponse(
    @Json(name = "idGroup") val idGroup: IdGroup? = null
)

@JsonClass(generateAdapter = true)
data class IdGroup(
    @Json(name = "rxnormId") val rxnormId: List<String>? = null
)

// --- RxNav Interactions ---
@JsonClass(generateAdapter = true)
data class RxNavInteractionListResponse(
    @Json(name = "fullInteractionTypeGroup") val fullInteractionTypeGroup: List<FullInteractionTypeGroup>? = null,
    @Json(name = "nlmDisclaimer") val nlmDisclaimer: String? = null
)

@JsonClass(generateAdapter = true)
data class FullInteractionTypeGroup(
    @Json(name = "sourceName") val sourceName: String? = null,
    @Json(name = "fullInteractionType") val fullInteractionType: List<FullInteractionType>? = null
)

@JsonClass(generateAdapter = true)
data class FullInteractionType(
    @Json(name = "comment") val comment: String? = null,
    @Json(name = "interactionPair") val interactionPair: List<InteractionPair>? = null
)

@JsonClass(generateAdapter = true)
data class InteractionPair(
    @Json(name = "interactionConcept") val interactionConcept: List<InteractionConcept>? = null,
    @Json(name = "severity") val severity: String? = null,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class InteractionConcept(
    @Json(name = "minConceptItem") val minConceptItem: MinConceptItem? = null,
    @Json(name = "sourceConceptItem") val sourceConceptItem: SourceConceptItem? = null
)

@JsonClass(generateAdapter = true)
data class MinConceptItem(
    @Json(name = "rxcui") val rxcui: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "tty") val tty: String? = null
)

@JsonClass(generateAdapter = true)
data class SourceConceptItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null
)

// --- OpenFDA Models ---
@JsonClass(generateAdapter = true)
data class OpenFdaLabelResponse(
    @Json(name = "results") val results: List<OpenFdaLabelResult>? = null
)

@JsonClass(generateAdapter = true)
data class OpenFdaLabelResult(
    @Json(name = "openfda") val openfda: OpenFdaField? = null,
    @Json(name = "description") val description: List<String>? = null,
    @Json(name = "active_ingredient") val activeIngredient: List<String>? = null,
    @Json(name = "drug_interactions") val drugInteractions: List<String>? = null,
    @Json(name = "food_interactions") val foodInteractions: List<String>? = null,
    @Json(name = "warnings") val warnings: List<String>? = null,
    @Json(name = "precautions") val precautions: List<String>? = null,
    @Json(name = "dosage_and_administration") val dosageAndAdministration: List<String>? = null,
    @Json(name = "indications_and_usage") val indicationsAndUsage: List<String>? = null,
    @Json(name = "spl_product_data_elements") val splProductDataElements: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class OpenFdaField(
    @Json(name = "brand_name") val brandName: List<String>? = null,
    @Json(name = "generic_name") val genericName: List<String>? = null,
    @Json(name = "manufacturer_name") val manufacturerName: List<String>? = null,
    @Json(name = "rxcui") val rxcui: List<String>? = null,
    @Json(name = "package_ndc") val packageNdc: List<String>? = null,
    @Json(name = "upc") val upc: List<String>? = null
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
    val brandName: String?,
    val genericName: String?,
    val rxcui: String?,
    val upcOrNdc: String?,
    val activeIngredients: String?,
    val visualDescription: String?,
    val foodWarnings: List<FoodWarningItem>,
    val warnings: List<String>,
    val dosageGuidance: String?,
    val manufacturer: String?
)

data class FoodWarningItem(
    val title: String,        // e.g., "Grapefruit Interaction"
    val icon: String,         // "grapefruit", "dairy", "alcohol", "potassium", "food"
    val warningText: String,  // Description / advice
    val severity: WarningSeverity = WarningSeverity.HIGH
)

enum class WarningSeverity {
    HIGH, MODERATE, INFO
}

data class DrugInteractionResult(
    val drug1Name: String,
    val drug1Rxcui: String?,
    val drug2Name: String,
    val drug2Rxcui: String?,
    val severity: WarningSeverity,
    val description: String
)
