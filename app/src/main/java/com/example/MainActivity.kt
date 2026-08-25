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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reminder.ReminderScheduler
import com.example.ui.DoseFlowViewModel
import com.example.ui.UserMessage
import com.example.ui.screens.HistoryAndExportScreen
import com.example.ui.screens.MedicationsManagerScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayDashboardScreen
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OledBlack
import com.example.ui.theme.PillViolet
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber


class MainActivity : androidx.fragment.app.FragmentActivity() {


    private val viewModel: DoseFlowViewModel by viewModels()

    private fun checkBiometricUnlock(onSuccess: (() -> Unit)? = null) {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(this@MainActivity, "🔓 Biometric Unlocked Successfully", Toast.LENGTH_SHORT).show()
                    onSuccess?.invoke()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@MainActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@MainActivity, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        )

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.security_locked))
            .setSubtitle(getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(getString(R.string.cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Create Notification Channel for alarms
        ReminderScheduler.createNotificationChannel(this)
        ReminderScheduler.scheduleWaterGoalCheck(this)
        ReminderScheduler.scheduleWaterIntervalCheck(this)
        ReminderScheduler.scheduleDailyMedicationCheck(this)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                val medications by viewModel.medications.collectAsStateWithLifecycle()
                val todayMedLogs by viewModel.todayMedLogs.collectAsStateWithLifecycle()
                val todayWaterLogs by viewModel.todayWaterLogs.collectAsStateWithLifecycle()
                val todayWaterSum by viewModel.todayWaterSum.collectAsStateWithLifecycle()
                val waterGoalMl by viewModel.waterGoalMl.collectAsStateWithLifecycle()
                val completionPercent by viewModel.dailyMedCompletionPercent.collectAsStateWithLifecycle()
                val allMedLogs by viewModel.allMedLogs.collectAsStateWithLifecycle()
                val allWaterLogs by viewModel.allWaterLogs.collectAsStateWithLifecycle()
                val csvString by viewModel.csvString.collectAsStateWithLifecycle()
                val reminderIntervalHours by viewModel.reminderIntervalHours.collectAsStateWithLifecycle()
                val snoozeMinutes by viewModel.snoozeMinutes.collectAsStateWithLifecycle()
                val isBiometricLocked by viewModel.isBiometricLocked.collectAsStateWithLifecycle()
                var isUnlocked by remember { mutableStateOf(!isBiometricLocked) }

                LaunchedEffect(isBiometricLocked) {
                    if (isBiometricLocked) {
                        isUnlocked = false
                        checkBiometricUnlock { isUnlocked = true }
                    } else {
                        isUnlocked = true
                    }
                }

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

                if (isBiometricLocked && !isUnlocked) {
                    BiometricLockOverlayScreen(
                        onUnlockRequested = {
                            checkBiometricUnlock { isUnlocked = true }
                        }
                    )
                } else if (!isOnboardingCompleted) {
                    com.example.ui.screens.OnboardingScreen(
                        onCompleteOnboarding = { viewModel.completeOnboarding() }
                    )
                } else {
                    val snackbarHostState = remember { SnackbarHostState() }
                    val scope = rememberCoroutineScope()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = OledBlack,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            GeometricBottomNavigation(
                                selectedTab = selectedTab,
                                onTabSelected = { 
                                    selectedTab = it 
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
                                    allMedLogs = allMedLogs,
                                    allWaterLogs = allWaterLogs,
                                    waterSumMl = todayWaterSum,
                                    waterGoalMl = waterGoalMl,
                                    completionPercent = completionPercent,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { viewModel.toggleTheme() },
                                    onTakePill = { med -> 
                                        viewModel.logMedicationAction(med, "TAKEN")
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Logged: ${med.name} taken",
                                                actionLabel = "UNDO"
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoLastMedication()
                                            }
                                        }
                                    },
                                    onSnoozePill = { med -> 
                                        viewModel.logMedicationAction(med, "SNOOZED") 
                                    },
                                    onSkipPill = { med -> 
                                        viewModel.logMedicationAction(med, "SKIPPED") 
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Skipped ${med.name}",
                                                actionLabel = "UNDO"
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoLastMedication()
                                            }
                                        }
                                    },
                                    onLogWater = { amount -> 
                                        viewModel.logWater(amount) 
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "+${amount}ml water logged",
                                                actionLabel = "UNDO"
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoLastWater()
                                            }
                                        }
                                    },
                                    onUndoWater = { viewModel.undoLastWater() },
                                    onOpenAddMedication = { selectedTab = 1 },
                                    onTestNotification = { med -> viewModel.testSendReminderNotification(med) }
                                )

                                1 -> MedicationsManagerScreen(
                                    medications = medications,
                                    waterGoalMl = waterGoalMl,
                                    onSaveMedication = { id, name, dosage, hour, min, freq, stock, color, iconType ->
                                        viewModel.saveMedication(id, name, dosage, hour, min, freq, stock, color, iconType)
                                    },
                                    onDeleteMedication = { med -> viewModel.deleteMedication(med) },
                                    onSetWaterGoal = { goal -> viewModel.setWaterGoal(goal) },
                                    onTestNotification = { med -> viewModel.testSendReminderNotification(med) }
                                )

                                2 -> ReportScreen(
                                    allMedLogs = allMedLogs,
                                    allWaterLogs = allWaterLogs,
                                    waterGoalMl = waterGoalMl
                                )

                                3 -> HistoryAndExportScreen(
                                    allMedLogs = allMedLogs,
                                    allWaterLogs = allWaterLogs,
                                    csvString = csvString,
                                    onGenerateCsv = { 
                                        viewModel.exportCsv() 
                                    },
                                    onClearCsv = { viewModel.clearCsv() },
                                    onDeleteLogs = { meds, water -> viewModel.deleteLogs(meds, water) },
                                    onUpdateMedLog = { log -> viewModel.updateMedicationLog(log) },
                                    onUpdateWaterLog = { log -> viewModel.updateWaterLog(log) },
                                    onRevisitOnboarding = { viewModel.resetOnboarding() }
                                )

                                                                 4 -> SettingsScreen(
                                     waterGoalMl = waterGoalMl,
                                     onSetWaterGoal = { viewModel.setWaterGoal(it) },
                                     reminderIntervalHours = reminderIntervalHours,
                                     onSetReminderInterval = { viewModel.setReminderIntervalHours(it) },
                                     snoozeMinutes = snoozeMinutes,
                                     onSetSnoozeMinutes = { viewModel.setSnoozeMinutes(it) },
                                     isDarkTheme = isDarkTheme,
                                     onToggleTheme = { viewModel.toggleTheme() },
                                     isBiometricLocked = isBiometricLocked,
                                     onToggleBiometric = { viewModel.setBiometricLocked(it) },
                                     onBackupDatabase = { viewModel.backupDatabase() },
                                     onRestoreDatabase = { json -> viewModel.restoreDatabase(json) },
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

            // Tab 2: Report
            NavItem(
                iconText = "R",
                label = "Report",
                isSelected = selectedTab == 2,
                activeColor = HydrationCyan,
                onClick = { onTabSelected(2) },
                testTag = "tab_report"
            )

            // Tab 3: History & Export
            NavItem(
                iconText = "H",
                label = "History",
                isSelected = selectedTab == 3,
                activeColor = SuccessEmerald,
                onClick = { onTabSelected(3) },
                testTag = "tab_history"
            )

            // Tab 4: Settings
            NavItem(
                iconText = "G",
                label = "Settings",
                isSelected = selectedTab == 4,
                activeColor = WarningAmber,
                onClick = { onTabSelected(4) },
                testTag = "tab_settings"
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

@Composable
fun BiometricLockOverlayScreen(onUnlockRequested: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.security_locked),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.authenticate_to_continue),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onUnlockRequested,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.unlock_doseflow), fontWeight = FontWeight.Bold)
            }
        }
    }
}
