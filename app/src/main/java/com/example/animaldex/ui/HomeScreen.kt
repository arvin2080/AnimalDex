package com.example.animaldex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlin.math.*

import io.github.sceneview.*
import io.github.sceneview.node.*
import io.github.sceneview.math.*

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
// HOME SCREEN
// ============================================================

@Composable
fun HomeScreen(
    animals: List<Animal>,
    onContinentSelected: (ContinentData) -> Unit,
    onGlobalGroupSelected: (IconGroup) -> Unit,
    onCameraClick: () -> Unit
) {

    var search by remember {
        mutableStateOf("")
    }


    var filter by remember {
        mutableStateOf(
            GroupFilter.ALL
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                PageBackgroundColor
            )
    ) {

        SearchHeader(
            leftText = "ANIMALDEX",

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


        CameraMenuButton(
            onClick =
                onCameraClick
        )


        if (search.isBlank()) {

            ContinentMenuBody(
                animals = animals,

                groupFilter =
                    filter,

                onContinentSelected =
                    onContinentSelected
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
}


// ============================================================
// CAMERA MENU BUTTON
// ============================================================

@Composable
fun CameraMenuButton(
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 7.dp,
                end = 7.dp,
                top = 5.dp,
                bottom = 2.dp
            )
            .height(
                45.dp
            )
            .background(
                color =
                    CameraButtonColor,

                shape =
                    RoundedCornerShape(
                        11.dp
                    )
            )
            .pointerInput(Unit) {

                detectTapGestures(
                    onTap = {

                        onClick()
                    }
                )
            },

        contentAlignment =
            Alignment.Center
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .width(
                        25.dp
                    )
                    .height(
                        18.dp
                    )
                    .background(
                        color =
                            Color.White,

                        shape =
                            RoundedCornerShape(
                                4.dp
                            )
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .size(
                            9.dp
                        )
                        .background(
                            color =
                                CameraButtonColor,

                            shape =
                                RoundedCornerShape(
                                    50
                                )
                        )
                )
            }


            Spacer(
                modifier =
                    Modifier.width(
                        9.dp
                    )
            )


            Text(
                text =
                    "ANIMAL SCANNER",

                color =
                    Color.White,

                fontFamily =
                    GameFont,

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.Black
            )
        }
    }
}


// ============================================================
// CONTINENT MENU BODY
// ============================================================

@Composable
fun ContinentMenuBody(
    animals: List<Animal>,
    groupFilter: GroupFilter,
    onContinentSelected: (ContinentData) -> Unit
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

        onContinentSelected =
            onContinentSelected
    )
}


// ============================================================
// AGENCEMENT GÉOGRAPHIQUE DES CONTINENTS
// ============================================================

private val FixedContinentLayout: List<Triple<String, Float, Float>> =
    listOf(

        Triple("NORTH AMERICA", -0.74f, 260f),
        Triple("SOUTH AMERICA", 0.14f, 290f),
        Triple("EUROPE", -0.81f, 25f),
        Triple("AFRICA", -0.12f, 21f),
        Triple("ASIA", -0.71f, 87f),
        Triple("OCEANIA", 0.43f, 135f),

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
// CONTINENT SPHERE (modèle 3D réel, rotation au doigt)
// ============================================================

private const val SphereScreenRadiusFraction = 0.38f
private const val ContinentHitRadiusFraction = 0.30f
private const val SphereRotationSensitivityDegrees = 0.35f

private const val EarthScale = 0.5f
private const val EarthDistance = 10f


@Composable
private fun ContinentSphere(
    entries: List<ContinentGridEntry>,
    onContinentSelected: (ContinentData) -> Unit
) {

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val cameraNode = rememberCameraNode(engine)

    val earthInstance =
        rememberModelInstance(
            modelLoader,
            "models/earth/planet_earth.glb"
        )


    val oceanEntry =
        remember(entries) {
            entries.firstOrNull {
                it.continent.name == "OCEAN"
            }
        }


    val anchors =
        remember(entries) {
            buildContinentAnchors(entries)
        }


    var rotationXDegrees by remember {
        mutableFloatStateOf(10f)
    }

    var rotationYDegrees by remember {
        mutableFloatStateOf(30f)
    }

    var containerSize by remember {
        mutableStateOf(IntSize.Zero)
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


    val latestHitZones =
        rememberUpdatedState(
            hitZones
        )


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

                    scale = Scale(EarthScale),
                    position = Position(0f, 0f, 0f)
                )
            }
        }


        // ----------------------------------------------------
        // COUCHE TACTILE INVISIBLE : glisser fait tourner la
        // Terre, taper sélectionne le continent le plus proche.
        // ----------------------------------------------------

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    containerSize = size
                }
                .pointerInput(Unit) {

                    detectDragGestures(
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

                            change.consume()
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


                            val (zones, sphereRadiusPx) =
                                latestHitZones.value

                            val hitRadiusPx =
                                sphereRadiusPx *
                                        ContinentHitRadiusFraction


                            val hit =
                                zones
                                    .filter {
                                        it.clickable
                                    }
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

                                onContinentSelected(
                                    hit.entry.continent
                                )

                            } else if (oceanEntry != null) {

                                val distance =
                                    sqrt(
                                        relativeX * relativeX +
                                                relativeY * relativeY
                                    )

                                if (distance <= sphereRadiusPx) {

                                    onContinentSelected(
                                        oceanEntry.continent
                                    )
                                }
                            }
                        }
                    )
                }
        )


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