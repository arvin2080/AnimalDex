package com.example.animaldex.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.animaldex.model.Animal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CameraScreen(animals: List<Animal>, onAnimalFound: (Animal) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("scanner_config", Context.MODE_PRIVATE) }
    var apiKey by remember { mutableStateOf(prefs.getString("openai_api_key", "") ?: "") }
    var showSettings by remember { mutableStateOf(false) }
    var permission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    ) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission = it }
    LaunchedEffect(Unit) { if (!permission) launcher.launch(Manifest.permission.CAMERA) }
    var capture by remember { mutableStateOf<ImageCapture?>(null) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Cadre un animal, puis appuie sur PHOTO") }
    BackHandler { onBack() }

    Box(Modifier.fillMaxSize().background(Color(0xFF101820))) {
        if (permission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                    val providerFuture = ProcessCameraProvider.getInstance(context)
                    providerFuture.addListener({
                        try {
                            val provider = providerFuture.get()
                            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                            val imageCapture = ImageCapture.Builder().build()
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                            capture = imageCapture
                        } catch (e: Exception) { status = "Caméra indisponible : ${e.message}" }
                    }, ContextCompat.getMainExecutor(context))
                    previewView
                }
            )
        } else {
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.align(Alignment.Center)) {
                Text("Autoriser la caméra")
            }
        }
        Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 36.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) { Text("← Retour") }
            Button(onClick = { showSettings = !showSettings }) { Text("Clé API ⚙") }
        }
        if (showSettings) {
            Column(Modifier.align(Alignment.Center).fillMaxWidth().padding(16.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).padding(14.dp)) {
                Text("Clé API OpenAI (à saisir une seule fois)")
                OutlinedTextField(apiKey, { apiKey = it }, label = { Text("Clé API OpenAI") },
                    visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    prefs.edit().putString("openai_api_key", apiKey.trim()).apply()
                    showSettings = false
                    status = "Clé enregistrée. Tu peux prendre une photo."
                }) { Text("Enregistrer") }
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp)).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(status, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Button(enabled = permission && capture != null && !busy && !showSettings, onClick = {
                val imageCapture = capture ?: return@Button
                val photo = File.createTempFile("animaldex_", ".jpg", context.cacheDir)
                busy = true
                status = "Capture…"
                imageCapture.takePicture(ImageCapture.OutputFileOptions.Builder(photo).build(),
                    ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exception: ImageCaptureException) {
                            photo.delete()
                            busy = false
                            status = "Erreur de capture : ${exception.message}"
                        }
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            status = "Identification en cours…"
                            scope.launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        val jpeg = compressPhoto(photo)
                                        identifyPhoto(context, jpeg)
                                    }
                                    val animal = matchAnimal(animals, result)
                                    if (animal != null) onAnimalFound(animal)
                                    else status = "${result.commonName ?: "Animal inconnu"} (${result.scientificName ?: "nom scientifique inconnu"}) : aucune fiche exacte dans AnimalDex."
                                } catch (e: Exception) {
                                    status = e.message ?: "Identification impossible. Vérifie Internet et le serveur."
                                } finally { photo.delete(); busy = false }
                            }
                        }
                    })
            }) { Text(if (busy) "Patiente…" else "PHOTO · Identifier") }
        }
    }
}
