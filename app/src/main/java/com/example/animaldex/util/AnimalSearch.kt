package com.example.animaldex.util

import com.example.animaldex.model.Animal
import com.example.animaldex.model.IconGroup


fun animalMatchesSearch(

    animal: Animal,

    search: String

): Boolean {

    if (search.isBlank()) {

        return true
    }


    val q =
        search
            .trim()
            .lowercase()


    val values = listOf(

        animal.displayName,

        animal.nameFr,

        animal.commonNameEN,

        animal.scientificName,

        animal.family,

        animal.genus
    )


    return values

        .filterNotNull()

        .any {

            it.lowercase()
                .contains(q)
        }
}


fun groupMatchesSearch(

    group: IconGroup,

    search: String

): Boolean {

    if (search.isBlank()) {

        return true
    }


    val q =
        search
            .trim()
            .lowercase()


    if (
        group.displayName
            .lowercase()
            .contains(q)
    ) {

        return true
    }


    return group.animals.any {

        animalMatchesSearch(
            it,
            search
        )
    }
}