package com.example.animaldex.model

data class IconGroup(

    val uuid: String,

    val animals: List<Animal>

) {

    val totalCount: Int
        get() =
            animals.size


    val discoveredCount: Int
        get() =
            animals.count {

                it.discovered
            }


    val imagePath: String
        get() =

            "file:///android_asset/thumbnails/$uuid.png"


    val displayName: String
        get() {

            val families =

                animals

                    .mapNotNull {

                        it.family
                    }

                    .filter {

                        it.isNotBlank()
                    }


            if (families.isNotEmpty()) {

                val mostCommonFamily =

                    families

                        .groupingBy {

                            it
                        }

                        .eachCount()

                        .maxByOrNull {

                            it.value
                        }

                        ?.key


                if (!mostCommonFamily.isNullOrBlank()) {

                    return mostCommonFamily
                }
            }


            val genera =

                animals

                    .mapNotNull {

                        it.genus
                    }

                    .filter {

                        it.isNotBlank()
                    }


            if (genera.isNotEmpty()) {

                return genera

                    .groupingBy {

                        it
                    }

                    .eachCount()

                    .maxByOrNull {

                        it.value
                    }

                    ?.key

                    ?: "ANIMAL"
            }


            return animals

                .firstOrNull()

                ?.scientificName

                ?.substringBefore(" ")

                ?: "ANIMAL"
        }
}