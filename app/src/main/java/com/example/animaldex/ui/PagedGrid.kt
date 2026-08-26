package com.example.animaldex.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.Color
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

import com.example.animaldex.model.Animal
import com.example.animaldex.model.ContinentData
import com.example.animaldex.model.IconGroup
import com.example.animaldex.util.GameFont
import com.example.animaldex.util.PageBackgroundColor


// Fond gris foncé par défaut pour les cases non capturées, quel que soit le continent
val UndiscoveredCardColor = Color(0xFF3A3A3D)


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
    onPageIndexChanged: (Int) -> Unit = {}
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

            onLongPress = onLongPress
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

        // Un seul continent par case, toujours 9 au total : jamais plus
        // d'une page, donc l'indicateur "x / y" et la barre du bas n'ont
        // aucune utilité ici. On les masque pour laisser les cases
        // s'étendre jusqu'en bas de l'écran.
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


    // Page de départ : celle fournie par l'appelant (pour reprendre où on
    // était), ramenée dans les bornes valides.
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


    // Prévient l'appelant à chaque changement de page, pour qu'il puisse
    // la mémoriser (ex. remonter jusqu'à AnimalDexApp) et la redonner
    // en initialPageIndex si cet écran est recréé plus tard.
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

        // ----------------------------------------------------
        // GRILLE DE LA PAGE, avec transition glissante smooth
        // au changement de pageIndex (swipe ou clavier)
        // ----------------------------------------------------

        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),

            targetState = pageIndex,

            transitionSpec = {

                if (targetState > initialState) {

                    // page suivante : nouvelle page entre par la droite,
                    // ancienne sort par la gauche
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

                    // page précédente : nouvelle page entre par la gauche,
                    // ancienne sort par la droite
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

                // Sur cet écran (liste des groupes d'un continent), un
                // simple tap sélectionne/surligne, et il faut un DOUBLE
                // tap pour ouvrir le groupe (remplace l'ancien appui long).
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
                            // capturé : teinte du continent (ex. orange pour Afrique)
                            continentColor
                        } else {
                            // non capturé : gris foncé, comme la référence Pokédex
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

            // Icône de l'animal, centrée et rétrécie (ne remplit plus toute la case)
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

            // Compteur 0/x dans le coin en haut à droite, lettres resserrées
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

            // Nom de l'animal en bas de la case
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
    onLongPress: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(animal.id) {

                // Sur cet écran (liste des animaux d'un groupe), un simple
                // tap suffit pour ouvrir directement la fiche de l'animal.
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
                    color =
                        if (animal.discovered) {
                            // capturé : teinte du continent, comme les groupes
                            continentColor
                        } else {
                            // non capturé : gris foncé, comme les groupes
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

            // Icône de l'animal, centrée et rétrécie, comme pour les groupes
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

            // Coche en haut à droite si l'animal est déjà découvert
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

            // Nom de l'animal en bas de la case, comme pour les groupes
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

                // Sur l'écran d'accueil, un simple tap suffit pour ouvrir
                // directement les groupes du continent.
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
                    // Toujours la couleur du continent, contrairement aux
                    // groupes qui grisent les cases non découvertes.
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

            // Image du continent, centrée et rétrécie. Fichier à ajouter
            // dans app/src/main/assets/continents/ (voir
            // ContinentData.imagePath pour la convention de nommage).
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

            // Compteur d'animaux découverts sur ce continent
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

            // Nom du continent en bas de la case
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