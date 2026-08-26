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
import com.example.animaldex.data.loadAnimalsFromDatabase
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

    // Sens de la dernière navigation : true si on va "plus loin"
    // (ex. HOME -> ICON_GROUPS), false si on revient en arrière.
    // Lu par transitionSpec pour choisir le sens du glissement
    // (ignoré pour la paire GROUP_ANIMALS <-> ANIMAL_DETAIL, qui
    // utilise un zoom plutôt qu'un glissement).
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

    var groupParentScreen by remember {
        mutableStateOf(Screen.ICON_GROUPS)
    }

    // Page courante dans la grille des groupes d'un continent (ICON_GROUPS).
    // Hissée ici pour survivre à un aller-retour vers GROUP_ANIMALS, où
    // IconGroupsScreen est temporairement retiré de la composition.
    var iconGroupsPageIndex by remember {
        mutableIntStateOf(0)
    }

    // Page courante dans la grille d'animaux d'un groupe (GROUP_ANIMALS).
    // Hissée ici (au-dessus de l'écran lui-même) pour survivre à un
    // aller-retour vers ANIMAL_DETAIL, où GroupAnimalsScreen est
    // temporairement retiré de la composition.
    var groupAnimalsPageIndex by remember {
        mutableIntStateOf(0)
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

        currentScreen = when (currentScreen) {
            Screen.ANIMAL_DETAIL -> Screen.GROUP_ANIMALS
            Screen.GROUP_ANIMALS -> groupParentScreen
            Screen.ICON_GROUPS -> Screen.HOME
            Screen.CAMERA -> Screen.HOME
            Screen.HOME -> Screen.HOME
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

                // Paire spéciale : liste des animaux d'un groupe <-> fiche
                // détaillée d'un animal. On y joue un zoom (avant en entrant
                // dans la fiche, arrière en en sortant) plutôt qu'un
                // glissement horizontal.
                val isAnimalDetailPair =
                    (
                            initialState == Screen.GROUP_ANIMALS &&
                                    targetState == Screen.ANIMAL_DETAIL
                            ) || (
                            initialState == Screen.ANIMAL_DETAIL &&
                                    targetState == Screen.GROUP_ANIMALS
                            )


                if (isAnimalDetailPair) {

                    if (targetState == Screen.ANIMAL_DETAIL) {

                        // Zoom avant : la fiche animal apparaît en grossissant
                        // légèrement depuis 85%, pendant que la liste des
                        // animaux s'éloigne (grossit au-delà de 100% en
                        // s'estompant), comme si on "plongeait" dans la case
                        // cliquée.
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

                        // Zoom arrière : on revient de la fiche animal vers
                        // la liste. La fiche rétrécit en s'estompant, la
                        // liste réapparaît en revenant de 115% vers 100%.
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

                    // Navigation vers l'avant (toutes les autres paires
                    // d'écrans) : nouvel écran entre par la droite, ancien
                    // sort par la gauche.
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

                    // Retour en arrière (toutes les autres paires
                    // d'écrans) : glissement inversé, nouvel écran entre
                    // par la gauche, ancien sort par la droite.
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
                                currentScreen = Screen.ANIMAL_DETAIL
                            },
                            onBack = {
                                navigatingForward = false
                                currentScreen = groupParentScreen
                            }
                        )
                    }
                }

                Screen.ANIMAL_DETAIL -> {
                    selectedAnimal?.let { animal ->
                        AnimalDetailScreen(
                            animal = animal,
                            color = selectedContinent?.normalColor ?: allAnimalsColor,
                            onBack = {
                                navigatingForward = false
                                currentScreen = Screen.GROUP_ANIMALS
                            }
                        )
                    }
                }

                Screen.CAMERA -> {
                    CameraScreen(
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