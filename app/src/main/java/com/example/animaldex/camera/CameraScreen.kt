package com.example.animaldex.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.viewinterop.AndroidView

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

import com.example.animaldex.model.Animal
import com.example.animaldex.util.GameFont


// ============================================================
// FILE D'ATTENTE "À IDENTIFIER PLUS TARD"
// ============================================================

private const val CameraPrefsName = "animaldex_camera"
private const val PrefKeyPendingPhotos = "pending_recognition_photos"


fun getPendingRecognitionPhotos(
    context: Context
): Set<String> {

    val prefs =
        context.getSharedPreferences(
            CameraPrefsName,
            Context.MODE_PRIVATE
        )

    return prefs.getStringSet(
        PrefKeyPendingPhotos,
        emptySet()
    ) ?: emptySet()
}


private fun addPendingRecognitionPhoto(
    context: Context,
    photoPath: String
) {

    val prefs =
        context.getSharedPreferences(
            CameraPrefsName,
            Context.MODE_PRIVATE
        )

    val current =
        prefs.getStringSet(
            PrefKeyPendingPhotos,
            emptySet()
        ) ?: emptySet()

    prefs.edit()
        .putStringSet(
            PrefKeyPendingPhotos,
            current + photoPath
        )
        .apply()
}


// ============================================================
// ÉTAT DU FLUX DE CAPTURE
// ============================================================

private enum class CaptureFlowState {
    PREVIEW,
    RECOGNIZING
}


// ============================================================
// CAMERA SCREEN
// ============================================================

@Composable
fun CameraScreen(
    animals: List<Animal>,
    onAnimalFound: (Animal) -> Unit,
    onBack: () -> Unit
) {

    val context =
        LocalContext.current


    var cameraPermissionGranted by remember {

        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }


    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            cameraPermissionGranted =
                granted
        }


    LaunchedEffect(Unit) {

        if (!cameraPermissionGranted) {

            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }


    BackHandler {

        onBack()
    }


    if (cameraPermissionGranted) {

        CameraPreview(
            animals = animals,
            onAnimalFound = onAnimalFound,
            onBack = onBack
        )

    } else {

        CameraPermissionScreen(
            onRequestPermission = {

                permissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            },

            onBack = onBack
        )
    }
}


