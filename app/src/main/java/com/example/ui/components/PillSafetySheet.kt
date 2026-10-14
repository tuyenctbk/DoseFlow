package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NoDrinks
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.api.DrugInteractionResult
import com.example.data.api.FoodWarningItem
import com.example.data.api.PillDetailInfo
import com.example.data.api.WarningSeverity
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.OledBlack
import com.example.ui.theme.PillViolet
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillSafetySheet(
    pillDetail: PillDetailInfo?,
    cabinetInteractions: List<DrugInteractionResult>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = OledBlack,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkCardBorder)
            )
        },
        modifier = Modifier.testTag("pill_safety_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pillDetail?.name ?: stringResource(R.string.pill_drug_safety),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.powered_by_nih_openfda),
                        style = MaterialTheme.typography.labelSmall,
                        color = HydrationCyan
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PillViolet.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PillViolet)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = PillViolet,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.rxnorm_verified),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PillViolet
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PillViolet)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.checking_interactions_fda),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Food & Dietary Warnings
                    if (pillDetail != null && pillDetail.foodWarnings.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.food_dietary_warnings),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = WarningAmber
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(pillDetail.foodWarnings) { warning ->
                            FoodWarningCard(warning = warning)
                        }
                    }

                    // 2. Active Cabinet Drug Interactions
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.cabinet_drug_interactions, cabinetInteractions.size),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = HydrationCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (cabinetInteractions.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessEmerald.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Medication,
                                        contentDescription = null,
                                        tint = SuccessEmerald,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.no_high_risk_interactions),
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = stringResource(R.string.no_interactions_desc),
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(cabinetInteractions) { inter ->
                            InteractionCard(interaction = inter)
                        }
                    }

                    // 3. Official Pill Visual Description
                    if (pillDetail != null) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.official_pill_description_fda),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = PillViolet
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2A)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = PillViolet,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.tablet_capsule_appearance),
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = pillDetail.visualDescription ?: stringResource(R.string.default_fda_visual_desc),
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        lineHeight = 18.sp
                                    )

                                    if (!pillDetail.activeIngredients.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = DarkCardBorder)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = stringResource(R.string.active_ingredients_label, pillDetail.activeIngredients),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = HydrationCyan
                                        )
                                    }

                                    if (!pillDetail.rxcui.isNullOrEmpty()) {
                                        Text(
                                            text = stringResource(R.string.rxcui_concept_id, pillDetail.rxcui),
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodWarningCard(warning: FoodWarningItem) {
    val (bgColor, borderColor, iconVector) = when (warning.icon) {
        "grapefruit" -> Triple(Color(0xFF2A1B12), Color(0xFFF97316), Icons.Default.Warning)
        "dairy" -> Triple(Color(0xFF1E293B), HydrationCyan, Icons.Default.LocalDrink)
        "alcohol" -> Triple(Color(0xFF2D1219), Color(0xFFEF4444), Icons.Default.NoDrinks)
        "potassium" -> Triple(Color(0xFF221F10), WarningAmber, Icons.Default.Warning)
        else -> Triple(Color(0xFF172033), PillViolet, Icons.Default.Restaurant)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(borderColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = warning.title,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = warning.severity.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = borderColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = warning.warningText,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun InteractionCard(interaction: DrugInteractionResult) {
    val isHigh = interaction.severity == WarningSeverity.HIGH
    val alertColor = if (isHigh) Color(0xFFEF4444) else WarningAmber
    val bgColor = if (isHigh) Color(0xFF2D1219) else Color(0xFF272112)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, alertColor.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = alertColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${interaction.drug1Name} ↔ ${interaction.drug2Name}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = alertColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isHigh) "HIGH RISK" else "MODERATE RISK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = alertColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = interaction.description,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )
        }
    }
}
