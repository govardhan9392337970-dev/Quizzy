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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzy.ui.theme.QuizzyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

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
                    userUid = auth.currentUser!!.uid,
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
    private fun HomeScreen(
        userUid: String,
        onLogout: () -> Unit
    ) {
        val context = LocalContext.current

        val goldenGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFE082),
                Color(0xFFFFF8E1)
            )
        )

        var name by rememberSaveable { mutableStateOf("Loading...") }
        var totalQuizzes by rememberSaveable { mutableStateOf(0) }   // keep simple for compatibility
        var bestScore by rememberSaveable { mutableStateOf(0) }      // keep simple for compatibility
        var isLoading by rememberSaveable { mutableStateOf(true) }

        LaunchedEffect(userUid) {
            db.collection("users").document(userUid)
                .get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: "Quizzy User"
                    totalQuizzes = (doc.getLong("totalQuizzes") ?: 0L).toInt()
                    bestScore = (doc.getLong("bestScore") ?: 0L).toInt()
                    isLoading = false
                }
                .addOnFailureListener {
                    name = "Quizzy User"
                    isLoading = false
                }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(goldenGradient)
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome 👋",
                                fontSize = 14.sp,
                                color = Color(0xFF6D4C41)
                            )
                            Text(
                                text = name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723),
                                maxLines = 1
                            )
                            Text(
                                text = "Ready to test your CS skills?",
                                fontSize = 13.sp,
                                color = Color(0xFF5D4037)
                            )
                        }

                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = Color(0xFF3E2723)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ✅ Apply weight HERE (RowScope) — not inside StatCard()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Quizzes",
                        value = if (isLoading) "..." else totalQuizzes.toString(),
                        icon = Icons.Default.Assessment
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Best Score",
                        value = if (isLoading) "..." else bestScore.toString(),
                        icon = Icons.Default.EmojiEvents
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Quick Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    modifier = Modifier.padding(start = 6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionButton(
                    title = "Start Quiz",
                    subtitle = "Begin a new CS quiz now",
                    icon = Icons.Default.PlayArrow,
                    onClick = {
                        Toast.makeText(context, "Start Quiz clicked", Toast.LENGTH_SHORT).show()
                        // startActivity(Intent(this@HomeActivity, QuizActivity::class.java))
                    }
                )

                ActionButton(
                    title = "Leaderboard",
                    subtitle = "See top scores (Firestore)",
                    icon = Icons.Default.Leaderboard,
                    onClick = {
                        Toast.makeText(context, "Leaderboard clicked", Toast.LENGTH_SHORT).show()
                        // startActivity(Intent(this@HomeActivity, LeaderboardActivity::class.java))
                    }
                )

                ActionButton(
                    title = "My Progress",
                    subtitle = "Check your quiz history",
                    icon = Icons.Default.Timeline,
                    onClick = {
                        Toast.makeText(context, "Progress clicked", Toast.LENGTH_SHORT).show()
                        // startActivity(Intent(this@HomeActivity, ProgressActivity::class.java))
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Tip: Try a timed quiz to challenge yourself ⏱️",
                    fontSize = 13.sp,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )
            }
        }
    }

    @Composable
    private fun StatCard(
        modifier: Modifier = Modifier,
        title: String,
        value: String,
        icon: ImageVector
    ) {
        Card(
            modifier = modifier.height(90.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = Color(0xFF3E2723))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = title, fontSize = 13.sp, color = Color(0xFF6D4C41))
                    Text(
                        text = value,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                }
            }
        }
    }

    @Composable
    private fun ActionButton(
        title: String,
        subtitle: String,
        icon: ImageVector,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = Color(0xFF3E2723))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color(0xFF5D4037)
                    )
                }


            }
        }
    }
}