@Composable
private fun CameraPreview(
    animals: List<Animal>,
    onAnimalFound: (Animal) -> Unit,
    onBack: () -> Unit
) {

    val context =
        LocalContext.current

    val lifecycleOwner =
        LocalLifecycleOwner.current

    val coroutineScope =
        rememberCoroutineScope()

    val prefs =
        remember {
            context.getSharedPreferences(
                "scanner_config",
                Context.MODE_PRIVATE
            )
        }


    var apiKey by remember {
        mutableStateOf(
            prefs.getString("openai_api_key", "") ?: ""
        )
    }

    var showSettings by remember {
        mutableStateOf(false)
    }


    var flowState by remember {
        mutableStateOf(CaptureFlowState.PREVIEW)
    }

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    var lastCapturedPhotoPath by remember {
        mutableStateOf<String?>(null)
    }

    var elapsedSeconds by remember {
        mutableFloatStateOf(0f)
    }

    var statusMessage by remember {
        mutableStateOf<String?>(null)
    }

    var recognitionJob by remember {
        mutableStateOf<Job?>(null)
    }


    LaunchedEffect(flowState) {

        if (flowState == CaptureFlowState.RECOGNIZING) {

            elapsedSeconds = 0f

            while (true) {

                kotlinx.coroutines.delay(100)

                elapsedSeconds += 0.1f
            }
        }
    }


    fun takePhotoAndRecognize() {

        val capture =
            imageCapture ?: return


        val outputDir =
            File(context.filesDir, "captures")

        if (!outputDir.exists()) {

            outputDir.mkdirs()
        }

        val fileName =
            SimpleDateFormat(
                "yyyyMMdd_HHmmss_SSS",
                Locale.US
            ).format(java.util.Date()) + ".jpg"

        val outputFile =
            File(outputDir, fileName)

        val outputOptions =
            ImageCapture.OutputFileOptions
                .Builder(outputFile)
                .build()


        statusMessage = null


        capture.takePicture(
            outputOptions,

            ContextCompat.getMainExecutor(context),

            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    output: ImageCapture.OutputFileResults
                ) {

                    lastCapturedPhotoPath =
                        outputFile.absolutePath

                    flowState =
                        CaptureFlowState.RECOGNIZING


                    recognitionJob =
                        coroutineScope.launch {

                            val startTimeMs =
                                System.currentTimeMillis()

                            var recognizedAnimal: Animal? = null
                            var failureMessage: String? = null


                            try {

                                val identification =
                                    withContext(Dispatchers.IO) {

                                        val jpeg =
                                            compressPhoto(outputFile)

                                        identifyPhoto(context, jpeg)
                                    }

                                Log.d(
                                    "AnimalDexDebug",
                                    "Reponse IA brute -> scientific='${identification.scientificName}' common='${identification.commonName}'"
                                )

                                val animal =
                                    matchAnimal(animals, identification)

                                if (animal != null) {

                                    recognizedAnimal = animal

                                } else {

                                    failureMessage =
                                        "${identification.commonName ?: "Animal inconnu"} " +
                                                "(${identification.scientificName ?: "nom scientifique inconnu"}) : " +
                                                "aucune fiche exacte dans AnimalDex."
                                }

                            } catch (
                                error: Exception
                            ) {

                                Log.d(
                                    "AnimalDexDebug",
                                    "Erreur pendant l'identification -> ${error.message}"
                                )

                                failureMessage =
                                    error.message
                                        ?: "Identification impossible. Vérifie Internet et ta clé API."
                            }


                            // ================================================
                            // BLOC DE TEST — À SUPPRIMER une fois l'API fiable.
                            // Si rien n'a été reconnu (erreur ou pas de fiche
                            // correspondante), simule la découverte d'un
                            // langur du Nilgiri pour tester l'animation de
                            // capture sans dépendre de l'API.
                            // ================================================
                            if (recognizedAnimal == null) {

                                val testFallback =
                                    animals.firstOrNull { candidate ->

                                        val hasNilgiri =
                                            listOfNotNull(
                                                candidate.nameFr,
                                                candidate.commonNameEN,
                                                candidate.scientificName
                                            ).any {
                                                it.contains("nilgiri", ignoreCase = true)
                                            }

                                        val hasLangur =
                                            listOfNotNull(
                                                candidate.nameFr,
                                                candidate.commonNameEN
                                            ).any {
                                                it.contains("langur", ignoreCase = true)
                                            }

                                        hasNilgiri && hasLangur
                                    }

                                Log.d(
                                    "AnimalDexDebug",
                                    "Bloc de test -> declenche, trouve='${testFallback?.displayName}'"
                                )

                                if (testFallback != null) {

                                    recognizedAnimal = testFallback
                                    failureMessage = null
                                }
                            }
                            // ================================================
                            // FIN DU BLOC DE TEST
                            // ================================================


                            val elapsed =
                                System.currentTimeMillis() - startTimeMs

                            val remaining =
                                2500L - elapsed

                            if (remaining > 0) {

                                kotlinx.coroutines.delay(remaining)
                            }


                            if (recognizedAnimal != null) {

                                onAnimalFound(recognizedAnimal)

                            } else {

                                statusMessage = failureMessage
                            }

                            flowState =
                                CaptureFlowState.PREVIEW
                        }
                }


                override fun onError(
                    exception: ImageCaptureException
                ) {

                    exception.printStackTrace()

                    statusMessage =
                        "Erreur de capture : ${exception.message}"

                    flowState =
                        CaptureFlowState.PREVIEW
                }
            }
        )
    }


    fun findLater() {

        recognitionJob?.cancel()

        lastCapturedPhotoPath?.let { path ->

            addPendingRecognitionPhoto(
                context,
                path
            )
        }

        statusMessage = null

        flowState =
            CaptureFlowState.PREVIEW
    }


    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        AndroidView(
            modifier =
                Modifier.fillMaxSize(),

            factory = { androidContext ->

                val previewView =
                    PreviewView(
                        androidContext
                    )


                previewView.scaleType =
                    PreviewView.ScaleType.FILL_CENTER


                val cameraProviderFuture =
                    ProcessCameraProvider
                        .getInstance(
                            androidContext
                        )


                cameraProviderFuture.addListener({

                    val cameraProvider =
                        cameraProviderFuture.get()


                    val preview =
                        Preview.Builder()
                            .build()


                    preview.surfaceProvider =
                        previewView.surfaceProvider


                    val capture =
                        ImageCapture.Builder()
                            .build()

                    imageCapture = capture


                    val cameraSelector =
                        CameraSelector
                            .DEFAULT_BACK_CAMERA


                    try {

                        cameraProvider
                            .unbindAll()


                        cameraProvider
                            .bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )

                    } catch (
                        exception: Exception
                    ) {

                        exception.printStackTrace()
                    }

                }, ContextCompat.getMainExecutor(context))


                previewView
            }
        )


        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = 35.dp,
                    end = 10.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            CameraSmallButton(
                icon = "⚙",
                onClick = {
                    showSettings = !showSettings
                }
            )


            CameraSmallButton(
                icon = "←",
                onClick = onBack
            )
        }


        if (flowState == CaptureFlowState.PREVIEW) {

            Box(
                modifier = Modifier
                    .align(
                        Alignment.Center
                    )
                    .fillMaxWidth(0.70f)
                    .fillMaxHeight(0.48f)
            ) {

                ScannerCorners()
            }
        }


        if (
            statusMessage != null &&
            flowState == CaptureFlowState.PREVIEW
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = 110.dp,
                        start = 20.dp,
                        end = 20.dp
                    )
                    .background(
                        Color.Black.copy(alpha = 0.75f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(
                        horizontal = 14.dp,
                        vertical = 9.dp
                    )
            ) {

                Text(
                    text = statusMessage ?: "",

                    color = Color.White,

                    fontFamily = GameFont,

                    fontSize = 9.sp,

                    fontWeight = FontWeight.Bold
                )
            }
        }


        if (flowState == CaptureFlowState.PREVIEW) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(64.dp)
                    .background(
                        if (!showSettings) {
                            Color(0xFFE53935)
                        } else {
                            Color(0xFFE53935).copy(alpha = 0.4f)
                        },
                        CircleShape
                    )
                    .pointerInput(showSettings) {

                        detectTapGestures(
                            onTap = {

                                if (!showSettings) {

                                    takePhotoAndRecognize()
                                }
                            }
                        )
                    }
            )
        }


        if (showSettings) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.55f)
                    )
                    .pointerInput(Unit) {

                        detectTapGestures(
                            onTap = {
                                showSettings = false
                            }
                        )
                    }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .background(
                        Color(0xFF101820),
                        RoundedCornerShape(14.dp)
                    )
                    .pointerInput(Unit) {

                        detectTapGestures(
                            onTap = {}
                        )
                    }
                    .padding(16.dp)
            ) {

                Text(
                    text = "CLÉ API OPENAI",

                    color = Color.White,

                    fontFamily = GameFont,

                    fontSize = 12.sp,

                    fontWeight = FontWeight.Black
                )


                Spacer(
                    modifier = Modifier.height(10.dp)
                )


                OutlinedTextField(
                    value = apiKey,

                    onValueChange = {
                        apiKey = it
                    },

                    label = {
                        Text("Clé API OpenAI")
                    },

                    visualTransformation =
                        PasswordVisualTransformation(),

                    singleLine = true,

                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(
                    modifier = Modifier.height(10.dp)
                )


                Box(
                    modifier = Modifier
                        .background(
                            Color.White,
                            RoundedCornerShape(8.dp)
                        )
                        .pointerInput(Unit) {

                            detectTapGestures(
                                onTap = {

                                    prefs.edit()
                                        .putString(
                                            "openai_api_key",
                                            apiKey.trim()
                                        )
                                        .apply()

                                    showSettings = false
                                }
                            )
                        }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 9.dp
                        )
                ) {

                    Text(
                        text = "ENREGISTRER",

                        color = Color.Black,

                        fontFamily = GameFont,

                        fontSize = 10.sp,

                        fontWeight = FontWeight.Black
                    )
                }
            }
        }


        if (flowState == CaptureFlowState.RECOGNIZING) {

            RecognizingOverlay(
                elapsedSeconds = elapsedSeconds,
                onFindLater = { findLater() }
            )
        }
    }
}


