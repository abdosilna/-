package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.AppTopBar
import com.example.ui.screens.admin.AdminPortalScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.chat.ChatConversationScreen
import com.example.ui.screens.chat.ChatListScreen
import com.example.ui.screens.communities.CommunitiesScreen
import com.example.ui.screens.communities.CommunityDetailScreen
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.profile.MyReportsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.SettingsScreen
import com.example.ui.screens.store.StoreScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        ArabicSocialApp()
      }
    }
  }
}

@Composable
fun ArabicSocialApp(viewModel: AppViewModel = viewModel()) {
  val isCheckingSession by viewModel.isSessionChecking.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val currentScreen by viewModel.currentScreen.collectAsState()
  val selectedCommunityId by viewModel.selectedCommunityId.collectAsState()
  val selectedConvId by viewModel.selectedConversationId.collectAsState()
  val selectedConvTitle by viewModel.selectedConversationTitle.collectAsState()

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // Listen for snackbar notifications
  LaunchedEffect(Unit) {
    viewModel.snackbarMessage.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  // 1. Session Restoration / Loading State
  if (isCheckingSession) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(DarkBg),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(PurplePrimary, PurpleDark))),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "ع", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
        CircularProgressIndicator(color = PurplePrimaryLight)
      }
    }
    return
  }

  // 2. Mandatory Authentication Gate
  if (currentUser == null) {
    AuthScreen(viewModel = viewModel)
    return
  }

  // 3. Main App Scaffold with Drawer & Bottom Navigation
  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      AppDrawerContent(
        user = currentUser,
        currentScreen = currentScreen,
        onNavigate = { screen -> viewModel.navigateTo(screen) },
        onLogout = { viewModel.logout() },
        onClose = { scope.launch { drawerState.close() } }
      )
    }
  ) {
    // Top bar title based on screen
    val topTitle = when (currentScreen) {
      Screen.FEED -> "التواصل العربي"
      Screen.COMMUNITIES -> "المجتمعات"
      Screen.COMMUNITY_DETAIL -> "تفاصيل المجتمع"
      Screen.CHAT_LIST -> "المحادثات"
      Screen.CHAT_ROOM -> selectedConvTitle
      Screen.STORE -> "متجر الأوسمة"
      Screen.PROFILE -> "الملف الشخصي"
      Screen.SETTINGS -> "الإعدادات"
      Screen.MY_REPORTS -> "بلاغاتي"
      else -> "التواصل العربي"
    }

    val showBottomBar = currentScreen in listOf(
      Screen.FEED,
      Screen.COMMUNITIES,
      Screen.CHAT_LIST,
      Screen.STORE,
      Screen.PROFILE
    )

    Scaffold(
      topBar = {
        if (currentScreen != Screen.COMMUNITY_DETAIL && currentScreen != Screen.CHAT_ROOM && currentScreen != Screen.MY_REPORTS && currentScreen != Screen.SETTINGS) {
          AppTopBar(
            title = topTitle,
            user = currentUser,
            onOpenDrawer = { scope.launch { drawerState.open() } }
          )
        }
      },
      bottomBar = {
        if (showBottomBar) {
          AppBottomNavigation(
            currentScreen = currentScreen,
            onSelect = { screen -> viewModel.navigateTo(screen) }
          )
        }
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      containerColor = DarkBg,
      contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (currentScreen) {
          Screen.FEED -> FeedScreen(viewModel = viewModel, currentUser = currentUser)
          Screen.COMMUNITIES -> CommunitiesScreen(viewModel = viewModel, currentUser = currentUser)
          Screen.COMMUNITY_DETAIL -> {
            selectedCommunityId?.let { id ->
              CommunityDetailScreen(viewModel = viewModel, communityId = id, currentUser = currentUser)
            } ?: run {
              FeedScreen(viewModel = viewModel, currentUser = currentUser)
            }
          }
          Screen.CHAT_LIST -> ChatListScreen(viewModel = viewModel, currentUser = currentUser)
          Screen.CHAT_ROOM -> {
            selectedConvId?.let { cid ->
              ChatConversationScreen(
                viewModel = viewModel,
                convId = cid,
                title = selectedConvTitle,
                currentUser = currentUser
              )
            } ?: run {
              ChatListScreen(viewModel = viewModel, currentUser = currentUser)
            }
          }
          Screen.STORE -> StoreScreen(viewModel = viewModel, currentUser = currentUser)
          Screen.PROFILE -> ProfileScreen(viewModel = viewModel, currentUser = currentUser)
          Screen.MY_REPORTS -> MyReportsScreen(viewModel = viewModel)
          Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
          else -> FeedScreen(viewModel = viewModel, currentUser = currentUser)
        }
      }
    }
  }
}
