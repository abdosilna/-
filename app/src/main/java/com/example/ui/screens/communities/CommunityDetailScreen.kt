package com.example.ui.screens.communities

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.example.data.model.CommunityEntity
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyState
import com.example.ui.screens.feed.PostCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(
  viewModel: AppViewModel,
  communityId: Long,
  currentUser: UserEntity?
) {
  BackHandler {
    viewModel.navigateTo(Screen.COMMUNITIES)
  }

  val community by viewModel.repository.getCommunityById(communityId).collectAsState(initial = null)
  val channels by viewModel.repository.getCommunityChannels(communityId).collectAsState(initial = emptyList())
  val posts by viewModel.repository.getCommunityPosts(communityId).collectAsState(initial = emptyList())

  var isMember by remember { mutableStateOf(false) }
  var userRoleInComm by remember { mutableStateOf<String?>(null) }
  var showAnnouncementEdit by remember { mutableStateOf(false) }
  var showCreateChannelDialog by remember { mutableStateOf(false) }

  LaunchedEffect(communityId, currentUser?.id) {
    isMember = viewModel.repository.isCommunityMember(communityId)
    userRoleInComm = viewModel.repository.getMemberRole(communityId)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(text = community?.name ?: "المجتمع", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.navigateTo(Screen.COMMUNITIES) },
            modifier = Modifier.testTag("btn_back_from_community")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
          }
        },
        actions = {
          IconButton(onClick = {
            viewModel.notify("تم نسخ رابط المجتمع: https://arabicsocial.app/c/$communityId")
          }) {
            Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = TextPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurfaceElevated)
      )
    },
    containerColor = DarkBg
  ) { innerPadding ->
    val comm = community
    if (comm == null) {
      Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PurplePrimary)
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Community Banner Card
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, PurpleDark)))
          ) {
            Column {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(110.dp)
                  .background(Brush.horizontalGradient(listOf(Color(comm.gradientStart), Color(comm.gradientEnd))))
                  .padding(16.dp),
                contentAlignment = Alignment.BottomStart
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(56.dp)
                      .clip(CircleShape)
                      .background(DarkSurfaceCard)
                      .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(text = comm.iconEmoji, fontSize = 28.sp)
                  }
                  Spacer(modifier = Modifier.width(14.dp))
                  Column {
                    Text(text = comm.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = "${comm.membersCount} عضو • ${comm.postsCount} منشور", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                  }
                }
              }

              Column(modifier = Modifier.padding(16.dp)) {
                Text(text = comm.description, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  if (userRoleInComm != null) {
                    Text(
                      text = "رتبتك: ${if (userRoleInComm == "OWNER") "مؤسس المجتمع 👑" else if (userRoleInComm == "MODERATOR") "مشرف 🛡️" else "عضو ✨"}",
                      color = AccentGoldLight,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                  } else {
                    Text(text = "غير منضم بعد", color = TextMuted, fontSize = 12.sp)
                  }

                  if (isMember) {
                    OutlinedButton(
                      onClick = {
                        viewModel.leaveCommunity(comm.id)
                        isMember = false
                      },
                      colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminRed),
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Text("مغادرة المجتمع", fontSize = 12.sp)
                    }
                  } else {
                    Button(
                      onClick = {
                        viewModel.joinCommunity(comm.id)
                        isMember = true
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Text("انضمام للمجتمع", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        }

        // Pinned Announcement
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, AccentGold.copy(alpha = 0.4f))))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.PushPin, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = "الإعلان المثبت", color = AccentGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (userRoleInComm == "OWNER" || userRoleInComm == "MODERATOR") {
                  TextButton(
                    onClick = { showAnnouncementEdit = true },
                    contentPadding = PaddingValues(0.dp)
                  ) {
                    Text(text = "تعديل الإعلان", color = PurplePrimaryLight, fontSize = 12.sp)
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = comm.pinnedAnnouncement.ifEmpty { "أهلاً وسهلاً بجميع الأعضاء! يُرجى الالتزام بقواعد الاحترام المتبادل والمشاركة الهادفة." },
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
              )
            }
          }
        }

        // Sub-Channels / Chat Rooms
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "غرف الحوار والمحادثات", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if (userRoleInComm == "OWNER" || userRoleInComm == "MODERATOR") {
              IconButton(onClick = { showCreateChannelDialog = true }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "إضافة غرفة", tint = PurplePrimaryLight)
              }
            }
          }
        }

        items(channels, key = { it.id }) { ch ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(DarkSurfaceElevated)
              .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
              .clickable {
                viewModel.openChatRoom(
                  convId = "comm_${comm.id}_ch_${ch.id}",
                  title = "${comm.name} • #${ch.name}"
                )
              }
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PurpleDark),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(text = "# ${ch.name}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(text = ch.topic, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
          }
        }

        // Community Posts Section
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "منشورات المجتمع (${posts.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (posts.isEmpty()) {
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
              modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
            ) {
              Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد منشورات في هذا المجتمع بعد. شارك أول منشور الآن!", color = TextMuted, fontSize = 13.sp)
              }
            }
          }
        } else {
          items(posts, key = { it.id }) { p ->
            PostCard(
              post = p,
              currentUserId = currentUser?.id ?: 0,
              onToggleLike = { viewModel.toggleLike(p.id) },
              onOpenComments = { },
              onReport = { },
              onShare = { }
            )
          }
        }
      }
    }
  }

  // Edit Announcement Dialog
  if (showAnnouncementEdit && community != null) {
    var newAnnounce by remember { mutableStateOf(community?.pinnedAnnouncement ?: "") }
    AlertDialog(
      onDismissRequest = { showAnnouncementEdit = false },
      containerColor = DarkSurfaceElevated,
      title = { Text("تعديل الإعلان المثبت", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        OutlinedTextField(
          value = newAnnounce,
          onValueChange = { newAnnounce = it },
          placeholder = { Text("اكتب إعلان المجتمع هنا...") },
          minLines = 3,
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateCommunityAnnouncement(communityId, newAnnounce)
            showAnnouncementEdit = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
          Text("حفظ التعديل")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAnnouncementEdit = false }) {
          Text("إلغاء", color = TextSecondary)
        }
      }
    )
  }

  // Create Channel Dialog
  if (showCreateChannelDialog) {
    var chName by remember { mutableStateOf("") }
    var chTopic by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showCreateChannelDialog = false },
      containerColor = DarkSurfaceElevated,
      title = { Text("إنشاء غرفة حوار جديدة", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          OutlinedTextField(
            value = chName,
            onValueChange = { chName = it },
            placeholder = { Text("اسم الغرفة (مثال: الأخبار التقنية)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = chTopic,
            onValueChange = { chTopic = it },
            placeholder = { Text("موضوع ونطاق الحوار في الغرفة") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (chName.isNotBlank()) {
              viewModel.createChannel(communityId, chName, chTopic)
              showCreateChannelDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
          Text("إنشاء الغرفة")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateChannelDialog = false }) {
          Text("إلغاء", color = TextSecondary)
        }
      }
    )
  }
}
