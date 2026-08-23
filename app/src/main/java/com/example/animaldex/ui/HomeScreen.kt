package com.example.animaldex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil3.compose.AsyncImage

import com.example.animaldex.model.Animal
import com.example.animaldex.model.ContinentData
import com.example.animaldex.model.GroupFilter
import com.example.animaldex.model.IconGroup

import com.example.animaldex.ui.components.SearchHeader

import com.example.animaldex.util.GameFont
import com.example.animaldex.util.allAnimalsColor
import com.example.animaldex.util.animalMatchesSearch
import com.example.animaldex.util.applyGroupFilter
import com.example.animaldex.util.buildIconGroups
import com.example.animaldex.util.continents


// ============================================================
// HOME SCREEN
// ============================================================

@Composable
fun HomeScreen(
    animals: List<Animal>,
    onContinentSelected: (ContinentData) -> Unit,
    onGlobalGroupSelected: (IconGroup) -> Unit,
    onCameraClick: () -> Unit
) {

    var search by remember {
        mutableStateOf("")
    }


    var filter by remember {
        mutableStateOf(
            GroupFilter.ALL
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFEEF3F8)
            )
    ) {

        // ====================================================
        // HEADER
        // ====================================================

        SearchHeader(
            leftText = "ANIMALDEX",

            search = search,

            onSearchChange = {
                search = it
            },

            filterLabel =
                filter.label,

            filterOptions =
                GroupFilter.entries.map {
                    it.label
                },

            onFilterSelected = { index ->

                filter =
                    GroupFilter.entries[index]
            },

            backgroundColor =
                allAnimalsColor
        )


        // ====================================================
        // CAMERA BUTTON
        // ====================================================

        CameraMenuButton(
            onClick =
                onCameraClick
        )


        // ====================================================
        // NORMAL CONTINENT MENU
        // ====================================================

        if (search.isBlank()) {

            ContinentMenuBody(
                animals = animals,

                groupFilter =
                    filter,

                onContinentSelected =
                    onContinentSelected
            )

        } else {

            // ================================================
            // GLOBAL SEARCH
            // ================================================

            val searchedAnimals =
                animals.filter {

                    animalMatchesSearch(
                        it,
                        search
                    )
                }


            val groups =
                applyGroupFilter(
                    buildIconGroups(
                        searchedAnimals
                    ),
                    filter
                )


            SearchGroupsBody(
                groups = groups,

                color =
                    allAnimalsColor,

                onGroupSelected =
                    onGlobalGroupSelected
            )
        }
    }
}


// ============================================================
// CAMERA MENU BUTTON
// ============================================================

@Composable
fun CameraMenuButton(
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 7.dp,
                end = 7.dp,
                top = 5.dp,
                bottom = 2.dp
            )
            .height(
                45.dp
            )
            .background(
                color =
                    Color(0xFF172A3A),

                shape =
                    RoundedCornerShape(
                        11.dp
                    )
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

        Row(
            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.Center
        ) {

            // ------------------------------------------------
            // SIMPLE CAMERA ICON
            // ------------------------------------------------

            Box(
                modifier = Modifier
                    .width(
                        25.dp
                    )
                    .height(
                        18.dp
                    )
                    .background(
                        color =
                            Color.White,

                        shape =
                            RoundedCornerShape(
                                4.dp
                            )
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .size(
                            9.dp
                        )
                        .background(
                            color =
                                Color(0xFF172A3A),

                            shape =
                                RoundedCornerShape(
                                    50
                                )
                        )
                )
            }


            Spacer(
                modifier =
                    Modifier.width(
                        9.dp
                    )
            )


            Text(
                text =
                    "ANIMAL SCANNER",

                color =
                    Color.White,

                fontFamily =
                    GameFont,

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.Black
            )
        }
    }
}


// ============================================================
// CONTINENT MENU
// ============================================================

@Composable
fun ContinentMenuBody(
    animals: List<Animal>,
    groupFilter: GroupFilter,
    onContinentSelected: (ContinentData) -> Unit
) {

    val listState =
        rememberLazyListState()


    LazyColumn(
        state =
            listState,

        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                horizontal =
                    7.dp,

                vertical =
                    7.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        itemsIndexed(
            continents
        ) { _, continent ->

            // ================================================
            // ANIMALS OF THIS CONTINENT
            // ================================================

            val continentAnimals =
                animals.filter {

                    it.continents.contains(
                        continent.name
                    )
                }


            // ================================================
            // ICON GROUPS
            // ================================================

            val filteredGroups =
                applyGroupFilter(
                    buildIconGroups(
                        continentAnimals
                    ),
                    groupFilter
                )


            // ================================================
            // SHOW SOME ICON EXAMPLES
            // ================================================

            val examples =
                filteredGroups.take(
                    4
                )


            ContinentCard(
                continent =
                    continent,

                examples =
                    examples,

                onOpen = {

                    onContinentSelected(
                        continent
                    )
                }
            )
        }


        item {

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )
        }
    }
}


// ============================================================
// CONTINENT CARD
// ============================================================

@Composable
fun ContinentCard(
    continent: ContinentData,
    examples: List<IconGroup>,
    onOpen: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                76.dp
            )
            .background(
                color =
                    continent.normalColor,

                shape =
                    RoundedCornerShape(
                        13.dp
                    )
            )
            .pointerInput(
                continent.name
            ) {

                detectTapGestures(
                    onTap = {

                        onOpen()
                    }
                )
            }
            .padding(
                horizontal =
                    13.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        // ====================================================
        // CONTINENT NAME
        // ====================================================

        Text(
            text =
                continent.name,

            modifier =
                Modifier.weight(
                    1f
                ),

            color =
                Color.White,

            fontFamily =
                GameFont,

            fontSize =
                16.sp,

            fontWeight =
                FontWeight.Black,

            maxLines =
                2
        )


        // ====================================================
        // EXAMPLE ANIMAL ICONS
        // ====================================================

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    6.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            examples.forEach { group ->

                if (
                    !group.imagePath
                        .isNullOrBlank()
                ) {

                    AsyncImage(
                        model =
                            group.imagePath,

                        contentDescription =
                            group.displayName,

                        modifier =
                            Modifier.size(
                                43.dp
                            ),

                        contentScale =
                            ContentScale.Fit
                    )
                }
            }
        }
    }
}