package com.example.animaldex.ui

import android.content.Context

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlin.math.*

import io.github.sceneview.*
import io.github.sceneview.node.*
import io.github.sceneview.math.*

import coil3.compose.AsyncImage

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

data class ContinentGridEntry(
    val continent: ContinentData,
    val discoveredCount: Int,
    val totalCount: Int
)


// ============================================================
// SKINS DE PLANÈTE
// ============================================================
//
// Un seul skin pour l'instant (le modèle actuel). Ajouter un skin
// plus tard = ajouter une entrée ici, rien d'autre à changer.

data class PlanetSkin(
    val id: String,
    val displayName: String,
    val modelAssetPath: String,
    val previewImagePath: String
)


private val AvailablePlanetSkins: List<PlanetSkin> =
    listOf(

        PlanetSkin(
            id = "planet_earth",
            displayName = "Terre",
            modelAssetPath = "models/earth/planet_earth.glb",
            previewImagePath = "file:///android_asset/models/earth/images/terre1.png"
        ),

        PlanetSkin(
            id = "mon_nouveau_skin",
            displayName = "NOM AFFICHÉ",
            modelAssetPath = "models/earth/mon_fichier.glb",
            previewImagePath = "file:///android_asset/models/earth/mon_apercu.png"
        )

        // Prochains skins : ajouter une entrée PlanetSkin ici.
    )


// ============================================================
// SKINS DE SATELLITE ("lune")
// ============================================================
//
// Même principe que les skins de planète, complètement indépendant.

data class SatelliteSkin(
    val id: String,
    val displayName: String,
    val modelAssetPath: String,
    val previewImagePath: String
)


private val AvailableSatelliteSkins: List<SatelliteSkin> =
    listOf(

        SatelliteSkin(
            id = "polaroid",
            displayName = "Polaroid",
            modelAssetPath = "models/satellite/low_poly_polaroid.glb",
            previewImagePath = "file:///android_asset/models/satellite/images/polaroid1.png"
        ),

        SatelliteSkin(
            id = "polaroid",
            displayName = "Polaroid",
            modelAssetPath = "models/satellite/low_poly_polaroid.glb",
            previewImagePath = "file:///android_asset/models/satellite/images/polaroid1.png"
        ),

        // Prochains skins de satellite : ajouter une entrée ici.
    )


// ============================================================
// PERSISTANCE (skin planète + satellite + réglages)
// ============================================================

private const val SettingsPrefsName = "animaldex_settings"

private const val PrefKeySelectedSkinId = "selected_skin_id"
private const val PrefKeyPlanetScale = "planet_scale"

private const val PrefKeySelectedSatelliteSkinId = "selected_satellite_skin_id"
private const val PrefKeySatelliteScale = "satellite_scale"
private const val PrefKeySatelliteOrbitRadius = "satellite_orbit_radius"
private const val PrefKeySatelliteOrbitEnabled = "satellite_orbit_enabled"

private const val DefaultPlanetScale = 0.5f
private const val MinPlanetScale = 0.1f
private const val MaxPlanetScale = 0.9f

private const val DefaultSatelliteScale = 2f
private const val MinSatelliteScale = 0.30f
private const val MaxSatelliteScale = 5f

private const val DefaultSatelliteOrbitRadius = 8f
private const val MinSatelliteOrbitRadius = 9f
private const val MaxSatelliteOrbitRadius = 15f


private fun loadSelectedSkinId(
    context: Context
): String {

    val prefs =
        context.getSharedPreferences(
            SettingsPrefsName,
            Context.MODE_PRIVATE
        )

    val fallback = AvailablePlanetSkins.first().id

    return prefs.getString(
        PrefKeySelectedSkinId,
        fallback
    ) ?: fallback
}


private fun saveSelectedSkinId(
    context: Context,
    id: String
) {

    context.getSharedPreferences(
        SettingsPrefsName,
        Context.MODE_PRIVATE
    )
        .edit()
        .putString(PrefKeySelectedSkinId, id)
        .apply()
}


private fun loadPlanetScale(
    context: Context
): Float {

    val prefs =
        context.getSharedPreferences(
            SettingsPrefsName,
            Context.MODE_PRIVATE
        )

    return prefs.getFloat(
        PrefKeyPlanetScale,
        DefaultPlanetScale
    )
}


private fun savePlanetScale(
    context: Context,
    scale: Float
) {

    context.getSharedPreferences(
        SettingsPrefsName,
        Context.MODE_PRIVATE
    )
        .edit()
        .putFloat(PrefKeyPlanetScale, scale)
        .apply()
}


private fun loadSelectedSatelliteSkinId(
    context: Context
): String {

    val prefs =
        context.getSharedPreferences(
            SettingsPrefsName,
            Context.MODE_PRIVATE
        )

    val fallback = AvailableSatelliteSkins.first().id

    return prefs.getString(
        PrefKeySelectedSatelliteSkinId,
        fallback
    ) ?: fallback
}


private fun saveSelectedSatelliteSkinId(
    context: Context,
    id: String
) {

    context.getSharedPreferences(
        SettingsPrefsName,
        Context.MODE_PRIVATE
    )
        .edit()
        .putString(PrefKeySelectedSatelliteSkinId, id)
        .apply()
}


private fun loadSatelliteScale(
    context: Context
): Float {

    val prefs =
        context.getSharedPreferences(
            SettingsPrefsName,
            Context.MODE_PRIVATE
        )

    return prefs.getFloat(
        PrefKeySatelliteScale,
        DefaultSatelliteScale
    )
}


private fun saveSatelliteScale(
    context: Context,
    scale: Float
) {

    context.getSharedPreferences(
        SettingsPrefsName,
        Context.MODE_PRIVATE
    )
        .edit()
        .putFloat(PrefKeySatelliteScale, scale)
        .apply()
}


private fun loadSatelliteOrbitRadius(
    context: Context
): Float {

    val prefs =
        context.getSharedPreferences(
            SettingsPrefsName,
            Context.MODE_PRIVATE
        )

    return prefs.getFloat(
        PrefKeySatelliteOrbitRadius,
        DefaultSatelliteOrbitRadius
    )
}


private fun saveSatelliteOrbitRadius(
    context: Context,
    radius: Float
) {

    context.getSharedPreferences(
        SettingsPrefsName,
        Context.MODE_PRIVATE
    )
        .edit()
        .putFloat(PrefKeySatelliteOrbitRadius, radius)
        .apply()
}


private fun loadSatelliteOrbitEnabled(
    context: Context
): Boolean {

    val prefs =
        context.getSharedPreferences(
            SettingsPrefsName,
            Context.MODE_PRIVATE
        )

    return prefs.getBoolean(
        PrefKeySatelliteOrbitEnabled,
        true
    )
}


