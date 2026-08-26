package com.example.animaldex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import com.example.animaldex.model.*
import com.example.animaldex.ui.components.SearchHeader
import com.example.animaldex.util.*


@Composable
fun GroupAnimalsScreen(
    group: IconGroup,
    color: Color,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onAnimalSelected: (Animal) -> Unit,
    onBack: () -> Unit
) {

    var search by remember(
        group.uuid
    ) {
        mutableStateOf("")
    }


    var filter by remember(
        group.uuid
    ) {
        mutableStateOf(
            AnimalFilter.ALL
        )
    }


    val searchedAnimals =
        group.animals.filter {

            animalMatchesSearch(
                it,
                search
            )
        }


    val animals =
        applyAnimalFilter(
            searchedAnimals,
            filter
        )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackgroundColor)
    ) {

        SearchHeader(
            leftText =
                group.displayName.uppercase(),

            search = search,

            onSearchChange = {
                search = it
            },

            filterLabel =
                filter.label,

            filterOptions =
                AnimalFilter.entries.map {
                    it.label
                },

            onFilterSelected = { index ->

                filter =
                    AnimalFilter.entries[index]
            },

            backgroundColor = PageBackgroundColor,

            onBack = onBack
        )


        if (animals.isEmpty()) {

            NoResultBody(PageBackgroundColor)

        } else {

            PagedAnimalGrid(
                animals = animals,

                color = color,

                showBack = false,

                onBack = onBack,

                onAnimalSelected =
                    onAnimalSelected,

                initialPageIndex =
                    currentPage,

                onPageIndexChanged =
                    onPageChange
            )
        }
    }
}


@Composable
fun SearchGroupsBody(
    groups: List<IconGroup>,
    color: Color,
    onGroupSelected: (IconGroup) -> Unit
) {

    if (groups.isEmpty()) {

        NoResultBody(PageBackgroundColor)

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
    currentPage: Int,
    onPageChange: (Int) -> Unit,
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
            .background(PageBackgroundColor)
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

            backgroundColor = PageBackgroundColor,

            onBack = onBack
        )


        if (groups.isEmpty()) {

            NoResultBody(PageBackgroundColor)

        } else {

            PagedGroupGrid(
                groups = groups,

                color =
                    continent.normalColor,

                showBack = false,

                onBack = onBack,

                onGroupSelected =
                    onGroupSelected,

                initialPageIndex =
                    currentPage,

                onPageIndexChanged =
                    onPageChange
            )
        }
    }
}