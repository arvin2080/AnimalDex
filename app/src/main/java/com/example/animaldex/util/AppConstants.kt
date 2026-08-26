package com.example.animaldex.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.animaldex.model.ContinentData

val GameFont =
    FontFamily.Monospace


val allAnimalsColor =
    Color(0xFF274C77)


// Fond gris foncé uniforme pour toutes les pages de grille (groupes/animaux),
// plus sombre que les cases, identique partout pour ne pas créer de
// discontinuité visuelle lors des transitions de glissement entre écrans.
val PageBackgroundColor =
    Color(0xFF1E1E20)


// Gris clair pour le bouton "ANIMAL SCANNER", nettement plus clair que
// PageBackgroundColor pour rester visible sur le fond de la page d'accueil.
val CameraButtonColor =
    Color(0xFFB5B5BC)


val continents = listOf(

    ContinentData(
        "AFRICA",
        Color(0xFFD97706),
        Color(0xFFA65300)
    ),

    ContinentData(
        "EUROPE",
        Color(0xFF4A6FA5),
        Color(0xFF2F4F7A)
    ),

    ContinentData(
        "ASIA",
        Color(0xFFB74747),
        Color(0xFF812E2E)
    ),

    ContinentData(
        "NORTH AMERICA",
        Color(0xFF4F7C5B),
        Color(0xFF35563F)
    ),

    ContinentData(
        "SOUTH AMERICA",
        Color(0xFF6A8F55),
        Color(0xFF48643A)
    ),

    ContinentData(
        "OCEANIA",
        Color(0xFF3C8D91),
        Color(0xFF286468)
    ),

    ContinentData(
        "OCEAN",
        Color(0xFF1E88E5),
        Color(0xFF0D47A1)
    ),

    ContinentData(
        "ANTARCTICA",
        Color(0xFF7597AE),
        Color(0xFF4C697C)
    ),

    ContinentData(
        "UNKNOWN",
        Color(0xFF6B7280),
        Color(0xFF40464F)
    )
)