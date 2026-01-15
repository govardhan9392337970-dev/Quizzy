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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzy.ui.theme.QuizzyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        // Load questions from Firestore
        LaunchedEffect(Unit) {
            db.collection("questions")
                .limit(5)
                .get()
                .addOnSuccessListener { qs ->
                    questions = qs.documents.mapNotNull { it.toObject(QuizQuestion::class.java) }
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
                .padding(16.dp)
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

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = "Question ${currentIndex + 1} of ${questions.size}",
                        fontSize = 14.sp,
                        color = Color(0xFF5D4037)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = q.question,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    q.options.forEachIndexed { index, option ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { selectedOption = index },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedOption == index)
                                    Color(0xFFFFC107)
                                else
                                    Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.padding(14.dp),
                                color = Color(0xFF3E2723)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (selectedOption == q.correctIndex) {
                            score++
                        }

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
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107),
                        contentColor = Color(0xFF3E2723)
                    )
                ) {
                    Text(
                        text = if (currentIndex == questions.lastIndex) "Finish Quiz" else "Next",
                        fontWeight = FontWeight.Bold
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
