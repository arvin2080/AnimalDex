package com.example.animaldex.navigation

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
import com.example.animaldex.util.allAnimalsColor


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

    val context =
        LocalContext.current


    // --------------------------------------------------------
    // DATABASE STATE
    // --------------------------------------------------------

    var animals by remember {

        mutableStateOf<List<Animal>>(
            emptyList()
        )
    }


    var loading by remember {

        mutableStateOf(true)
    }


    var errorMessage by remember {

        mutableStateOf<String?>(
            null
        )
    }


    // --------------------------------------------------------
    // NAVIGATION STATE
    // --------------------------------------------------------

    var currentScreen by remember {

        mutableStateOf(
            Screen.HOME
        )
    }


    var selectedContinent by remember {

        mutableStateOf<ContinentData?>(
            null
        )
    }


    var selectedGroup by remember {

        mutableStateOf<IconGroup?>(
            null
        )
    }


    var selectedAnimal by remember {

        mutableStateOf<Animal?>(
            null
        )
    }


    var groupParentScreen by remember {

        mutableStateOf(
            Screen.ICON_GROUPS
        )
    }


    // ========================================================
    // LOAD DATABASE
    // ========================================================

    LaunchedEffect(Unit) {

        try {

            animals =
                withContext(
                    Dispatchers.IO
                ) {

                    loadAnimalsFromDatabase(
                        context
                    )
                }

        } catch (
            error: Exception
        ) {

            error.printStackTrace()


            errorMessage =
                error.message
                    ?: "Unknown database error"

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

        ErrorScreen(
            errorMessage!!
        )

        return
    }


    // ========================================================
    // ANDROID BACK BUTTON
    // ========================================================

    BackHandler(
        enabled =
            currentScreen !=
                    Screen.HOME
    ) {

        currentScreen =
            when (
                currentScreen
            ) {

                Screen.ANIMAL_DETAIL -> {

                    Screen.GROUP_ANIMALS
                }


                Screen.GROUP_ANIMALS -> {

                    groupParentScreen
                }


                Screen.ICON_GROUPS -> {

                    Screen.HOME
                }


                Screen.CAMERA -> {

                    Screen.HOME
                }


                Screen.HOME -> {

                    Screen.HOME
                }
            }
    }


    // ========================================================
    // NAVIGATION
    // ========================================================

    when (
        currentScreen
    ) {

        // ----------------------------------------------------
        // HOME
        // ----------------------------------------------------

        Screen.HOME -> {

            HomeScreen(
                animals = animals,

                onContinentSelected = { continent ->

                    selectedContinent =
                        continent


                    currentScreen =
                        Screen.ICON_GROUPS
                },

                onGlobalGroupSelected = { group ->

                    selectedGroup =
                        group


                    selectedContinent =
                        null


                    groupParentScreen =
                        Screen.HOME


                    currentScreen =
                        Screen.GROUP_ANIMALS
                },

                onCameraClick = {

                    currentScreen =
                        Screen.CAMERA
                }
            )
        }


        // ----------------------------------------------------
        // CONTINENT ICON GROUPS
        // ----------------------------------------------------

        Screen.ICON_GROUPS -> {

            selectedContinent
                ?.let { continent ->

                    IconGroupsScreen(
                        continent =
                            continent,

                        allAnimals =
                            animals,

                        onGroupSelected = { group ->

                            selectedGroup =
                                group


                            groupParentScreen =
                                Screen.ICON_GROUPS


                            currentScreen =
                                Screen.GROUP_ANIMALS
                        },

                        onBack = {

                            currentScreen =
                                Screen.HOME
                        }
                    )
                }
        }


        // ----------------------------------------------------
        // ANIMALS INSIDE ICON GROUP
        // ----------------------------------------------------

        Screen.GROUP_ANIMALS -> {

            selectedGroup
                ?.let { group ->

                    GroupAnimalsScreen(
                        group =
                            group,

                        color =
                            selectedContinent
                                ?.normalColor
                                ?: allAnimalsColor,

                        onAnimalSelected = { animal ->

                            selectedAnimal =
                                animal


                            currentScreen =
                                Screen.ANIMAL_DETAIL
                        },

                        onBack = {

                            currentScreen =
                                groupParentScreen
                        }
                    )
                }
        }


        // ----------------------------------------------------
        // ANIMAL DETAIL
        // ----------------------------------------------------

        Screen.ANIMAL_DETAIL -> {

            selectedAnimal
                ?.let { animal ->

                    AnimalDetailScreen(
                        animal =
                            animal,

                        color =
                            selectedContinent
                                ?.normalColor
                                ?: allAnimalsColor,

                        onBack = {

                            currentScreen =
                                Screen.GROUP_ANIMALS
                        }
                    )
                }
        }


        // ----------------------------------------------------
        // CAMERA / ANIMAL SCANNER
        // ----------------------------------------------------

        Screen.CAMERA -> {

            CameraScreen(
                onBack = {

                    currentScreen =
                        Screen.HOME
                }
            )
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
            .background(
                Color(0xFFEEF3F8)
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text =
                "LOADING ANIMALDEX...",

            color =
                allAnimalsColor,

            fontFamily =
                GameFont,

            fontSize =
                15.sp,

            fontWeight =
                FontWeight.Black
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
            .background(
                Color(0xFFEEF3F8)
            )
            .padding(
                20.dp
            ),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                "DATABASE ERROR",

            color =
                Color.Red,

            fontFamily =
                GameFont,

            fontWeight =
                FontWeight.Black
        )


        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )


        Text(
            text =
                message,

            color =
                Color.Black,

            textAlign =
                TextAlign.Center
        )
    }
}