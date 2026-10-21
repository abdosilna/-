package com.example.ui.screens.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Report
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
import com.example.data.model.MessageEntity
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.ReportModalDialog
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatListScreen(viewModel: AppViewModel, currentUser: UserEntity?) {
  val communities by viewModel.communities.collectAsState()

  Scaffold(containerColor = DarkBg) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (communities.isEmpty()) {
        EmptyState(
          icon = Icons.AutoMirrored.Filled.Chat,
          title = "لا توجد محادثات نشطة",
          subtitle = "انضم إلى مجتمع للمشاركة في غرف الحوار العامة والنقاشات المباشرة!",
          actionText = "استكشاف المجتمعات",
          onAction = { viewModel.navigateTo(Screen.COMMUNITIES) }
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Text(
              text = "غرف الحوار والمحادثات",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "اختر الغرفة المناسبة وابدأ التواصل مع الأعضاء",
              color = TextSecondary,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          // List of community conversation rooms
          items(communities, key = { it.id }) { comm ->
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
              border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, PurpleDark.copy(alpha = 0.4f)))),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.openChatRoom(
                    convId = "comm_${comm.id}_main",
                    title = "غرفة: ${comm.name}"
                  )
                }
                .testTag("chat_room_item_${comm.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(PurpleDark),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = comm.iconEmoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = comm.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                  Text(
                    text = "غرفة النقاش العام • ${comm.membersCount} عضو",
                    color = TextSecondary,
                    fontSize = 12.sp
                  )
                }
                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
  viewModel: AppViewModel,
  convId: String,
  title: String,
  currentUser: UserEntity?
) {
  BackHandler {
    viewModel.navigateTo(Screen.CHAT_LIST)
  }

  val messages by viewModel.repository.getMessages(convId).collectAsState(initial = emptyList())
  val listState = rememberLazyListState()
  var inputMessage by remember { mutableStateOf("") }
  var messageToReport by remember { mutableStateOf<MessageEntity?>(null) }
  var showEmojiBar by remember { mutableStateOf(false) }

  val quickEmojis = listOf("👍", "❤️", "👏", "🔥", "✨", "😂", "🌹", "🎉")

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = "محادثة مشفرة وآمنة", color = AccentEmerald, fontSize = 11.sp)
          }
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.navigateTo(Screen.CHAT_LIST) },
            modifier = Modifier.testTag("btn_back_from_chat")
          ) {
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
    ) {
      // Messages List
      Box(modifier = Modifier.weight(1f)) {
        if (messages.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(CircleShape)
                  .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = PurplePrimaryLight, modifier = Modifier.size(28.dp))
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(text = "مرحباً بك في الغرفة!", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Text(text = "ابدأ المحادثة بإرسال أول رسالة أو تحية للأعضاء", color = TextSecondary, fontSize = 12.sp)
            }
          }
        } else {
          LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(messages, key = { it.id }) { msg ->
              val isMe = msg.senderId == (currentUser?.id ?: 0L)
              ChatMessageBubble(
                message = msg,
                isMe = isMe,
                onReport = { messageToReport = msg }
              )
            }
          }
        }
      }

      // Quick Emoji Selector
      if (showEmojiBar) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          quickEmojis.forEach { em ->
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkSurfaceCard)
                .clickable {
                  viewModel.sendMessage(convId, em, "STICKER")
                  showEmojiBar = false
                },
              contentAlignment = Alignment.Center
            ) {
              Text(text = em, fontSize = 18.sp)
            }
          }
        }
      }

      // Chat Input Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkSurfaceElevated)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { showEmojiBar = !showEmojiBar },
          modifier = Modifier.size(40.dp)
        ) {
          Icon(Icons.Default.SentimentSatisfiedAlt, contentDescription = "رموز تعبيرية", tint = if (showEmojiBar) AccentGold else TextSecondary)
        }

        OutlinedTextField(
          value = inputMessage,
          onValueChange = { inputMessage = it },
          placeholder = { Text("اكتب رسالتك...", color = TextMuted, fontSize = 13.sp) },
          maxLines = 3,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = PurplePrimary,
            unfocusedBorderColor = DarkBorder
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("input_chat_message")
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            if (inputMessage.isNotBlank()) {
              viewModel.sendMessage(convId, inputMessage, "TEXT")
              inputMessage = ""
            }
          },
          enabled = inputMessage.isNotBlank(),
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(PurplePrimary)
            .testTag("btn_send_chat_message")
        ) {
          Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.White, modifier = Modifier.size(20.dp))
        }
      }
    }
  }

  // Report message dialog
  messageToReport?.let { msg ->
    ReportModalDialog(
      targetTitle = "رسالة من ${msg.senderName}",
      onDismiss = { messageToReport = null },
      onSubmit = { reason, _ ->
        viewModel.reportMessage(msg.id, msg.senderName, msg.content, reason)
        messageToReport = null
      }
    )
  }
}

@Composable
fun ChatMessageBubble(
  message: MessageEntity,
  isMe: Boolean,
  onReport: () -> Unit
) {
  val timeFormatted = remember(message.timestamp) {
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Bottom
  ) {
    if (!isMe) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(DarkSurfaceCard),
        contentAlignment = Alignment.Center
      ) {
        Text(text = message.senderAvatar.ifEmpty { "👤" }, fontSize = 14.sp)
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Box(
      modifier = Modifier
        .widthIn(max = 280.dp)
        .clip(
          RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isMe) 16.dp else 4.dp,
            bottomEnd = if (isMe) 4.dp else 16.dp
          )
        )
        .background(
          if (isMe) Brush.linearGradient(listOf(PurplePrimary, PurpleDark))
          else Brush.linearGradient(listOf(DarkSurfaceCard, DarkSurfaceElevated))
        )
        .border(
          1.dp,
          if (isMe) PurplePrimaryLight.copy(alpha = 0.3f) else DarkBorder,
          RoundedCornerShape(16.dp)
        )
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Column {
        if (!isMe) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = message.senderName,
                color = AccentGoldLight,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
              Spacer(modifier = Modifier.width(4.dp))
              RoleBadge(role = message.senderRole)
            }
            IconButton(onClick = onReport, modifier = Modifier.size(18.dp)) {
              Icon(Icons.Outlined.Report, contentDescription = "إبلاغ", tint = TextMuted, modifier = Modifier.size(12.dp))
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }

        if (message.mediaType == "STICKER") {
          Text(text = message.content, fontSize = 32.sp)
        } else {
          Text(
            text = message.content,
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 18.sp
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = timeFormatted,
          color = Color.White.copy(alpha = 0.6f),
          fontSize = 10.sp,
          modifier = Modifier.align(if (isMe) Alignment.Start else Alignment.End)
        )
      }
    }
  }
}
