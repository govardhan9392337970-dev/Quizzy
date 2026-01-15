package com.example.quizzy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzy.ui.theme.QuizzyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AttemptRow(
    val uid: String = "",
    val score: Long = 0,
    val total: Long = 0,
    val createdAt: Long = 0
)

class ProgressActivity : ComponentActivity() {

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
                ProgressScreen(
                    uid = auth.currentUser!!.uid,
                    onBack = {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun ProgressScreen(uid: String, onBack: () -> Unit) {

        val goldenGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFE082),
                Color(0xFFFFF8E1)
            )
        )

        var isLoading by remember { mutableStateOf(true) }
        var rows by remember { mutableStateOf<List<AttemptRow>>(emptyList()) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        var totalAttempts by remember { mutableStateOf(0) }
        var bestScore by remember { mutableStateOf(0) }

        // ✅ FIX: remove orderBy from Firestore to avoid composite index requirement
        // We fetch attempts by uid, then sort locally.
        LaunchedEffect(uid) {
            db.collection("attempts")
                .whereEqualTo("uid", uid)
                .limit(50)
                .get()
                .addOnSuccessListener { qs ->
                    val list = qs.documents.mapNotNull { it.toObject(AttemptRow::class.java) }
                        .sortedByDescending { it.createdAt } // ✅ local sort (no index needed)

                    rows = list
                    totalAttempts = rows.size
                    bestScore = rows.maxOfOrNull { it.score.toInt() } ?: 0

                    isLoading = false
                }
                .addOnFailureListener { e ->
                    errorMsg = e.message ?: "Failed to load progress"
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "My Progress 📈",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3E2723)
                                )
                                Text(
                                    text = "Your recent quiz attempts",
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

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryChip(label = "Attempts", value = totalAttempts.toString())
                            SummaryChip(label = "Best Score", value = bestScore.toString())
                            SummaryChip(label = "Mode", value = "CS")
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
                            Text(text = errorMsg ?: "Error", color = Color(0xFF3E2723))
                        }
                    }

                    rows.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No attempts yet.\nStart a quiz to see progress here!",
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
                            items(rows) { row ->
                                AttemptItem(row)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SummaryChip(label: String, value: String) {
        Column {
            Text(label, fontSize = 12.sp, color = Color(0xFF6D4C41))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
        }
    }

    @Composable
    private fun AttemptItem(row: AttemptRow) {
        val total = if (row.total <= 0) 1 else row.total
        val percent = ((row.score.toDouble() / total.toDouble()) * 100).toInt()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {

                Text(
                    text = "Score: ${row.score}/${row.total}  •  $percent%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Date: ${formatDateTime(row.createdAt)}",
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037)
                )
            }
        }
    }

    private fun formatDateTime(millis: Long): String {
        if (millis <= 0) return "N/A"
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.UK)
        return sdf.format(Date(millis))
    }
}
