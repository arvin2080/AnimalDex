package com.example.animaldex.ui

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


            // ----------------------------------------------------
            // ZONE DU HAUT : trois blocs indépendants dans le même
            // Box, chacun avec sa propre position :
            // 1. Image : ancrée en haut à gauche, avec un Spacer
            //    généreux au-dessus pour la faire descendre.
            // 2. Nom + sous-titre : ancrés en haut, centrés dans la
            //    moitié DROITE de l'écran (inchangé).
            // 3. Infos (CONTINENT, FAMILLE, GENRE, STATUS) : bloc
            //    séparé, décalé horizontalement pour démarrer juste
            //    après l'image (pas au milieu de l'écran), et
            //    décalé verticalement pour rester visuellement sous
            //    le nom, comme avant.
            // ----------------------------------------------------

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 7.dp
                    )
            ) {

                // 1. Image, avec Spacer dédié au-dessus pour la
                // décaler vers le bas.
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


                // 2. Nom + sous-titre, centrés dans la moitié
                // droite de l'écran, tapables (retour en arrière).
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


                // 3. Infos : démarrent juste après l'image (padding
                // start ≈ largeur de l'image + petit espace), et
                // décalées vers le bas pour rester sous le nom.
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


                    DetailInfo(
                        title = "STATUS",

                        value =
                            if (
                                animal.discovered
                            ) {

                                "DISCOVERED"

                            } else {

                                "UNDISCOVERED"
                            }
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
    value: String
) {

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

            modifier =
                Modifier.fillMaxWidth(),

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