@Composable
private fun RecognizingOverlay(
    elapsedSeconds: Float,
    onFindLater: () -> Unit
) {

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "scan_line"
        )

    val scanLineFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,

        animationSpec = infiniteRepeatable(
            animation = tween(
                1600,
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),

        label = "scan_line_fraction"
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF041226),
                        Color(0xFF0B2A52)
                    )
                )
            )
    ) {

        Canvas(
            modifier =
                Modifier.fillMaxSize()
        ) {

            val y = size.height * scanLineFraction

            drawLine(
                color =
                    Color(0xFF6FD8FF).copy(alpha = 0.85f),

                start = Offset(0f, y),
                end = Offset(size.width, y),

                strokeWidth = 3.dp.toPx()
            )
        }


        Column(
            modifier = Modifier
                .align(Alignment.Center),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "ANALYSE EN COURS",

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 15.sp,

                fontWeight = FontWeight.Black
            )


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            Text(
                text = "Reconnaissance de l'espèce...",

                color = Color(0xFF9FCBEE),

                fontFamily = GameFont,

                fontSize = 9.sp,

                fontWeight = FontWeight.Bold
            )
        }


        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .background(
                    Color.Black.copy(alpha = 0.35f),
                    RoundedCornerShape(8.dp)
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 6.dp
                )
        ) {

            Text(
                text =
                    "%.1fs".format(elapsedSeconds),

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 10.sp,

                fontWeight = FontWeight.Black
            )
        }


        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .background(
                    Color(0xFF1565C0),
                    RoundedCornerShape(10.dp)
                )
                .pointerInput(Unit) {

                    detectTapGestures(
                        onTap = {
                            onFindLater()
                        }
                    )
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                )
        ) {

            Text(
                text = "FIND LATER",

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 10.sp,

                fontWeight = FontWeight.Black
            )
        }
    }
}


