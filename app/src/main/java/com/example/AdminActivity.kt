package com.example

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserEntity
import com.example.ui.screens.admin.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class AdminActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        AdminStandaloneApp()
      }
    }
  }
}

@Composable
fun AdminStandaloneApp(viewModel: AppViewModel = viewModel()) {
  val context = LocalContext.current
  val adminPrefs: SharedPreferences = remember {
    context.getSharedPreferences("arabic_social_admin_session", Context.MODE_PRIVATE)
  }

  var adminUserId by remember {
    mutableStateOf(adminPrefs.getLong("logged_in_admin_id", -1L))
  }
  var loggedAdminUser by remember { mutableStateOf<UserEntity?>(null) }
  var isChecking by remember { mutableStateOf(true) }

  val scope = rememberCoroutineScope()

  // Validate admin session
  LaunchedEffect(adminUserId) {
    if (adminUserId > 0) {
      val user = viewModel.repository.getUserByIdSync(adminUserId)
      if (user != null && user.role == "ADMIN" && !user.isBanned) {
        loggedAdminUser = user
      } else {
        adminPrefs.edit().remove("logged_in_admin_id").apply()
        loggedAdminUser = null
        adminUserId = -1L
      }
    } else {
      loggedAdminUser = null
    }
    isChecking = false
  }

  if (isChecking) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(DarkBg),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator(color = AdminRed)
    }
    return
  }

  if (loggedAdminUser == null) {
    AdminStandaloneLoginScreen(
      onLoginSuccess = { user ->
        adminPrefs.edit().putLong("logged_in_admin_id", user.id).apply()
        adminUserId = user.id
        loggedAdminUser = user
      },
      onOpenMainApp = {
        context.startActivity(Intent(context, MainActivity::class.java))
      },
      viewModel = viewModel
    )
  } else {
    AdminStandaloneMainScreen(
      admin = loggedAdminUser!!,
      onLogout = {
        adminPrefs.edit().remove("logged_in_admin_id").apply()
        adminUserId = -1L
        loggedAdminUser = null
      },
      onOpenMainApp = {
        context.startActivity(Intent(context, MainActivity::class.java))
      },
      viewModel = viewModel
    )
  }
}

@Composable
fun AdminStandaloneLoginScreen(
  onLoginSuccess: (UserEntity) -> Unit,
  onOpenMainApp: () -> Unit,
  viewModel: AppViewModel
) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  val scope = rememberCoroutineScope()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(Color(0xFF090412), Color(0xFF140722), Color(0xFF1F082C))
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
      // Red & Gold Shield Icon
      Box(
        modifier = Modifier
          .size(88.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(AdminRed, Color(0xFF590D22))))
          .border(2.dp, AccentGold, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Default.AdminPanelSettings,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(46.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "بوابة الإدارة المستقلة",
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextPrimary
      )

      Text(
        text = "تطبيق إدارة منصة التواصل العربي والمشرفين",
        fontSize = 13.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(26.dp))

      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(DarkBorder, AdminRed.copy(alpha = 0.4f)))),
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
                .padding(10.dp)
            ) {
              Text(
                text = errorMessage ?: "",
                color = AdminRed,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
              )
            }
            Spacer(modifier = Modifier.height(14.dp))
          }

          Text("اسم المستخدم الإداري", color = TextSecondary, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("مثال: admin", color = TextMuted) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = AdminRed) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = AdminRed,
              unfocusedBorderColor = DarkBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("admin_login_username")
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text("كلمة المرور الإدارية", color = TextSecondary, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AdminRed) },
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
              focusedBorderColor = AdminRed,
              unfocusedBorderColor = DarkBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("admin_login_password")
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              if (username.isBlank() || password.isBlank()) {
                errorMessage = "يرجى إدخال اسم المستخدم وكلمة المرور"
                return@Button
              }
              isLoading = true
              errorMessage = null
              scope.launch {
                val res = viewModel.repository.login(username, password)
                isLoading = false
                res.onSuccess { u ->
                  if (u.role == "ADMIN") {
                    onLoginSuccess(u)
                  } else {
                    errorMessage = "هذا الحساب ليس لديه صلاحيات ADMIN الإدارية العليا"
                  }
                }.onFailure { err ->
                  errorMessage = err.message ?: "فشل تسجيل الدخول"
                }
              }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_admin_login_submit")
          ) {
            if (isLoading) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
              Text("تسجيل الدخول الإداري", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Quick fill shortcut
          TextButton(
            onClick = {
              username = "admin"
              password = "Admin1234"
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("ملء بيانات المدير المبدئي (admin / Admin1234)", color = PurplePrimaryLight, fontSize = 12.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedButton(
        onClick = onOpenMainApp,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("فتح تطبيق التواصل العربي للمستخدمين", fontSize = 13.sp)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStandaloneMainScreen(
  admin: UserEntity,
  onLogout: () -> Unit,
  onOpenMainApp: () -> Unit,
  viewModel: AppViewModel
) {
  var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(AdminRed),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("إدارة التواصل العربي", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text("المسؤول: ${admin.displayName}", color = AccentGoldLight, fontSize = 11.sp)
            }
          }
        },
        actions = {
          IconButton(onClick = onOpenMainApp) {
            Icon(Icons.Default.OpenInNew, contentDescription = "فتح تطبيق المستخدمين", tint = PurplePrimaryLight)
          }
          IconButton(onClick = onLogout) {
            Icon(Icons.Default.ExitToApp, contentDescription = "تسجيل الخروج", tint = AdminRed)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurfaceElevated)
      )
    },
    containerColor = DarkBg
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = DarkSurfaceElevated,
        contentColor = AdminRed,
        edgePadding = 12.dp
      ) {
        val tabs = listOf(
          AdminTab.DASHBOARD to "الرئيسية",
          AdminTab.REPORTS to "البلاغات",
          AdminTab.USERS to "المستخدمين",
          AdminTab.COMMUNITIES to "المجتمعات",
          AdminTab.POSTS to "المنشورات",
          AdminTab.AUDIT_LOGS to "سجل الإجراءات"
        )
        tabs.forEach { (tab, label) ->
          Tab(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            text = { Text(label, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
          )
        }
      }

      Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        when (selectedTab) {
          AdminTab.DASHBOARD -> AdminDashboardTab(viewModel, onNavigateTab = { selectedTab = it })
          AdminTab.REPORTS -> AdminReportsTab(viewModel)
          AdminTab.USERS -> AdminUsersTab(viewModel)
          AdminTab.COMMUNITIES -> AdminCommunitiesTab(viewModel)
          AdminTab.POSTS -> AdminPostsTab(viewModel)
          AdminTab.AUDIT_LOGS -> AdminAuditTab(viewModel)
        }
      }
    }
  }
}
