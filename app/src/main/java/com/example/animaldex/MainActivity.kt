package com.example.animaldex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.example.animaldex.naviguation.AnimalDexApp
import com.example.animaldex.ui.theme.AnimalDexTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        enableEdgeToEdge()


        setContent {

            AnimalDexTheme {

                AnimalDexApp()
            }
        }
    }
}