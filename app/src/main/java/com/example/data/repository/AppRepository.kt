package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class AdminStats(
  val totalUsers: Int,
  val totalPosts: Int,
  val totalCommunities: Int,
  val pendingReports: Int
)

class AppRepository(context: Context) {
  private val db = AppDatabase.getDatabase(context)
  private val userDao = db.userDao()
  private val communityDao = db.communityDao()
  private val postDao = db.postDao()
  private val chatDao = db.chatDao()
  private val reportDao = db.reportDao()
  private val storeDao = db.storeDao()
  private val auditDao = db.auditDao()

  private val prefs: SharedPreferences =
    context.getSharedPreferences("arabic_social_session", Context.MODE_PRIVATE)

  private val _currentUserId = MutableStateFlow(prefs.getLong("logged_in_user_id", -1L))
  val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

  val currentUser: Flow<UserEntity?> = _currentUserId.flatMapLatest { id ->
    if (id > 0) userDao.getUserById(id) else flowOf(null)
  }

  suspend fun initializeDefaults() = withContext(Dispatchers.IO) {
    // 1. Seed store items if empty
    val initialItems = listOf(
      StoreItemEntity(name = "تاج التميز الذهبي", description = "وسام ملكي لامع يظهر بجانب اسمك", type = "BADGE", costPoints = 50, iconOrText = "👑"),
      StoreItemEntity(name = "شعلة الحماس", description = "وسام الأعضاء الأكثر نشاطاً في المنصة", type = "BADGE", costPoints = 30, iconOrText = "🔥"),
      StoreItemEntity(name = "وسام الماسة", description = "وسام فاخر للأعضاء المؤسسين", type = "BADGE", costPoints = 80, iconOrText = "💎"),
      StoreItemEntity(name = "فيلسوف المجتمع", description = "لقب مميز لأصحاب النقاشات والآراء العميقة", type = "TITLE", costPoints = 40, iconOrText = "فيلسوف المجتمع"),
      StoreItemEntity(name = "سفير المحبة", description = "لقب لأصحاب الأثر الطيب والتفاعل الإيجابي", type = "TITLE", costPoints = 25, iconOrText = "سفير المحبة"),
      StoreItemEntity(name = "صقر العرب", description = "لقب للمبدعين والرواد في المجتمعات", type = "TITLE", costPoints = 60, iconOrText = "صقر العرب")
    )
    storeDao.insertStoreItems(initialItems)

    // 2. Ensure an admin account exists for admin moderation, but NO dummy normal users
    val existingAdmin = userDao.getUserByUsername("admin")
    if (existingAdmin == null) {
      val admin = UserEntity(
        username = "admin",
        email = "admin@arabicsocial.app",
        displayName = "الإدارة العليا",
        passwordHash = hashPassword("Admin1234"),
        avatar = "🛡️",
        bio = "الحساب الرسمي لإدارة تطبيق التواصل العربي",
        role = "ADMIN",
        points = 9999,
        activeBadge = "⚡",
        activeTitle = "مسؤول النظام"
      )
      userDao.insertUser(admin)
    }
  }

  // --- Auth & Session ---

