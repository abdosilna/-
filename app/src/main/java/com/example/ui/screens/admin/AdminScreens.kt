package com.example.ui.screens.admin

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
import com.example.data.model.*
import com.example.ui.components.EmptyState
import com.example.ui.components.RoleBadge
import com.example.ui.screens.profile.ReportStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

enum class AdminTab {
  DASHBOARD,
  REPORTS,
  USERS,
  COMMUNITIES,
  POSTS,
  AUDIT_LOGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(viewModel: AppViewModel, currentUser: UserEntity?) {
  BackHandler {
    viewModel.navigateTo(Screen.FEED)
  }

  // Security Verification: If not admin, block access
  if (currentUser?.role != "ADMIN") {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(DarkBg)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Security, contentDescription = null, tint = AdminRed, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("منطقة إدارية محمية", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("يتطلب الدخول إلى لوحة الإدارة صلاحيات ADMIN العليا.", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = { viewModel.navigateTo(Screen.FEED) },
          colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
          Text("العودة إلى التطبيق الرئيسي")
        }
      }
    }
    return
  }

  var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AdminRed),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("لوحة الإدارة المستقلة", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text("صلاحية الإدارة العليا (${currentUser.displayName})", color = AdminRed, fontSize = 11.sp)
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.navigateTo(Screen.FEED) },
            modifier = Modifier.testTag("admin_back_to_app")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع للتطبيق", tint = TextPrimary)
          }
        },
        actions = {
          TextButton(onClick = { viewModel.logout() }) {
            Text("خروج", color = AdminRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
      // Horizontal Scrollable Tab Bar
      ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = DarkSurfaceElevated,
        contentColor = PurplePrimaryLight,
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

      // Tab Content
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

@Composable
fun AdminDashboardTab(viewModel: AppViewModel, onNavigateTab: (AdminTab) -> Unit) {
  val stats by viewModel.adminStats.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text("نظرة عامة على النظام", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

    // Metric Cards Grid
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      AdminMetricCard(
        title = "إجمالي الأعضاء",
        count = "${stats.totalUsers}",
        icon = Icons.Default.People,
        accentColor = PurplePrimaryLight,
        modifier = Modifier.weight(1f).clickable { onNavigateTab(AdminTab.USERS) }
      )
      AdminMetricCard(
        title = "المنشورات",
        count = "${stats.totalPosts}",
        icon = Icons.Default.Article,
        accentColor = AccentCyan,
        modifier = Modifier.weight(1f).clickable { onNavigateTab(AdminTab.POSTS) }
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      AdminMetricCard(
        title = "المجتمعات",
        count = "${stats.totalCommunities}",
        icon = Icons.Default.Groups,
        accentColor = AccentGold,
        modifier = Modifier.weight(1f).clickable { onNavigateTab(AdminTab.COMMUNITIES) }
      )
      AdminMetricCard(
        title = "بلاغات معلقة",
        count = "${stats.pendingReports}",
        icon = Icons.Default.ReportProblem,
        accentColor = if (stats.pendingReports > 0) AdminRed else AccentEmerald,
        modifier = Modifier.weight(1f).clickable { onNavigateTab(AdminTab.REPORTS) }
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Quick Admin Actions Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
      border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, AdminRed.copy(alpha = 0.3f)))),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("إجراءات الرقابة والإشراف السريع", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Button(
          onClick = { onNavigateTab(AdminTab.REPORTS) },
          colors = ButtonDefaults.buttonColors(containerColor = if (stats.pendingReports > 0) AdminRed else PurpleDark),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("مراجعة البلاغات النشطة (${stats.pendingReports})", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = { onNavigateTab(AdminTab.AUDIT_LOGS) },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.History, contentDescription = null, tint = PurplePrimaryLight, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("فحص سجل الإجراءات والقرارات", color = TextPrimary)
        }
      }
    }
  }
}

@Composable
fun AdminMetricCard(
  title: String,
  count: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, accentColor.copy(alpha = 0.3f)))),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, color = TextSecondary, fontSize = 12.sp)
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = count,
        color = Color.White,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp
      )
    }
  }
}

