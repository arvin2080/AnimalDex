package com.example.animaldex.model

import androidx.compose.ui.graphics.Color

data class ContinentData(

    val name: String,

    val normalColor: Color,

    val selectedColor: Color

) {

    // ============================================================
    // IMAGE DU CONTINENT
    // ============================================================

    // Chemin vers l'image du continent, à ajouter plus tard dans
    // app/src/main/assets/continents/. Nom de fichier dérivé du nom
    // du continent (minuscules, espaces remplacés par des underscores) :
    // ex. "NORTH AMERICA" -> "north_america.png".
    val imagePath: String
        get() {

            val slug =
                name
                    .lowercase()
                    .replace(" ", "_")


            return "file:///android_asset/continents/$slug.png"
        }
}