package com.example.quizzy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzy.ui.theme.QuizzyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizzyTheme {
                SplashScreen()
            }
        }
    }
}

@Composable
fun SplashScreen() {

    // Yellow → Golden gradient
    val goldenGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFFE082), // Light Amber
            Color(0xFFFFF8E1)  // Soft cream
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(goldenGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {

            // App Title
            Text(
                text = "Quizzy",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723), // Dark brown for contrast
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Caption / Tagline
            Text(
                text = "Test your Computer Science knowledge\nLearn • Compete • Improve",
                fontSize = 16.sp,
                color = Color(0xFF5D4037),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}
