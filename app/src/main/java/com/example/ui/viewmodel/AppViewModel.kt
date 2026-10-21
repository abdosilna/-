package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AdminStats
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
  FEED,
  COMMUNITIES,
  COMMUNITY_DETAIL,
  CHAT_LIST,
  CHAT_ROOM,
  STORE,
  PROFILE,
  SETTINGS,
  MY_REPORTS,
  PROJECTS,
  ADMIN_DASHBOARD,
  ADMIN_REPORTS,
  ADMIN_USERS,
  ADMIN_COMMUNITIES,
  ADMIN_POSTS,
  ADMIN_AUDIT
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
  val repository = AppRepository(application)

  val isSessionChecking = MutableStateFlow(true)
  val currentUser: StateFlow<UserEntity?> = repository.currentUser.stateIn(
    viewModelScope,
    SharingStarted.Eagerly,
    null
  )

  val currentScreen = MutableStateFlow(Screen.FEED)
  val selectedCommunityId = MutableStateFlow<Long?>(null)
  val selectedConversationId = MutableStateFlow<String?>(null)
  val selectedConversationTitle = MutableStateFlow("المحادثة")

  val snackbarMessage = MutableSharedFlow<String>()

  // Data streams
  val feedPosts = repository.getFeedPosts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val communities = repository.getAllCommunities().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val storeItems = repository.getStoreItems().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val userInventory = repository.getUserInventory().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val myReports = repository.getMyReports().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


  // Admin streams
  val adminStats: StateFlow<AdminStats> = repository.getAdminStats().stateIn(
    viewModelScope,
    SharingStarted.Lazily,
    AdminStats(0, 0, 0, 0)
  )
  val adminReports = repository.getAllReports().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val adminUsers = repository.getAllUsersAdmin().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val adminCommunities = repository.getAllCommunitiesAdmin().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val adminPosts = repository.getAllPostsAdmin().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val adminAuditLogs = repository.getAuditLogs().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  init {
    viewModelScope.launch {
      repository.initializeDefaults()
      // Give short delay to let session restore smoothly
      kotlinx.coroutines.delay(150)
      isSessionChecking.value = false
    }
  }

  fun navigateTo(screen: Screen) {
    currentScreen.value = screen
  }

  fun openCommunity(id: Long) {
    selectedCommunityId.value = id
    currentScreen.value = Screen.COMMUNITY_DETAIL
  }

  fun openChatRoom(convId: String, title: String) {
    selectedConversationId.value = convId
    selectedConversationTitle.value = title
    currentScreen.value = Screen.CHAT_ROOM
  }

  fun notify(message: String) {
    viewModelScope.launch { snackbarMessage.emit(message) }
  }

  // --- Auth ---

  fun login(identifier: String, pass: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      val res = repository.login(identifier, pass)
      res.onSuccess {
        notify("أهلاً بك يا ${it.displayName}")
        onResult(true, null)
      }.onFailure {
        onResult(false, it.message ?: "فشل تسجيل الدخول")
      }
    }
  }

  fun register(
    username: String,
    email: String,
    displayName: String,
    pass: String,
    age: Int,
    onResult: (Boolean, String?) -> Unit
  ) {
    viewModelScope.launch {
      val res = repository.register(username, email, displayName, pass, age)
      res.onSuccess {
        notify("تم إنشاء حسابك بنجاح! حصلت على 100 نقطة ترحيبية 🎁")
        onResult(true, null)
      }.onFailure {
        onResult(false, it.message ?: "فشل إنشاء الحساب")
      }
    }
  }

  fun logout() {
    repository.logout()
    currentScreen.value = Screen.FEED
    notify("تم تسجيل الخروج بنجاح")
  }

  fun updateProfile(name: String, bio: String, avatar: String, age: Int) {
    viewModelScope.launch {
      val res = repository.updateProfile(name, bio, avatar, age)
      res.onSuccess { notify("تم تحديث الملف الشخصي بنجاح") }
        .onFailure { notify(it.message ?: "تعذر التحديث") }
    }
  }

  // --- Posts ---

  fun createPost(content: String, communityId: Long?, mediaType: String = "NONE", onComplete: () -> Unit) {
    viewModelScope.launch {
      val res = repository.createPost(content, communityId, mediaType)
      res.onSuccess {
        notify("تم نشر مشاركتك بنجاح (+10 نقاط) 🌟")
        onComplete()
      }.onFailure {
        notify(it.message ?: "تعذر النشر")
      }
    }
  }

  fun toggleLike(postId: Long) {
    viewModelScope.launch {
      repository.toggleLike(postId)
    }
  }

  fun addComment(postId: Long, content: String, onComplete: () -> Unit) {
    viewModelScope.launch {
      val res = repository.addComment(postId, content)
      res.onSuccess {
        notify("تمت إضافة تعليقك (+3 نقاط)")
        onComplete()
      }.onFailure {
        notify(it.message ?: "تعذر إضافة التعليق")
      }
    }
  }

  fun reportPost(postId: Long, postSummary: String, reason: String, details: String) {
    viewModelScope.launch {
      val res = repository.reportTarget("POST", postId, postSummary, reason, details)
      res.onSuccess { notify("شكراً لمساهمتك، تم إرسال البلاغ للإدارة للمراجعة.") }
        .onFailure { notify(it.message ?: "تعذر إرسال البلاغ") }
    }
  }

  // --- Communities ---

  fun createCommunity(
    name: String,
    description: String,
    iconEmoji: String,
    isPrivate: Boolean,
    passcode: String,
    onSuccess: (Long) -> Unit
  ) {
    viewModelScope.launch {
      val res = repository.createCommunity(name, description, iconEmoji, isPrivate, passcode)
      res.onSuccess { id ->
        notify("تم إنشاء المجتمع بنجاح! (+20 نقطة) 🎉")
        onSuccess(id)
      }.onFailure {
        notify(it.message ?: "تعذر إنشاء المجتمع")
      }
    }
  }

  fun joinCommunity(communityId: Long, passcode: String = "") {
    viewModelScope.launch {
      val res = repository.joinCommunity(communityId, passcode)
      res.onSuccess { notify("انضممت إلى المجتمع بنجاح!") }
        .onFailure { notify(it.message ?: "تعذر الانضمام") }
    }
  }

  fun leaveCommunity(communityId: Long) {
    viewModelScope.launch {
      val res = repository.leaveCommunity(communityId)
      res.onSuccess { notify("تمت مغادرة المجتمع") }
        .onFailure { notify(it.message ?: "تعذر الخروج") }
    }
  }

  fun updateCommunityAnnouncement(communityId: Long, announcement: String) {
    viewModelScope.launch {
      val res = repository.updateCommunityAnnouncement(communityId, announcement)
      res.onSuccess { notify("تم تحديث الإعلان المثبت") }
        .onFailure { notify(it.message ?: "تعذر التعديل") }
    }
  }

  fun createChannel(communityId: Long, name: String, topic: String) {
    viewModelScope.launch {
      val res = repository.createCommunityChannel(communityId, name, topic)
      res.onSuccess { notify("تم إنشاء الغرفة بنجاح") }
        .onFailure { notify(it.message ?: "تعذر إنشاء الغرفة") }
    }
  }

  // --- Chat ---

  fun sendMessage(convId: String, content: String, mediaType: String = "TEXT") {
    viewModelScope.launch {
      repository.sendMessage(convId, content, mediaType)
    }
  }

  fun reportMessage(msgId: Long, senderName: String, text: String, reason: String) {
    viewModelScope.launch {
      val res = repository.reportTarget("MESSAGE", msgId, "رسالة من $senderName: $text", reason, "")
      res.onSuccess { notify("تم إرسال بلاغ عن الرسالة بنجاح") }
    }
  }

  // --- Store ---

  fun purchaseItem(itemId: Long) {
    viewModelScope.launch {
      val res = repository.purchaseStoreItem(itemId)
      res.onSuccess { notify("تم شراء العنصر وتفعيله في ملفك بنجاح! 🎉") }
        .onFailure { notify(it.message ?: "فشلت عملية الشراء") }
    }
  }

  // --- Admin Moderation ---

  fun adminResolveReport(reportId: Long, status: String, decision: String, actionTaken: String) {
    viewModelScope.launch {
      val res = repository.resolveReport(reportId, status, decision, actionTaken)
      res.onSuccess { notify("تم تحديث حالة البلاغ وتسجيل الإجراء الإداري بنجاح") }
        .onFailure { notify(it.message ?: "تعذر التحديث") }
    }
  }

  fun adminBanUser(userId: Long, isBanned: Boolean, reason: String) {
    viewModelScope.launch {
      val res = repository.setBanUser(userId, isBanned, reason)
      res.onSuccess { notify(if (isBanned) "تم حظر المستخدم وتوثيقه في السجل" else "تم فك الحظر") }
        .onFailure { notify(it.message ?: "فشل الإجراء") }
    }
  }

  fun adminSetUserRole(userId: Long, role: String) {
    viewModelScope.launch {
      val res = repository.setUserRole(userId, role)
      res.onSuccess { notify("تم تحديث الرتبة إلى $role بنجاح") }
        .onFailure { notify(it.message ?: "فشل تحديث الرتبة") }
    }
  }

  fun adminRestrictCommunity(communityId: Long, isRestricted: Boolean, reason: String) {
    viewModelScope.launch {
      val res = repository.setCommunityRestricted(communityId, isRestricted, reason)
      res.onSuccess { notify(if (isRestricted) "تم تقييد المجتمع" else "تم إلغاء التقييد") }
    }
  }

  fun adminDeleteCommunity(communityId: Long, reason: String) {
    viewModelScope.launch {
      val res = repository.deleteCommunity(communityId, reason)
      res.onSuccess { notify("تم حذف المجتمع بنجاح") }
    }
  }

  fun adminHidePost(postId: Long, isHidden: Boolean, reason: String) {
    viewModelScope.launch {
      val res = repository.setPostHidden(postId, isHidden, reason)
      res.onSuccess { notify(if (isHidden) "تم إخفاء المنشور" else "تم إظهار المنشور") }
    }
  }

  fun adminDeletePost(postId: Long, reason: String) {
    viewModelScope.launch {
      val res = repository.deletePost(postId, reason)
      res.onSuccess { notify("تم حذف المنشور نهائياً") }
    }
  }

}
