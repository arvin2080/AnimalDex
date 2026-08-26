package com.example.animaldex.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.animaldex.model.Animal
import com.example.animaldex.model.ContinentData
import com.example.animaldex.model.GroupFilter
import com.example.animaldex.model.IconGroup

import com.example.animaldex.ui.components.SearchHeader

import com.example.animaldex.util.CameraButtonColor
import com.example.animaldex.util.GameFont
import com.example.animaldex.util.PageBackgroundColor
import com.example.animaldex.util.allAnimalsColor
import com.example.animaldex.util.animalMatchesSearch
import com.example.animaldex.util.applyGroupFilter
import com.example.animaldex.util.buildIconGroups
import com.example.animaldex.util.continents


// ============================================================
// CONTINENT GRID ENTRY
// ============================================================

// Associe un continent au nombre d'animaux découverts/total pour ce
// continent, pour l'affichage façon compteur "x/y" dans la grille
// d'accueil (même style que les groupes d'animaux).
data class ContinentGridEntry(
    val continent: ContinentData,
    val discoveredCount: Int,
    val totalCount: Int
)


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
                PageBackgroundColor
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
        // NORMAL CONTINENT GRID
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
                    CameraButtonColor,

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
                                CameraButtonColor,

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
                    PageBackgroundColor,

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
// CONTINENT GRID (grille de 9, une case par continent)
// ============================================================

@Composable
fun ContinentMenuBody(
    animals: List<Animal>,
    groupFilter: GroupFilter,
    onContinentSelected: (ContinentData) -> Unit
) {

    val entries =
        remember(
            animals
        ) {

            continents.map { continent ->

                val continentAnimals =
                    animals.filter {

                        it.continents.contains(
                            continent.name
                        )
                    }


                ContinentGridEntry(
                    continent = continent,

                    discoveredCount =
                        continentAnimals.count {
                            it.discovered
                        },

                    totalCount =
                        continentAnimals.size
                )
            }
        }


    // Le filtre du header (ALL / A-Z / DISCOVERED) trie maintenant les
    // continents eux-mêmes, puisque chaque case représente un continent
    // entier (il n'y a plus d'icônes d'exemples à filtrer à l'intérieur).
    val orderedEntries =
        when (groupFilter) {

            GroupFilter.ALL ->
                entries

            GroupFilter.ALPHABETICAL ->
                entries.sortedBy {
                    it.continent.name.lowercase()
                }

            GroupFilter.DISCOVERED ->
                entries.sortedWith(

                    compareByDescending<ContinentGridEntry> {

                        it.discoveredCount > 0
                    }

                        .thenByDescending {

                            it.discoveredCount
                        }

                        .thenBy {

                            it.continent.name.lowercase()
                        }
                )
        }


    PagedContinentGrid(
        entries = orderedEntries,

        onContinentSelected =
            onContinentSelected
    )
}