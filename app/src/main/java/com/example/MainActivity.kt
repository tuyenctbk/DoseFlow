package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reminder.ReminderScheduler
import com.example.ui.DoseFlowViewModel
import com.example.ui.UserMessage
import com.example.ui.screens.HistoryAndExportScreen
import com.example.ui.screens.MedicationsManagerScreen
import com.example.ui.screens.TodayDashboardScreen
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OledBlack
import com.example.ui.theme.PillViolet
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : ComponentActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val viewModel: DoseFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Create Notification Channel for alarms
        ReminderScheduler.createNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                val medications by viewModel.medications.collectAsStateWithLifecycle()
                val todayMedLogs by viewModel.todayMedLogs.collectAsStateWithLifecycle()
                val todayWaterLogs by viewModel.todayWaterLogs.collectAsStateWithLifecycle()
                val todayWaterSum by viewModel.todayWaterSum.collectAsStateWithLifecycle()
                val waterGoalMl by viewModel.waterGoalMl.collectAsStateWithLifecycle()
                val completionPercent by viewModel.dailyMedCompletionPercent.collectAsStateWithLifecycle()
                val allMedLogs by viewModel.allMedLogs.collectAsStateWithLifecycle()
                val allWaterLogs by viewModel.allWaterLogs.collectAsStateWithLifecycle()
                val csvString by viewModel.csvString.collectAsStateWithLifecycle()

                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

                var selectedTab by remember { mutableIntStateOf(0) }

                LaunchedEffect(Unit) {
                    viewModel.userMessage.collect { msg ->
                        when (msg) {
                            is UserMessage.Toast -> {
                                Toast.makeText(this@MainActivity, msg.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                if (!isOnboardingCompleted) {
                    com.example.ui.screens.OnboardingScreen(
                        onCompleteOnboarding = { viewModel.completeOnboarding() }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = OledBlack,
                        bottomBar = {
                            GeometricBottomNavigation(
                                selectedTab = selectedTab,
                                onTabSelected = { 
                                    selectedTab = it 
                                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                                        putString(FirebaseAnalytics.Param.SCREEN_NAME, "tab_$it")
                                        putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
                                    })
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedTab) {
                                0 -> TodayDashboardScreen(
                                    medications = medications,
                                    todayMedLogs = todayMedLogs,
                                    waterSumMl = todayWaterSum,
                                    waterGoalMl = waterGoalMl,
                                    completionPercent = completionPercent,
                                    onTakePill = { med -> 
                                        viewModel.logMedicationAction(med, "TAKEN")
                                        firebaseAnalytics.logEvent("medication_taken", Bundle().apply {
                                            putString("med_name", med.name)
                                        })
                                    },
                                    onSnoozePill = { med -> 
                                        viewModel.logMedicationAction(med, "SNOOZED") 
                                        firebaseAnalytics.logEvent("medication_snoozed", Bundle().apply {
                                            putString("med_name", med.name)
                                        })
                                    },
                                    onSkipPill = { med -> 
                                        viewModel.logMedicationAction(med, "SKIPPED") 
                                        firebaseAnalytics.logEvent("medication_skipped", Bundle().apply {
                                            putString("med_name", med.name)
                                        })
                                    },
                                    onLogWater = { amount -> 
                                        viewModel.logWater(amount) 
                                        firebaseAnalytics.logEvent("water_logged", Bundle().apply {
                                            putInt("amount_ml", amount)
                                        })
                                    },
                                    onUndoWater = { viewModel.undoLastWater() },
                                    onOpenAddMedication = { selectedTab = 1 },
                                    onTestNotification = { med -> viewModel.testSendReminderNotification(med) }
                                )

                                1 -> MedicationsManagerScreen(
                                    medications = medications,
                                    waterGoalMl = waterGoalMl,
                                    onSaveMedication = { id, name, dosage, hour, min, freq, stock, color ->
                                        viewModel.saveMedication(id, name, dosage, hour, min, freq, stock, color)
                                        firebaseAnalytics.logEvent("medication_saved", Bundle().apply {
                                            putString("med_name", name)
                                            putString("frequency", freq)
                                        })
                                    },
                                    onDeleteMedication = { med -> viewModel.deleteMedication(med) },
                                    onSetWaterGoal = { goal -> viewModel.setWaterGoal(goal) },
                                    onTestNotification = { med -> viewModel.testSendReminderNotification(med) }
                                )

                                2 -> HistoryAndExportScreen(
                                    allMedLogs = allMedLogs,
                                    allWaterLogs = allWaterLogs,
                                    csvString = csvString,
                                    onGenerateCsv = { 
                                        viewModel.exportCsv() 
                                        firebaseAnalytics.logEvent("csv_exported", null)
                                    },
                                    onClearCsv = { viewModel.clearCsv() },
                                    onRevisitOnboarding = { viewModel.resetOnboarding() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeometricBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(OledBlack)
            .border(androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder))
            .navigationBarsPadding()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .testTag("geometric_bottom_navigation")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Flow (Today Dashboard)
            NavItem(
                iconText = "D",
                label = "Flow",
                isSelected = selectedTab == 0,
                activeColor = SuccessEmerald,
                onClick = { onTabSelected(0) },
                testTag = "tab_flow"
            )

            // Tab 1: Schedules (Medications)
            NavItem(
                iconText = "S",
                label = "Schedules",
                isSelected = selectedTab == 1,
                activeColor = PillViolet,
                onClick = { onTabSelected(1) },
                testTag = "tab_schedules"
            )

            // Tab 2: History & Export
            NavItem(
                iconText = "H",
                label = "History",
                isSelected = selectedTab == 2,
                activeColor = HydrationCyan,
                onClick = { onTabSelected(2) },
                testTag = "tab_history"
            )
        }
    }
}

@Composable
fun NavItem(
    iconText: String,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val color = if (isSelected) activeColor else TextMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, color, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = color
        )
    }
}
