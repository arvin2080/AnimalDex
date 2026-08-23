package com.example.animaldex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
    backgroundColor: Color
) {

    var filterMenuOpen by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(backgroundColor)
    ) {

        if (!leftText.isNullOrBlank()) {

            Text(
                text = leftText,

                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 9.dp)
                    .fillMaxWidth(0.42f),

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 13.sp,

                fontWeight = FontWeight.Black,

                maxLines = 1,

                overflow = TextOverflow.Ellipsis
            )
        }


        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.43f)
                .height(37.dp)
                .padding(end = 4.dp)
                .background(
                    Color.White,
                    RoundedCornerShape(8.dp)
                )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 9.dp,
                        end = 43.dp
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
                        fontSize = 10.sp,
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
                    .width(39.dp)
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
                            .width(22.dp)
                            .height(3.dp)
                            .background(
                                Color.Gray,
                                RoundedCornerShape(2.dp)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(3.dp)
                            .background(
                                Color.Gray,
                                RoundedCornerShape(2.dp)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .width(10.dp)
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
    }
}