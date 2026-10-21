package com.example.ui.screens.feed

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommentEntity
import com.example.data.model.PostEntity
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.ReportModalDialog
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: AppViewModel, currentUser: UserEntity?) {
  val posts by viewModel.feedPosts.collectAsState()
  val communities by viewModel.communities.collectAsState()

  var showCreateDialog by remember { mutableStateOf(false) }
  var activePostForComments by remember { mutableStateOf<PostEntity?>(null) }
  var postToReport by remember { mutableStateOf<PostEntity?>(null) }

  Scaffold(
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showCreateDialog = true },
        containerColor = PurplePrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_create_post")
      ) {
        Icon(Icons.Default.Add, contentDescription = "إنشاء منشور")
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "منشور جديد", fontWeight = FontWeight.Bold)
      }
    },
    containerColor = DarkBg
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (posts.isEmpty()) {
        EmptyState(
          icon = Icons.Default.DynamicFeed,
          title = "لا توجد منشورات بعد",
          subtitle = "كن أول من يشارك أفكاره وتجاربه مع أعضاء المجتمع العربي!",
          actionText = "إنشاء أول منشور (+10 نقاط)",
          onAction = { showCreateDialog = true }
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          item {
            // Welcome Greeting Bar
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
              border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, PurplePrimary.copy(alpha = 0.4f))))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceCard),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = currentUser?.avatar ?: "👋", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "مرحباً، ${currentUser?.displayName}!",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                  Text(
                    text = "شارك ما تفكر فيه مع المجتمع واكسب النقاط",
                    color = TextSecondary,
                    fontSize = 12.sp
                  )
                }
                Button(
                  onClick = { showCreateDialog = true },
                  colors = ButtonDefaults.buttonColors(containerColor = PurpleDark),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(text = "اكتب الآن", fontSize = 12.sp, color = TextPrimary)
                }
              }
            }
          }

          items(posts, key = { it.id }) { post ->
            PostCard(
              post = post,
              currentUserId = currentUser?.id ?: 0,
              onToggleLike = { viewModel.toggleLike(post.id) },
              onOpenComments = { activePostForComments = post },
              onReport = { postToReport = post },
              onShare = {
                viewModel.notify("تم نسخ رابط المنشور بنجاح للمشاركة")
              }
            )
          }
        }
      }
    }
  }

  // Create Post Dialog
  if (showCreateDialog) {
    CreatePostModalDialog(
      communities = communities,
      onDismiss = { showCreateDialog = false },
      onSubmit = { text, commId, mediaType ->
        viewModel.createPost(text, commId, mediaType) {
          showCreateDialog = false
        }
      }
    )
  }

  // Comments Bottom Sheet
  activePostForComments?.let { post ->
    CommentsBottomSheet(
      post = post,
      viewModel = viewModel,
      currentUser = currentUser,
      onDismiss = { activePostForComments = null }
    )
  }

  // Report Dialog
  postToReport?.let { post ->
    ReportModalDialog(
      targetTitle = "منشور بواسطة ${post.authorName}",
      onDismiss = { postToReport = null },
      onSubmit = { reason, details ->
        viewModel.reportPost(post.id, post.content.take(50), reason, details)
        postToReport = null
      }
    )
  }
}

