package com.example.animaldex.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlinx.coroutines.delay

import com.example.animaldex.model.Animal
import com.example.animaldex.model.ContinentData
import com.example.animaldex.model.IconGroup
import com.example.animaldex.util.GameFont
import com.example.animaldex.util.PageBackgroundColor


// Fond gris foncé par défaut pour les cases non capturées, quel que soit le continent
val UndiscoveredCardColor = Color(0xFF3A3A3D)

// Réglages de l'animation de découverte (case qui se remplit de bas
// en haut) déclenchée depuis la capture par l'appareil photo.
private const val RevealPreDelayMs = 200L
private const val RevealFillDurationMs = 700
private const val RevealHoldDelayMs = 400L

// Étincelles qui jaillissent de la case une fois le remplissage
// terminé — purement décoratif, ne touche pas au minutage de
// l'enchaînement (remplissage -> pause -> zoom).
private const val SparkleDurationMs = 550

private data class SparkleParticle(
    val angleDegrees: Float,
    val distanceDp: Float,
    val sizeDp: Float,
    val delayFraction: Float
)

private val RevealSparkles: List<SparkleParticle> =
    listOf(
        SparkleParticle(20f, 78f, 5f, 0.00f),
        SparkleParticle(65f, 88f, 4f, 0.08f),
        SparkleParticle(100f, 72f, 6f, 0.03f),
        SparkleParticle(140f, 92f, 4f, 0.15f),
        SparkleParticle(180f, 80f, 5f, 0.05f),
        SparkleParticle(220f, 85f, 4f, 0.12f),
        SparkleParticle(260f, 74f, 6f, 0.02f),
        SparkleParticle(300f, 90f, 4f, 0.18f),
        SparkleParticle(340f, 76f, 5f, 0.07f)
    )


// Dessine une étincelle à 4 pointes (style clipart) à une position,
// taille et transparence données.
private fun DrawScope.drawSparkle(
    center: Offset,
    armLength: Float,
    color: Color,
    alpha: Float
) {

    if (alpha <= 0f) return

    val thin = armLength * 0.28f

    val path = Path().apply {

        moveTo(center.x, center.y - armLength)
        lineTo(center.x + thin, center.y)
        lineTo(center.x, center.y + armLength)
        lineTo(center.x - thin, center.y)
        close()

        moveTo(center.x - armLength, center.y)
        lineTo(center.x, center.y - thin)
        lineTo(center.x + armLength, center.y)
        lineTo(center.x, center.y + thin)
        close()
    }

    drawPath(
        path = path,
        color = color.copy(alpha = alpha)
    )
}


fun isConfirmKey(
    event: KeyEvent
): Boolean {

    return (
            event.key == Key.Enter ||
                    event.key == Key.NumPadEnter
            )
}


@Composable
fun PagedGroupGrid(
    groups: List<IconGroup>,
    color: Color,
    showBack: Boolean,
    onBack: () -> Unit,
    onGroupSelected: (IconGroup) -> Unit,
    initialPageIndex: Int = 0,
    onPageIndexChanged: (Int) -> Unit = {}
) {

    PagedGrid(
        itemCount = groups.size,

        color = color,

        showBack = showBack,

        onBack = onBack,

        initialPageIndex = initialPageIndex,

        onPageIndexChanged = onPageIndexChanged,

        onOpenIndex = { index ->
            onGroupSelected(
                groups[index]
            )
        }
    ) { index, selected, onTap, onLongPress ->

        IconGroupItem(
            group = groups[index],

            continentColor = color,

            selected = selected,

            onTap = onTap,

            onLongPress = onLongPress
        )
    }
}


@Composable
fun PagedAnimalGrid(
    animals: List<Animal>,
    color: Color,
    showBack: Boolean,
    onBack: () -> Unit,
    onAnimalSelected: (Animal) -> Unit,
    initialPageIndex: Int = 0,
    onPageIndexChanged: (Int) -> Unit = {},
    revealAnimalId: Int? = null,
    onRevealComplete: () -> Unit = {}
) {

    PagedGrid(
        itemCount = animals.size,

        color = color,

        showBack = showBack,

        onBack = onBack,

        initialPageIndex = initialPageIndex,

        onPageIndexChanged = onPageIndexChanged,

        onOpenIndex = { index ->
            onAnimalSelected(
                animals[index]
            )
        }
    ) { index, selected, onTap, onLongPress ->

        AnimalGridItem(
            animal = animals[index],

            continentColor = color,

            selected = selected,

            onTap = onTap,

            onLongPress = onLongPress,

            isRevealing =
                revealAnimalId != null &&
                        animals[index].id == revealAnimalId,

            onRevealFillComplete =
                onRevealComplete
        )
    }
}


