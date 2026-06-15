package com.masar.maintenance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.masar.maintenance.data.Car
import com.masar.maintenance.data.Net
import com.masar.maintenance.data.RequestDetail
import com.masar.maintenance.ui.Labels
import com.masar.maintenance.ui.components.*
import com.masar.maintenance.ui.theme.*

@Composable
fun CarHistoryScreen(nav: NavController, id: Int) {
    val role = Net.session.userRole

    MasarScaffold(
        title = "سجل السيارة",
        onBack = { nav.popBackStack() },
        actions = {
            if (role == "admin") {
                IconButton(onClick = { nav.navigate("carForm?id=$id") }) {
                    Icon(Icons.Filled.Edit, contentDescription = "تعديل", tint = Red)
                }
            }
        }
    ) { pad ->
        RemoteContent(
            reloadKey = id,
            load = { Net.repo.carHistory(id) }
        ) { (car, requests) ->
            Column(
                Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                if (car != null) CarInfoCard(car)

                SectionTitle("الطلبات (${requests.size})")
                if (requests.isEmpty()) {
                    EmptyBox("لا توجد طلبات على هذه السيارة", "∅")
                } else {
                    requests.forEach { r ->
                        HistoryRequestCard(r) { nav.navigate("request/${r.id}") }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CarInfoCard(c: Car) {
    MasarCard {
        if (!c.photo.isNullOrBlank()) {
            RemoteImage(
                c.photo,
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
                placeholder = "🚗"
            )
            Spacer(Modifier.height(12.dp))
        }
        Text(c.name, color = Txt, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(c.plateFull ?: "—", color = Muted, fontSize = 14.sp)

        Spacer(Modifier.height(10.dp))
        InfoRow("الكود", c.carCode ?: "—")
        InfoRow("الموديل", c.model ?: "—")
        InfoRow("العداد", c.odometer?.let { "%,d كم".format(it) } ?: "—")
        InfoRow("تغيير الزيت القادم", c.nextOilChangeKm?.let { "%,d كم".format(it) } ?: "—")

        Spacer(Modifier.height(8.dp))
        ExpiryRow("انتهاء الاستمارة", c.registrationExpiry, c.regDays)
        ExpiryRow("انتهاء التأمين", c.insuranceExpiry, c.insDays)

        if (!c.platePhoto.isNullOrBlank() || !c.registrationPhoto.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text("المستندات:", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!c.platePhoto.isNullOrBlank()) {
                    RemoteImage(
                        c.platePhoto,
                        modifier = Modifier.weight(1f).height(110.dp).clip(RoundedCornerShape(8.dp)),
                        placeholder = "🔖"
                    )
                }
                if (!c.registrationPhoto.isNullOrBlank()) {
                    RemoteImage(
                        c.registrationPhoto,
                        modifier = Modifier.weight(1f).height(110.dp).clip(RoundedCornerShape(8.dp)),
                        placeholder = "📄"
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text(label, color = Muted, fontSize = 13.sp, modifier = Modifier.width(150.dp))
        Text(value, color = Txt, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ExpiryRow(label: String, date: String?, days: Int?) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 13.sp, modifier = Modifier.width(150.dp))
        Text(date ?: "—", color = Txt, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        if (days != null) {
            Spacer(Modifier.width(8.dp))
            val txt = if (days < 0) "منتهٍ منذ ${-days} يوم" else "باقٍ $days يوم"
            Badge(txt, Labels.daysColor(days))
        }
    }
}

@Composable
private fun HistoryRequestCard(r: RequestDetail, onClick: () -> Unit) {
    Surface(
        color = Panel, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.serialNo, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Badge(r.statusLabel.ifBlank { Labels.status(r.status) }, Labels.statusColor(r.status))
            }
            if (!r.problemDescription.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(r.problemDescription, color = Txt, fontSize = 14.sp)
            }
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                val typeLabel = if (r.type == "periodic") "صيانة دورية" else "صيانة عامة"
                Text(typeLabel, color = Muted, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                if (r.items.isNotEmpty()) Text("${r.items.size} بند", color = Muted, fontSize = 12.sp)
                r.createdAt?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(it, color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}
