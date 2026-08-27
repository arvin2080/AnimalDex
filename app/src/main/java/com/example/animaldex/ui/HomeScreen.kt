package com.example.animaldex.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
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

        "AFRICA" to listOf(
            Offset(-0.60f, -0.95f), Offset(0.15f, -1.00f), Offset(0.55f, -0.85f),
            Offset(1.05f, -0.45f), Offset(0.60f, -0.05f), Offset(0.70f, 0.30f),
            Offset(0.35f, 0.65f), Offset(0.05f, 1.00f), Offset(-0.25f, 0.65f),
            Offset(-0.40f, 0.30f), Offset(-0.20f, -0.05f), Offset(-0.65f, -0.15f),
            Offset(-0.80f, -0.50f)
        ),

        "ANTARCTICA" to listOf(
            Offset(0.00f, -1.00f), Offset(0.55f, -0.85f), Offset(0.90f, -0.45f),
            Offset(0.60f, -0.10f), Offset(0.85f, 0.30f), Offset(0.55f, 0.75f),
            Offset(0.10f, 0.95f), Offset(-0.35f, 0.80f), Offset(-0.20f, 0.40f),
            Offset(-0.60f, 0.55f), Offset(-0.90f, 0.20f), Offset(-0.80f, -0.15f),
            Offset(-1.15f, -0.45f), Offset(-0.90f, -0.60f), Offset(-0.55f, -0.75f)
        ),

        "ASIA" to listOf(
            Offset(-1.10f, -0.85f), Offset(-0.55f, -1.05f), Offset(0.15f, -1.10f),
            Offset(0.65f, -0.95f), Offset(1.05f, -0.60f), Offset(0.80f, -0.20f),
            Offset(1.00f, 0.15f), Offset(0.60f, 0.30f), Offset(0.70f, 0.55f),
            Offset(0.35f, 0.55f), Offset(0.45f, 0.90f), Offset(0.15f, 0.75f),
            Offset(0.10f, 1.05f), Offset(-0.20f, 0.70f), Offset(-0.45f, 0.60f),
            Offset(-0.55f, 0.35f), Offset(-0.90f, 0.40f), Offset(-1.00f, 0.00f),
            Offset(-0.85f, -0.40f)
        ),

        "NORTH AMERICA" to listOf(
            Offset(-1.05f, -0.65f), Offset(-0.65f, -1.00f), Offset(0.05f, -1.10f),
            Offset(0.55f, -0.90f), Offset(0.80f, -0.55f), Offset(0.45f, -0.35f),
            Offset(0.75f, -0.10f), Offset(0.55f, 0.20f), Offset(0.80f, 0.35f),
            Offset(0.50f, 0.15f), Offset(0.30f, 0.10f), Offset(0.10f, 0.55f),
            Offset(0.05f, 0.90f), Offset(0.15f, 1.15f), Offset(-0.10f, 0.85f),
            Offset(-0.25f, 0.50f), Offset(-0.15f, 0.05f), Offset(-0.65f, 0.05f),
            Offset(-0.95f, -0.30f)
        ),

        "SOUTH AMERICA" to listOf(
            Offset(-0.20f, -1.05f), Offset(0.20f, -0.85f), Offset(0.55f, -0.55f),
            Offset(0.85f, -0.10f), Offset(0.65f, 0.25f), Offset(0.50f, 0.60f),
            Offset(0.25f, 0.90f), Offset(0.05f, 1.15f), Offset(-0.15f, 0.85f),
            Offset(-0.30f, 0.45f), Offset(-0.40f, 0.05f), Offset(-0.45f, -0.40f),
            Offset(-0.40f, -0.75f)
        ),

        "EUROPE" to listOf(
            Offset(0.05f, -1.10f), Offset(0.50f, -0.95f), Offset(0.85f, -0.70f),
            Offset(0.70f, -0.35f), Offset(0.95f, -0.10f), Offset(0.75f, 0.20f),
            Offset(0.75f, 0.45f), Offset(0.50f, 0.55f), Offset(0.35f, 0.95f),
            Offset(0.15f, 0.75f), Offset(-0.10f, 0.60f), Offset(-0.25f, 0.55f),
            Offset(-0.55f, 0.65f), Offset(-0.70f, 0.35f), Offset(-0.85f, -0.05f),
            Offset(-0.55f, -0.35f), Offset(-0.60f, -0.75f), Offset(-0.20f, -0.95f)
        ),

        "OCEANIA" to listOf(
            Offset(-0.75f, -0.35f), Offset(-0.30f, -0.65f), Offset(0.20f, -0.70f),
            Offset(0.55f, -0.50f), Offset(0.35f, -0.20f), Offset(0.80f, 0.10f),
            Offset(0.60f, 0.45f), Offset(0.15f, 0.35f), Offset(-0.35f, 0.50f),
            Offset(-0.75f, 0.15f)
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
// CONTINENT SPHERE (modèle 3D réel, rotation au doigt)
// ============================================================

private const val SphereScreenRadiusFraction = 0.25f
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
    var rotationYDegrees by remember { mutableFloatStateOf(30f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }


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

                    scale = Scale(EarthScale),
                    position = Position(0f, 0f, 0f)
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

        val density = androidx.compose.ui.platform.LocalDensity.current

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