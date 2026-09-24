package com.example.animaldex.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

import com.example.animaldex.model.Animal
import com.example.animaldex.util.GameFont


@Composable
fun AnimalDetailScreen(
    animal: Animal,
    color: Color,
    justCaptured: Boolean = false,
    onBack: () -> Unit
) {

    val focusRequester =
        remember {
            FocusRequester()
        }


    LaunchedEffect(Unit) {

        focusRequester.requestFocus()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .focusRequester(
                focusRequester
            )
            .onPreviewKeyEvent { event ->

                if (
                    event.type !=
                    KeyEventType.KeyDown
                ) {

                    return@onPreviewKeyEvent false
                }


                if (
                    isConfirmKey(event)
                ) {

                    onBack()

                    true

                } else {

                    false
                }
            }
            .focusable()
    ) {

        Column(
            modifier =
                Modifier.fillMaxSize()
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 7.dp
                    )
            ) {

                Column(
                    modifier = Modifier
                        .align(
                            Alignment.TopStart
                        )
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(32.dp)
                    )


                    Box(
                        modifier = Modifier
                            .size(102.dp)
                            .background(
                                Color.White,
                                RoundedCornerShape(
                                    12.dp
                                )
                            )
                            .padding(5.dp),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        if (
                            animal.localImagePath
                                .isNullOrBlank()
                        ) {

                            Text(
                                text = "?",

                                color = Color.Black,

                                fontSize = 40.sp,

                                fontWeight =
                                    FontWeight.Black
                            )

                        } else {

                            AsyncImage(
                                model =
                                    animal.localImagePath,

                                contentDescription =
                                    animal.displayName,

                                modifier =
                                    Modifier.fillMaxSize(),

                                contentScale =
                                    ContentScale.Fit
                            )
                        }
                    }
                }


                Column(
                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .fillMaxWidth(0.5f)
                        .pointerInput(Unit) {

                            detectTapGestures(
                                onTap = {
                                    onBack()
                                }
                            )
                        },

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            animal.displayName.uppercase(),

                        modifier =
                            Modifier.fillMaxWidth(),

                        color = Color.White,

                        fontFamily = GameFont,

                        fontSize = 15.sp,

                        lineHeight = 16.sp,

                        fontWeight =
                            FontWeight.Black,

                        maxLines = 1,

                        overflow =
                            TextOverflow.Ellipsis,

                        textAlign =
                            TextAlign.Center
                    )


                    Text(
                        text =
                            animal.scientificName,

                        modifier =
                            Modifier.fillMaxWidth(),

                        color =
                            Color.White.copy(
                                alpha = 0.80f
                            ),

                        fontSize = 9.sp,

                        lineHeight = 10.sp,

                        fontStyle =
                            FontStyle.Italic,

                        fontWeight =
                            FontWeight.Medium,

                        maxLines = 1,

                        overflow =
                            TextOverflow.Ellipsis,

                        textAlign =
                            TextAlign.Center
                    )
                }


                Column(
                    modifier = Modifier
                        .align(
                            Alignment.TopStart
                        )
                        .padding(
                            start = 113.dp,
                            top = 38.dp
                        )
                ) {

                    DetailInfo(
                        title = "CONTINENT",

                        value =
                            animal.continents
                                .joinToString(" • ")
                    )


                    if (
                        !animal.family
                            .isNullOrBlank()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )


                        DetailInfo(
                            title = "FAMILLE",

                            value =
                                animal.family
                        )
                    }


                    if (
                        !animal.genus
                            .isNullOrBlank()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )


                        DetailInfo(
                            title = "GENRE",

                            value =
                                animal.genus
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )


                    // STATUS affiche maintenant le nombre de captures
                    // en plus du statut découvert/non découvert.
                    // Petit effet "pop" quand on arrive tout juste
                    // d'une capture (justCaptured), pour bien mettre
                    // en valeur l'incrément du compteur.
                    DetailInfo(
                        title = "STATUS",

                        value =
                            if (animal.discovered) {
                                "DISCOVERED • ${animal.captureCount}×"
                            } else {
                                "UNDISCOVERED"
                            },

                        animatePop = justCaptured
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(
                        start = 6.dp,
                        end = 6.dp,
                        bottom = 5.dp
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        5.dp
                    )
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            Color.Black.copy(
                                alpha = 0.18f
                            ),
                            RoundedCornerShape(
                                8.dp
                            )
                        )
                        .padding(
                            horizontal = 7.dp,
                            vertical = 5.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.fillMaxSize()
                    ) {

                        Text(
                            text = "DESCRIPTION",

                            color = Color.White,

                            fontFamily = GameFont,

                            fontSize = 11.sp,

                            lineHeight = 12.sp,

                            fontWeight =
                                FontWeight.Black,

                            maxLines = 1
                        )


                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )


                        Text(
                            text =
                                animal.descriptionFr
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: "Aucune description disponible.",

                            modifier =
                                Modifier.fillMaxWidth(),

                            color =
                                Color.White.copy(
                                    alpha = 0.95f
                                ),

                            fontSize = 9.sp,

                            lineHeight = 10.5.sp
                        )
                    }
                }


                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            Color.White.copy(
                                alpha = 0.13f
                            ),
                            RoundedCornerShape(
                                8.dp
                            )
                        )
                        .padding(
                            horizontal = 7.dp,
                            vertical = 5.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.fillMaxSize()
                    ) {

                        Text(
                            text = "★ FUN FACT",

                            color = Color.White,

                            fontFamily = GameFont,

                            fontSize = 11.sp,

                            lineHeight = 12.sp,

                            fontWeight =
                                FontWeight.Black,

                            maxLines = 1
                        )


                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )


                        Text(
                            text =
                                animal.funFactFr
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: "Aucun fun fact disponible.",

                            modifier =
                                Modifier.fillMaxWidth(),

                            color =
                                Color.White.copy(
                                    alpha = 0.97f
                                ),

                            fontSize = 9.sp,

                            lineHeight = 10.5.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DetailInfo(
    title: String,
    value: String,
    animatePop: Boolean = false
) {

    // Petit effet "pop" (agrandissement puis retour à la taille
    // normale, avec un léger rebond) — utilisé uniquement pour STATUS
    // juste après une capture ; sans effet pour tous les autres
    // appels (CONTINENT, FAMILLE, GENRE), qui gardent animatePop à
    // sa valeur par défaut (false).
    val popScale =
        remember {
            Animatable(
                if (animatePop) 0.5f else 1f
            )
        }


    LaunchedEffect(animatePop) {

        if (animatePop) {

            popScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }


    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text = title,

            color =
                Color.White.copy(
                    alpha = 0.62f
                ),

            fontFamily = GameFont,

            fontSize = 6.5.sp,

            lineHeight = 7.sp,

            fontWeight =
                FontWeight.Black,

            maxLines = 1
        )


        Text(
            text = value,

            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = popScale.value
                    scaleY = popScale.value
                },

            color = Color.White,

            fontFamily = GameFont,

            fontSize = 9.sp,

            lineHeight = 10.sp,

            fontWeight =
                FontWeight.Bold,

            maxLines = 1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}