private fun saveSatelliteOrbitEnabled(
    context: Context,
    enabled: Boolean
) {

    context.getSharedPreferences(
        SettingsPrefsName,
        Context.MODE_PRIVATE
    )
        .edit()
        .putBoolean(PrefKeySatelliteOrbitEnabled, enabled)
        .apply()
}


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

    val context = androidx.compose.ui.platform.LocalContext.current

    var search by remember {
        mutableStateOf("")
    }


    var filter by remember {
        mutableStateOf(
            GroupFilter.ALL
        )
    }


    // --------------------------------------------------------
    // ÉTAT PERSISTANT : skin planète + taille + skin satellite +
    // ses réglages. Chargé une seule fois depuis SharedPreferences,
    // sauvegardé à chaque changement.
    // --------------------------------------------------------

    var selectedSkinId by remember {
        mutableStateOf(
            loadSelectedSkinId(context)
        )
    }

    var planetScale by remember {
        mutableFloatStateOf(
            loadPlanetScale(context)
        )
    }

    val selectedSkin =
        remember(selectedSkinId) {
            AvailablePlanetSkins.firstOrNull {
                it.id == selectedSkinId
            } ?: AvailablePlanetSkins.first()
        }


    var selectedSatelliteSkinId by remember {
        mutableStateOf(
            loadSelectedSatelliteSkinId(context)
        )
    }

    var satelliteScale by remember {
        mutableFloatStateOf(
            loadSatelliteScale(context)
        )
    }

    var satelliteOrbitRadius by remember {
        mutableFloatStateOf(
            loadSatelliteOrbitRadius(context)
        )
    }

    var satelliteOrbitEnabled by remember {
        mutableStateOf(
            loadSatelliteOrbitEnabled(context)
        )
    }

    val selectedSatelliteSkin =
        remember(selectedSatelliteSkinId) {
            AvailableSatelliteSkins.firstOrNull {
                it.id == selectedSatelliteSkinId
            } ?: AvailableSatelliteSkins.first()
        }


    // --------------------------------------------------------
    // ÉTAT DU MENU RÉGLAGES
    // --------------------------------------------------------

    var showSettingsMenu by remember {
        mutableStateOf(false)
    }

    var showSkinsPanel by remember {
        mutableStateOf(false)
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    PageBackgroundColor
                )
        ) {

            SearchHeader(
                leftText = "ANIMALDEX",
                backIcon = "⚙",

                onBack = {
                    showSettingsMenu = true
                    showSkinsPanel = false
                },

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



            if (search.isBlank()) {

                ContinentMenuBody(
                    animals = animals,

                    groupFilter =
                        filter,

                    planetScale =
                        planetScale,

                    selectedSkin =
                        selectedSkin,

                    satelliteScale =
                        satelliteScale,

                    selectedSatelliteSkin =
                        selectedSatelliteSkin,

                    satelliteOrbitRadius =
                        satelliteOrbitRadius,

                    satelliteOrbitEnabled =
                        satelliteOrbitEnabled,

                    onContinentSelected =
                        onContinentSelected,

                    onCameraClick =
                        onCameraClick
                )

            } else {

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

        // ----------------------------------------------------
        // MENU RÉGLAGES (voile + panneau)
        // ----------------------------------------------------

        if (showSettingsMenu) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.55f)
                    )
                    .pointerInput(Unit) {

                        detectTapGestures(
                            onTap = {

                                showSettingsMenu = false
                                showSkinsPanel = false
                            }
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .background(
                        PageBackgroundColor,
                        RoundedCornerShape(14.dp)
                    )
                    .pointerInput(Unit) {

                        // Absorbe les taps pour ne pas fermer le
                        // menu quand on touche à l'intérieur.
                        detectTapGestures(
                            onTap = {}
                        )
                    }
                    .padding(16.dp)
            ) {

                if (!showSkinsPanel) {

                    SettingsMainPanel(
                        onSkinsClick = {
                            showSkinsPanel = true
                        }
                    )

                } else {

                    SkinsPanel(
                        planetSkins = AvailablePlanetSkins,
                        selectedPlanetSkinId = selectedSkinId,
                        planetScale = planetScale,

                        satelliteSkins = AvailableSatelliteSkins,
                        selectedSatelliteSkinId = selectedSatelliteSkinId,
                        satelliteScale = satelliteScale,
                        satelliteOrbitRadius = satelliteOrbitRadius,
                        satelliteOrbitEnabled = satelliteOrbitEnabled,

                        onBack = {
                            showSkinsPanel = false
                        },

                        onPlanetSkinSelected = { skin ->

                            selectedSkinId = skin.id

                            saveSelectedSkinId(
                                context,
                                skin.id
                            )
                        },

                        onPlanetScaleChange = { newScale ->

                            planetScale = newScale

                            savePlanetScale(
                                context,
                                newScale
                            )
                        },

                        onSatelliteSkinSelected = { skin ->

                            selectedSatelliteSkinId = skin.id

                            saveSelectedSatelliteSkinId(
                                context,
                                skin.id
                            )
                        },

                        onSatelliteScaleChange = { newScale ->

                            satelliteScale = newScale

                            saveSatelliteScale(
                                context,
                                newScale
                            )
                        },

                        onSatelliteOrbitRadiusChange = { newRadius ->

                            satelliteOrbitRadius = newRadius

                            saveSatelliteOrbitRadius(
                                context,
                                newRadius
                            )
                        },

                        onSatelliteOrbitEnabledChange = { newEnabled ->

                            satelliteOrbitEnabled = newEnabled

                            saveSatelliteOrbitEnabled(
                                context,
                                newEnabled
                            )
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// PANNEAU RÉGLAGES PRINCIPAL (6 boutons, 2 colonnes)
// ============================================================

@Composable
private fun SettingsMainPanel(
    onSkinsClick: () -> Unit
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "RÉGLAGES",

            color = Color.White,

            fontFamily = GameFont,

            fontSize = 13.sp,

            fontWeight = FontWeight.Black
        )


        Spacer(
            modifier = Modifier.height(14.dp)
        )


        val labels =
            listOf(
                "SKINS", "BIENTÔT",
                "BIENTÔT", "BIENTÔT",
                "BIENTÔT", "BIENTÔT"
            )

        for (row in 0 until 3) {

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                for (col in 0 until 2) {

                    val index = row * 2 + col
                    val label = labels[index]
                    val isEnabled = (label == "SKINS")

                    Box(
                        modifier = Modifier
                            .size(
                                width = 110.dp,
                                height = 48.dp
                            )
                            .background(
                                Color.White.copy(
                                    alpha = if (isEnabled) 0.20f else 0.08f
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .pointerInput(isEnabled) {

                                detectTapGestures(
                                    onTap = {

                                        if (isEnabled) {
                                            onSkinsClick()
                                        }
                                    }
                                )
                            },

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = label,

                            color =
                                Color.White.copy(
                                    alpha = if (isEnabled) 1f else 0.45f
                                ),

                            fontFamily = GameFont,

                            fontSize = 10.sp,

                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }


            if (row < 2) {

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }
        }
    }
}


// ============================================================
// PANNEAU SKINS — onglet TERRE / SATELLITE
// ============================================================

private enum class SkinCategory {
    TERRE, SATELLITE
}


@Composable
private fun SkinsPanel(
    planetSkins: List<PlanetSkin>,
    selectedPlanetSkinId: String,
    planetScale: Float,

    satelliteSkins: List<SatelliteSkin>,
    selectedSatelliteSkinId: String,
    satelliteScale: Float,
    satelliteOrbitRadius: Float,
    satelliteOrbitEnabled: Boolean,

    onBack: () -> Unit,
    onPlanetSkinSelected: (PlanetSkin) -> Unit,
    onPlanetScaleChange: (Float) -> Unit,
    onSatelliteSkinSelected: (SatelliteSkin) -> Unit,
    onSatelliteScaleChange: (Float) -> Unit,
    onSatelliteOrbitRadiusChange: (Float) -> Unit,
    onSatelliteOrbitEnabledChange: (Boolean) -> Unit
) {

    var category by remember {
        mutableStateOf(SkinCategory.TERRE)
    }


    Column {

        Row(
            verticalAlignment =
                Alignment.CenterVertically,

            modifier = Modifier
                .pointerInput(Unit) {

                    detectTapGestures(
                        onTap = {
                            onBack()
                        }
                    )
                }
        ) {

            Text(
                text = "←",

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 14.sp,

                lineHeight = 14.sp,

                fontWeight = FontWeight.Black
            )


            Spacer(
                modifier = Modifier.width(8.dp)
            )


            Text(
                text = "SKINS",

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 13.sp,

                fontWeight = FontWeight.Black
            )
        }


        Spacer(
            modifier = Modifier.height(14.dp)
        )


        // ------------------------------------------------
        // ONGLETS TERRE / SATELLITE
        // ------------------------------------------------

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            listOf(
                SkinCategory.TERRE to "TERRE",
                SkinCategory.SATELLITE to "SATELLITE"
            ).forEach { (cat, label) ->

                val isActive = category == cat

                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(
                                alpha = if (isActive) 0.25f else 0.10f
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .pointerInput(cat) {

                            detectTapGestures(
                                onTap = {
                                    category = cat
                                }
                            )
                        }
                        .padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                ) {

                    Text(
                        text = label,

                        color =
                            Color.White.copy(
                                alpha = if (isActive) 1f else 0.6f
                            ),

                        fontFamily = GameFont,

                        fontSize = 10.sp,

                        fontWeight = FontWeight.Black
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        if (category == SkinCategory.TERRE) {

            Text(
                text = "TAILLE DE LA PLANÈTE",

                color =
                    Color.White.copy(alpha = 0.65f),

                fontFamily = GameFont,

                fontSize = 9.sp,

                fontWeight = FontWeight.Black
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            LabeledSlider(
                initialValue = planetScale,
                minValue = MinPlanetScale,
                maxValue = MaxPlanetScale,
                onValueChange = onPlanetScaleChange
            )


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            SkinGrid(
                items = planetSkins,
                selectedId = selectedPlanetSkinId,
                getId = { it.id },
                getDisplayName = { it.displayName },
                getPreviewPath = { it.previewImagePath },
                onSelected = onPlanetSkinSelected
            )

        } else {

            Text(
                text = "TAILLE DU SATELLITE",

                color =
                    Color.White.copy(alpha = 0.65f),

                fontFamily = GameFont,

                fontSize = 9.sp,

                fontWeight = FontWeight.Black
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            LabeledSlider(
                initialValue = satelliteScale,
                minValue = MinSatelliteScale,
                maxValue = MaxSatelliteScale,
                onValueChange = onSatelliteScaleChange
            )


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            Text(
                text = "RAYON DE L'ORBITE",

                color =
                    Color.White.copy(alpha = 0.65f),

                fontFamily = GameFont,

                fontSize = 9.sp,

                fontWeight = FontWeight.Black
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            LabeledSlider(
                initialValue = satelliteOrbitRadius,
                minValue = MinSatelliteOrbitRadius,
                maxValue = MaxSatelliteOrbitRadius,
                onValueChange = onSatelliteOrbitRadiusChange
            )


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            Row(
                verticalAlignment =
                    Alignment.CenterVertically,

                modifier = Modifier
                    .pointerInput(Unit) {

                        detectTapGestures(
                            onTap = {

                                onSatelliteOrbitEnabledChange(
                                    !satelliteOrbitEnabled
                                )
                            }
                        )
                    }
            ) {

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(3.dp)
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    if (!satelliteOrbitEnabled) {

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }


                Spacer(
                    modifier = Modifier.width(8.dp)
                )


                Text(
                    text = "Désactiver l'orbite (coin fixe)",

                    color =
                        Color.White.copy(alpha = 0.75f),

                    fontFamily = GameFont,

                    fontSize = 8.sp,

                    fontWeight = FontWeight.Bold
                )
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            SkinGrid(
                items = satelliteSkins,
                selectedId = selectedSatelliteSkinId,
                getId = { it.id },
                getDisplayName = { it.displayName },
                getPreviewPath = { it.previewImagePath },
                onSelected = onSatelliteSkinSelected
            )
        }
    }
}


// ============================================================
// GRILLE DE SKINS (générique — réutilisée pour planète et
// satellite)
// ============================================================

@Composable
private fun <T> SkinGrid(
    items: List<T>,
    selectedId: String,
    getId: (T) -> String,
    getDisplayName: (T) -> String,
    getPreviewPath: (T) -> String,
    onSelected: (T) -> Unit
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),

        horizontalArrangement =
            Arrangement.spacedBy(10.dp),

        verticalArrangement =
            Arrangement.spacedBy(10.dp),

        modifier = Modifier
            .height(160.dp)
            .width(260.dp)
    ) {

        items(items) { item ->

            val isSelected =
                getId(item) == selectedId

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally,

                modifier = Modifier
                    .pointerInput(getId(item)) {

                        detectTapGestures(
                            onTap = {
                                onSelected(item)
                            }
                        )
                    }
            ) {

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Color.White.copy(alpha = 0.10f),
                            RoundedCornerShape(10.dp)
                        )
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Color.White.copy(alpha = 0.25f),
                                    RoundedCornerShape(10.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .padding(4.dp),

                    contentAlignment =
                        Alignment.Center
                ) {

                    AsyncImage(
                        model = getPreviewPath(item),

                        contentDescription =
                            getDisplayName(item),

                        modifier =
                            Modifier.fillMaxSize(),

                        contentScale =
                            ContentScale.Fit
                    )
                }


                Spacer(
                    modifier = Modifier.height(4.dp)
                )


                Text(
                    text =
                        getDisplayName(item).uppercase(),

                    color =
                        if (isSelected) {
                            Color.White
                        } else {
                            Color.White.copy(alpha = 0.65f)
                        },

                    fontFamily = GameFont,

                    fontSize = 8.sp,

                    fontWeight = FontWeight.Black,

                    maxLines = 1
                )
            }
        }
    }
}


// ============================================================
// CURSEUR GÉNÉRIQUE (pin glissable) — réutilisé pour taille de
// planète, taille de satellite, rayon d'orbite.
// ============================================================

@Composable
private fun LabeledSlider(
    initialValue: Float,
    minValue: Float,
    maxValue: Float,
    onValueChange: (Float) -> Unit
) {

    val density =
        androidx.compose.ui.platform.LocalDensity.current

    val trackWidthDp = 260.dp
    val pinSizeDp = 22.dp

    val trackWidthPx =
        with(density) { trackWidthDp.toPx() }

    val pinSizePx =
        with(density) { pinSizeDp.toPx() }


    var localValue by remember(initialValue) {
        mutableFloatStateOf(initialValue)
    }

    val fraction =
        ((localValue - minValue) / (maxValue - minValue))
            .coerceIn(0f, 1f)

    val pinOffsetPx =
        fraction * (trackWidthPx - pinSizePx)


    Box(
        modifier = Modifier
            .width(trackWidthDp)
            .height(pinSizeDp)
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Color.White.copy(alpha = 0.25f),
                    RoundedCornerShape(2.dp)
                )
        )


        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(
                    x = with(density) { pinOffsetPx.toDp() }
                )
                .size(pinSizeDp)
                .background(
                    Color.White,
                    CircleShape
                )
                .pointerInput(Unit) {

                    detectDragGestures(
                        onDrag = { change, dragAmount ->

                            val deltaFraction =
                                dragAmount.x /
                                        (trackWidthPx - pinSizePx)

                            val newValue =
                                (
                                        localValue +
                                                deltaFraction * (maxValue - minValue)
                                        ).coerceIn(minValue, maxValue)

                            localValue = newValue

                            onValueChange(newValue)

                            change.consume()
                        }
                    )
                }
        )
    }
}


// ============================================================
// CAMERA MENU BUTTON
// ============================================================


// ============================================================
// CONTINENT MENU BODY
// ============================================================

@Composable
fun ContinentMenuBody(
    animals: List<Animal>,
    groupFilter: GroupFilter,
    planetScale: Float,
    selectedSkin: PlanetSkin,
    satelliteScale: Float,
    selectedSatelliteSkin: SatelliteSkin,
    satelliteOrbitRadius: Float,
    satelliteOrbitEnabled: Boolean,
    onContinentSelected: (ContinentData) -> Unit,
    onCameraClick: () -> Unit
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


    ContinentSphere(
        entries = entries,

        planetScale =
            planetScale,

        selectedSkin =
            selectedSkin,

        satelliteScale =
            satelliteScale,

        selectedSatelliteSkin =
            selectedSatelliteSkin,

        satelliteOrbitRadius =
            satelliteOrbitRadius,

        satelliteOrbitEnabled =
            satelliteOrbitEnabled,

        onContinentSelected =
            onContinentSelected,

        onSatelliteClick =
            onCameraClick
    )
}


// ============================================================
// AGENCEMENT GÉOGRAPHIQUE DES CONTINENTS
// ============================================================
//
// Chaque ligne : (nom, uy, thetaDegrees).
// uy : -1 = pôle nord (haut du globe), 0 = équateur, +1 = pôle sud
// (bas du globe). thetaDegrees : 0-360°, position autour du globe
// (comme une longitude).

private val FixedContinentLayout: List<Triple<String, Float, Float>> =
    listOf(

        Triple("NORTH AMERICA", 0.74f, 60f),
        Triple("SOUTH AMERICA", -0.14f, 30f),
        Triple("EUROPE", 0.81f, 290f),
        Triple("AFRICA", 0.12f, 290f),
        Triple("ASIA", 0.71f, 220f),
        Triple("OCEANIA", -0.23f, 180f),

        Triple("UNKNOWN", -1f, 0f),
        Triple("ANTARCTICA", 1f, 0f)
    )


private fun sphericalDirection(
    azimuthRad: Float,
    elevationRad: Float
): Triple<Float, Float, Float> {

    val cosEl = cos(elevationRad)

    return Triple(
        cosEl * sin(azimuthRad),
        sin(elevationRad),
        cosEl * cos(azimuthRad)
    )
}

private fun crossProduct(
    a: Triple<Float, Float, Float>,
    b: Triple<Float, Float, Float>
): Triple<Float, Float, Float> {

    return Triple(
        a.second * b.third - a.third * b.second,
        a.third * b.first - a.first * b.third,
        a.first * b.second - a.second * b.first
    )
}

private fun normalizeVector(
    v: Triple<Float, Float, Float>
): Triple<Float, Float, Float> {

    val len =
        sqrt(v.first * v.first + v.second * v.second + v.third * v.third)
            .let { if (it < 0.0001f) 1f else it }

    return Triple(v.first / len, v.second / len, v.third / len)
}

private fun dotProduct(
    a: Triple<Float, Float, Float>,
    b: Triple<Float, Float, Float>
): Float {

    return a.first * b.first + a.second * b.second + a.third * b.third
}


private data class ContinentAnchor(
    val entry: ContinentGridEntry,
    val unit: Triple<Float, Float, Float>
)


private fun buildContinentAnchors(
    entries: List<ContinentGridEntry>
): List<ContinentAnchor> {

    return FixedContinentLayout.mapNotNull { (name, uy, thetaDegrees) ->

        val entry =
            entries.firstOrNull {
                it.continent.name == name
            }
                ?: return@mapNotNull null

        val thetaRadians =
            thetaDegrees * (PI.toFloat() / 180f)

        val radiusAtY =
            sqrt(
                (1f - uy * uy)
                    .coerceAtLeast(0f)
            )

        val unit =
            Triple(
                radiusAtY * cos(thetaRadians),
                uy,
                radiusAtY * sin(thetaRadians)
            )

        ContinentAnchor(entry, unit)
    }
}


private data class ContinentHitZone(
    val entry: ContinentGridEntry,
    val xPx: Float,
    val yPx: Float,
    val clickable: Boolean
)


// ============================================================
// SILHOUETTES DE CONTINENTS "COLLÉES" SUR LA SPHÈRE
// ============================================================
//
// Formes stylisées low-poly, en coordonnées locales : X positif =
// EST, Y positif = SUD. "Collées" sur la surface de la sphère via
// un patch tangent, donc elles se courbent avec le globe au lieu
// de rester plates face à l'écran.

private fun generateCirclePolygon(
    points: Int,
    radius: Float
): List<Offset> {

    return (0 until points).map { i ->

        val angle = (2.0 * PI * i / points).toFloat()

        Offset(cos(angle) * radius, sin(angle) * radius)
    }
}


private val DefaultSilhouette: List<Offset> =
    generateCirclePolygon(points = 12, radius = 0.85f)


private val ContinentSilhouettes: Map<String, List<Offset>> =
    mapOf(

        "NORTH AMERICA" to listOf(
            Offset(-0.37f, -0.12f), Offset(-0.213f, -0.294f), Offset(0.0f, -0.58f),
            Offset(0.302f, -0.416f), Offset(0.451f, -0.146f), Offset(0.362f, 0.118f),
            Offset(0.174f, 0.239f), Offset(0.0f, 0.408f), Offset(-0.32f, 0.441f),
            Offset(-0.951f, 0.309f)
        ),

        "SOUTH AMERICA" to listOf(
            Offset(-0.523f, -0.119f), Offset(-0.376f, -0.3f), Offset(-0.199f, -0.413f),
            Offset(0.0f, -0.543f), Offset(0.21f, -0.437f), Offset(0.278f, -0.222f),
            Offset(0.451f, -0.103f), Offset(0.961f, 0.219f), Offset(0.782f, 0.623f),
            Offset(0.214f, 0.445f), Offset(0.0f, 0.316f), Offset(-0.182f, 0.379f),
            Offset(-0.36f, 0.287f), Offset(-0.846f, 0.193f)
        ),

        "EUROPE" to listOf(
            Offset(-0.449f, -0.12f), Offset(-0.373f, -0.373f), Offset(-0.157f, -0.587f),
            Offset(0.248f, -0.926f), Offset(0.707f, -0.707f), Offset(0.904f, -0.242f),
            Offset(0.401f, 0.108f), Offset(0.322f, 0.322f), Offset(0.21f, 0.782f),
            Offset(-0.214f, 0.797f), Offset(-0.579f, 0.579f), Offset(-0.583f, 0.156f)
        ),

        "AFRICA" to listOf(
            Offset(-0.772f, -0.154f), Offset(-0.582f, -0.389f), Offset(-0.402f, -0.602f),
            Offset(-0.139f, -0.7f), Offset(0.137f, -0.689f), Offset(0.335f, -0.502f),
            Offset(0.64f, -0.428f), Offset(0.981f, -0.195f), Offset(0.976f, 0.194f),
            Offset(0.714f, 0.477f), Offset(0.356f, 0.533f), Offset(0.119f, 0.596f),
            Offset(-0.113f, 0.568f), Offset(-0.387f, 0.579f), Offset(-0.727f, 0.486f),
            Offset(-0.922f, 0.183f)
        ),

        "ASIA" to listOf(
            Offset(-0.77f, -0.136f), Offset(-0.661f, -0.382f), Offset(-0.448f, -0.533f),
            Offset(-0.34f, -0.934f), Offset(0.0f, -0.933f), Offset(0.322f, -0.885f),
            Offset(0.643f, -0.766f), Offset(0.794f, -0.459f), Offset(0.825f, -0.146f),
            Offset(0.715f, 0.126f), Offset(0.847f, 0.489f), Offset(0.54f, 0.644f),
            Offset(0.304f, 0.836f), Offset(0.0f, 0.987f), Offset(-0.339f, 0.931f),
            Offset(-0.637f, 0.759f), Offset(-0.805f, 0.465f), Offset(-0.84f, 0.148f)
        ),

        "OCEANIA" to listOf(
            Offset(-0.573f, -0.153f), Offset(-0.368f, -0.368f), Offset(-0.139f, -0.518f),
            Offset(0.169f, -0.632f), Offset(0.665f, -0.665f), Offset(0.703f, -0.188f),
            Offset(0.966f, 0.259f), Offset(0.659f, 0.659f), Offset(0.147f, 0.55f),
            Offset(-0.116f, 0.432f), Offset(-0.343f, 0.343f), Offset(-0.586f, 0.157f)
        ),

        "ANTARCTICA" to listOf(
            Offset(-0.851f, -0.194f), Offset(-0.661f, -0.527f), Offset(-0.23f, -0.479f),
            Offset(0.0f, -0.421f), Offset(0.168f, -0.349f), Offset(0.444f, -0.354f),
            Offset(0.915f, -0.209f), Offset(0.975f, 0.223f), Offset(0.594f, 0.474f),
            Offset(0.268f, 0.556f), Offset(0.0f, 0.885f), Offset(-0.416f, 0.863f),
            Offset(-0.721f, 0.575f), Offset(-0.818f, 0.187f)
        ),

        "UNKNOWN" to DefaultSilhouette
    )



private val ContinentSizeMultiplier: Map<String, Float> =
    mapOf(
        "ASIA" to 1.00f, "AFRICA" to 0.90f, "NORTH AMERICA" to 0.88f,
        "SOUTH AMERICA" to 0.75f, "ANTARCTICA" to 0.80f, "EUROPE" to 0.78f,
        "OCEANIA" to 0.55f, "UNKNOWN" to 0.45f
    )

private const val ContinentPatchScale = 0.55f


// Base locale à un point de la sphère : tangentEast (direction est,
// theta croissant) et tangentSouth (direction sud, uy croissant).
private fun buildTangentBasis(
    center: Triple<Float, Float, Float>,
    thetaRadians: Float
): Pair<Triple<Float, Float, Float>, Triple<Float, Float, Float>> {

    val tangentEast =
        Triple(-sin(thetaRadians), 0f, cos(thetaRadians))

    val tangentSouth =
        crossProduct(tangentEast, center)

    return tangentEast to tangentSouth
}


// Forme figée d'un continent (sommets fixes dans l'espace du monde,
// indépendants de la rotation de la caméra).
private data class ContinentBodyVertices(
    val entry: ContinentGridEntry,
    val center: Triple<Float, Float, Float>,
    val vertices: List<Triple<Float, Float, Float>>
)


private fun buildContinentBodyVertices(
    entries: List<ContinentGridEntry>
): List<ContinentBodyVertices> {

    return FixedContinentLayout.mapNotNull { (name, uy, thetaDegrees) ->

        val entry =
            entries.firstOrNull { it.continent.name == name }
                ?: return@mapNotNull null

        val thetaRadians = thetaDegrees * (PI.toFloat() / 180f)

        val radiusAtY =
            sqrt((1f - uy * uy).coerceAtLeast(0f))

        val center =
            Triple(
                radiusAtY * cos(thetaRadians),
                uy,
                radiusAtY * sin(thetaRadians)
            )

        val (tangentEast, tangentSouth) =
            buildTangentBasis(center, thetaRadians)

        val silhouette = ContinentSilhouettes[name] ?: DefaultSilhouette
        val patchScale =
            ContinentPatchScale * (ContinentSizeMultiplier[name] ?: 1f)

        val vertices =
            silhouette.map { localPoint ->

                val lx = localPoint.x * patchScale
                val ly = localPoint.y * patchScale

                val px = center.first + lx * tangentEast.first + ly * tangentSouth.first
                val py = center.second + lx * tangentEast.second + ly * tangentSouth.second
                val pz = center.third + lx * tangentEast.third + ly * tangentSouth.third

                normalizeVector(Triple(px, py, pz))
            }

        ContinentBodyVertices(entry, center, vertices)
    }
}


// ============================================================
// CONTINENT SPHERE (modèle 3D réel, rotation au doigt, satellite
// en orbite ou fixe dans le coin)
// ============================================================


private const val SphereScreenRadiusFraction = 0.25f
private const val ContinentHitRadiusFraction = 0.30f
private const val SphereRotationSensitivityDegrees = 0.35f

private const val EarthDistance = 10f

// Rotation de fond, toujours active, très lente (≈ un tour complet
// toutes les ~3 minutes à 60 fps).
private const val IdleRotationDegreesPerFrame = -0.20f

// Facteur de décélération de l'inertie, appliqué chaque frame après
// un lâcher (plus proche de 1 = ralentit plus lentement).
private const val FlingDecayFactor = 0.90f

// En dessous de ce seuil (degrés/frame), l'inertie est considérée
// comme arrêtée.
private const val FlingStopThreshold = 0.01f

// Satellite : vitesse d'orbite autour de la Terre, vitesse de
// rotation sur lui-même, et léger angle d'inclinaison de l'orbite
// (purement esthétique, non réglable pour l'instant).
private const val SatelliteOrbitDegreesPerFrame = 0.35f
private const val SatelliteSpinDegreesPerFrame = 0.6f
private const val SatelliteOrbitElevationDegrees = 18f

// Le balayage VISIBLE à l'écran de l'orbite (dans le plan right/up)
// est mis à l'échelle séparément du réglage RAYON DE L'ORBITE, qui va
// jusqu'à 5 : projeté tel quel sur un cercle face à la caméra, ça
// pousserait le satellite hors de l'écran sur les côtés (la largeur
// de l'écran étant généralement la dimension la plus contraignante,
// contrairement à la hauteur). Ce facteur ramène le réglage dans une
// plage qui reste toujours visible et cliquable, quelle que soit sa
// valeur — seule la profondeur (vers/depuis la caméra) continue
// d'utiliser le réglage brut, sans cette limite.
private const val SatelliteLateralScale = 0.25f

// Inclinaison de l'axe de rotation PROPRE du satellite (sur lui-même,
// pas son orbite) — 0° = spin bien droit sur l'axe Y du modèle,
// comme actuellement. Une valeur non nulle le fait "spin en biais",
// un peu comme l'inclinaison de 23,5° de l'axe des pôles de la Terre.
private const val SatelliteSpinTiltDegrees = 20f

// Position "coin en haut à gauche" quand l'orbite est désactivée —
// exprimée par rapport à la caméra elle-même (donc reste fixe à
// l'écran quelle que soit la rotation de la Terre).
private const val SatelliteHudForwardOffset = 6f
private const val SatelliteHudRightOffset = 2f
private const val SatelliteHudUpOffset = 1.5f

// Rayon de la zone tactile autour du satellite (indépendant de son
// échelle visuelle, pour garder une cible facile à toucher).
private val SatelliteHitRadiusDp = 60.dp


@Composable
private fun ContinentSphere(
    entries: List<ContinentGridEntry>,
    planetScale: Float,
    selectedSkin: PlanetSkin,
    satelliteScale: Float,
    selectedSatelliteSkin: SatelliteSkin,
    satelliteOrbitRadius: Float,
    satelliteOrbitEnabled: Boolean,
    onContinentSelected: (ContinentData) -> Unit,
    onSatelliteClick: () -> Unit
) {

    val density = androidx.compose.ui.platform.LocalDensity.current

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val cameraNode = rememberCameraNode(engine)

    val earthInstance =
        rememberModelInstance(
            modelLoader,
            selectedSkin.modelAssetPath
        )

    val satelliteInstance =
        rememberModelInstance(
            modelLoader,
            selectedSatelliteSkin.modelAssetPath
        )


    val oceanEntry =
        remember(entries) {
            entries.firstOrNull { it.continent.name == "OCEAN" }
        }


    val anchors =
        remember(entries) {
            buildContinentAnchors(entries)
        }

    val bodyVertices =
        remember(entries) {
            buildContinentBodyVertices(entries)
        }


    var rotationXDegrees by remember { mutableFloatStateOf(10f) }
    var rotationYDegrees by remember { mutableFloatStateOf(200f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Vitesse d'inertie courante (degrés/frame), décroît vers 0
    // après un lâcher. Dernier delta de glissement brut, utilisé
    // pour amorcer l'inertie au moment du relâchement.
    var flingVelocityX by remember { mutableFloatStateOf(0f) }
    var flingVelocityY by remember { mutableFloatStateOf(0f) }
    var lastDragDeltaX by remember { mutableFloatStateOf(0f) }
    var lastDragDeltaY by remember { mutableFloatStateOf(0f) }

    // Satellite : angle d'orbite autour de la Terre + angle de
    // rotation sur lui-même, tous deux incrémentés en continu.
    var satelliteOrbitAngleDegrees by remember { mutableFloatStateOf(0f) }
    var satelliteSpinDegrees by remember { mutableFloatStateOf(0f) }


    // Boucle continue : rotation de fond + décélération de l'inertie
    // + avancement du satellite (orbite et rotation sur lui-même).
    LaunchedEffect(Unit) {

        while (true) {

            withFrameMillis {}

            rotationYDegrees += IdleRotationDegreesPerFrame

            if (
                abs(flingVelocityX) > FlingStopThreshold ||
                abs(flingVelocityY) > FlingStopThreshold
            ) {

                rotationYDegrees +=
                    -flingVelocityX * SphereRotationSensitivityDegrees

                val newRotationX =
                    rotationXDegrees +
                            (flingVelocityY * SphereRotationSensitivityDegrees)

                rotationXDegrees =
                    newRotationX.coerceIn(-75f, 75f)

                flingVelocityX *= FlingDecayFactor
                flingVelocityY *= FlingDecayFactor

            } else {

                flingVelocityX = 0f
                flingVelocityY = 0f
            }

            satelliteOrbitAngleDegrees =
                (satelliteOrbitAngleDegrees + SatelliteOrbitDegreesPerFrame) % 360f

            satelliteSpinDegrees =
                (satelliteSpinDegrees + SatelliteSpinDegreesPerFrame) % 360f
        }
    }


    val azimuthRad = rotationYDegrees * (PI.toFloat() / 180f)
    val elevationRad = rotationXDegrees * (PI.toFloat() / 180f)

    val eyeDir = sphericalDirection(azimuthRad, elevationRad)
    val worldUp = Triple(0f, 1f, 0f)
    val right = normalizeVector(crossProduct(worldUp, eyeDir))
    val up = crossProduct(eyeDir, right)

    SideEffect {
        cameraNode.lookAt(
            eye = Position(
                EarthDistance * eyeDir.first,
                EarthDistance * eyeDir.second,
                EarthDistance * eyeDir.third
            ),
            center = Position(0f, 0f, 0f),
            up = Direction(y = 1f)
        )
    }


    // --------------------------------------------------------
    // POSITION DU SATELLITE : soit en orbite autour de la Terre
    // (coordonnées sphériques, même principe que la caméra), soit
    // fixe par rapport à la caméra elle-même (reste dans le coin
    // en haut à gauche quelle que soit la rotation de la Terre).
    // --------------------------------------------------------

    val satelliteWorldPosition: Triple<Float, Float, Float> =

        if (satelliteOrbitEnabled) {

            val orbitAzimuthRad =
                satelliteOrbitAngleDegrees * (PI.toFloat() / 180f)

            val orbitTiltRad =
                SatelliteOrbitElevationDegrees * (PI.toFloat() / 180f)

            // Orbite verrouillée au repère de la CAMÉRA (right / up /
            // eyeDir) plutôt qu'aux axes fixes du monde (X / Y / Z).
            //
            // Pourquoi : dans cette scène, "la Terre qui tourne" est en
            // réalité la caméra qui orbite autour d'une Terre immobile
            // (le modèle Terre n'a jamais de rotation appliquée). Donc
            // même une orbite calculée en coordonnées monde-fixe (comme
            // avant) reste filmée par cette même caméra mobile, et
            // paraît entraînée par elle à l'écran — exactement comme les
            // continents.
            //
            // En exprimant plutôt la position du satellite comme une
            // combinaison de right/up/eyeDir — qui sont recalculés à
            // CHAQUE frame à partir de la rotation actuelle de la caméra
            // — le mouvement de la caméra s'annule exactement au rendu :
            // seul l'angle propre du satellite (satelliteOrbitAngleDegrees)
            // fait bouger le satellite à l'écran. La rotation automatique
            // de fond ET le drag au doigt n'ont donc plus aucun effet sur
            // son orbite.
            //
            // L'orbite se déroule maintenant dans le plan right/up, c'est
            // à dire le plan FACE à l'écran (comme un cadran d'horloge) :
            // right et up sont les deux axes du champ visuel, donc le
            // satellite trace un vrai cercle visible à l'écran plutôt
            // qu'une trajectoire vue de tranche. L'axe de cette orbite
            // (la normale à son plan) est donc "eyeDir", la direction de
            // visée elle-même.
            val raw =
                sphericalDirection(orbitAzimuthRad, orbitTiltRad)

            // raw.second = petite inclinaison esthétique le long de
            // "eyeDir" (l'axe de l'orbite, vers/depuis l'écran) — utilise
            // le réglage RAYON DE L'ORBITE en entier, sans limite (la
            // profondeur ne pousse pas hors du cadre de la même façon).
            // raw.first / raw.third = le balayage circulaire réel, projeté
            // sur "right" et "up" — mis à l'échelle par SatelliteLateralScale
            // pour rester toujours dans la zone visible/tactile, quel que
            // soit le réglage RAYON DE L'ORBITE.
            val lateralRadius =
                satelliteOrbitRadius * SatelliteLateralScale

            Triple(
                lateralRadius * (raw.first * right.first + raw.third * up.first) +
                        satelliteOrbitRadius * raw.second * eyeDir.first,

                lateralRadius * (raw.first * right.second + raw.third * up.second) +
                        satelliteOrbitRadius * raw.second * eyeDir.second,

                lateralRadius * (raw.first * right.third + raw.third * up.third) +
                        satelliteOrbitRadius * raw.second * eyeDir.third
            )

        } else {

            val eyeX = EarthDistance * eyeDir.first
            val eyeY = EarthDistance * eyeDir.second
            val eyeZ = EarthDistance * eyeDir.third

            Triple(
                eyeX - eyeDir.first * SatelliteHudForwardOffset +
                        right.first * SatelliteHudRightOffset +
                        up.first * SatelliteHudUpOffset,

                eyeY - eyeDir.second * SatelliteHudForwardOffset +
                        right.second * SatelliteHudRightOffset +
                        up.second * SatelliteHudUpOffset,

                eyeZ - eyeDir.third * SatelliteHudForwardOffset +
                        right.third * SatelliteHudRightOffset +
                        up.third * SatelliteHudUpOffset
            )
        }

    val satellitePosition =
        Position(
            satelliteWorldPosition.first,
            satelliteWorldPosition.second,
            satelliteWorldPosition.third
        )

    // Position écran approximative du satellite (même projection
    // simplifiée que celle utilisée pour les continents ci-dessous),
    // utilisée pour savoir si un tap le touche.
    val sphereRadiusPx =
        min(containerSize.width, containerSize.height) * SphereScreenRadiusFraction

    val satelliteScreenX =
        sphereRadiusPx * dotProduct(satelliteWorldPosition, right)

    val satelliteScreenY =
        -sphereRadiusPx * dotProduct(satelliteWorldPosition, up)

    val latestSatelliteScreenPos =
        rememberUpdatedState(
            Offset(satelliteScreenX, satelliteScreenY)
        )


    val hitZones =
        remember(anchors, rotationXDegrees, rotationYDegrees, containerSize) {

            val sphereRadiusPx =
                min(containerSize.width, containerSize.height) * SphereScreenRadiusFraction

            anchors.map { anchor ->

                val p = anchor.unit
                val screenXDir = dotProduct(p, right)
                val screenYDir = dotProduct(p, up)

                ContinentHitZone(
                    entry = anchor.entry,
                    xPx = sphereRadiusPx * screenXDir,
                    yPx = -sphereRadiusPx * screenYDir,
                    clickable = dotProduct(p, eyeDir) > 0f
                )
            } to sphereRadiusPx
        }


    val latestHitZones = rememberUpdatedState(hitZones)


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {

        SceneView(
            modifier = Modifier.fillMaxSize(),

            cameraManipulator = null,
            cameraNode = cameraNode,

            engine = engine,

            modelLoader = modelLoader,

            environmentLoader = environmentLoader,

            mainLightNode = rememberMainLightNode(engine) {
                intensity = 100_000f
            },

            environment = rememberEnvironment(environmentLoader) {
                createEnvironment(environmentLoader)
            }

        ) {

            earthInstance?.let { instance ->

                ModelNode(
                    modelInstance = instance,

                    autoAnimate = false,

                    scale = Scale(planetScale),
                    position = Position(0f, 0f, 0f)
                )
            }

            satelliteInstance?.let { instance ->

                ModelNode(
                    modelInstance = instance,

                    autoAnimate = false,

                    scale = Scale(satelliteScale),
                    position = satellitePosition,

                    // Rotation sur l'axe Y (le spin lui-même) + une
                    // inclinaison fixe sur X (SatelliteSpinTiltDegrees) pour
                    // que ce spin se fasse "en biais" plutôt que bien droit.
                    // ATTENTION : le commentaire plus haut dans l'historique
                    // du projet notait qu'une combinaison à deux axes avait
                    // posé un problème sur le modèle de la Terre — si tu
                    // vois le même souci ici (à-coups, axe qui part dans le
                    // mauvais sens...), baisse SatelliteSpinTiltDegrees ou
                    // remplace x par z pour voir si ça se comporte mieux.
                    rotation = Rotation(
                        x = SatelliteSpinTiltDegrees,
                        y = satelliteSpinDegrees
                    )
                )
            }
        }


        // ----------------------------------------------------
        // COUCHE TACTILE INVISIBLE : glisser fait tourner la
        // caméra, taper sélectionne le continent le plus proche.
        // ----------------------------------------------------

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    containerSize = size
                }
                .pointerInput(Unit) {

                    detectDragGestures(
                        onDragStart = {

                            // Attraper le globe stoppe l'inertie en cours.
                            flingVelocityX = 0f
                            flingVelocityY = 0f
                        },

                        onDrag = { change, dragAmount ->

                            rotationYDegrees +=
                                -dragAmount.x *
                                        SphereRotationSensitivityDegrees

                            val newRotationX =
                                rotationXDegrees +
                                        (
                                                dragAmount.y *
                                                        SphereRotationSensitivityDegrees
                                                )

                            rotationXDegrees =
                                newRotationX.coerceIn(-75f, 75f)

                            lastDragDeltaX = dragAmount.x
                            lastDragDeltaY = dragAmount.y

                            change.consume()
                        },

                        onDragEnd = {

                            // Amorce l'inertie avec la vitesse du dernier
                            // mouvement enregistré.
                            flingVelocityX = lastDragDeltaX
                            flingVelocityY = lastDragDeltaY
                        }
                    )
                }
                .pointerInput(Unit) {

                    detectTapGestures(
                        onTap = { tapOffset ->

                            val centerX = containerSize.width / 2f
                            val centerY = containerSize.height / 2f

                            val relativeX = tapOffset.x - centerX
                            val relativeY = tapOffset.y - centerY


                            // Le satellite est prioritaire sur les continents :
                            // s'il est touché, ça ouvre le scanner d'animaux,
                            // exactement comme le bouton "ANIMAL SCANNER".
                            val satelliteScreenPos =
                                latestSatelliteScreenPos.value

                            val satelliteHitRadiusPx =
                                with(density) { SatelliteHitRadiusDp.toPx() }

                            val satelliteDx =
                                satelliteScreenPos.x - relativeX

                            val satelliteDy =
                                satelliteScreenPos.y - relativeY

                            val satelliteHit =
                                satelliteDx * satelliteDx + satelliteDy * satelliteDy <=
                                        satelliteHitRadiusPx * satelliteHitRadiusPx


                            if (satelliteHit) {

                                onSatelliteClick()

                            } else {

                                val (zones, sphereRadiusPx) =
                                    latestHitZones.value

                                val hitRadiusPx =
                                    sphereRadiusPx * ContinentHitRadiusFraction


                                val hit =
                                    zones
                                        .filter { it.clickable }
                                        .minByOrNull { zone ->

                                            val dx = zone.xPx - relativeX
                                            val dy = zone.yPx - relativeY

                                            dx * dx + dy * dy
                                        }


                                if (
                                    hit != null &&
                                    run {

                                        val dx = hit.xPx - relativeX
                                        val dy = hit.yPx - relativeY

                                        dx * dx + dy * dy <=
                                                hitRadiusPx * hitRadiusPx
                                    }
                                ) {

                                    onContinentSelected(hit.entry.continent)

                                } else if (oceanEntry != null) {

                                    val distance =
                                        sqrt(
                                            relativeX * relativeX +
                                                    relativeY * relativeY
                                        )

                                    if (distance <= sphereRadiusPx) {

                                        onContinentSelected(oceanEntry.continent)
                                    }
                                }
                            }
                        }
                    )
                }
        )


        // ----------------------------------------------------
        // MASQUES DE CONTINENTS COURBÉS SUR LA SPHÈRE — silhouettes
        // fixes dans l'espace du monde, projetées selon l'angle de
        // vue courant.
        // ----------------------------------------------------

        val (visibleZones, visibleSphereRadiusPx) = hitZones

        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(
                    with(androidx.compose.ui.platform.LocalDensity.current) {
                        (visibleSphereRadiusPx * 2).toDp()
                    }
                )
        ) {

            val canvasCenter =
                Offset(size.width / 2f, size.height / 2f)

            bodyVertices
                .filter { body ->
                    dotProduct(body.center, eyeDir) > 0f
                }
                .forEach { body ->

                    val path = Path()

                    body.vertices.forEachIndexed { index, vertex ->

                        val px =
                            canvasCenter.x +
                                    visibleSphereRadiusPx * dotProduct(vertex, right)

                        val py =
                            canvasCenter.y -
                                    visibleSphereRadiusPx * dotProduct(vertex, up)

                        if (index == 0) {
                            path.moveTo(px, py)
                        } else {
                            path.lineTo(px, py)
                        }
                    }

                    path.close()

                    drawPath(
                        path = path,
                        color = body.entry.continent.normalColor.copy(alpha = 0.75f)
                    )
                }
        }

        bodyVertices
            .filter { body -> dotProduct(body.center, eyeDir) > 0f }
            .forEach { body ->

                val xPx = visibleSphereRadiusPx * dotProduct(body.center, right)
                val yPx = -visibleSphereRadiusPx * dotProduct(body.center, up)

                Text(
                    text = body.entry.continent.name,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(
                            x = with(density) { xPx.toDp() },
                            y = with(density) { yPx.toDp() }
                        ),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }


        // ----------------------------------------------------
        // CRÉDIT DE L'AUTEUR (licence CC-BY-4.0)
        // ----------------------------------------------------

        Text(
            text =
                "Modèle 3D : \"Planet Earth\" par kaede256 (Sketchfab, CC-BY-4.0)",

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.35f)
                )
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                ),

            color =
                Color.White.copy(alpha = 0.75f),

            fontSize = 8.sp,

            textAlign =
                androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}