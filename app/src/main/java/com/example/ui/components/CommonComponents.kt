package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
  title: String,
  user: UserEntity?,
  onOpenDrawer: () -> Unit
) {
  TopAppBar(
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(PurplePrimary, PurpleDark))),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "ع", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = TextPrimary
        )
      }
    },
    navigationIcon = {
      IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("menu_drawer_button")) {
        Icon(Icons.Default.Menu, contentDescription = "القائمة الجانبية", tint = TextPrimary)
      }
    },
    actions = {
      user?.let {
        PointsBadge(points = it.points)
      }
      Spacer(modifier = Modifier.width(8.dp))
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = DarkSurfaceElevated
    )
  )
}

@Composable
fun AppBottomNavigation(
  currentScreen: Screen,
  onSelect: (Screen) -> Unit
) {
  NavigationBar(
    containerColor = DarkSurfaceElevated,
    tonalElevation = 8.dp,
    windowInsets = WindowInsets.navigationBars,
    modifier = Modifier.testTag("bottom_nav_bar")
  ) {
    val items = listOf(
      Triple(Screen.FEED, "الرئيسية", Icons.Default.Home to Icons.Outlined.Home),
      Triple(Screen.COMMUNITIES, "المجتمعات", Icons.Default.Groups to Icons.Outlined.Groups),
      Triple(Screen.CHAT_LIST, "المحادثات", Icons.AutoMirrored.Filled.Chat to Icons.AutoMirrored.Filled.Chat),
      Triple(Screen.STORE, "المتجر", Icons.Default.ShoppingBag to Icons.Outlined.ShoppingBag),
      Triple(Screen.PROFILE, "حسابي", Icons.Default.Person to Icons.Outlined.Person)
    )

    items.forEach { (screen, label, icons) ->
      val isSelected = currentScreen == screen
      NavigationBarItem(
        selected = isSelected,
        onClick = { onSelect(screen) },
        icon = {
          Icon(
            imageVector = if (isSelected) icons.first else icons.second,
            contentDescription = label
          )
        },
        label = {
          Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = Color.White,
          selectedTextColor = PurplePrimaryLight,
          indicatorColor = PurpleDark,
          unselectedIconColor = TextMuted,
          unselectedTextColor = TextMuted
        ),
        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
      )
    }
  }
}

