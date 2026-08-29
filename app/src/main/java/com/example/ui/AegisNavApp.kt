package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.viewmodel.BrowserViewModel

@Composable
fun AegisNavApp(
    viewModel: BrowserViewModel,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    BrowserMainScreen(
        viewModel = viewModel,
        navController = navController
    )
}
