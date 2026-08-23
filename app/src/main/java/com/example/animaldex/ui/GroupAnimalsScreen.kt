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
            .background(color)
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

            backgroundColor = color
        )


        if (animals.isEmpty()) {

            NoResultBody(color)

        } else {

            PagedAnimalGrid(
                animals = animals,

                color = color,

                showBack = true,

                onBack = onBack,

                onAnimalSelected =
                    onAnimalSelected
            )
        }
    }
}