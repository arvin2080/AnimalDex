package com.example.animaldex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import com.example.animaldex.model.*
import com.example.animaldex.ui.components.SearchHeader
import com.example.animaldex.util.*


@Composable
fun SearchGroupsBody(
    groups: List<IconGroup>,
    color: androidx.compose.ui.graphics.Color,
    onGroupSelected: (IconGroup) -> Unit
) {

    if (groups.isEmpty()) {

        NoResultBody(color)

        return
    }


    PagedGroupGrid(
        groups = groups,

        color = color,

        showBack = false,

        onBack = {},

        onGroupSelected =
            onGroupSelected
    )
}


@Composable
fun IconGroupsScreen(
    continent: ContinentData,
    allAnimals: List<Animal>,
    onGroupSelected: (IconGroup) -> Unit,
    onBack: () -> Unit
) {

    var search by remember(
        continent.name
    ) {
        mutableStateOf("")
    }


    var filter by remember(
        continent.name
    ) {
        mutableStateOf(
            GroupFilter.ALL
        )
    }


    val continentAnimals =
        remember(
            continent.name,
            allAnimals
        ) {

            allAnimals.filter {

                it.continents.contains(
                    continent.name
                )
            }
        }


    val searchedAnimals =
        continentAnimals.filter {

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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                continent.normalColor
            )
    ) {

        SearchHeader(
            leftText = null,

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
                continent.normalColor
        )


        if (groups.isEmpty()) {

            NoResultBody(
                continent.normalColor
            )

        } else {

            PagedGroupGrid(
                groups = groups,

                color =
                    continent.normalColor,

                showBack = true,

                onBack = onBack,

                onGroupSelected =
                    onGroupSelected
            )
        }
    }
}