  private fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
  }

  suspend fun getUserByIdSync(id: Long): UserEntity? = withContext(Dispatchers.IO) {
    userDao.getUserByIdSync(id)
  }

  suspend fun login(identifier: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
    val cleanId = identifier.trim()
    val user = if (cleanId.contains("@")) {
      userDao.getUserByEmail(cleanId)
    } else {
      userDao.getUserByUsername(cleanId)
    }

    if (user == null) {
      return@withContext Result.failure(Exception("اسم المستخدم أو البريد الإلكتروني غير مسجل"))
    }

    if (user.passwordHash != hashPassword(password)) {
      return@withContext Result.failure(Exception("كلمة المرور غير صحيحة"))
    }

    if (user.isBanned) {
      return@withContext Result.failure(Exception("هذا الحساب محظور: ${user.banReason}"))
    }

    // Save session
    prefs.edit().putLong("logged_in_user_id", user.id).apply()
    _currentUserId.value = user.id
    Result.success(user)
  }

  suspend fun register(
    username: String,
    email: String,
    displayName: String,
    password: String,
    age: Int
  ): Result<UserEntity> = withContext(Dispatchers.IO) {
    val cleanUsername = username.trim().lowercase()
    val cleanEmail = email.trim().lowercase()

    if (cleanUsername.length < 3) {
      return@withContext Result.failure(Exception("اسم المستخدم يجب أن يتكون من 3 أحرف على الأقل"))
    }
    if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
      return@withContext Result.failure(Exception("يرجى إدخال بريد إلكتروني صحيح"))
    }
    if (password.length < 6) {
      return@withContext Result.failure(Exception("كلمة المرور يجب أن لا تقل عن 6 خانات"))
    }

    if (userDao.getUserByUsername(cleanUsername) != null) {
      return@withContext Result.failure(Exception("اسم المستخدم محجوز بالفعل"))
    }
    if (userDao.getUserByEmail(cleanEmail) != null) {
      return@withContext Result.failure(Exception("البريد الإلكتروني مسجل مسبقاً"))
    }

    val newUser = UserEntity(
      username = cleanUsername,
      email = cleanEmail,
      displayName = displayName.trim().ifEmpty { cleanUsername },
      passwordHash = hashPassword(password),
      avatar = "👤",
      bio = "عضو جديد في منصة التواصل العربي",
      age = if (age in 12..120) age else 20,
      role = "MEMBER",
      points = 100 // Welcome points
    )

    val id = userDao.insertUser(newUser)
    val savedUser = newUser.copy(id = id)

    // Save session immediately
    prefs.edit().putLong("logged_in_user_id", id).apply()
    _currentUserId.value = id

    Result.success(savedUser)
  }

  fun logout() {
    prefs.edit().remove("logged_in_user_id").apply()
    _currentUserId.value = -1L
  }

  suspend fun updateProfile(name: String, bio: String, avatar: String, age: Int): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("غير مسجل الدخول"))
    val user = userDao.getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))
    val updated = user.copy(
      displayName = name.trim().ifEmpty { user.displayName },
      bio = bio.trim(),
      avatar = avatar,
      age = age
    )
    userDao.updateUser(updated)
    Result.success(Unit)
  }

  suspend fun equipItem(itemValue: String, isTitle: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("غير مسجل الدخول"))
    val user = userDao.getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))
    val updated = if (isTitle) user.copy(activeTitle = itemValue) else user.copy(activeBadge = itemValue)
    userDao.updateUser(updated)
    Result.success(Unit)
  }

  // --- Posts & Feed ---

  fun getFeedPosts(): Flow<List<PostEntity>> = postDao.getAllVisiblePosts()

  fun getCommunityPosts(communityId: Long): Flow<List<PostEntity>> = postDao.getPostsForCommunity(communityId)

  suspend fun createPost(
    content: String,
    communityId: Long?,
    mediaType: String = "NONE",
    mediaRes: String = ""
  ): Result<Long> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("يرجى تسجيل الدخول أولاً"))
    val user = userDao.getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))

    var commName: String? = null
    if (communityId != null) {
      val comm = communityDao.getCommunityByIdSync(communityId)
      commName = comm?.name
    }

    val post = PostEntity(
      authorId = user.id,
      authorName = user.displayName,
      authorAvatar = user.avatar,
      authorRole = user.role,
      authorBadge = user.activeBadge,
      authorTitle = user.activeTitle,
      communityId = communityId,
      communityName = commName,
      content = content.trim(),
      mediaType = mediaType,
      mediaRes = mediaRes
    )

    val postId = postDao.insertPost(post)

    // Reward 10 points for creating a post
    userDao.updateUserPoints(user.id, user.points + 10)

    if (communityId != null) {
      val comm = communityDao.getCommunityByIdSync(communityId)
      if (comm != null) {
        communityDao.updateCommunity(comm.copy(postsCount = comm.postsCount + 1))
      }
    }

    Result.success(postId)
  }

  suspend fun isPostLiked(postId: Long): Boolean = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext false
    postDao.isPostLiked(postId, uid) > 0
  }

  suspend fun toggleLike(postId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("غير مسجل"))
    val isLiked = postDao.isPostLiked(postId, uid) > 0

    if (isLiked) {
      postDao.deleteLike(postId, uid)
    } else {
      postDao.insertLike(PostLikeEntity(postId = postId, userId = uid))
    }

    val count = postDao.getLikesCountForPost(postId)
    val post = postDao.getPostById(postId).first()
    if (post != null) {
      postDao.updatePost(post.copy(likesCount = count))
    }

    Result.success(!isLiked)
  }

  fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> = postDao.getCommentsForPost(postId)

  suspend fun addComment(postId: Long, text: String): Result<Long> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("غير مسجل"))
    val user = userDao.getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("غير مسجل"))

    val comment = CommentEntity(
      postId = postId,
      authorId = user.id,
      authorName = user.displayName,
      authorAvatar = user.avatar,
      authorRole = user.role,
      content = text.trim()
    )

    val commentId = postDao.insertComment(comment)

    val post = postDao.getPostById(postId).first()
    if (post != null) {
      postDao.updatePost(post.copy(commentsCount = post.commentsCount + 1))
    }

    // Reward 3 points for commenting
    userDao.updateUserPoints(user.id, user.points + 3)

    Result.success(commentId)
  }

  suspend fun reportTarget(
    targetType: String,
    targetId: Long,
    targetSummary: String,
    reason: String,
    details: String
  ): Result<Long> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("غير مسجل"))
    val user = userDao.getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("غير مسجل"))

    val report = ReportEntity(
      reporterId = user.id,
      reporterName = user.displayName,
      targetType = targetType,
      targetId = targetId,
      targetSummary = targetSummary,
      reason = reason,
      details = details,
      status = "NEW"
    )

    val repId = reportDao.insertReport(report)

    if (targetType == "POST") {
      val post = postDao.getPostById(targetId).first()
      if (post != null) {
        postDao.updatePost(post.copy(reportCount = post.reportCount + 1))
      }
    }

    Result.success(repId)
  }

  fun getMyReports(): Flow<List<ReportEntity>> {
    val uid = _currentUserId.value
    return if (uid > 0) reportDao.getReportsByReporter(uid) else flowOf(emptyList())
  }

  // --- Communities ---

  fun getAllCommunities(): Flow<List<CommunityEntity>> = communityDao.getAllCommunities()

  fun getCommunityById(id: Long): Flow<CommunityEntity?> = communityDao.getCommunityById(id)

  suspend fun createCommunity(
    name: String,
    description: String,
    iconEmoji: String,
    isPrivate: Boolean,
    passcode: String
  ): Result<Long> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("غير مسجل"))
    val user = userDao.getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("غير مسجل"))

    val slug = name.trim().replace("\\s+".toRegex(), "-")
    val community = CommunityEntity(
      name = name.trim(),
      slug = slug,
      description = description.trim(),
      iconEmoji = iconEmoji.ifEmpty { "🏛️" },
      isPrivate = isPrivate,
      passcode = passcode.trim(),
      creatorId = user.id,
      creatorName = user.displayName,
      membersCount = 1
    )

    val commId = communityDao.insertCommunity(community)

    // Add creator as OWNER
    communityDao.insertMember(
      CommunityMemberEntity(communityId = commId, userId = user.id, role = "OWNER")
    )

    // Default channels
    communityDao.insertChannel(CommunityChannelEntity(communityId = commId, name = "النقاش العام", topic = "غرفة الترحيب والنقاش العام بين الأعضاء", isDefault = true))
    communityDao.insertChannel(CommunityChannelEntity(communityId = commId, name = "الإعلانات الرسمية", topic = "أخبار وتحديثات المجتمع من المشرفين", isDefault = false))

    // Reward points
    userDao.updateUserPoints(user.id, user.points + 20)

    Result.success(commId)
  }

  suspend fun isCommunityMember(communityId: Long): Boolean = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext false
    communityDao.getMember(communityId, uid) != null
  }

  suspend fun getMemberRole(communityId: Long): String? = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext null
    communityDao.getMember(communityId, uid)?.role
  }

  suspend fun joinCommunity(communityId: Long, enteredPasscode: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("يرجى تسجيل الدخول"))
    val comm = communityDao.getCommunityByIdSync(communityId) ?: return@withContext Result.failure(Exception("المجتمع غير موجود"))

    if (comm.isPrivate && comm.passcode.isNotEmpty() && comm.passcode != enteredPasscode) {
      return@withContext Result.failure(Exception("رمز الانضمام غير صحيح للمجتمع الخاص"))
    }

    val existing = communityDao.getMember(communityId, uid)
    if (existing != null) {
      return@withContext Result.failure(Exception("أنت منضم بالفعل لهذا المجتمع"))
    }

    communityDao.insertMember(CommunityMemberEntity(communityId = communityId, userId = uid, role = "MEMBER"))
    communityDao.updateCommunity(comm.copy(membersCount = comm.membersCount + 1))

    Result.success(Unit)
  }

  suspend fun leaveCommunity(communityId: Long): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("غير مسجل"))
    val comm = communityDao.getCommunityByIdSync(communityId) ?: return@withContext Result.failure(Exception("المجتمع غير موجود"))

    communityDao.deleteMember(communityId, uid)
    val newCount = (comm.membersCount - 1).coerceAtLeast(0)
    communityDao.updateCommunity(comm.copy(membersCount = newCount))

    Result.success(Unit)
  }

  fun getCommunityChannels(communityId: Long): Flow<List<CommunityChannelEntity>> =
    communityDao.getChannelsForCommunity(communityId)

  suspend fun createCommunityChannel(communityId: Long, name: String, topic: String): Result<Long> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    val role = getMemberRole(communityId)
    if (role != "OWNER" && role != "MODERATOR") {
      return@withContext Result.failure(Exception("صلاحية غير كافية لإنشاء غرفة"))
    }
    val channelId = communityDao.insertChannel(
      CommunityChannelEntity(communityId = communityId, name = name.trim(), topic = topic.trim())
    )
    Result.success(channelId)
  }

  suspend fun updateCommunityAnnouncement(communityId: Long, text: String): Result<Unit> = withContext(Dispatchers.IO) {
    val comm = communityDao.getCommunityByIdSync(communityId) ?: return@withContext Result.failure(Exception("المجتمع غير موجود"))
    val role = getMemberRole(communityId)
    if (role != "OWNER" && role != "MODERATOR") {
      return@withContext Result.failure(Exception("صلاحية غير كافية لتعديل الإعلان"))
    }
    communityDao.updateCommunity(comm.copy(pinnedAnnouncement = text.trim()))
    Result.success(Unit)
  }

  // --- Chat & Messages ---

  fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
    chatDao.getMessagesForConversation(conversationId)

  suspend fun sendMessage(
    conversationId: String,
    content: String,
    mediaType: String = "TEXT"
  ): Result<Long> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("غير مسجل"))
    val user = userDao.getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("غير مسجل"))

    val msg = MessageEntity(
      conversationId = conversationId,
      senderId = user.id,
      senderName = user.displayName,
      senderAvatar = user.avatar,
      senderRole = user.role,
      content = content.trim(),
      mediaType = mediaType
    )

    val msgId = chatDao.insertMessage(msg)
    Result.success(msgId)
  }

  // --- Store & Inventory ---

  fun getStoreItems(): Flow<List<StoreItemEntity>> = storeDao.getAllStoreItems()

  fun getUserInventory(): Flow<List<UserInventoryEntity>> {
    val uid = _currentUserId.value
    return if (uid > 0) storeDao.getUserInventory(uid) else flowOf(emptyList())
  }

  suspend fun purchaseStoreItem(itemId: Long): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    if (uid <= 0) return@withContext Result.failure(Exception("يرجى تسجيل الدخول أولاً"))
    val user = userDao.getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))

    val items = storeDao.getAllStoreItems().first()
    val item = items.find { it.id == itemId } ?: return@withContext Result.failure(Exception("العنصر غير متوفر"))

    if (user.points < item.costPoints) {
      return@withContext Result.failure(Exception("رصيد نقاطك غير كافٍ. رصيدك الحالي: ${user.points} نقطة"))
    }

    // Check if already owned
    val inventory = storeDao.getUserInventory(uid).first()
    if (inventory.any { it.itemId == itemId }) {
      return@withContext Result.failure(Exception("أنت تمتلك هذا العنصر بالفعل!"))
    }

    // Deduct points & insert inventory
    userDao.updateUserPoints(uid, user.points - item.costPoints)
    storeDao.insertInventory(UserInventoryEntity(userId = uid, itemId = itemId))

    // Automatically equip
    if (item.type == "BADGE") {
      userDao.updateUser(user.copy(points = user.points - item.costPoints, activeBadge = item.iconOrText))
    } else if (item.type == "TITLE") {
      userDao.updateUser(user.copy(points = user.points - item.costPoints, activeTitle = item.iconOrText))
    }

    Result.success(Unit)
  }

  // --- Admin Portal ---

  fun getAdminStats(): Flow<AdminStats> = combine(
    userDao.getUserCount(),
    postDao.getPostCount(),
    communityDao.getCommunityCount(),
    reportDao.getPendingReportCount()
  ) { u, p, c, r ->
    AdminStats(totalUsers = u, totalPosts = p, totalCommunities = c, pendingReports = r)
  }

  fun getAllReports(): Flow<List<ReportEntity>> = reportDao.getAllReports()

  suspend fun resolveReport(
    reportId: Long,
    status: String, // RESOLVED, REJECTED, IN_REVIEW
    decision: String,
    actionTaken: String = ""
  ): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    val user = userDao.getUserByIdSync(uid)
    val adminName = user?.displayName ?: "المدير"

    val report = reportDao.getReportById(reportId) ?: return@withContext Result.failure(Exception("البلاغ غير موجود"))
    reportDao.updateReportStatus(reportId, status, decision, adminName, System.currentTimeMillis())

    auditDao.insertAuditLog(
      AdminAuditLogEntity(
        adminName = adminName,
        action = "معالجة بلاغ #$reportId ($status)",
        targetDescription = "بلاغ عن ${report.targetType} #${report.targetId}: ${report.targetSummary}",
        notes = "القرار: $decision | الإجراء: $actionTaken"
      )
    )

    Result.success(Unit)
  }

  fun getAllUsersAdmin(): Flow<List<UserEntity>> = userDao.getAllUsers()

  suspend fun setBanUser(userId: Long, isBanned: Boolean, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    val admin = userDao.getUserByIdSync(uid)
    val targetUser = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))

    userDao.updateUserBan(userId, isBanned, reason)

    auditDao.insertAuditLog(
      AdminAuditLogEntity(
        adminName = admin?.displayName ?: "المدير",
        action = if (isBanned) "حظر المستخدم" else "فك حظر المستخدم",
        targetDescription = "@${targetUser.username} (${targetUser.displayName})",
        notes = reason
      )
    )

    Result.success(Unit)
  }

  suspend fun setUserRole(userId: Long, newRole: String): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    val admin = userDao.getUserByIdSync(uid)
    val targetUser = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))

    userDao.updateUserRole(userId, newRole)

    auditDao.insertAuditLog(
      AdminAuditLogEntity(
        adminName = admin?.displayName ?: "المدير",
        action = "تغيير رتبة المستخدم إلى $newRole",
        targetDescription = "@${targetUser.username} (${targetUser.displayName})",
        notes = "تحديث الصلاحيات الإدارية"
      )
    )

    Result.success(Unit)
  }

  fun getAllCommunitiesAdmin(): Flow<List<CommunityEntity>> = communityDao.getAllCommunitiesAdmin()

  suspend fun setCommunityRestricted(communityId: Long, isRestricted: Boolean, reason: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    val admin = userDao.getUserByIdSync(uid)
    val comm = communityDao.getCommunityByIdSync(communityId) ?: return@withContext Result.failure(Exception("المجتمع غير موجود"))

    communityDao.updateCommunityRestriction(communityId, isRestricted)

    auditDao.insertAuditLog(
      AdminAuditLogEntity(
        adminName = admin?.displayName ?: "المدير",
        action = if (isRestricted) "تقييد/تعليق مجتمع" else "إلغاء تقييد مجتمع",
        targetDescription = comm.name,
        notes = reason
      )
    )

    Result.success(Unit)
  }

  suspend fun deleteCommunity(communityId: Long, reason: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    val admin = userDao.getUserByIdSync(uid)
    val comm = communityDao.getCommunityByIdSync(communityId) ?: return@withContext Result.failure(Exception("المجتمع غير موجود"))

    communityDao.deleteCommunity(communityId)

    auditDao.insertAuditLog(
      AdminAuditLogEntity(
        adminName = admin?.displayName ?: "المدير",
        action = "حذف مجتمع نهائياً",
        targetDescription = comm.name,
        notes = reason
      )
    )

    Result.success(Unit)
  }

  fun getAllPostsAdmin(): Flow<List<PostEntity>> = postDao.getAllPostsAdmin()

  suspend fun setPostHidden(postId: Long, isHidden: Boolean, reason: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    val admin = userDao.getUserByIdSync(uid)
    val post = postDao.getPostById(postId).first() ?: return@withContext Result.failure(Exception("المنشور غير موجود"))

    postDao.hidePost(postId, isHidden)

    auditDao.insertAuditLog(
      AdminAuditLogEntity(
        adminName = admin?.displayName ?: "المدير",
        action = if (isHidden) "إخفاء منشور مخالف" else "إعادة إظهار المنشور",
        targetDescription = "منشور #${post.id} بواسطة ${post.authorName}",
        notes = reason
      )
    )

    Result.success(Unit)
  }

  suspend fun deletePost(postId: Long, reason: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
    val uid = _currentUserId.value
    val admin = userDao.getUserByIdSync(uid)
    val post = postDao.getPostById(postId).first() ?: return@withContext Result.failure(Exception("المنشور غير موجود"))

    postDao.deletePost(postId)

    auditDao.insertAuditLog(
      AdminAuditLogEntity(
        adminName = admin?.displayName ?: "المدير",
        action = "حذف منشور نهائياً",
        targetDescription = "منشور #${post.id} بواسطة ${post.authorName}",
        notes = reason
      )
    )

    Result.success(Unit)
  }

  fun getAuditLogs(): Flow<List<AdminAuditLogEntity>> = auditDao.getAllAuditLogs()
}
