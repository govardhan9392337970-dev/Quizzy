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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzy.ui.theme.QuizzyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            QuizzyTheme {
                SignUpScreen(
                    onSignupSuccess = {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    },
                    onGoToLogin = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun SignUpScreen(
        onSignupSuccess: () -> Unit,
        onGoToLogin: () -> Unit
    ) {
        val context = LocalContext.current

        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        var passwordVisible by remember { mutableStateOf(false) }
        var confirmVisible by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }

        // Golden theme gradient (same as splash/login)
        val goldenGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFE082),
                Color(0xFFFFF8E1)
            )
        )

        fun doSignup() {
            val n = fullName.trim()
            val e = email.trim()
            val p = password
            val c = confirmPassword

            when {
                n.isEmpty() -> {
                    Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                    return
                }
                e.isEmpty() -> {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                    return
                }
                p.length < 6 -> {
                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return
                }
                p != c -> {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            isLoading = true
            auth.createUserWithEmailAndPassword(e, p)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        isLoading = false
                        Toast.makeText(
                            context,
                            task.exception?.message ?: "Signup failed",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    val uid = auth.currentUser?.uid ?: run {
                        isLoading = false
                        Toast.makeText(context, "User created but UID missing", Toast.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }

                    // Save basic user profile in Firestore
                    val userDoc = hashMapOf(
                        "uid" to uid,
                        "name" to n,
                        "email" to e,
                        "createdAt" to System.currentTimeMillis(),
                        "totalQuizzes" to 0,
                        "bestScore" to 0
                    )

                    db.collection("users").document(uid)
                        .set(userDoc)
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "Account created ✅", Toast.LENGTH_SHORT).show()
                            onSignupSuccess()
                        }
                        .addOnFailureListener { ex ->
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Account created but profile save failed: ${ex.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            // Still allow user to continue
                            onSignupSuccess()
                        }
                }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(goldenGradient)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.88f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Join Quizzy and start improving your CS skills",
                    fontSize = 14.sp,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password") },
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle confirm password"
                            )
                        }
                    },
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { doSignup() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { doSignup() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107),
                        contentColor = Color(0xFF3E2723)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF3E2723)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Creating account...")
                    } else {
                        Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Already have an account? Login",
                    fontSize = 14.sp,
                    color = Color(0xFF6D4C41),
                    modifier = Modifier.clickable { onGoToLogin() }
                )
            }
        }
    }
}