@Composable
fun AdminReportsTab(viewModel: AppViewModel) {
  val reports by viewModel.adminReports.collectAsState()
  var reportToModerate by remember { mutableStateOf<ReportEntity?>(null) }

  if (reports.isEmpty()) {
    EmptyState(
      icon = Icons.Default.CheckCircle,
      title = "لا توجد أي بلاغات حالياً",
      subtitle = "سجل البلاغات نظيف بالكامل. لا توجد مخالفات مسجلة."
    )
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(reports, key = { it.id }) { rep ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, PurpleDark.copy(alpha = 0.4f)))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "بلاغ #${rep.id} على [${rep.targetType}]",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              ReportStatusBadge(status = rep.status)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "مُرسل البلاغ: ${rep.reporterName}", color = TextSecondary, fontSize = 12.sp)
            Text(text = "السبب: ${rep.reason}", color = AccentGoldLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "المحتوى المبلغ عنه: ${rep.targetSummary}", color = TextPrimary, fontSize = 12.sp)
            if (rep.details.isNotEmpty()) {
              Text(text = "ملاحظات إضافية: ${rep.details}", color = TextMuted, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              Button(
                onClick = { reportToModerate = rep },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
              ) {
                Text("اتخاذ قرار إداري", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }

  // Moderate Report Dialog
  reportToModerate?.let { rep ->
    var decisionText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("RESOLVED") }

    AlertDialog(
      onDismissRequest = { reportToModerate = null },
      containerColor = DarkSurfaceElevated,
      title = { Text("معالجة البلاغ #${rep.id}", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("حدد قرار الإدارة النهائي:", color = TextSecondary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = selectedStatus == "RESOLVED",
              onClick = { selectedStatus = "RESOLVED" },
              label = { Text("حل البلاغ") },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentEmerald, selectedLabelColor = Color.White)
            )
            FilterChip(
              selected = selectedStatus == "REJECTED",
              onClick = { selectedStatus = "REJECTED" },
              label = { Text("رفض البلاغ") },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AdminRed, selectedLabelColor = Color.White)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = decisionText,
            onValueChange = { decisionText = it },
            placeholder = { Text("سبب القرار أو الإجراء المتخذ...", color = TextMuted) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.adminResolveReport(
              rep.id,
              selectedStatus,
              decisionText.ifEmpty { if (selectedStatus == "RESOLVED") "تمت معالجة المخالفة واتخاذ الإجراء اللازم" else "البلاغ لا يخالف المعايير المجتمعية" },
              actionTaken = "تحديث حالة البلاغ إلى $selectedStatus"
            )
            reportToModerate = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
          Text("حفظ القرار وتوثيقه")
        }
      },
      dismissButton = {
        TextButton(onClick = { reportToModerate = null }) {
          Text("إلغاء", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
fun AdminUsersTab(viewModel: AppViewModel) {
  val users by viewModel.adminUsers.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var userToEdit by remember { mutableStateOf<UserEntity?>(null) }

  val filteredUsers = remember(users, searchQuery) {
    if (searchQuery.isBlank()) users
    else users.filter { it.displayName.contains(searchQuery, true) || it.username.contains(searchQuery, true) || it.email.contains(searchQuery, true) }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("بحث عن مستخدم بالاسم أو المعرف...", color = TextMuted, fontSize = 13.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(filteredUsers, key = { it.id }) { u ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, if (u.isBanned) AdminRed.copy(alpha = 0.5f) else PurpleDark.copy(alpha = 0.3f)))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurfaceCard),
              contentAlignment = Alignment.Center
            ) {
              Text(text = u.avatar.ifEmpty { "👤" }, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = u.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                RoleBadge(role = u.role)
              }
              Text(text = "@${u.username} • ${u.email}", color = TextSecondary, fontSize = 11.sp)
              if (u.isBanned) {
                Text(text = "محظور: ${u.banReason}", color = AdminRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            Button(
              onClick = { userToEdit = u },
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text("إدارة الحساب", color = PurplePrimaryLight, fontSize = 11.sp)
            }
          }
        }
      }
    }
  }

  userToEdit?.let { u ->
    var banReason by remember { mutableStateOf(u.banReason) }
    AlertDialog(
      onDismissRequest = { userToEdit = null },
      containerColor = DarkSurfaceElevated,
      title = { Text("إدارة حساب: ${u.displayName}", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("تغيير الصلاحية / الرتبة:", color = TextSecondary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("MEMBER", "MODERATOR", "ADMIN").forEach { r ->
              FilterChip(
                selected = u.role == r,
                onClick = {
                  viewModel.adminSetUserRole(u.id, r)
                  userToEdit = null
                },
                label = { Text(r, fontSize = 11.sp) }
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = DarkBorder)
          Spacer(modifier = Modifier.height(14.dp))

          Text("حالة الحظر:", color = TextSecondary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(6.dp))
          if (u.isBanned) {
            Button(
              onClick = {
                viewModel.adminBanUser(u.id, false, "فك الحظر بقرار المدير")
                userToEdit = null
              },
              colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
            ) {
              Text("فك الحظر عن المستخدم")
            }
          } else {
            OutlinedTextField(
              value = banReason,
              onValueChange = { banReason = it },
              placeholder = { Text("سبب الحظر الموثق...") },
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = {
                viewModel.adminBanUser(u.id, true, banReason.ifEmpty { "مخالفة معايير المجتمع" })
                userToEdit = null
              },
              colors = ButtonDefaults.buttonColors(containerColor = AdminRed)
            ) {
              Text("تأكيد حظر المستخدم")
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { userToEdit = null }) {
          Text("إغلاق", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
fun AdminCommunitiesTab(viewModel: AppViewModel) {
  val communities by viewModel.adminCommunities.collectAsState()

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(communities, key = { it.id }) { comm ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, if (comm.isRestricted) AdminRed.copy(alpha = 0.4f) else PurpleDark.copy(alpha = 0.3f)))),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = comm.iconEmoji, fontSize = 20.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = comm.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            if (comm.isRestricted) {
              Text(text = "مقيد ✕", color = AdminRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "المؤسس: ${comm.creatorName} • ${comm.membersCount} عضو • ${comm.postsCount} منشور", color = TextSecondary, fontSize = 11.sp)
          Text(text = comm.description, color = TextMuted, fontSize = 12.sp, maxLines = 2)

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                viewModel.adminRestrictCommunity(comm.id, !comm.isRestricted, "قرار إداري")
              },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(if (comm.isRestricted) "إلغاء التقييد" else "تقييد المجتمع", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = {
                viewModel.adminDeleteCommunity(comm.id, "حذف المجتمع لمخالفة القوانين")
              },
              colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text("حذف نهائي", fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminPostsTab(viewModel: AppViewModel) {
  val posts by viewModel.adminPosts.collectAsState()

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(posts, key = { it.id }) { post ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "منشور #${post.id} بواسطة ${post.authorName}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            if (post.isHidden) {
              Text(text = "مخفي ✕", color = AdminOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = post.content, color = TextSecondary, fontSize = 13.sp, maxLines = 3)

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            OutlinedButton(
              onClick = { viewModel.adminHidePost(post.id, !post.isHidden, "مراجعة إدارية") },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(if (post.isHidden) "إظهار المنشور" else "إخفاء المنشور", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = { viewModel.adminDeletePost(post.id, "حذف نهائي لمخالفة المحتوى") },
              colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text("حذف", fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminAuditTab(viewModel: AppViewModel) {
  val logs by viewModel.adminAuditLogs.collectAsState()

  if (logs.isEmpty()) {
    EmptyState(
      icon = Icons.Default.History,
      title = "سجل الإجراءات فارغ",
      subtitle = "كل قرار إداري أو إجراء حظر أو معالجة بلاغ سيُوثق هنا آلياً."
    )
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(logs, key = { it.id }) { log ->
        val time = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, AdminRed.copy(alpha = 0.2f)))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = log.action, color = AccentGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(text = time, color = TextMuted, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "المسؤول: ${log.adminName}", color = TextSecondary, fontSize = 11.sp)
            Text(text = "الهدف: ${log.targetDescription}", color = TextPrimary, fontSize = 12.sp)
            if (log.notes.isNotEmpty()) {
              Text(text = "الملاحظات: ${log.notes}", color = TextMuted, fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}
