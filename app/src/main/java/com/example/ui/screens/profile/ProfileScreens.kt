package com.example.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportEntity
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.PointsBadge
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(viewModel: AppViewModel, currentUser: UserEntity?) {
  var showEditModal by remember { mutableStateOf(false) }

  Scaffold(containerColor = DarkBg) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Profile Hero Card
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(DarkBorder, PurpleDark))),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(PurplePrimary, PurpleDark)))
              .border(3.dp, PurplePrimaryLight, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(text = currentUser?.avatar ?: "👤", fontSize = 42.sp)
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = currentUser?.displayName ?: "",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          Text(
            text = "@${currentUser?.username}",
            color = TextSecondary,
            fontSize = 13.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            RoleBadge(role = currentUser?.role ?: "MEMBER")
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceCard)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${currentUser?.activeBadge} ${currentUser?.activeTitle}",
                color = AccentGoldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Bio
          Text(
            text = currentUser?.bio ?: "",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = DarkBorder)
          Spacer(modifier = Modifier.height(16.dp))

          // Stats Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "${currentUser?.points ?: 0}", color = AccentGoldLight, fontWeight = FontWeight.Bold, fontSize = 18.sp)
              Text(text = "النقاط", color = TextSecondary, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "${currentUser?.age ?: 20}", color = PurplePrimaryLight, fontWeight = FontWeight.Bold, fontSize = 18.sp)
              Text(text = "العمر", color = TextSecondary, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "نشط ✓", color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 18.sp)
              Text(text = "الحالة", color = TextSecondary, fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Button(
            onClick = { showEditModal = true },
            colors = ButtonDefaults.buttonColors(containerColor = PurpleDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_edit_profile")
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "تعديل الملف الشخصي", fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Quick Links Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, Color(0xFF261447)))),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          ProfileMenuItem(
            icon = Icons.Default.ReportProblem,
            title = "بلاغاتي ومتابعتها",
            subtitle = "تتبع البلاغات التي أرسلتها وقرار الإدارة",
            onClick = { viewModel.navigateTo(Screen.MY_REPORTS) }
          )
          HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
          ProfileMenuItem(
            icon = Icons.Default.Settings,
            title = "الإعدادات العامة والخصوصية",
            subtitle = "المظهر، التنبيهات، واللغة",
            onClick = { viewModel.navigateTo(Screen.SETTINGS) }
          )
        }
      }
    }
  }

  // Edit Profile Modal
  if (showEditModal && currentUser != null) {
    EditProfileModal(
      user = currentUser,
      onDismiss = { showEditModal = false },
      onSubmit = { name, bio, avatar, age ->
        viewModel.updateProfile(name, bio, avatar, age)
        showEditModal = false
      }
    )
  }
}

@Composable
fun ProfileMenuItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  iconTint: Color = PurplePrimaryLight,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(DarkSurfaceCard),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
    }
    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
  }
}

