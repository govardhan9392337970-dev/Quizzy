package com.example.quizzy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
        val context = LocalContext.current

        val goldenGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFE082),
                Color(0xFFFFF8E1)
            )
        )

        // Simple default values (no fancy state APIs)
        var name by remember { mutableStateOf("Quizzy User") }
        var totalAttempts by remember { mutableStateOf(0) }
        var bestScore by remember { mutableStateOf(0) }

        // Fetch data (optional)
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(goldenGradient)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ✅ Compact header (fixes "home page too high" feeling)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Welcome 👋", fontSize = 12.sp, color = Color(0xFF6D4C41))
                            Text(
                                text = name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Ready to test your CS skills?",
                                fontSize = 12.sp,
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

            // ✅ Small stats row (not tall)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            item {
                SimpleActionCard(
                    title = "Start Quiz",
                    subtitle = "Answer 5 CS questions",
                    icon = Icons.Default.PlayArrow
                ) {
                    Toast.makeText(context, "Quiz screen next ✅", Toast.LENGTH_SHORT).show()
                    // startActivity(Intent(this@HomeActivity, QuizActivity::class.java))
                }
            }

            item {
                SimpleActionCard(
                    title = "Leaderboard",
                    subtitle = "Top scores (Firestore)",
                    icon = Icons.Default.Leaderboard
                ) {
                    Toast.makeText(context, "Leaderboard screen next ✅", Toast.LENGTH_SHORT).show()
                    // startActivity(Intent(this@HomeActivity, LeaderboardActivity::class.java))
                }
            }

            item {
                SimpleActionCard(
                    title = "My Progress",
                    subtitle = "Your recent attempts",
                    icon = Icons.Default.Timeline
                ) {
                    Toast.makeText(context, "Progress screen next ✅", Toast.LENGTH_SHORT).show()
                    // startActivity(Intent(this@HomeActivity, ProgressActivity::class.java))
                }
            }

            item {
                Text(
                    text = "Tip: Try again to beat your best score ✨",
                    fontSize = 13.sp,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }
    }

    @Composable
    private fun StatChip(label: String, value: String) {
        Column {
            Text(label, fontSize = 12.sp, color = Color(0xFF6D4C41))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
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
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = Color(0xFF3E2723))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                    Text(subtitle, fontSize = 12.sp, color = Color(0xFF5D4037))
                }
            }
        }
    }
}
