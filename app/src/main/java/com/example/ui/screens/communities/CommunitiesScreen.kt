package com.example.ui.screens.communities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@Composable
fun CommunitiesScreen(viewModel: AppViewModel, currentUser: UserEntity?) {
  val communities by viewModel.communities.collectAsState()
  var showCreateModal by remember { mutableStateOf(false) }
  var communityToJoinWithPass by remember { mutableStateOf<CommunityEntity?>(null) }

  Scaffold(
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showCreateModal = true },
        containerColor = PurplePrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_create_community")
      ) {
        Icon(Icons.Default.GroupAdd, contentDescription = "إنشاء مجتمع")
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "إنشاء مجتمع جديد", fontWeight = FontWeight.Bold)
      }
    },
    containerColor = DarkBg
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (communities.isEmpty()) {
        EmptyState(
          icon = Icons.Default.Groups,
          title = "لا توجد مجتمعات بعد",
          subtitle = "كن رائداً وابدأ أول مجتمع عربي يجمع المهتمين بمجالك!",
          actionText = "إنشاء مجتمعك الأول (+20 نقطة)",
          onAction = { showCreateModal = true }
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          item {
            Text(
              text = "المجتمعات المتاحة (${communities.size})",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "استكشف المجتمعات، شارك في الغرف الحوارية، واكتسب أصدقاء جدد",
              color = TextSecondary,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          items(communities, key = { it.id }) { comm ->
            CommunityCardItem(
              community = comm,
              onOpen = { viewModel.openCommunity(comm.id) },
              onJoin = {
                if (comm.isPrivate && comm.passcode.isNotEmpty()) {
                  communityToJoinWithPass = comm
                } else {
                  viewModel.joinCommunity(comm.id)
                }
              }
            )
          }
        }
      }
    }
  }

  // Create Community Modal
  if (showCreateModal) {
    CreateCommunityModalDialog(
      onDismiss = { showCreateModal = false },
      onSubmit = { name, desc, emoji, isPrivate, pass ->
        viewModel.createCommunity(name, desc, emoji, isPrivate, pass) { newId ->
          showCreateModal = false
          viewModel.openCommunity(newId)
        }
      }
    )
  }

  // Join Private Community Passcode Dialog
  communityToJoinWithPass?.let { comm ->
    var enteredPass by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { communityToJoinWithPass = null },
      containerColor = DarkSurfaceElevated,
      title = { Text("مجتمع خاص: ${comm.name}", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("يتطلب هذا المجتمع رمز دخول أو دعوة للانضمام:", color = TextSecondary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = enteredPass,
            onValueChange = { enteredPass = it },
            placeholder = { Text("أدخل رمز المرور", color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.joinCommunity(comm.id, enteredPass)
            communityToJoinWithPass = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
          Text("انضمام")
        }
      },
      dismissButton = {
        TextButton(onClick = { communityToJoinWithPass = null }) {
          Text("إلغاء", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
fun CommunityCardItem(
  community: CommunityEntity,
  onOpen: () -> Unit,
  onJoin: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, PurpleDark.copy(alpha = 0.5f)))),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onOpen() }
      .testTag("community_item_${community.id}")
  ) {
    Column {
      // Community Header Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(70.dp)
          .background(Brush.horizontalGradient(listOf(Color(community.gradientStart), Color(community.gradientEnd))))
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(DarkSurfaceCard)
                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(text = community.iconEmoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = community.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = "بواسطة: ${community.creatorName}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
              )
            }
          }

          if (community.isPrivate) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "خاص", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Community Body & Stats
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = community.description,
          color = TextSecondary,
          fontSize = 13.sp,
          maxLines = 2,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.People, contentDescription = null, tint = PurplePrimaryLight, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "${community.membersCount} عضو", color = TextPrimary, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Article, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "${community.postsCount} منشور", color = TextPrimary, fontSize = 12.sp)
            }
          }

          Button(
            onClick = onOpen,
            colors = ButtonDefaults.buttonColors(containerColor = PurpleDark),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(text = "دخول المجتمع", fontSize = 12.sp, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun CreateCommunityModalDialog(
  onDismiss: () -> Unit,
  onSubmit: (name: String, desc: String, emoji: String, isPrivate: Boolean, pass: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }
  var emoji by remember { mutableStateOf("🏛️") }
  var isPrivate by remember { mutableStateOf(false) }
  var passcode by remember { mutableStateOf("") }

  val emojis = listOf("🏛️", "💻", "📚", "🎨", "⚽", "☕", "🚀", "🎮", "💡", "🌍")

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceElevated,
    shape = RoundedCornerShape(20.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.GroupAdd, contentDescription = null, tint = PurplePrimaryLight)
        Spacer(modifier = Modifier.width(8.dp))
        Text("تأسيس مجتمع جديد", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text("اسم المجتمع", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          placeholder = { Text("مثال: ملتقى التقنية والذكاء الاصطناعي", color = TextMuted) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_comm_name")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("نبذة ووصف المجتمع", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          placeholder = { Text("صف أهداف المجتمع والمواضيع المطروحة...", color = TextMuted) },
          minLines = 2,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth().testTag("input_comm_desc")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("أيقونة المجتمع الرمزية", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          emojis.take(6).forEach { e ->
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (emoji == e) PurplePrimary else DarkSurfaceCard)
                .border(1.dp, if (emoji == e) AccentGold else DarkBorder, CircleShape)
                .clickable { emoji = e },
              contentAlignment = Alignment.Center
            ) {
              Text(text = e, fontSize = 18.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Privacy switch
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceCard)
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "مجتمع خاص (يتطلب رمز دخول)", color = TextPrimary, fontSize = 13.sp)
          Switch(
            checked = isPrivate,
            onCheckedChange = { isPrivate = it },
            colors = SwitchDefaults.colors(checkedThumbColor = PurplePrimary)
          )
        }

        if (isPrivate) {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = passcode,
            onValueChange = { passcode = it },
            placeholder = { Text("رمز الدخول السري", color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && desc.isNotBlank()) {
            onSubmit(name, desc, emoji, isPrivate, passcode)
          }
        },
        enabled = name.isNotBlank() && desc.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("btn_confirm_create_comm")
      ) {
        Text("إنشاء المجتمع (+20 نقطة)", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء", color = TextSecondary)
      }
    }
  )
}
