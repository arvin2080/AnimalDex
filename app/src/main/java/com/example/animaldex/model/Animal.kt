package com.example.animaldex.model


data class Animal(

    val id: Int,

    val nameFr: String?,

    val commonNameEN: String?,

    val scientificName: String,

    val family: String?,

    val genus: String?,

    val continents: List<String>,

    val phylopicUuid: String?,

    val discovered: Boolean,

    val descriptionFr: String?,

    val funFactFr: String?

) {

    // ============================================================
    // DISPLAY NAME
    // ============================================================

    val displayName: String
        get() {

            if (!nameFr.isNullOrBlank()) {

                return firstName(nameFr)
                    ?: nameFr
            }


            val english =
                firstName(commonNameEN)


            if (!english.isNullOrBlank()) {

                return english
            }


            return scientificName
        }


    // ============================================================
    // LOCAL PHYLOPIC IMAGE
    // ============================================================

    val localImagePath: String?
        get() {

            if (phylopicUuid.isNullOrBlank()) {

                return null
            }


            return "file:///android_asset/thumbnails/$phylopicUuid.png"
        }
}


// ============================================================
// FIRST COMMON NAME
// ============================================================

fun firstName(
    names: String?
): String? {

    if (names.isNullOrBlank()) {

        return null
    }


    return names
        .split("|")
        .firstOrNull()
        ?.trim()
}