@Composable
fun PostCard(
  post: PostEntity,
  currentUserId: Long,
  onToggleLike: () -> Unit,
  onOpenComments: () -> Unit,
  onReport: () -> Unit,
  onShare: () -> Unit
) {
  val timeFormatted = remember(post.createdAt) {
    val diff = System.currentTimeMillis() - post.createdAt
    when {
      diff < 60_000 -> "الآن"
      diff < 3600_000 -> "منذ ${diff / 60_000} د"
      diff < 86400_000 -> "منذ ${diff / 3600_000} س"
      else -> SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(post.createdAt))
    }
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(DarkBorder, Color(0xFF261447)))),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("post_card_${post.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Avatar, Name, Role, Community, Options
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(DarkSurfaceCard)
            .border(1.5.dp, PurplePrimaryLight, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(text = post.authorAvatar.ifEmpty { "👤" }, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = post.authorName,
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            RoleBadge(role = post.authorRole)
            if (post.authorBadge.isNotEmpty()) {
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = post.authorBadge, fontSize = 12.sp)
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = timeFormatted, color = TextMuted, fontSize = 11.sp)
            if (!post.communityName.isNullOrEmpty()) {
              Text(text = " • ", color = TextMuted, fontSize = 11.sp)
              Text(
                text = "مجتمع ${post.communityName}",
                color = PurplePrimaryLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        // Report Button
        IconButton(
          onClick = onReport,
          modifier = Modifier.size(32.dp).testTag("btn_report_post_${post.id}")
        ) {
          Icon(Icons.Outlined.Report, contentDescription = "إبلاغ", tint = TextMuted, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Post Content
      Text(
        text = post.content,
        color = TextPrimary,
        fontSize = 15.sp,
        lineHeight = 22.sp
      )

      // Media preview if any
      if (post.mediaType == "IMAGE") {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              Brush.linearGradient(
                listOf(Color(0xFF2C1350), Color(0xFF190933))
              )
            )
            .border(1.dp, PurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Image, contentDescription = null, tint = PurplePrimaryLight, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "صورة مرفقة بالمنشور", color = TextSecondary, fontSize = 13.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(6.dp))

      // Interaction Row: Likes, Comments, Share
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Like Button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggleLike() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("btn_like_post_${post.id}")
        ) {
          Icon(
            imageVector = if (post.likesCount > 0) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "إعجاب",
            tint = if (post.likesCount > 0) AccentRose else TextSecondary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${post.likesCount}",
            color = if (post.likesCount > 0) AccentRose else TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Comment Button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onOpenComments() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("btn_comments_post_${post.id}")
        ) {
          Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = "تعليق",
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${post.commentsCount}",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Share Button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onShare() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "مشاركة",
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "مشاركة",
            color = TextSecondary,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
fun CreatePostModalDialog(
  communities: List<com.example.data.model.CommunityEntity>,
  onDismiss: () -> Unit,
  onSubmit: (text: String, communityId: Long?, mediaType: String) -> Unit
) {
  var content by remember { mutableStateOf("") }
  var selectedCommunityId by remember { mutableStateOf<Long?>(null) }
  var attachImage by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceElevated,
    shape = RoundedCornerShape(20.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.EditNote, contentDescription = null, tint = PurplePrimaryLight)
        Spacer(modifier = Modifier.width(8.dp))
        Text("إنشاء منشور جديد", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          placeholder = { Text("ماذا يدور في ذهنك؟ اكتب هنا...", color = TextMuted) },
          minLines = 4,
          maxLines = 8,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = PurplePrimary,
            unfocusedBorderColor = DarkBorder
          ),
          modifier = Modifier.fillMaxWidth().testTag("input_create_post_content")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Community selector
        Text(text = "النشر في:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedCommunityId == null,
            onClick = { selectedCommunityId = null },
            label = { Text("الرئيسية (عام)", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PurplePrimary,
              selectedLabelColor = Color.White
            )
          )
          communities.take(3).forEach { comm ->
            FilterChip(
              selected = selectedCommunityId == comm.id,
              onClick = { selectedCommunityId = comm.id },
              label = { Text(comm.name, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PurplePrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image attachment toggle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceCard)
            .clickable { attachImage = !attachImage }
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = PurplePrimaryLight)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (attachImage) "تم إرفاق صورة (انقر للإلغاء)" else "إرفاق صورة مع المنشور",
            color = if (attachImage) AccentGoldLight else TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }

        if (attachImage) {
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF281144))
              .border(1.dp, PurplePrimary, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "معاينة: تم تجهيز الصورة للنشر ✓", color = AccentEmerald, fontSize = 12.sp)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (content.isNotBlank()) {
            onSubmit(content, selectedCommunityId, if (attachImage) "IMAGE" else "NONE")
          }
        },
        enabled = content.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("btn_submit_post")
      ) {
        Text("نشر (+10 نقاط)", color = Color.White, fontWeight = FontWeight.Bold)
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
fun CommentsBottomSheet(
  post: PostEntity,
  viewModel: AppViewModel,
  currentUser: UserEntity?,
  onDismiss: () -> Unit
) {
  val comments by viewModel.repository.getCommentsForPost(post.id).collectAsState(initial = emptyList())
  var newCommentText by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceElevated,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
      Text(
        text = "التعليقات (${comments.size})",
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
      )
      Spacer(modifier = Modifier.height(12.dp))

      if (comments.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "لا توجد تعليقات بعد. كن أول من يترك بصمته!", color = TextMuted, fontSize = 13.sp)
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .heightIn(max = 300.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(comments, key = { it.id }) { c ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceCard)
                .padding(10.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(PurpleDark),
                contentAlignment = Alignment.Center
              ) {
                Text(text = c.authorAvatar.ifEmpty { "👤" }, fontSize = 14.sp)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = c.authorName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  RoleBadge(role = c.authorRole)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = c.content, color = TextSecondary, fontSize = 13.sp)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Comment Input
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = newCommentText,
          onValueChange = { newCommentText = it },
          placeholder = { Text("أضف تعليقك هنا...", color = TextMuted, fontSize = 13.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = PurplePrimary,
            unfocusedBorderColor = DarkBorder
          ),
          modifier = Modifier.weight(1f).testTag("input_new_comment")
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
          onClick = {
            if (newCommentText.isNotBlank()) {
              viewModel.addComment(post.id, newCommentText) {
                newCommentText = ""
              }
            }
          },
          enabled = newCommentText.isNotBlank(),
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(PurplePrimary)
            .testTag("btn_send_comment")
        ) {
          Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.White)
        }
      }
    }
  }
}
