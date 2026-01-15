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

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        // ✅ IMPORTANT:
        // Removed auto-skip to Home so GDPR popup shows every time on Login screen.

        setContent {
            QuizzyTheme {
                LoginScreen(
                    onLoginSuccess = {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun LoginScreen(onLoginSuccess: () -> Unit) {
        val context = LocalContext.current

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }

        // ✅ GDPR popup must appear every time:
        var showGdpr by remember { mutableStateOf(true) }
        var gdprAccepted by remember { mutableStateOf(false) }

        val goldenGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFE082),
                Color(0xFFFFF8E1)
            )
        )

        fun doLogin() {
            if (!gdprAccepted) {
                Toast.makeText(context, "Please accept GDPR consent to continue", Toast.LENGTH_SHORT).show()
                showGdpr = true
                return
            }

            val e = email.trim()
            val p = password

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return
            }

            isLoading = true
            auth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Login successful ✅", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    } else {
                        Toast.makeText(
                            context,
                            task.exception?.message ?: "Login failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // ✅ GDPR Dialog (Always shows on entering login)
        if (showGdpr) {
            AlertDialog(
                onDismissRequest = { /* block dismiss to force a choice */ },
                title = { Text("GDPR Consent") },
                text = {
                    Text(
                        "Quizzy uses Firebase Authentication and Firestore to store your login and quiz results.\n\n" +
                                "We store:\n" +
                                "• Your email (for login)\n" +
                                "• Your quiz attempts (score, total, timestamp)\n\n" +
                                "By clicking Agree, you consent to this data being processed for app functionality.\n" +
                                "You can request deletion by contacting the app owner."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            gdprAccepted = true
                            showGdpr = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color(0xFF3E2723)
                        )
                    ) {
                        Text("Agree")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            // user does not consent -> exit screen
                            Toast.makeText(context, "Consent required to use Quizzy", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    ) {
                        Text("Decline")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(goldenGradient)
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.88f))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Quizzy",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Login to continue your CS quiz journey",
                    fontSize = 13.sp,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = gdprAccepted && !isLoading
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = gdprAccepted && !isLoading
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { doLogin() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = gdprAccepted && !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { doLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = gdprAccepted && !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107),
                        contentColor = Color(0xFF3E2723)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF3E2723)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Signing in...")
                    } else {
                        Text("Login", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Don’t have an account? Sign up",
                    fontSize = 13.sp,
                    color = Color(0xFF6D4C41),
                    modifier = Modifier.clickable {
                        startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Forgot password?",
                    fontSize = 12.sp,
                    color = Color(0xFF8D6E63),
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Add reset flow if needed", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "GDPR: Tap here to view consent again",
                    fontSize = 11.sp,
                    color = Color(0xFF6D4C41),
                    modifier = Modifier.clickable { showGdpr = true }
                )
            }
        }
    }
}
