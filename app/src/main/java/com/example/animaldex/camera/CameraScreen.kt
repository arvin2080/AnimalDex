package com.example.animaldex.camera

import android.Manifest
import android.content.pm.PackageManager

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat

import com.example.animaldex.util.GameFont


@Composable
fun CameraScreen(
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
    onBack: () -> Unit
) {

    val context =
        LocalContext.current


    val lifecycleOwner =
        LocalLifecycleOwner.current


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
                                preview
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


        // --------------------------------------------------------
        // TOP OVERLAY
        // --------------------------------------------------------

        Row(
            modifier = Modifier
                .align(
                    Alignment.TopCenter
                )
                .fillMaxWidth()
                .padding(
                    top = 35.dp,
                    start = 10.dp,
                    end = 10.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            CameraBackButton(
                onClick = onBack
            )


            Spacer(
                modifier =
                    Modifier.weight(1f)
            )


            Box(
                modifier = Modifier
                    .background(
                        Color.Black.copy(
                            alpha = 0.65f
                        ),
                        RoundedCornerShape(
                            10.dp
                        )
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
            ) {

                Text(
                    text =
                        "ANIMAL SCANNER",

                    color =
                        Color.White,

                    fontFamily =
                        GameFont,

                    fontSize =
                        10.sp,

                    fontWeight =
                        FontWeight.Black
                )
            }
        }


        // --------------------------------------------------------
        // SCANNER FRAME
        // --------------------------------------------------------

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


        // --------------------------------------------------------
        // BOTTOM MESSAGE
        // --------------------------------------------------------

        Box(
            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .padding(
                    bottom = 18.dp
                )
                .background(
                    Color.Black.copy(
                        alpha = 0.70f
                    ),
                    RoundedCornerShape(
                        12.dp
                    )
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 9.dp
                )
        ) {

            Text(
                text =
                    "POINT AT AN ANIMAL",

                color =
                    Color.White,

                fontFamily =
                    GameFont,

                fontSize =
                    9.sp,

                fontWeight =
                    FontWeight.Black
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

        // TOP LEFT

        Box(
            modifier = Modifier
                .align(
                    Alignment.TopStart
                )
                .width(
                    cornerLength
                )
                .height(
                    lineWidth
                )
                .background(
                    lineColor
                )
        )


        Box(
            modifier = Modifier
                .align(
                    Alignment.TopStart
                )
                .width(
                    lineWidth
                )
                .height(
                    cornerLength
                )
                .background(
                    lineColor
                )
        )


        // TOP RIGHT

        Box(
            modifier = Modifier
                .align(
                    Alignment.TopEnd
                )
                .width(
                    cornerLength
                )
                .height(
                    lineWidth
                )
                .background(
                    lineColor
                )
        )


        Box(
            modifier = Modifier
                .align(
                    Alignment.TopEnd
                )
                .width(
                    lineWidth
                )
                .height(
                    cornerLength
                )
                .background(
                    lineColor
                )
        )


        // BOTTOM LEFT

        Box(
            modifier = Modifier
                .align(
                    Alignment.BottomStart
                )
                .width(
                    cornerLength
                )
                .height(
                    lineWidth
                )
                .background(
                    lineColor
                )
        )


        Box(
            modifier = Modifier
                .align(
                    Alignment.BottomStart
                )
                .width(
                    lineWidth
                )
                .height(
                    cornerLength
                )
                .background(
                    lineColor
                )
        )


        // BOTTOM RIGHT

        Box(
            modifier = Modifier
                .align(
                    Alignment.BottomEnd
                )
                .width(
                    cornerLength
                )
                .height(
                    lineWidth
                )
                .background(
                    lineColor
                )
        )


        Box(
            modifier = Modifier
                .align(
                    Alignment.BottomEnd
                )
                .width(
                    lineWidth
                )
                .height(
                    cornerLength
                )
                .background(
                    lineColor
                )
        )
    }
}


@Composable
private fun CameraBackButton(
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .background(
                Color.Black.copy(
                    alpha = 0.70f
                ),
                RoundedCornerShape(
                    9.dp
                )
            )
            .pointerInput(Unit) {

                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            }
            .padding(
                horizontal = 14.dp,
                vertical = 9.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text =
                "← BACK",

            color =
                Color.White,

            fontFamily =
                GameFont,

            fontSize =
                9.sp,

            fontWeight =
                FontWeight.Black
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
                    Alignment.BottomEnd
                )
                .padding(
                    10.dp
                )
        ) {

            CameraBackButton(
                onClick = onBack
            )
        }
    }
}