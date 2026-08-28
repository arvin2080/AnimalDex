package com.example.animaldex.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import com.example.animaldex.util.GameFont


@Composable
fun SearchHeader(
    leftText: String?,
    search: String,
    onSearchChange: (String) -> Unit,
    filterLabel: String,
    filterOptions: List<String>,
    onFilterSelected: (Int) -> Unit,
    backgroundColor: Color,
    backButtonColor: Color = Color.White.copy(alpha = 0.18f),
    backIcon: String = "←",
    onBack: (() -> Unit)? = null
) {

    var filterMenuOpen by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(backgroundColor)
    ) {

        // ----------------------------------------------------
        // GAUCHE : titre uniquement (plus de flèche retour ici)
        // ----------------------------------------------------

        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.50f)
                .padding(start = 8.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            if (!leftText.isNullOrBlank()) {

                Text(
                    text = leftText,

                    color = Color.White,

                    fontFamily = GameFont,

                    fontSize = 13.sp,

                    fontWeight = FontWeight.Black,

                    maxLines = 1,

                    overflow = TextOverflow.Ellipsis
                )
            }
        }


        // ----------------------------------------------------
        // DROITE : barre de recherche (raccourcie) + flèche retour
        // ----------------------------------------------------

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.42f)
                .padding(end = 12.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(8.dp)
                    )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 8.dp,
                            end = 36.dp
                        ),

                    contentAlignment = Alignment.CenterStart
                ) {

                    if (search.isBlank()) {

                        Text(
                            text = "SEARCH...",

                            color = Color.Black.copy(
                                alpha = 0.32f
                            ),

                            fontFamily = GameFont,

                            fontSize = 8.sp,

                            fontWeight = FontWeight.Bold
                        )
                    }


                    BasicTextField(
                        value = search,

                        onValueChange = onSearchChange,

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true,

                        textStyle = TextStyle(
                            color = Color.Black,
                            fontFamily = GameFont,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),

                        cursorBrush = SolidColor(
                            Color.Black
                        )
                    )
                }


                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(33.dp)
                        .fillMaxHeight()
                        .pointerInput(filterLabel) {

                            detectTapGestures(
                                onTap = {
                                    filterMenuOpen = true
                                }
                            )
                        },

                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.End,

                        verticalArrangement = Arrangement.spacedBy(
                            4.dp
                        )
                    ) {

                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.dp)
                                .background(
                                    Color.Gray,
                                    RoundedCornerShape(2.dp)
                                )
                        )

                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height(3.dp)
                                .background(
                                    Color.Gray,
                                    RoundedCornerShape(2.dp)
                                )
                        )

                        Box(
                            modifier = Modifier
                                .width(9.dp)
                                .height(3.dp)
                                .background(
                                    Color.Gray,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }


                    DropdownMenu(
                        expanded = filterMenuOpen,

                        onDismissRequest = {
                            filterMenuOpen = false
                        }
                    ) {

                        filterOptions.forEachIndexed { index, option ->

                            DropdownMenuItem(
                                text = {

                                    Text(
                                        text =
                                            if (option == filterLabel) {
                                                "●  $option"
                                            } else {
                                                "   $option"
                                            },

                                        fontFamily = GameFont,

                                        fontWeight = FontWeight.Bold
                                    )
                                },

                                onClick = {

                                    onFilterSelected(index)

                                    filterMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }


            if (onBack != null) {

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            backButtonColor,
                            CircleShape
                        )
                        .pointerInput(Unit) {

                            detectTapGestures(
                                onTap = {
                                    onBack()
                                }
                            )
                        },

                    contentAlignment = Alignment.Center
                ) {

                    if (backIcon == "←") {

                        // Flèche dessinée à la main, pour garantir un centrage
                        // exact au pixel près, sans dépendre des métriques
                        // asymétriques de la police.
                        Canvas(
                            modifier = Modifier.size(13.dp)
                        ) {

                            val strokeWidthPx = 1.8.dp.toPx()
                            val w = size.width
                            val h = size.height
                            val midY = h / 2f
                            val headSize = w * 0.42f

                            drawLine(
                                color = Color.White,
                                start = Offset(w, midY),
                                end = Offset(0f, midY),
                                strokeWidth = strokeWidthPx,
                                cap = StrokeCap.Round
                            )

                            drawLine(
                                color = Color.White,
                                start = Offset(0f, midY),
                                end = Offset(headSize, 0f),
                                strokeWidth = strokeWidthPx,
                                cap = StrokeCap.Round
                            )

                            drawLine(
                                color = Color.White,
                                start = Offset(0f, midY),
                                end = Offset(headSize, h),
                                strokeWidth = strokeWidthPx,
                                cap = StrokeCap.Round
                            )
                        }

                    } else {

                        Text(
                            text = backIcon,

                            color = Color.White,

                            fontFamily = GameFont,

                            fontSize = 14.sp,

                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}