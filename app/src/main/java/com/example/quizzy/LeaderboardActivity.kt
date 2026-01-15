package com.example.quizzy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzy.ui.theme.QuizzyTheme
import com.google.firebase.firestore.FirebaseFirestore

data class LeaderboardRow(
    val uid: String = "",
    val score: Long = 0,
    val total: Long = 0,
    val createdAt: Long = 0
)

class LeaderboardActivity : ComponentActivity() {

    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuizzyTheme {
                LeaderboardScreen(
                    onBack = {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun LeaderboardScreen(onBack: () -> Unit) {

        val goldenGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFE082),
                Color(0xFFFFF8E1)
            )
        )

        var isLoading by remember { mutableStateOf(true) }
        var rows by remember { mutableStateOf<List<LeaderboardRow>>(emptyList()) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            db.collection("attempts")
                .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener { qs ->
                    rows = qs.documents.mapNotNull { it.toObject(LeaderboardRow::class.java) }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    errorMsg = e.message ?: "Failed to load leaderboard"
                    isLoading = false
                }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(goldenGradient)
                .padding(16.dp)
        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                // Simple header (no TopAppBar to avoid version issues)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Leaderboard 🏆",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                            Text(
                                text = "Top 20 quiz attempts",
                                fontSize = 12.sp,
                                color = Color(0xFF5D4037)
                            )
                        }

                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMsg != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = errorMsg ?: "Error",
                                color = Color(0xFF3E2723)
                            )
                        }
                    }

                    rows.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No attempts yet.\nPlay a quiz to appear here!",
                                color = Color(0xFF3E2723)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(rows) { index, row ->
                                LeaderboardItem(rank = index + 1, row = row)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LeaderboardItem(rank: Int, row: LeaderboardRow) {
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

                // Rank badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Score: ${row.score}/${row.total}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                    Text(
                        text = "User: ${shortUid(row.uid)}",
                        fontSize = 12.sp,
                        color = Color(0xFF5D4037),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    private fun shortUid(uid: String): String {
        if (uid.isBlank()) return "Unknown"
        return if (uid.length <= 8) uid else uid.take(4) + "..." + uid.takeLast(4)
    }
}
