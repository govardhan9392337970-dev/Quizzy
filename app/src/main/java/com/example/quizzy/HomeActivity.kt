package com.example.quizzy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzy.ui.theme.QuizzyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : ComponentActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            QuizzyTheme {
                HomeScreen(
                    uid = auth.currentUser!!.uid,
                    onLogout = {
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun HomeScreen(uid: String, onLogout: () -> Unit) {

        val goldenGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFE082),
                Color(0xFFFFF8E1)
            )
        )

        var name by remember { mutableStateOf("Quizzy User") }
        var totalAttempts by remember { mutableStateOf(0) }
        var bestScore by remember { mutableStateOf(0) }

        // Load user + stats
        LaunchedEffect(uid) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: "Quizzy User"
                }

            db.collection("attempts")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener { qs ->
                    totalAttempts = qs.size()
                    bestScore = qs.documents.maxOfOrNull {
                        (it.getLong("score") ?: 0L).toInt()
                    } ?: 0
                }
        }

        // ✅ Compact layout: smaller paddings + smaller fonts + smaller cards
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(goldenGradient)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Header (compact)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Welcome 👋", fontSize = 11.sp, color = Color(0xFF6D4C41))
                            Text(
                                text = name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Ready to test your CS skills?",
                                fontSize = 11.sp,
                                color = Color(0xFF5D4037)
                            )
                        }

                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color(0xFF3E2723)
                            )
                        }
                    }
                }
            }

            // Stats row (compact)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatChip("Attempts", totalAttempts.toString())
                        StatChip("Best", bestScore.toString())
                        StatChip("Mode", "CS")
                    }
                }
            }

            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                SimpleActionCard(
                    title = "Start Quiz",
                    subtitle = "5 CS questions",
                    icon = Icons.Default.PlayArrow
                ) {
                    startActivity(Intent(this@HomeActivity, QuizActivity::class.java))
                }
            }

            item {
                SimpleActionCard(
                    title = "Leaderboard",
                    subtitle = "Top scores",
                    icon = Icons.Default.Leaderboard
                ) {
                    startActivity(Intent(this@HomeActivity, LeaderboardActivity::class.java))
                }
            }

            item {
                SimpleActionCard(
                    title = "My Progress",
                    subtitle = "Quiz history",
                    icon = Icons.Default.Timeline
                ) {
                    startActivity(Intent(this@HomeActivity, ProgressActivity::class.java))
                }
            }

            item {
                Text(
                    text = "Tip: Try again to beat your best score ✨",
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }

    @Composable
    private fun StatChip(label: String, value: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = Color(0xFF6D4C41))
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
            )
        }
    }

    @Composable
    private fun SimpleActionCard(
        title: String,
        subtitle: String,
        icon: ImageVector,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = Color(0xFF3E2723))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                    Text(
                        subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF5D4037)
                    )
                }
            }
        }
    }
}
