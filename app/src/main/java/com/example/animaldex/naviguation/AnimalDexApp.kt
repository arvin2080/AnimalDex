package com.example.animaldex.naviguation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.animaldex.camera.CameraScreen
import com.example.animaldex.data.incrementAnimalCapture
import com.example.animaldex.data.loadAnimalsFromDatabase
import com.example.animaldex.data.readAnimalCaptureCount
import com.example.animaldex.model.Animal
import com.example.animaldex.model.ContinentData
import com.example.animaldex.model.IconGroup
import com.example.animaldex.ui.AnimalDetailScreen
import com.example.animaldex.ui.GroupAnimalsScreen
import com.example.animaldex.ui.HomeScreen
import com.example.animaldex.ui.IconGroupsScreen
import com.example.animaldex.util.GameFont
import com.example.animaldex.util.PageBackgroundColor
import com.example.animaldex.util.allAnimalsColor
import com.example.animaldex.util.buildIconGroups
import com.example.animaldex.util.continents

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith


// ============================================================
// SCREENS
// ============================================================

enum class Screen {
    HOME,
    ICON_GROUPS,
    GROUP_ANIMALS,
    ANIMAL_DETAIL,
    CAMERA
}


// ============================================================
// MAIN APP
// ============================================================

@Composable
fun AnimalDexApp() {

    val context = LocalContext.current

    // --------------------------------------------------------
    // DATABASE STATE
    // --------------------------------------------------------

    var animals by remember {
        mutableStateOf<List<Animal>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // --------------------------------------------------------
    // NAVIGATION STATE
    // --------------------------------------------------------

    var currentScreen by remember {
        mutableStateOf(Screen.HOME)
    }

    var navigatingForward by remember {
        mutableStateOf(true)
    }

    var selectedContinent by remember {
        mutableStateOf<ContinentData?>(null)
    }

    var selectedGroup by remember {
        mutableStateOf<IconGroup?>(null)
    }

    var selectedAnimal by remember {
        mutableStateOf<Animal?>(null)
    }

    // D'où on vient quand on quitte ANIMAL_DETAIL — normalement
    // GROUP_ANIMALS, mais CAMERA quand on arrive directement d'une
    // capture d'un animal déjà connu.
    var detailParentScreen by remember {
        mutableStateOf(Screen.GROUP_ANIMALS)
    }

    var groupParentScreen by remember {
        mutableStateOf(Screen.ICON_GROUPS)
    }

    var iconGroupsPageIndex by remember {
        mutableIntStateOf(0)
    }

    var groupAnimalsPageIndex by remember {
        mutableIntStateOf(0)
    }

    // --------------------------------------------------------
    // ÉTAT PROPRE AU FLUX "CAPTURE PAR L'APPAREIL PHOTO"
    // --------------------------------------------------------

    var justDiscoveredAnimalId by remember {
        mutableStateOf<Int?>(null)
    }

    var justCapturedFlag by remember {
        mutableStateOf(false)
    }


    fun handleAnimalRecognized(
        recognizedAnimal: Animal
    ) {

        val wasAlreadyDiscovered =
            recognizedAnimal.discovered


        incrementAnimalCapture(
            context,
            recognizedAnimal.id
        )

        val newCount =
            readAnimalCaptureCount(
                context,
                recognizedAnimal.id
            )

        val updatedAnimal =
            recognizedAnimal.copy(
                discovered = true,
                captureCount = newCount
            )


        animals =
            animals.map { animal ->

                if (animal.id == recognizedAnimal.id) {
                    updatedAnimal
                } else {
                    animal
                }
            }


        selectedAnimal =
            updatedAnimal

        selectedContinent =
            continents.firstOrNull { continent ->
                updatedAnimal.continents.contains(
                    continent.name
                )
            }

        selectedGroup =
            buildIconGroups(animals).firstOrNull { group ->
                group.animals.any {
                    it.id == recognizedAnimal.id
                }
            }

        groupParentScreen =
            Screen.HOME

        groupAnimalsPageIndex = 0

        navigatingForward = true


        if (wasAlreadyDiscovered) {

            justDiscoveredAnimalId = null
            justCapturedFlag = true

            detailParentScreen = Screen.CAMERA

            currentScreen = Screen.ANIMAL_DETAIL

        } else {

            justCapturedFlag = false
            justDiscoveredAnimalId = recognizedAnimal.id

            currentScreen = Screen.GROUP_ANIMALS
        }
    }


    // ========================================================
    // LOAD DATABASE
    // ========================================================

    LaunchedEffect(Unit) {
        try {
            animals = withContext(Dispatchers.IO) {
                loadAnimalsFromDatabase(context)
            }
        } catch (error: Exception) {
            error.printStackTrace()
            errorMessage = error.message ?: "Unknown database error"
        } finally {
            loading = false
        }
    }

    // ========================================================
    // LOADING
    // ========================================================

    if (loading) {
        LoadingScreen()
        return
    }

    // ========================================================
    // DATABASE ERROR
    // ========================================================

    if (errorMessage != null) {
        ErrorScreen(errorMessage!!)
        return
    }

    // ========================================================
    // ANDROID BACK BUTTON
    // ========================================================

    BackHandler(
        enabled = currentScreen != Screen.HOME
    ) {
        navigatingForward = false

        when (currentScreen) {

            Screen.ANIMAL_DETAIL -> {
                justCapturedFlag = false
                currentScreen = detailParentScreen
            }

            Screen.GROUP_ANIMALS -> {
                justDiscoveredAnimalId = null
                currentScreen = groupParentScreen
            }

            Screen.ICON_GROUPS ->
                currentScreen = Screen.HOME

            Screen.CAMERA ->
                currentScreen = Screen.HOME

            Screen.HOME ->
                currentScreen = Screen.HOME
        }
    }

    // ========================================================
    // NAVIGATION
    // ========================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackgroundColor)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {

                val isAnimalDetailPair =
                    (
                            initialState == Screen.GROUP_ANIMALS &&
                                    targetState == Screen.ANIMAL_DETAIL
                            ) || (
                            initialState == Screen.ANIMAL_DETAIL &&
                                    targetState == Screen.GROUP_ANIMALS
                            )

                val isContinentZoomPair =
                    (
                            initialState == Screen.HOME &&
                                    targetState == Screen.ICON_GROUPS
                            ) || (
                            initialState == Screen.ICON_GROUPS &&
                                    targetState == Screen.HOME
                            )


                if (isAnimalDetailPair) {

                    if (targetState == Screen.ANIMAL_DETAIL) {

                        (
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(300)
                                ) + fadeIn(tween(300))
                                ) togetherWith (
                                scaleOut(
                                    targetScale = 1.15f,
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(200))
                                )

                    } else {

                        (
                                scaleIn(
                                    initialScale = 1.15f,
                                    animationSpec = tween(300)
                                ) + fadeIn(tween(300))
                                ) togetherWith (
                                scaleOut(
                                    targetScale = 0.85f,
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(200))
                                )
                    }

                } else if (isContinentZoomPair) {

                    if (targetState == Screen.ICON_GROUPS) {

                        (
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(300)
                                ) + fadeIn(tween(300))
                                ) togetherWith (
                                scaleOut(
                                    targetScale = 1.15f,
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(200))
                                )

                    } else {

                        (
                                scaleIn(
                                    initialScale = 1.15f,
                                    animationSpec = tween(300)
                                ) + fadeIn(tween(300))
                                ) togetherWith (
                                scaleOut(
                                    targetScale = 0.85f,
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(200))
                                )
                    }

                } else if (navigatingForward) {

                    (
                            slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { fullWidth -> fullWidth } + fadeIn(tween(300))
                            ) togetherWith (
                            slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { fullWidth -> -fullWidth } + fadeOut(tween(200))
                            )

                } else {

                    (
                            slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { fullWidth -> -fullWidth } + fadeIn(tween(300))
                            ) togetherWith (
                            slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { fullWidth -> fullWidth } + fadeOut(tween(200))
                            )
                }
            },
            label = "screen_transition"
        ) { screen ->

            when (screen) {

                Screen.HOME -> {
                    HomeScreen(
                        animals = animals,
                        onContinentSelected = { continent ->
                            navigatingForward = true
                            selectedContinent = continent
                            iconGroupsPageIndex = 0
                            currentScreen = Screen.ICON_GROUPS
                        },
                        onGlobalGroupSelected = { group ->
                            navigatingForward = true
                            selectedGroup = group
                            selectedContinent = null
                            groupParentScreen = Screen.HOME
                            groupAnimalsPageIndex = 0
                            currentScreen = Screen.GROUP_ANIMALS
                        },
                        onCameraClick = {
                            navigatingForward = true
                            currentScreen = Screen.CAMERA
                        }
                    )
                }

                Screen.ICON_GROUPS -> {
                    selectedContinent?.let { continent ->
                        IconGroupsScreen(
                            continent = continent,
                            allAnimals = animals,
                            currentPage = iconGroupsPageIndex,
                            onPageChange = { newPage ->
                                iconGroupsPageIndex = newPage
                            },
                            onGroupSelected = { group ->
                                navigatingForward = true
                                selectedGroup = group
                                groupParentScreen = Screen.ICON_GROUPS
                                groupAnimalsPageIndex = 0
                                currentScreen = Screen.GROUP_ANIMALS
                            },
                            onBack = {
                                navigatingForward = false
                                currentScreen = Screen.HOME
                            }
                        )
                    }
                }

                Screen.GROUP_ANIMALS -> {
                    selectedGroup?.let { group ->
                        GroupAnimalsScreen(
                            group = group,
                            color = selectedContinent?.normalColor ?: allAnimalsColor,
                            currentPage = groupAnimalsPageIndex,
                            onPageChange = { newPage ->
                                groupAnimalsPageIndex = newPage
                            },
                            onAnimalSelected = { animal ->
                                navigatingForward = true
                                selectedAnimal = animal
                                justCapturedFlag = false
                                detailParentScreen = Screen.GROUP_ANIMALS
                                currentScreen = Screen.ANIMAL_DETAIL
                            },
                            onBack = {
                                navigatingForward = false
                                justDiscoveredAnimalId = null
                                currentScreen = groupParentScreen
                            },
                            revealAnimalId = justDiscoveredAnimalId,
                            onRevealComplete = {
                                justDiscoveredAnimalId = null
                                justCapturedFlag = true
                                detailParentScreen = Screen.GROUP_ANIMALS
                                navigatingForward = true
                                currentScreen = Screen.ANIMAL_DETAIL
                            }
                        )
                    }
                }

                Screen.ANIMAL_DETAIL -> {
                    selectedAnimal?.let { animal ->
                        AnimalDetailScreen(
                            animal = animal,
                            color = selectedContinent?.normalColor ?: allAnimalsColor,
                            justCaptured = justCapturedFlag,
                            onBack = {
                                navigatingForward = false
                                justCapturedFlag = false
                                currentScreen = detailParentScreen
                            }
                        )
                    }
                }

                Screen.CAMERA -> {
                    CameraScreen(
                        animals = animals,
                        onAnimalFound = { animal ->
                            handleAnimalRecognized(animal)
                        },
                        onBack = {
                            navigatingForward = false
                            currentScreen = Screen.HOME
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// LOADING SCREEN
// ============================================================

@Composable
fun LoadingScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF3F8)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "LOADING ANIMALDEX...",
            color = allAnimalsColor,
            fontFamily = GameFont,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}


// ============================================================
// ERROR SCREEN
// ============================================================

@Composable
fun ErrorScreen(
    message: String
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF3F8))
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DATABASE ERROR",
            color = Color.Red,
            fontFamily = GameFont,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = message,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}