@Composable
fun EditProfileModal(
  user: UserEntity,
  onDismiss: () -> Unit,
  onSubmit: (name: String, bio: String, avatar: String, age: Int) -> Unit
) {
  var name by remember { mutableStateOf(user.displayName) }
  var bio by remember { mutableStateOf(user.bio) }
  var avatar by remember { mutableStateOf(user.avatar) }
  var ageStr by remember { mutableStateOf(user.age.toString()) }

  val avatars = listOf("👑", "🌟", "🦁", "🦅", "⚡", "💎", "🏹", "🌙", "🛡️", "🚀")

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceElevated,
    shape = RoundedCornerShape(20.dp),
    title = { Text("تعديل البيانات الشخصية", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text("اختر صورتك الرمزية:", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          avatars.take(5).forEach { a ->
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (avatar == a) PurplePrimary else DarkSurfaceCard)
                .clickable { avatar = a },
              contentAlignment = Alignment.Center
            ) {
              Text(text = a, fontSize = 20.sp)
            }
          }
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text("الاسم الظاهر", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("النبذة التعريفية", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = bio,
          onValueChange = { bio = it },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("العمر", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = ageStr,
          onValueChange = { ageStr = it },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val ageInt = ageStr.toIntOrNull() ?: user.age
          onSubmit(name, bio, avatar, ageInt)
        },
        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
      ) {
        Text("حفظ التغييرات")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء", color = TextSecondary)
      }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(viewModel: AppViewModel) {
  BackHandler { viewModel.navigateTo(Screen.PROFILE) }

  val reports by viewModel.myReports.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("بلاغاتي ومتابعتها", color = TextPrimary, fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigateTo(Screen.PROFILE) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurfaceElevated)
      )
    },
    containerColor = DarkBg
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (reports.isEmpty()) {
        EmptyState(
          icon = Icons.Default.CheckCircle,
          title = "لا توجد بلاغات مرسلة",
          subtitle = "لم تقم بإرسال أي بلاغات بعد. شكراً لمساعدتنا في الحفاظ على مجتمع آمن ونظيف!"
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(reports, key = { it.id }) { rep ->
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
              border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, PurpleDark.copy(alpha = 0.3f)))),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = "بلاغ #${rep.id} (${rep.targetType})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  ReportStatusBadge(status = rep.status)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "السبب: ${rep.reason}", color = AccentGoldLight, fontSize = 13.sp)
                Text(text = "المحتوى: ${rep.targetSummary}", color = TextSecondary, fontSize = 12.sp)

                if (rep.adminDecision.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(DarkSurfaceCard)
                      .padding(8.dp)
                  ) {
                    Text(
                      text = "قرار الإدارة (${rep.resolvedBy}): ${rep.adminDecision}",
                      color = AccentEmerald,
                      fontSize = 11.sp
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ReportStatusBadge(status: String) {
  val (color, text) = when (status) {
    "RESOLVED" -> AccentEmerald to "تم الحل ✓"
    "REJECTED" -> AdminRed to "مرفوض ✕"
    "IN_REVIEW" -> AdminOrange to "قيد المراجعة"
    else -> AccentCyan to "جديد"
  }
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.15f))
      .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
  BackHandler { viewModel.navigateTo(Screen.PROFILE) }

  var notificationsEnabled by remember { mutableStateOf(true) }
  var autoPlayMedia by remember { mutableStateOf(false) }
  var showInfoDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("الإعدادات العامة", color = TextPrimary, fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigateTo(Screen.PROFILE) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
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
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Preferences
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("تفضيلات التطبيق", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("الإشعارات والتنبيهات", color = TextPrimary, fontSize = 13.sp)
              Text("تلقي إشعارات عند التفاعل أو التعليقات", color = TextSecondary, fontSize = 11.sp)
            }
            Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("التشغيل التلقائي للوسائط", color = TextPrimary, fontSize = 13.sp)
              Text("تحميل ومعاينة الصور والوسائط تلقائياً", color = TextSecondary, fontSize = 11.sp)
            }
            Switch(checked = autoPlayMedia, onCheckedChange = { autoPlayMedia = it })
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("اللغة الافتراضية", color = TextPrimary, fontSize = 13.sp)
              Text("اللغة العربية (RTL)", color = TextSecondary, fontSize = 11.sp)
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(PurpleDark)
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text("العربية", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Legal & Help
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          ProfileMenuItem(
            icon = Icons.Default.PrivacyTip,
            title = "سياسة الخصوصية",
            subtitle = "كيفية حماية وتشفير بيانات المستخدمين",
            onClick = {
              showInfoDialog = "سياسة الخصوصية" to "نلتزم بأعلى معايير حماية الخصوصية. جميع كلمات المرور مشفرة بتقنية SHA-256 ولا يتم مشاركة أي بيانات شخصية مع أطراف خارجية. نستخدم التخزين المحلي الآمن لحماية خصوصيتك التامة."
            }
          )
          HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
          ProfileMenuItem(
            icon = Icons.Default.Gavel,
            title = "شروط الاستخدام",
            subtitle = "قواعد النشر والسلوك المجتمعي",
            onClick = {
              showInfoDialog = "شروط الاستخدام" to "يُمنع منعاً باتاً نشر أي محتوى مسيء، تشهيري، أو مخالف للقيم والآداب العامة. يحق لإدارة المنصة حظر أي حساب مخالف أو حذف المنشورات المخالفة بناءً على تقارير المشرفين."
            }
          )
          HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
          ProfileMenuItem(
            icon = Icons.Default.Info,
            title = "عن تطبيق التواصل العربي",
            subtitle = "الإصدار 1.0 - منصة التواصل العربي",
            onClick = {
              showInfoDialog = "عن التطبيق" to "تطبيق التواصل العربي: بيئة عربية آمنة ومتكاملة للتواصل الهادف، بناء المجتمعات التخصصية، ونظام تقدير المكافآت والأوسمة. تم التطوير بأحدث تقنيات Jetpack Compose وتصميم داكن بنفسجي فاخر."
            }
          )
        }
      }
    }
  }

  showInfoDialog?.let { (title, content) ->
    AlertDialog(
      onDismissRequest = { showInfoDialog = null },
      containerColor = DarkSurfaceElevated,
      title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = { Text(content, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp) },
      confirmButton = {
        Button(onClick = { showInfoDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)) {
          Text("حسناً")
        }
      }
    )
  }
}
