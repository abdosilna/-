package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val username: String,
  val email: String,
  val displayName: String,
  val passwordHash: String,
  val avatar: String = "👑",
  val bio: String = "عضو في منصة التواصل العربي",
  val age: Int = 22,
  val role: String = "MEMBER", // MEMBER, MODERATOR, ADMIN
  val points: Int = 100, // Welcome bonus points
  val activeBadge: String = "🌟",
  val activeTitle: String = "عضو نشط",
  val isBanned: Boolean = false,
  val banReason: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "communities")
data class CommunityEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val slug: String,
  val description: String,
  val iconEmoji: String = "🏛️",
  val gradientStart: Long = 0xFF7B2CBF,
  val gradientEnd: Long = 0xFF3C1361,
  val isPrivate: Boolean = false,
  val passcode: String = "",
  val creatorId: Long,
  val creatorName: String,
  val membersCount: Int = 1,
  val postsCount: Int = 0,
  val pinnedAnnouncement: String = "",
  val isRestricted: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_members")
data class CommunityMemberEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val communityId: Long,
  val userId: Long,
  val role: String = "MEMBER", // OWNER, MODERATOR, MEMBER
  val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_channels")
data class CommunityChannelEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val communityId: Long,
  val name: String,
  val topic: String,
  val isDefault: Boolean = false
)

@Entity(tableName = "posts")
data class PostEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val authorId: Long,
  val authorName: String,
  val authorAvatar: String,
  val authorRole: String = "MEMBER",
  val authorBadge: String = "🌟",
  val authorTitle: String = "",
  val communityId: Long? = null,
  val communityName: String? = null,
  val content: String,
  val mediaType: String = "NONE", // NONE, IMAGE
  val mediaRes: String = "",
  val likesCount: Int = 0,
  val commentsCount: Int = 0,
  val sharesCount: Int = 0,
  val reportCount: Int = 0,
  val isHidden: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "post_likes")
data class PostLikeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val postId: Long,
  val userId: Long
)

@Entity(tableName = "comments")
data class CommentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val postId: Long,
  val authorId: Long,
  val authorName: String,
  val authorAvatar: String,
  val authorRole: String = "MEMBER",
  val content: String,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val conversationId: String,
  val senderId: Long,
  val senderName: String,
  val senderAvatar: String,
  val senderRole: String = "MEMBER",
  val content: String,
  val mediaType: String = "TEXT", // TEXT, IMAGE, STICKER, VOICE
  val timestamp: Long = System.currentTimeMillis(),
  val isReported: Boolean = false
)

@Entity(tableName = "reports")
data class ReportEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val reporterId: Long,
  val reporterName: String,
  val targetType: String, // POST, MESSAGE, COMMUNITY, USER
  val targetId: Long,
  val targetSummary: String,
  val reason: String,
  val details: String = "",
  val status: String = "NEW", // NEW, IN_REVIEW, RESOLVED, REJECTED
  val adminDecision: String = "",
  val resolvedBy: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "store_items")
data class StoreItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val description: String,
  val type: String, // BADGE, TITLE, THEME
  val costPoints: Int,
  val iconOrText: String,
  val communityId: Long? = null
)

@Entity(tableName = "user_inventory")
data class UserInventoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long,
  val itemId: Long,
  val purchasedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_audit_logs")
data class AdminAuditLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val adminName: String,
  val action: String,
  val targetDescription: String,
  val notes: String = "",
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val description: String,
  val authorId: Long,
  val authorName: String,
  val category: String = "تطبيق ويب", // تطبيق ويب, تطبيق أندرويد, أداة برمجية, قالب Acode
  val version: String = "1.0.0",
  val fileSize: String = "1.2 MB",
  val downloadUrl: String = "",
  val projectContent: String = "",
  val downloadsCount: Int = 0,
  val isApproved: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
)

