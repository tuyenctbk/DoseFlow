package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.OledBlack
import com.example.ui.theme.PillViolet
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerModal(
    onDismissRequest: () -> Unit,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var manualCode by remember { mutableStateOf("") }
    var isManualEntry by remember { mutableStateOf(!hasCameraPermission) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControlRef by remember { mutableStateOf<CameraControl?>(null) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBlack)
                .testTag("barcode_scanner_dialog"),
            color = OledBlack
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (hasCameraPermission && !isManualEntry) {
                    CameraBarcodePreview(
                        onBarcodeScanned = { barcode ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBarcodeDetected(barcode)
                            onDismissRequest()
                        },
                        onCameraControlReady = { control ->
                            cameraControlRef = control
                        }
                    )

                    // Futuristic Scanner Target Frame Overlay with corner brackets and animated laser
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .align(Alignment.Center)
                    ) {
                        // Background semi-transparent guide border
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.5.dp, HydrationCyan.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                        )

                        // Four decorative neon corner brackets
                        val cornerBracketColor = HydrationCyan
                        val bracketStroke = 4.dp
                        val bracketLen = 28.dp

                        // Top-left
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(bracketLen)
                                .padding(2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(bracketStroke).background(cornerBracketColor, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.fillMaxHeight().width(bracketStroke).background(cornerBracketColor, RoundedCornerShape(2.dp)))
                        }

                        // Top-right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(bracketLen)
                                .padding(2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(bracketStroke).background(cornerBracketColor, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.align(Alignment.TopEnd).fillMaxHeight().width(bracketStroke).background(cornerBracketColor, RoundedCornerShape(2.dp)))
                        }

                        // Bottom-left
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .size(bracketLen)
                                .padding(2.dp)
                        ) {
                            Box(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(bracketStroke).background(cornerBracketColor, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.fillMaxHeight().width(bracketStroke).background(cornerBracketColor, RoundedCornerShape(2.dp)))
                        }

                        // Bottom-right
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(bracketLen)
                                .padding(2.dp)
                        ) {
                            Box(modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth().height(bracketStroke).background(cornerBracketColor, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.align(Alignment.BottomEnd).fillMaxHeight().width(bracketStroke).background(cornerBracketColor, RoundedCornerShape(2.dp)))
                        }

                        // Animated Laser Scan Bar
                        val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
                        val laserProgress by infiniteTransition.animateFloat(
                            initialValue = 0.08f,
                            targetValue = 0.92f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "laser_y"
                        )
                        val laserAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.45f,
                            targetValue = 0.95f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "laser_alpha"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .fillMaxHeight(laserProgress)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                HydrationCyan.copy(alpha = 0f),
                                                HydrationCyan.copy(alpha = laserAlpha),
                                                PillViolet.copy(alpha = laserAlpha),
                                                HydrationCyan.copy(alpha = laserAlpha),
                                                HydrationCyan.copy(alpha = 0f)
                                            )
                                        ),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 310.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.align_barcode_frame),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.scans_upc_ean_ndc),
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Manual Entry / Fallback View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(PillViolet.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Barcode",
                                tint = PillViolet,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.lookup_medication_barcode),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.type_upc_ndc_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = manualCode,
                            onValueChange = { manualCode = it },
                            label = { Text(stringResource(R.string.upc_or_ndc_label)) },
                            placeholder = { Text(stringResource(R.string.upc_placeholder)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (manualCode.isNotBlank()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onBarcodeDetected(manualCode.trim())
                                        onDismissRequest()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manual_barcode_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PillViolet,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedLabelColor = PillViolet
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (manualCode.isNotBlank()) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onBarcodeDetected(manualCode.trim())
                                    onDismissRequest()
                                }
                            },
                            enabled = manualCode.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_manual_barcode_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PillViolet),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.find_medication), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Top Bar Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .testTag("close_barcode_scanner")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }

                        // Flashlight Toggle (when camera mode is active)
                        if (hasCameraPermission && !isManualEntry) {
                            IconButton(
                                onClick = {
                                    isTorchOn = !isTorchOn
                                    cameraControlRef?.enableTorch(isTorchOn)
                                },
                                modifier = Modifier
                                    .background(
                                        if (isTorchOn) HydrationCyan.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.6f),
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (isTorchOn) HydrationCyan else Color.Transparent,
                                        CircleShape
                                    )
                                    .testTag("toggle_camera_torch")
                            ) {
                                Icon(
                                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Flashlight",
                                    tint = if (isTorchOn) HydrationCyan else Color.White
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isManualEntry,
                            onClick = {
                                if (!hasCameraPermission) {
                                    launcher.launch(Manifest.permission.CAMERA)
                                } else {
                                    isManualEntry = false
                                }
                            },
                            label = { Text(stringResource(R.string.camera_scan)) },
                            leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HydrationCyan,
                                selectedLabelColor = OledBlack
                            )
                        )

                        FilterChip(
                            selected = isManualEntry,
                            onClick = { isManualEntry = true },
                            label = { Text(stringResource(R.string.type_code)) },
                            leadingIcon = { Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PillViolet,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraBarcodePreview(
    onBarcodeScanned: (String) -> Unit,
    onCameraControlReady: (CameraControl) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var cameraProviderInstance by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    var isScanned by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProviderInstance?.unbindAll()
            } catch (e: Exception) {
                Log.e("CameraBarcodePreview", "Unbind failed: ${e.message}")
            }
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProviderInstance = cameraProvider

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val barcodeScannerOptions = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_UPC_A,
                            Barcode.FORMAT_UPC_E,
                            Barcode.FORMAT_EAN_13,
                            Barcode.FORMAT_EAN_8,
                            Barcode.FORMAT_CODE_128,
                            Barcode.FORMAT_QR_CODE
                        )
                        .build()

                    val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !isScanned) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        if (!rawValue.isNullOrBlank() && !isScanned) {
                                            isScanned = true
                                            // Always dispatch UI callback to main thread
                                            ContextCompat.getMainExecutor(ctx).execute {
                                                onBarcodeScanned(rawValue)
                                            }
                                            break
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CameraBarcodePreview", "Barcode error: ${e.message}")
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    onCameraControlReady(camera.cameraControl)
                } catch (exc: Exception) {
                    Log.e("CameraBarcodePreview", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
