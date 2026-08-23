package com.example.animaldex.util

import com.example.animaldex.model.Animal
import com.example.animaldex.model.AnimalFilter
import com.example.animaldex.model.GroupFilter
import com.example.animaldex.model.IconGroup


fun buildIconGroups(
    animals: List<Animal>
): List<IconGroup> {

    val groups =

        animals

            .filter {

                !it.phylopicUuid
                    .isNullOrBlank()
            }

            .groupBy {

                it.phylopicUuid!!
            }

            .map { entry ->

                IconGroup(

                    uuid =
                        entry.key,

                    animals =
                        entry.value
                )
            }


    return diversifyGroups(
        groups
    )
}


fun groupCategory(
    group: IconGroup
): String {

    val family =

        group.animals

            .firstOrNull()

            ?.family

            ?.lowercase()

            ?: ""


    return when {

        family in setOf(

            "felidae",
            "canidae",
            "ursidae",
            "bovidae",
            "cervidae",
            "mustelidae",
            "hominidae",
            "cercopithecidae",
            "macropodidae",
            "leporidae",
            "elephantidae",
            "rhinocerotidae",
            "equidae",
            "giraffidae",
            "suidae",
            "delphinidae",
            "phocidae",
            "otariidae",
            "camelidae"

        ) -> "MAMMAL"


        family in setOf(

            "accipitridae",
            "strigidae",
            "tytonidae",
            "psittacidae",
            "psittaculidae",
            "cacatuidae",
            "anatidae",
            "corvidae",
            "falconidae",
            "ramphastidae",
            "bucerotidae",
            "alcedinidae",
            "spheniscidae",
            "phoenicopteridae",
            "pelecanidae",
            "ciconiidae"

        ) -> "BIRD"


        family in setOf(

            "viperidae",
            "elapidae",
            "pythonidae",
            "boidae",
            "gekkonidae",
            "varanidae",
            "chamaeleonidae",
            "crocodylidae",
            "alligatoridae",
            "testudinidae",
            "cheloniidae"

        ) -> "REPTILE"


        family in setOf(

            "ranidae",
            "bufonidae",
            "hylidae",
            "ambystomatidae",
            "salamandridae",
            "dendrobatidae"

        ) -> "AMPHIBIAN"


        family in setOf(

            "lamnidae",
            "sphyrnidae",
            "carcharhinidae",
            "mobulidae",
            "scombridae",
            "salmonidae",
            "muraenidae",
            "syngnathidae",
            "pomacentridae",
            "acanthuridae",
            "tetraodontidae"

        ) -> "FISH"


        else -> "OTHER"
    }
}


fun stableScore(
    group: IconGroup
): Int {

    return (
            group.uuid.hashCode()
                    xor
                    group.displayName.hashCode()
            )
}


fun diversifyGroups(
    groups: List<IconGroup>
): List<IconGroup> {

    val categoryOrder = listOf(

        "MAMMAL",

        "BIRD",

        "REPTILE",

        "FISH",

        "AMPHIBIAN",

        "OTHER"
    )


    val buckets =

        categoryOrder

            .associateWith { category ->

                groups

                    .filter {

                        groupCategory(
                            it
                        ) == category
                    }

                    .sortedBy {

                        stableScore(
                            it
                        )
                    }

                    .toMutableList()
            }


    val result =
        mutableListOf<IconGroup>()


    var added =
        true


    while (added) {

        added =
            false


        categoryOrder.forEach { category ->

            val bucket =
                buckets[category]


            if (
                bucket != null &&
                bucket.isNotEmpty()
            ) {

                result.add(
                    bucket.removeAt(0)
                )


                added =
                    true
            }
        }
    }


    return result
}


fun applyGroupFilter(

    groups: List<IconGroup>,

    filter: GroupFilter

): List<IconGroup> {

    return when (filter) {

        GroupFilter.ALL ->

            groups


        GroupFilter.ALPHABETICAL ->

            groups.sortedBy {

                it.displayName
                    .lowercase()
            }


        GroupFilter.DISCOVERED ->

            groups.sortedWith(

                compareByDescending<IconGroup> {

                    it.discoveredCount > 0
                }

                    .thenByDescending {

                        it.discoveredCount
                    }

                    .thenBy {

                        it.displayName
                            .lowercase()
                    }
            )
    }
}


fun applyAnimalFilter(

    animals: List<Animal>,

    filter: AnimalFilter

): List<Animal> {

    return when (filter) {

        AnimalFilter.ALL ->

            animals


        AnimalFilter.ALPHABETICAL ->

            animals.sortedBy {

                it.displayName
                    .lowercase()
            }


        AnimalFilter.DISCOVERED ->

            animals.sortedWith(

                compareByDescending<Animal> {

                    it.discovered
                }

                    .thenBy {

                        it.displayName
                            .lowercase()
                    }
            )
    }
}