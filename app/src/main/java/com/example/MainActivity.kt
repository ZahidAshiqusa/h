package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zaidashiq.ultra.AppContainer
import com.zaidashiq.ultra.ZaidApplication
import com.zaidashiq.ultra.ui.UltraAppRoot
import com.zaidashiq.ultra.ui.UltraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as? ZaidApplication)?.container
            ?: AppContainer(applicationContext)

        setContent {
            val ultraViewModel: UltraViewModel = viewModel(
                factory = UltraViewModel.provideFactory(appContainer)
            )
            UltraAppRoot(viewModel = ultraViewModel)
        }
    }
}
