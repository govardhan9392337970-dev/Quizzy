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
import kotlin.math.min

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0
)

class QuizActivity : ComponentActivity() {

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuizzyTheme {
                QuizScreen(
                    onFinish = { score, total ->
                        saveResult(score, total)
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun QuizScreen(onFinish: (Int, Int) -> Unit) {

        val goldenGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFE082),
                Color(0xFFFFF8E1)
            )
        )

        var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
        var currentIndex by remember { mutableStateOf(0) }
        var selectedOption by remember { mutableStateOf(-1) }
        var score by remember { mutableStateOf(0) }
        var isLoading by remember { mutableStateOf(true) }

        // Load questions from Firestore (simple)
        LaunchedEffect(Unit) {
            db.collection("questions")
                .get()
                .addOnSuccessListener { qs ->
                    val all = qs.documents.mapNotNull { it.toObject(QuizQuestion::class.java) }
                    // ✅ take up to 5; shuffle so it feels random without complex Firestore queries
                    questions = all.shuffled().take(min(5, all.size))
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(goldenGradient)
                .padding(horizontal = 12.dp, vertical = 8.dp) // ✅ smaller padding
        ) {

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            if (questions.isEmpty()) {
                Text(
                    text = "No questions found.\nPlease add questions in Firestore.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF3E2723)
                )
                return@Box
            }

            val q = questions[currentIndex]

            // ✅ Use LazyColumn so the screen never feels “too high” / overflowing
            Column(modifier = Modifier.fillMaxSize()) {

                // Compact header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            fontSize = 11.sp,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = q.question,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3E2723)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Options list (scrollable)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 10.dp)
                ) {
                    items(q.options.size) { index ->
                        val option = q.options[index]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOption = index },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedOption == index)
                                    Color(0xFFFFC107)
                                else
                                    Color.White.copy(alpha = 0.92f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.padding(12.dp), // ✅ smaller padding
                                fontSize = 13.sp,
                                color = Color(0xFF3E2723)
                            )
                        }
                    }
                }

                // Compact button
                Button(
                    onClick = {
                        if (selectedOption == q.correctIndex) score++

                        if (currentIndex == questions.lastIndex) {
                            onFinish(score, questions.size)
                        } else {
                            currentIndex++
                            selectedOption = -1
                        }
                    },
                    enabled = selectedOption != -1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp), // ✅ smaller height
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107),
                        contentColor = Color(0xFF3E2723)
                    )
                ) {
                    Text(
                        text = if (currentIndex == questions.lastIndex) "Finish Quiz" else "Next",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    private fun saveResult(score: Int, total: Int) {
        val uid = auth.currentUser?.uid ?: return

        val attempt = hashMapOf(
            "uid" to uid,
            "score" to score,
            "total" to total,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("attempts").add(attempt)
    }
}
