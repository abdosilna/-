package com.example.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.StoreItemEntity
import com.example.data.model.UserEntity
import com.example.ui.components.PointsBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@Composable
fun StoreScreen(viewModel: AppViewModel, currentUser: UserEntity?) {
  val items by viewModel.storeItems.collectAsState()
  val inventory by viewModel.userInventory.collectAsState()

  var selectedFilter by remember { mutableStateOf("ALL") }

  val filteredItems = remember(items, selectedFilter) {
    when (selectedFilter) {
      "BADGE" -> items.filter { it.type == "BADGE" }
      "TITLE" -> items.filter { it.type == "TITLE" }
      else -> items
    }
  }

  Scaffold(containerColor = DarkBg) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // User Points Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, AccentGold.copy(alpha = 0.5f))))
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = "رصيدك المتاح", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = "🪙", fontSize = 24.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "${currentUser?.points ?: 0} نقطة",
                    color = AccentGoldLight,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                  )
                }
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(PurpleDark)
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = "المتجر العربي",
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorder)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "💡 ملاحظة: النقاط ليست أموالاً حقيقية ولا تباع ولا تشترى؛ إنها نظام تقديري لنشاطك وتفاعلك الإيجابي داخل المنصة.",
              color = TextMuted,
              fontSize = 11.sp,
              lineHeight = 16.sp
            )
          }
        }
      }

      // Filter Chips
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedFilter == "ALL",
            onClick = { selectedFilter = "ALL" },
            label = { Text("جميع العناصر", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PurplePrimary, selectedLabelColor = Color.White)
          )
          FilterChip(
            selected = selectedFilter == "BADGE",
            onClick = { selectedFilter = "BADGE" },
            label = { Text("الأوسمة والشارات", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PurplePrimary, selectedLabelColor = Color.White)
          )
          FilterChip(
            selected = selectedFilter == "TITLE",
            onClick = { selectedFilter = "TITLE" },
            label = { Text("الألقاب التقديرية", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PurplePrimary, selectedLabelColor = Color.White)
          )
        }
      }

      // Items List
      items(filteredItems, key = { it.id }) { item ->
        val isOwned = inventory.any { it.itemId == item.id }
        val canAfford = (currentUser?.points ?: 0) >= item.costPoints

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, PurpleDark.copy(alpha = 0.4f)))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DarkSurfaceCard)
                .border(1.5.dp, if (isOwned) AccentEmerald else PurplePrimaryLight, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(text = if (item.type == "BADGE") item.iconOrText else "🏷️", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.name,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = item.description,
                color = TextSecondary,
                fontSize = 12.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                PointsBadge(points = item.costPoints)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (item.type == "BADGE") "وسام شخصي" else "لقب ظاهر",
                  color = TextMuted,
                  fontSize = 11.sp
                )
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isOwned) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(AccentEmerald.copy(alpha = 0.2f))
                  .border(1.dp, AccentEmerald, RoundedCornerShape(10.dp))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(text = "مملوك ✓", color = AccentEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            } else {
              Button(
                onClick = { viewModel.purchaseItem(item.id) },
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_buy_item_${item.id}")
              ) {
                Text(
                  text = if (canAfford) "شراء" else "نقاطك غير كافية",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}
