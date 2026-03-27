package com.example.quizzy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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

        LaunchedEffect(Unit) {
            db.collection("questions")
                .get()
                .addOnSuccessListener { qs ->
                    val all = qs.documents.mapNotNull { it.toObject(QuizQuestion::class.java) }
                    questions = all.shuffled().take(min(5, all.size))
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }

        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(goldenGradient)
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF3E2723)
                        )
                    }

                    questions.isEmpty() -> {
                        Text(
                            text = "No questions found.\nPlease add questions in Firestore.",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF3E2723),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    else -> {
                        val q = questions[currentIndex]

                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Header Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.95f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Text(
                                        text = "Question ${currentIndex + 1} of ${questions.size}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6D4C41),
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = q.question,
                                        fontSize = 20.sp,
                                        lineHeight = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3E2723)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Options area scrolls properly
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                itemsIndexed(q.options) { index, option ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedOption = index },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedOption == index) {
                                                Color(0xFFFFC107)
                                            } else {
                                                Color.White.copy(alpha = 0.92f)
                                            }
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                    ) {
                                        Text(
                                            text = option,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 16.dp
                                            ),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF3E2723)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Bottom button always visible
                            Button(
                                onClick = {
                                    val newScore =
                                        if (selectedOption == q.correctIndex) score + 1 else score

                                    if (currentIndex == questions.lastIndex) {
                                        onFinish(newScore, questions.size)
                                    } else {
                                        score = newScore
                                        currentIndex++
                                        selectedOption = -1
                                    }
                                },
                                enabled = selectedOption != -1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFC107),
                                    contentColor = Color(0xFF3E2723),
                                    disabledContainerColor = Color(0xFFE0D7B8),
                                    disabledContentColor = Color(0xFF8D6E63)
                                )
                            ) {
                                Text(
                                    text = if (currentIndex == questions.lastIndex) "Finish Quiz" else "Next",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
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