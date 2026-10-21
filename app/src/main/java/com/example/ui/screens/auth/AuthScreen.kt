package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@Composable
fun AuthScreen(viewModel: AppViewModel) {
  var isRegisterMode by remember { mutableStateOf(false) }

  // Form states
  var identifier by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var regDisplayName by remember { mutableStateOf("") }
  var regUsername by remember { mutableStateOf("") }
  var regEmail by remember { mutableStateOf("") }
  var regPassword by remember { mutableStateOf("") }
  var regAge by remember { mutableStateOf("22") }

  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(DarkBg, DarkSurface, Color(0xFF1E0C38))
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(24.dp)
        .windowInsetsPadding(WindowInsets.statusBars),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // Logo & Brand Header
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(PurplePrimary, PurpleDark)))
          .border(2.dp, PurplePrimaryLight, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "تواصل", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "تطبيق التواصل العربي",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      Text(
        text = "المجتمع العربي المفضل للمشاركة والنقاشات الهادفة",
        fontSize = 13.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Mode Selector Tabs
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(DarkSurfaceElevated)
          .padding(4.dp)
      ) {
        Button(
          onClick = {
            isRegisterMode = false
            errorMessage = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (!isRegisterMode) PurplePrimary else Color.Transparent
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f).testTag("tab_login")
        ) {
          Text(
            text = "تسجيل الدخول",
            fontWeight = FontWeight.Bold,
            color = if (!isRegisterMode) Color.White else TextMuted
          )
        }

        Button(
          onClick = {
            isRegisterMode = true
            errorMessage = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isRegisterMode) PurplePrimary else Color.Transparent
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f).testTag("tab_register")
        ) {
          Text(
            text = "حساب جديد",
            fontWeight = FontWeight.Bold,
            color = if (isRegisterMode) Color.White else TextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Card Container
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, PurplePrimary.copy(alpha = 0.3f)))),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          if (errorMessage != null) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AdminRed.copy(alpha = 0.15f))
                .border(1.dp, AdminRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(12.dp)
            ) {
              Text(
                text = errorMessage ?: "",
                color = AdminRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
              )
            }
            Spacer(modifier = Modifier.height(14.dp))
          }

          if (!isRegisterMode) {
            // LOGIN FORM
            Text(text = "اسم المستخدم أو البريد الإلكتروني", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
              value = identifier,
              onValueChange = { identifier = it },
              placeholder = { Text("مثال: admin أو user@example.com", color = TextMuted) },
              singleLine = true,
              leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PurplePrimaryLight) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = DarkBorder
              ),
              modifier = Modifier.fillMaxWidth().testTag("input_login_identifier")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "كلمة المرور", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              singleLine = true,
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PurplePrimaryLight) },
              trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                  Icon(
                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = TextMuted
                  )
                }
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = DarkBorder
              ),
              modifier = Modifier.fillMaxWidth().testTag("input_login_password")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = {
                if (identifier.isBlank() || password.isBlank()) {
                  errorMessage = "يرجى إدخال اسم المستخدم وكلمة المرور"
                  return@Button
                }
                isLoading = true
                errorMessage = null
                viewModel.login(identifier, password) { success, err ->
                  isLoading = false
                  if (!success) {
                    errorMessage = err
                  }
                }
              },
              enabled = !isLoading,
              colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_submit_login")
            ) {
              if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
              } else {
                Text(text = "تسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 16.sp)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Admin test shortcut helper
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceElevated)
                .padding(10.dp)
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Shield, contentDescription = null, tint = AdminRed, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = "حساب الإدارة المبدئي (للاختبار والمراجعة):", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "المستخدم: admin | كلمة المرور: Admin1234",
                  color = TextMuted,
                  fontSize = 11.sp
                )
                TextButton(
                  onClick = {
                    identifier = "admin"
                    password = "Admin1234"
                  },
                  contentPadding = PaddingValues(0.dp),
                  modifier = Modifier.align(Alignment.End)
                ) {
                  Text(text = "ملء بيانات المدير تلقائياً", color = PurplePrimaryLight, fontSize = 11.sp)
                }
              }
            }
          } else {
            // REGISTER FORM
            Text(text = "الاسم الظاهر", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = regDisplayName,
              onValueChange = { regDisplayName = it },
              placeholder = { Text("مثال: فيصل الخالدي", color = TextMuted) },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PurplePrimary, unfocusedBorderColor = DarkBorder),
              modifier = Modifier.fillMaxWidth().testTag("input_reg_displayname")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "اسم المستخدم (بالأحرف الإنجليزية)", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = regUsername,
              onValueChange = { regUsername = it },
              placeholder = { Text("مثال: faisal22", color = TextMuted) },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PurplePrimary, unfocusedBorderColor = DarkBorder),
              modifier = Modifier.fillMaxWidth().testTag("input_reg_username")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "البريد الإلكتروني", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = regEmail,
              onValueChange = { regEmail = it },
              placeholder = { Text("user@domain.com", color = TextMuted) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PurplePrimary, unfocusedBorderColor = DarkBorder),
              modifier = Modifier.fillMaxWidth().testTag("input_reg_email")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "العمر", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = regAge,
              onValueChange = { regAge = it },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PurplePrimary, unfocusedBorderColor = DarkBorder),
              modifier = Modifier.fillMaxWidth().testTag("input_reg_age")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "كلمة المرور (6 خانات على الأقل)", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = regPassword,
              onValueChange = { regPassword = it },
              singleLine = true,
              visualTransformation = PasswordVisualTransformation(),
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PurplePrimary, unfocusedBorderColor = DarkBorder),
              modifier = Modifier.fillMaxWidth().testTag("input_reg_password")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Bonus badge
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceElevated)
                .padding(10.dp)
            ) {
              Text(text = "🎁", fontSize = 18.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "هدية ترحيبية: 100 نقطة مجانية فور إنشاء حسابك!",
                color = AccentGoldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = {
                val ageInt = regAge.toIntOrNull() ?: 20
                if (regUsername.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                  errorMessage = "يرجى تعبئة جميع الحقول المطلوبة"
                  return@Button
                }
                isLoading = true
                errorMessage = null
                viewModel.register(regUsername, regEmail, regDisplayName, regPassword, ageInt) { success, err ->
                  isLoading = false
                  if (!success) {
                    errorMessage = err
                  }
                }
              },
              enabled = !isLoading,
              colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_submit_register")
            ) {
              if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
              } else {
                Text(text = "إنشاء الحساب والبدء", fontWeight = FontWeight.Bold, fontSize = 16.sp)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
