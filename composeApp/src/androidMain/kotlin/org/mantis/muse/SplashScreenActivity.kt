package org.mantis.muse

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.notificationBar)

        setContent {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                delay(2000)  // Wait for 2 seconds before launching the MainActivity
                context.startActivity(Intent(context, MainActivity::class.java))
            }
            Spacer(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
    }
}