@Composable
fun PagedContinentGrid(
    entries: List<ContinentGridEntry>,
    onContinentSelected: (ContinentData) -> Unit,
    initialPageIndex: Int = 0,
    onPageIndexChanged: (Int) -> Unit = {}
) {

    PagedGrid(
        itemCount = entries.size,

        color = PageBackgroundColor,

        showBack = false,

        onBack = {},

        showPageIndicator = false,

        initialPageIndex = initialPageIndex,

        onPageIndexChanged = onPageIndexChanged,

        onOpenIndex = { index ->
            onContinentSelected(
                entries[index].continent
            )
        }
    ) { index, selected, onTap, onLongPress ->

        ContinentGridItem(
            entry = entries[index],

            selected = selected,

            onTap = onTap,

            onLongPress = onLongPress
        )
    }
}


@Composable
fun PagedGrid(
    itemCount: Int,
    color: Color,
    showBack: Boolean,
    onBack: () -> Unit,
    onOpenIndex: (Int) -> Unit,
    initialPageIndex: Int = 0,
    onPageIndexChanged: (Int) -> Unit = {},
    showPageIndicator: Boolean = true,

    itemContent: @Composable (
        index: Int,
        selected: Boolean,
        onTap: () -> Unit,
        onLongPress: () -> Unit
    ) -> Unit
) {

    val columns = 3
    val rows = 3
    val pageSize = 9


    val pageCount =
        if (itemCount == 0) {
            1
        } else {
            (
                    itemCount +
                            pageSize -
                            1
                    ) / pageSize
        }


    var pageIndex by remember(
        itemCount
    ) {
        mutableIntStateOf(
            initialPageIndex.coerceIn(
                0,
                pageCount - 1
            )
        )
    }


    var selectedLocalIndex by remember(
        itemCount
    ) {
        mutableIntStateOf(0)
    }


    var backSelected by remember {
        mutableStateOf(false)
    }


    var totalDrag by remember {
        mutableFloatStateOf(0f)
    }


    val focusRequester =
        remember {
            FocusRequester()
        }


    LaunchedEffect(Unit) {

        focusRequester.requestFocus()
    }


    LaunchedEffect(pageIndex) {

        onPageIndexChanged(pageIndex)
    }


    fun previousPage() {

        if (pageIndex > 0) {

            pageIndex--

            selectedLocalIndex = 0

            backSelected = false
        }
    }


    fun nextPage() {

        if (
            pageIndex < pageCount - 1
        ) {

            pageIndex++

            selectedLocalIndex = 0

            backSelected = false
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackgroundColor)
            .pointerInput(
                pageIndex,
                pageCount
            ) {

                detectHorizontalDragGestures(

                    onDragStart = {
                        totalDrag = 0f
                    },

                    onHorizontalDrag = {
                            change,
                            drag ->

                        totalDrag += drag

                        if (
                            abs(totalDrag) > 18f
                        ) {
                            change.consume()
                        }
                    },

                    onDragEnd = {

                        if (
                            totalDrag < -75f
                        ) {

                            nextPage()

                        } else if (
                            totalDrag > 75f
                        ) {

                            previousPage()
                        }

                        totalDrag = 0f
                    },

                    onDragCancel = {

                        totalDrag = 0f
                    }
                )
            }
            .focusRequester(
                focusRequester
            )
            .onPreviewKeyEvent { event ->

                if (
                    event.type !=
                    KeyEventType.KeyDown
                ) {

                    return@onPreviewKeyEvent false
                }


                val pageItemCountForKeys =
                    minOf(
                        pageSize,
                        itemCount - pageIndex * pageSize
                    ).coerceAtLeast(0)


                if (backSelected) {

                    when {

                        event.key ==
                                Key.DirectionUp -> {

                            backSelected = false

                            if (
                                pageItemCountForKeys > 0
                            ) {

                                selectedLocalIndex =
                                    pageItemCountForKeys - 1
                            }

                            true
                        }


                        isConfirmKey(
                            event
                        ) -> {

                            if (showBack) {
                                onBack()
                            }

                            true
                        }


                        else -> false
                    }

                } else {

                    val row =
                        selectedLocalIndex /
                                columns


                    val column =
                        selectedLocalIndex %
                                columns


                    when {

                        event.key ==
                                Key.DirectionRight -> {

                            if (
                                column < columns - 1 &&
                                selectedLocalIndex + 1 < pageItemCountForKeys
                            ) {

                                selectedLocalIndex++

                            } else {

                                nextPage()
                            }

                            true
                        }


                        event.key ==
                                Key.DirectionLeft -> {

                            if (
                                column > 0
                            ) {

                                selectedLocalIndex--

                            } else {

                                previousPage()
                            }

                            true
                        }


                        event.key ==
                                Key.DirectionDown -> {

                            val nextIndex =
                                selectedLocalIndex +
                                        columns


                            if (
                                nextIndex < pageItemCountForKeys
                            ) {

                                selectedLocalIndex =
                                    nextIndex

                            } else if (
                                showBack
                            ) {

                                backSelected = true
                            }

                            true
                        }


                        event.key ==
                                Key.DirectionUp -> {

                            if (row > 0) {

                                selectedLocalIndex =
                                    (
                                            selectedLocalIndex -
                                                    columns
                                            ).coerceAtLeast(0)
                            }

                            true
                        }


                        isConfirmKey(
                            event
                        ) -> {

                            if (
                                pageItemCountForKeys > 0
                            ) {

                                onOpenIndex(
                                    pageIndex * pageSize +
                                            selectedLocalIndex
                                )
                            }

                            true
                        }


                        else -> false
                    }
                }
            }
            .focusable()
    ) {

        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),

            targetState = pageIndex,

            transitionSpec = {

                if (targetState > initialState) {

                    (
                            slideInHorizontally(
                                animationSpec = tween(280)
                            ) { fullWidth -> fullWidth } + fadeIn(tween(280))
                            ) togetherWith (
                            slideOutHorizontally(
                                animationSpec = tween(280)
                            ) { fullWidth -> -fullWidth } + fadeOut(tween(200))
                            )

                } else {

                    (
                            slideInHorizontally(
                                animationSpec = tween(280)
                            ) { fullWidth -> -fullWidth } + fadeIn(tween(280))
                            ) togetherWith (
                            slideOutHorizontally(
                                animationSpec = tween(280)
                            ) { fullWidth -> fullWidth } + fadeOut(tween(200))
                            )
                }
            },

            label = "page_transition"

        ) { animatedPageIndex ->

            val firstIndex =
                animatedPageIndex * pageSize


            val pageItemCount =
                minOf(
                    pageSize,
                    itemCount - firstIndex
                ).coerceAtLeast(0)


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 5.dp,
                        vertical = 3.dp
                    ),

                verticalArrangement =
                    Arrangement.SpaceEvenly
            ) {

                for (
                row in 0 until rows
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),

                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {

                        for (
                        column in 0 until columns
                        ) {

                            val localIndex =
                                row * columns +
                                        column


                            val globalIndex =
                                firstIndex +
                                        localIndex


                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),

                                contentAlignment =
                                    Alignment.Center
                            ) {

                                if (
                                    localIndex < pageItemCount
                                ) {

                                    itemContent(
                                        globalIndex,

                                        animatedPageIndex == pageIndex &&
                                                !backSelected &&
                                                localIndex ==
                                                selectedLocalIndex,

                                        {
                                            selectedLocalIndex =
                                                localIndex

                                            backSelected = false
                                        },

                                        {
                                            selectedLocalIndex =
                                                localIndex

                                            backSelected = false

                                            onOpenIndex(
                                                globalIndex
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        if (showPageIndicator) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp)
                    .padding(
                        start = 8.dp,
                        end = 7.dp,
                        bottom = 4.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "${pageIndex + 1} / $pageCount",

                    color =
                        Color.White.copy(
                            alpha = 0.80f
                        ),

                    fontFamily = GameFont,

                    fontSize = 10.sp,

                    fontWeight =
                        FontWeight.Black
                )


                Spacer(
                    Modifier.weight(1f)
                )


                if (showBack) {

                    BackButton(
                        selected = backSelected,

                        onClick = onBack
                    )
                }
            }
        }
    }
}


@Composable
fun IconGroupItem(
    group: IconGroup,
    continentColor: Color,
    selected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {

    val isDiscovered =
        group.discoveredCount > 0


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(group.uuid) {

                detectTapGestures(
                    onTap = {
                        onTap()
                    },

                    onDoubleTap = {
                        onLongPress()
                    }
                )
            }
            .padding(2.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(
                    if (selected) {
                        140.dp
                    } else {
                        150.dp
                    }
                )
                .background(
                    color =
                        if (isDiscovered) {
                            continentColor
                        } else {
                            UndiscoveredCardColor
                        },

                    shape =
                        RoundedCornerShape(
                            11.dp
                        )
                )
                .then(
                    if (selected) {
                        Modifier.background(
                            Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(11.dp)
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(6.dp),

            contentAlignment =
                Alignment.Center
        ) {

            AsyncImage(
                model = group.imagePath,

                contentDescription =
                    group.displayName,

                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.55f),

                contentScale =
                    ContentScale.Fit
            )

            Text(
                text =
                    "${group.discoveredCount}/${group.totalCount}",

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 10.sp,

                letterSpacing = (-0.5).sp,

                fontWeight = FontWeight.Black,

                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
            )

            Text(
                text =
                    group.displayName.uppercase(),

                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(2.dp),

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 10.sp,

                fontWeight =
                    FontWeight.Black,

                maxLines = 2,

                lineHeight = 10.sp,

                overflow =
                    TextOverflow.Ellipsis,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}


@Composable
fun AnimalGridItem(
    animal: Animal,
    continentColor: Color,
    selected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    isRevealing: Boolean = false,
    onRevealFillComplete: () -> Unit = {}
) {

    val fillFraction =
        remember {
            Animatable(0f)
        }

    var sparklesTriggered by remember {
        mutableStateOf(false)
    }

    val sparkleProgress =
        remember {
            Animatable(0f)
        }


    LaunchedEffect(isRevealing) {

        if (isRevealing) {

            delay(RevealPreDelayMs)

            fillFraction.animateTo(
                targetValue = 1f,
                animationSpec = tween(RevealFillDurationMs)
            )

            sparklesTriggered = true

            delay(RevealHoldDelayMs)

            onRevealFillComplete()
        }
    }


    LaunchedEffect(sparklesTriggered) {

        if (sparklesTriggered) {

            sparkleProgress.snapTo(0f)

            sparkleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(SparkleDurationMs)
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(animal.id) {

                detectTapGestures(
                    onTap = {
                        onLongPress()
                    }
                )
            }
            .padding(2.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Box(
            contentAlignment =
                Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(
                        if (selected) {
                            140.dp
                        } else {
                            150.dp
                        }
                    )
                    .background(
                        color =
                            if (isRevealing) {
                                UndiscoveredCardColor
                            } else if (animal.discovered) {
                                continentColor
                            } else {
                                UndiscoveredCardColor
                            },

                        shape =
                            RoundedCornerShape(
                                11.dp
                            )
                    )
                    .then(
                        if (selected) {
                            Modifier.background(
                                Color.White.copy(alpha = 0.25f),
                                RoundedCornerShape(11.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(6.dp),

                contentAlignment =
                    Alignment.Center
            ) {

                if (isRevealing) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(
                                fillFraction.value
                            )
                            .background(
                                continentColor,
                                RoundedCornerShape(11.dp)
                            )
                    )
                }


                if (
                    animal.localImagePath
                        .isNullOrBlank()
                ) {

                    Text(
                        text = "?",

                        color = Color.White,

                        fontFamily = GameFont,

                        fontSize = 35.sp,

                        fontWeight =
                            FontWeight.Black
                    )

                } else {

                    AsyncImage(
                        model =
                            animal.localImagePath,

                        contentDescription =
                            animal.displayName,

                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(0.55f),

                        contentScale =
                            ContentScale.Fit
                    )
                }

                if (animal.discovered) {

                    Text(
                        text = "✓",

                        color = Color.White,

                        fontFamily = GameFont,

                        fontSize = 10.sp,

                        fontWeight = FontWeight.Black,

                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                    )
                }

                Text(
                    text =
                        animal.displayName.uppercase(),

                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(2.dp),

                    color = Color.White,

                    fontFamily = GameFont,

                    fontSize = 10.sp,

                    fontWeight =
                        FontWeight.Black,

                    maxLines = 2,

                    lineHeight = 10.sp,

                    overflow =
                        TextOverflow.Ellipsis,

                    textAlign =
                        TextAlign.Center
                )
            }


            // Étincelles, dessinées par-dessus, capables de déborder
            // au-delà du bord de la case (unbounded = ignore la
            // contrainte de largeur étroite imposée par la colonne
            // de la grille).
            if (sparklesTriggered) {

                Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .wrapContentSize(unbounded = true)
                ) {

                    val centerOffset =
                        Offset(
                            size.width / 2f,
                            size.height / 2f
                        )

                    RevealSparkles.forEach { sparkle ->

                        val localProgress =
                            (
                                    (sparkleProgress.value - sparkle.delayFraction) /
                                            (1f - sparkle.delayFraction)
                                    ).coerceIn(0f, 1f)

                        if (localProgress <= 0f) {
                            return@forEach
                        }

                        val eased =
                            1f - (1f - localProgress) * (1f - localProgress)

                        val angleRad =
                            sparkle.angleDegrees * (PI.toFloat() / 180f)

                        val distancePx =
                            sparkle.distanceDp.dp.toPx() * eased

                        val sparkleCenter =
                            Offset(
                                centerOffset.x + cos(angleRad) * distancePx,
                                centerOffset.y + sin(angleRad) * distancePx
                            )

                        val alpha =
                            when {

                                localProgress < 0.15f ->
                                    localProgress / 0.15f

                                localProgress < 0.5f ->
                                    1f

                                else ->
                                    (1f - localProgress) / 0.5f
                            }

                        drawSparkle(
                            center = sparkleCenter,
                            armLength = sparkle.sizeDp.dp.toPx(),
                            color = Color.White,
                            alpha = alpha
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ContinentGridItem(
    entry: ContinentGridEntry,
    selected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {

    val continent =
        entry.continent


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(continent.name) {

                detectTapGestures(
                    onTap = {
                        onLongPress()
                    }
                )
            }
            .padding(2.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(
                    if (selected) {
                        140.dp
                    } else {
                        150.dp
                    }
                )
                .background(
                    color = continent.normalColor,

                    shape =
                        RoundedCornerShape(
                            11.dp
                        )
                )
                .then(
                    if (selected) {
                        Modifier.background(
                            Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(11.dp)
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(6.dp),

            contentAlignment =
                Alignment.Center
        ) {

            AsyncImage(
                model = continent.imagePath,

                contentDescription =
                    continent.name,

                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.55f),

                contentScale =
                    ContentScale.Fit
            )

            Text(
                text =
                    "${entry.discoveredCount}/${entry.totalCount}",

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 10.sp,

                letterSpacing = (-0.5).sp,

                fontWeight = FontWeight.Black,

                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
            )

            Text(
                text =
                    continent.name.uppercase(),

                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(2.dp),

                color = Color.White,

                fontFamily = GameFont,

                fontSize = 10.sp,

                fontWeight =
                    FontWeight.Black,

                maxLines = 2,

                lineHeight = 10.sp,

                overflow =
                    TextOverflow.Ellipsis,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}


@Composable
fun BackButton(
    selected: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .background(
                if (selected) {
                    Color.White
                } else {
                    Color.White.copy(
                        alpha = 0.20f
                    )
                },

                RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {

                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            }
            .padding(
                horizontal = 13.dp,
                vertical = 8.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text = "←",

            color =
                if (selected) {
                    Color.Black
                } else {
                    Color.White
                },

            fontFamily = GameFont,

            fontSize = 9.sp,

            fontWeight =
                FontWeight.Black
        )
    }
}


@Composable
fun NoResultBody(
    color: Color
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text = "NO RESULT",

            color = Color.White,

            fontFamily = GameFont,

            fontSize = 14.sp,

            fontWeight =
                FontWeight.Black
        )
    }
}