package com.masar.maintenance.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.masar.maintenance.data.Net
import com.masar.maintenance.ui.Labels
import com.masar.maintenance.ui.components.*
import com.masar.maintenance.ui.theme.*
import kotlinx.coroutines.launch

private data class HomeItem(val label: String, val route: String, val icon: String, val desc: String)

@Composable
fun HomeScreen(nav: NavController) {
    val scope = rememberCoroutineScope()
    val role = Net.session.userRole
    val name = Net.session.userName

    val items = remember(role) { buildItems(role) }

    MasarScaffold(
        title = "نظام مسار للصيانة",
        actions = {
            IconButton(onClick = {
                scope.launch {
                    Net.repo.logout()
                    nav.navigate("login") { popUpTo("home") { inclusive = true } }
                }
            }) { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "خروج", tint = Red) }
        }
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MasarCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val photo = Net.session.userPhoto
                        if (!photo.isNullOrBlank()) {
                            RemoteImage(
                                photo,
                                modifier = Modifier.size(44.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                            )
                        } else {
                            Avatar(name.take(1).ifBlank { "م" })
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(name.ifBlank { "مستخدم" }, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Txt)
                            Text(Labels.role(role), color = Muted)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            items(items) { it -> HomeRow(it) { nav.navigate(it.route) } }
        }
    }
}

@Composable
private fun HomeRow(item: HomeItem, onClick: () -> Unit) {
    Surface(
        color = Panel, shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(item.icon, fontSize = 24.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(item.label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Txt)
                Text(item.desc, color = Muted, fontSize = 13.sp)
            }
            Text("‹", color = Muted, fontSize = 22.sp)
        }
    }
}

private fun buildItems(role: String): List<HomeItem> {
    val list = mutableListOf<HomeItem>()
    list += HomeItem("متابعة الطلبات", "requests?scope=", "⇄", "كل طلبات الصيانة وحالتها")
    list += HomeItem("سجل السيارات", "cars", "⛍", "بحث وعرض السيارات")
    when (role) {
        "office" -> list += HomeItem("إنشاء طلب صيانة", "newRequest", "＋", "إدخال عطل سيارة وإسناده للصيانة")
        "maintenance" -> list += HomeItem("صيانة دورية", "periodic", "🛢", "غيار زيت/كفرات/بطارية — تُغلق من الصيانة")
        "purchasing" -> list += HomeItem("متابعة المشتريات", "requests?scope=purchasing_followup", "₪", "الطلبات المنتظرة للشراء")
        "admin" -> {
            list += HomeItem("إنشاء طلب صيانة", "newRequest", "＋", "إدخال عطل سيارة وإسناده للصيانة")
            list += HomeItem("لوحة المعلومات", "dashboard", "▣", "المؤشرات والتنبيهات")
            list += HomeItem("متابعة المشتريات", "requests?scope=purchasing_followup", "₪", "الطلبات المنتظرة للشراء")
            list += HomeItem("متابعة الموظفين", "staff", "☖", "طلبات كل موظف وتأخيره")
            list += HomeItem("الموظفون", "employees", "⛁", "إدارة حسابات الموظفين")
            list += HomeItem("الشركات المورّدة", "companies", "⌂", "شركات قطع الغيار")
        }
    }
    return list.distinctBy { it.route + it.label }
}
