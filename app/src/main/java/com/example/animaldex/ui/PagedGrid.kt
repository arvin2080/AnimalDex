package com.example.animaldex.ui

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
import com.example.animaldex.model.IconGroup
import com.example.animaldex.util.GameFont


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
    onGroupSelected: (IconGroup) -> Unit
) {

    PagedGrid(
        itemCount = groups.size,

        color = color,

        showBack = showBack,

        onBack = onBack,

        onOpenIndex = { index ->
            onGroupSelected(
                groups[index]
            )
        }
    ) { index, selected, onTap, onLongPress ->

        IconGroupItem(
            group = groups[index],

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
    onAnimalSelected: (Animal) -> Unit
) {

    PagedGrid(
        itemCount = animals.size,

        color = color,

        showBack = showBack,

        onBack = onBack,

        onOpenIndex = { index ->
            onAnimalSelected(
                animals[index]
            )
        }
    ) { index, selected, onTap, onLongPress ->

        AnimalGridItem(
            animal = animals[index],

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
        mutableIntStateOf(0)
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


    LaunchedEffect(itemCount) {

        pageIndex = 0

        selectedLocalIndex = 0
    }


    val firstIndex =
        pageIndex * pageSize


    val pageItemCount =
        minOf(
            pageSize,
            itemCount - firstIndex
        ).coerceAtLeast(0)


    fun previousPage() {

        if (pageIndex > 0) {

            pageIndex--

            selectedLocalIndex = 0

            backSelected = false
        }
    }


    fun nextPage() {

        if (
            pageIndex <
            pageCount - 1
        ) {

            pageIndex++

            selectedLocalIndex = 0

            backSelected = false
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
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


                if (backSelected) {

                    when {

                        event.key ==
                                Key.DirectionUp -> {

                            backSelected = false

                            if (
                                pageItemCount > 0
                            ) {

                                selectedLocalIndex =
                                    pageItemCount - 1
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
                                column <
                                columns - 1 &&
                                selectedLocalIndex + 1 <
                                pageItemCount
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
                                nextIndex <
                                pageItemCount
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
                                pageItemCount > 0
                            ) {

                                onOpenIndex(
                                    firstIndex +
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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
                                localIndex <
                                pageItemCount
                            ) {

                                itemContent(
                                    globalIndex,

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


@Composable
fun IconGroupItem(
    group: IconGroup,
    selected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(group.uuid) {

                detectTapGestures(
                    onTap = {
                        onTap()
                    },

                    onLongPress = {
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

        Text(
            text =
                "${group.discoveredCount} / ${group.totalCount}",

            color = Color.White,

            fontFamily = GameFont,

            fontSize = 11.sp,

            fontWeight =
                FontWeight.Black
        )


        Spacer(
            Modifier.height(2.dp)
        )


        Box(
            modifier = Modifier
                .size(
                    if (selected) {
                        82.dp
                    } else {
                        75.dp
                    }
                )
                .background(
                    color =
                        if (selected) {
                            Color.White
                        } else {
                            Color.White.copy(
                                alpha = 0.15f
                            )
                        },

                    shape =
                        RoundedCornerShape(
                            11.dp
                        )
                )
                .padding(3.dp),

            contentAlignment =
                Alignment.Center
        ) {

            AsyncImage(
                model = group.imagePath,

                contentDescription =
                    group.displayName,

                modifier =
                    Modifier.fillMaxSize(),

                contentScale =
                    ContentScale.Fit
            )
        }


        Spacer(
            Modifier.height(3.dp)
        )


        Text(
            text =
                group.displayName.uppercase(),

            modifier =
                Modifier.fillMaxWidth(),

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


@Composable
fun AnimalGridItem(
    animal: Animal,
    selected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(animal.id) {

                detectTapGestures(
                    onTap = {
                        onTap()
                    },

                    onLongPress = {
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
                        82.dp
                    } else {
                        75.dp
                    }
                )
                .background(
                    color =
                        if (selected) {
                            Color.White
                        } else {
                            Color.White.copy(
                                alpha = 0.15f
                            )
                        },

                    shape =
                        RoundedCornerShape(
                            11.dp
                        )
                )
                .padding(3.dp),

            contentAlignment =
                Alignment.Center
        ) {

            if (
                animal.localImagePath
                    .isNullOrBlank()
            ) {

                Text(
                    text = "?",

                    color = Color.Black,

                    fontSize = 35.sp
                )

            } else {

                AsyncImage(
                    model =
                        animal.localImagePath,

                    contentDescription =
                        animal.displayName,

                    modifier =
                        Modifier.fillMaxSize(),

                    contentScale =
                        ContentScale.Fit
                )
            }
        }


        Spacer(
            Modifier.height(3.dp)
        )


        Text(
            text =
                animal.displayName.uppercase(),

            modifier =
                Modifier.fillMaxWidth(),

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
            text = "← BACK",

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