@Composable
fun AppDrawerContent(
  user: UserEntity?,
  currentScreen: Screen,
  onNavigate: (Screen) -> Unit,
  onLogout: () -> Unit,
  onClose: () -> Unit
) {
  ModalDrawerSheet(
    drawerContainerColor = DarkSurface,
    modifier = Modifier.width(300.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.verticalGradient(listOf(PurpleDark, DarkSurfaceElevated)))
        .padding(top = 40.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(PurpleSurfaceGradient())
              .border(2.dp, PurplePrimaryLight, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(text = user?.avatar ?: "👤", fontSize = 28.sp)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = user?.displayName ?: "زائر",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "@${user?.username ?: "guest"}",
              color = TextSecondary,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              RoleBadge(role = user?.role ?: "MEMBER")
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = user?.activeBadge ?: "🌟", fontSize = 14.sp)
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceCard)
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "رصيد النقاط", color = TextSecondary, fontSize = 13.sp)
          PointsBadge(points = user?.points ?: 0)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Navigation items
    val navItems = mutableListOf(
      Triple(Screen.FEED, "الصفحة الرئيسية", Icons.Default.Home),
      Triple(Screen.COMMUNITIES, "تصفح المجتمعات", Icons.Default.Groups),
      Triple(Screen.CHAT_LIST, "صندوق المحادثات", Icons.AutoMirrored.Filled.Chat),
      Triple(Screen.STORE, "متجر الأوسمة والرتب", Icons.Default.ShoppingBag),
      Triple(Screen.MY_REPORTS, "بلاغاتي ومتابعتها", Icons.Default.ReportProblem),
      Triple(Screen.PROFILE, "الملف الشخصي", Icons.Default.Person),
      Triple(Screen.SETTINGS, "الإعدادات والخصوصية", Icons.Default.Settings)
    )

    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
      navItems.forEach { (screen, title, icon) ->
        val selected = currentScreen == screen
        NavigationDrawerItem(
          icon = { Icon(icon, contentDescription = title, tint = if (selected) PurplePrimaryLight else TextSecondary) },
          label = { Text(title, color = if (selected) Color.White else TextPrimary, fontSize = 14.sp) },
          selected = selected,
          onClick = {
            onNavigate(screen)
            onClose()
          },
          colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = DarkSurfaceCard,
            unselectedContainerColor = Color.Transparent
          ),
          modifier = Modifier.padding(vertical = 2.dp)
        )
      }
    }

    // Logout button
    Box(modifier = Modifier.padding(16.dp)) {
      Button(
        onClick = {
          onLogout()
          onClose()
        },
        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("drawer_logout_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "تسجيل الخروج", tint = AdminRed)
        Spacer(modifier = Modifier.width(8.dp))
        Text("تسجيل الخروج", color = AdminRed, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun RoleBadge(role: String) {
  val (bgColor, textColor, text) = when (role) {
    "ADMIN" -> Triple(AdminRed.copy(alpha = 0.2f), AdminRed, "إدارة")
    "MODERATOR" -> Triple(AccentCyan.copy(alpha = 0.2f), AccentCyan, "مشرف")
    else -> Triple(PurplePrimary.copy(alpha = 0.2f), PurplePrimaryLight, "عضو")
  }
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bgColor)
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(text = text, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
fun PointsBadge(points: Int) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(DarkSurfaceElevated)
      .border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
      .padding(horizontal = 10.dp, vertical = 4.dp)
  ) {
    Text(text = "🪙", fontSize = 12.sp)
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "$points",
      color = AccentGoldLight,
      fontWeight = FontWeight.Bold,
      fontSize = 12.sp
    )
  }
}

@Composable
fun EmptyState(
  icon: ImageVector,
  title: String,
  subtitle: String,
  actionText: String? = null,
  onAction: (() -> Unit)? = null
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(DarkSurfaceElevated)
          .border(1.dp, DarkBorder, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = PurplePrimaryLight, modifier = Modifier.size(36.dp))
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = title,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = subtitle,
        color = TextSecondary,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
      )
      if (actionText != null && onAction != null) {
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onAction,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
          Text(text = actionText, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun PurpleSurfaceGradient(): Brush {
  return Brush.linearGradient(
    colors = listOf(PurplePrimary, PurpleDark)
  )
}

@Composable
fun ReportModalDialog(
  targetTitle: String,
  onDismiss: () -> Unit,
  onSubmit: (reason: String, details: String) -> Unit
) {
  var selectedReason by remember { mutableStateOf("محتوى غير لائق") }
  var details by remember { mutableStateOf("") }
  val reasons = listOf("محتوى غير لائق", "سب أو تنمر", "احتيال أو سبام", "معلومات مضللة", "أخرى")

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceElevated,
    title = {
      Text(text = "الإبلاغ عن محتوى مخالف", color = TextPrimary, fontWeight = FontWeight.Bold)
    },
    text = {
      Column {
        Text(text = "البلاغ عن: $targetTitle", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "حدد سبب البلاغ:", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        reasons.forEach { r ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedReason = r }
              .padding(vertical = 4.dp)
          ) {
            RadioButton(
              selected = selectedReason == r,
              onClick = { selectedReason = r },
              colors = RadioButtonDefaults.colors(selectedColor = PurplePrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = r, color = TextPrimary, fontSize = 14.sp)
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = details,
          onValueChange = { details = it },
          label = { Text("تفاصيل إضافية (اختياري)", color = TextSecondary) },
          maxLines = 3,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = PurplePrimary,
            unfocusedBorderColor = DarkBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSubmit(selectedReason, details) },
        colors = ButtonDefaults.buttonColors(containerColor = AdminRed)
      ) {
        Text("إرسال البلاغ", color = Color.White)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء", color = TextSecondary)
      }
    }
  )
}