@Composable
private fun ScannerCorners() {

    val lineColor =
        Color.White


    val lineWidth =
        4.dp


    val cornerLength =
        27.dp


    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(cornerLength)
                .height(lineWidth)
                .background(lineColor)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(lineWidth)
                .height(cornerLength)
                .background(lineColor)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(cornerLength)
                .height(lineWidth)
                .background(lineColor)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(lineWidth)
                .height(cornerLength)
                .background(lineColor)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(cornerLength)
                .height(lineWidth)
                .background(lineColor)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(lineWidth)
                .height(cornerLength)
                .background(lineColor)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(cornerLength)
                .height(lineWidth)
                .background(lineColor)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(lineWidth)
                .height(cornerLength)
                .background(lineColor)
        )
    }
}


@Composable
private fun CameraSmallButton(
    icon: String,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                Color.Black.copy(alpha = 0.70f),
                CircleShape
            )
            .pointerInput(Unit) {

                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            },

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text = icon,

            color = Color.White,

            fontFamily = GameFont,

            fontSize = 13.sp,

            fontWeight = FontWeight.Black
        )
    }
}


@Composable
private fun CameraPermissionScreen(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF101820)
            )
    ) {

        Column(
            modifier =
                Modifier.align(
                    Alignment.Center
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    "CAMERA REQUIRED",

                color =
                    Color.White,

                fontFamily =
                    GameFont,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Black
            )


            Spacer(
                modifier =
                    Modifier.height(
                        15.dp
                    )
            )


            Box(
                modifier = Modifier
                    .background(
                        Color.White,
                        RoundedCornerShape(
                            10.dp
                        )
                    )
                    .pointerInput(Unit) {

                        detectTapGestures(
                            onTap = {

                                onRequestPermission()
                            }
                        )
                    }
                    .padding(
                        horizontal = 18.dp,
                        vertical = 10.dp
                    )
            ) {

                Text(
                    text =
                        "ALLOW CAMERA",

                    color =
                        Color.Black,

                    fontFamily =
                        GameFont,

                    fontSize =
                        10.sp,

                    fontWeight =
                        FontWeight.Black
                )
            }
        }


        Box(
            modifier = Modifier
                .align(
                    Alignment.TopEnd
                )
                .padding(
                    top = 35.dp,
                    end = 10.dp
                )
        ) {

            CameraSmallButton(
                icon = "←",
                onClick = onBack
            )
        }
    }
}