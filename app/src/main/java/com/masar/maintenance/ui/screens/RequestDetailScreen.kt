package com.masar.maintenance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.masar.maintenance.data.*
import com.masar.maintenance.ui.Labels
import com.masar.maintenance.ui.components.*
import com.masar.maintenance.ui.theme.*
import kotlinx.coroutines.launch

/** الدور المنفّذ لكل مرحلة (لإظهار زر الإجراء للمستخدم المناسب أو الإدارة). */
private fun actorRoleFor(status: String): String? = when (status) {
    "new_office", "maintenance_received" -> "maintenance"
    "sent_to_purchasing", "purchasing_received", "admin_approved", "purchasing_buying" -> "purchasing"
    "sent_to_admin" -> "admin"
    "part_delivered", "maintenance_fixing" -> "maintenance"
    else -> null
}

@Composable
fun RequestDetailScreen(nav: NavController, id: Int) {
    var reload by remember { mutableIntStateOf(0) }

    MasarScaffold(title = "تفاصيل الطلب", onBack = { nav.popBackStack() }) { pad ->
        RemoteContent(
            reloadKey = "$id|$reload",
            load = { Net.repo.requestDetail(id) }
        ) { d ->
            Column(
                Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                HeaderCard(d)

                if (d.items.isNotEmpty()) {
                    SectionTitle("البنود (${d.items.size})")
                    d.items.forEach { ItemCard(it) }
                }

                if (d.timeline.isNotEmpty()) {
                    SectionTitle("خط الزمن")
                    TimelineList(d.timeline)
                }

                Spacer(Modifier.height(18.dp))
                ActionZone(d) { reload++ }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HeaderCard(d: RequestDetail) {
    MasarCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(d.serialNo, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(d.car?.name ?: "—", color = Txt, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(d.car?.plateFull ?: "", color = Muted, fontSize = 13.sp)
            }
            Badge(d.statusLabel.ifBlank { Labels.status(d.status) }, Labels.statusColor(d.status))
        }
        // صورة السيارة (تسهّل على الموظف)
        if (!d.car?.photo.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            RemoteImage(d.car?.photo, modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(10.dp)))
        }
        // سبب رفض الإدارة (يظهر للمشتريات)
        if (!d.rejectReason.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Surface(color = RedStatus.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, RedStatus), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("رفض الإدارة:", color = RedStatus, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(d.rejectReason, color = Txt, fontSize = 13.sp)
                }
            }
        }
        if (!d.problemDescription.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text("العطل:", color = Muted, fontSize = 12.sp)
            Text(d.problemDescription, color = Txt, fontSize = 14.sp)
        }
        if (!d.problemPhoto.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text("صورة العطل (المكتب):", color = Muted, fontSize = 12.sp)
            RemoteImage(d.problemPhoto, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(10.dp)))
        }
        if (!d.purchasingPhoto.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text("صورة المشتريات:", color = Muted, fontSize = 12.sp)
            RemoteImage(d.purchasingPhoto, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(10.dp)))
        }
        if (!d.completionNote.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text("ملاحظة الإغلاق: ${d.completionNote}", color = Txt, fontSize = 13.sp)
        }
        if (!d.completionPhoto.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            RemoteImage(d.completionPhoto, modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(10.dp)))
        }
    }
}

@Composable
private fun ItemCard(it: RequestItem) {
    Spacer(Modifier.height(10.dp))
    Surface(
        color = Panel2, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(it.name, color = Txt, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (it.adminApproved == 1) {
                    Badge("معتمد", Green); Spacer(Modifier.width(6.dp))
                }
                val amount = it.invoiceAmount ?: it.price
                if (amount != null) {
                    Text("${fmtNum(amount)} ر.س", color = Txt, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else {
                    Badge("بلا سعر", Muted)
                }
            }
            if (!it.note.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(it.note, color = Muted, fontSize = 13.sp)
            }
            val extra = buildList {
                add(Labels.itemKind(it.itemKind))
                it.companyName?.let { c -> add("المورّد: $c") }
                it.supplierName?.let { c -> add("المورّد/البنشر: $c") }
                if (it.tireCount != null) add("إطارات: ${it.tireCount} (${Labels.tirePos(it.tirePosition)})")
                if (it.odometerNext != null) add("قادم: ${fmtNum(it.odometerNext.toDouble())} كم")
            }
            if (extra.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(extra.joinToString(" · "), color = Muted, fontSize = 12.sp)
            }
            // الفرع + رابط الموقع (تظهر للصيانة)
            val branchTxt = it.branchName ?: it.branchNote
            val mapUrl = it.branchMapsUrl ?: it.branchMapUrl
            if (!branchTxt.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                val ctx = androidx.compose.ui.platform.LocalContext.current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍 الفرع: $branchTxt", color = Txt, fontSize = 13.sp)
                    if (!mapUrl.isNullOrBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text("فتح الموقع", color = Blue, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                runCatching {
                                    ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(mapUrl)))
                                }
                            })
                    }
                }
            }
            if (!it.photo.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                RemoteImage(it.photo, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(10.dp)))
            }
            if (!it.donePhoto.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("صورة التنفيذ:", color = Muted, fontSize = 12.sp)
                RemoteImage(it.donePhoto, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(10.dp)))
            }
            if (!it.invoicePhoto.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                if (it.invoicePhoto!!.lowercase().endsWith(".pdf")) {
                    val ctx = androidx.compose.ui.platform.LocalContext.current
                    Text("📄 فتح الفاتورة", color = Blue, fontSize = 13.sp, modifier = Modifier.clickable {
                        runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(com.masar.maintenance.ui.imageUrl(it.invoicePhoto)))) }
                    })
                } else {
                    Text("الفاتورة:", color = Muted, fontSize = 12.sp)
                    RemoteImage(it.invoicePhoto, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(10.dp)))
                }
            }
            if (it.quotes.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("عروض الأسعار:", color = Muted, fontSize = 12.sp)
                it.quotes.forEach { qf ->
                    Spacer(Modifier.height(6.dp))
                    if (qf.fileType == "pdf") {
                        Text("📄 عرض${qf.companyName?.let { " — $it" } ?: ""}", color = Blue, fontSize = 13.sp)
                    } else {
                        RemoteImage(qf.filePath, modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp)))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineList(entries: List<TimelineEntry>) {
    Surface(
        color = Panel, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            entries.forEachIndexed { i, t ->
                if (i > 0) HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.padding(top = 5.dp).size(9.dp).clip(CircleShape).background(Red))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(Labels.stage(t.stage), color = Txt, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val meta = buildString {
                            append(t.actorName ?: "—")
                            t.actorRole?.let { append(" · ${Labels.role(it)}") }
                            t.createdAt?.let { append(" · $it") }
                        }
                        Text(meta, color = Muted, fontSize = 12.sp)
                        if (!t.note.isNullOrBlank()) {
                            Text(t.note, color = Muted, fontSize = 12.sp)
                        }
                        if (!t.photo.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            RemoteImage(t.photo, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)))
                        }
                    }
                }
            }
        }
    }
}

/** منطقة الإجراء حسب المرحلة والدور (تعكس renderActionZone في اللوحة). */
@Composable
private fun ActionZone(d: RequestDetail, onDone: () -> Unit) {
    val role = Net.session.userRole
    val actor = actorRoleFor(d.status)

    if (d.status == "completed") {
        Surface(
            color = Green.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(18.dp), contentAlignment = Alignment.Center) {
                Text("اكتمل الطلب وعادت السيارة للنظام", color = Green, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // الصيانة الدورية تُغلق من شاشة «صيانة دورية» في قسم الصيانة (لا تذهب للمشتريات)
    if (d.type == "periodic") {
        Surface(
            color = Panel, shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                Text("صيانة دورية قيد التنفيذ — تُغلق من شاشة «صيانة دورية» لدى موظف الصيانة.", color = Muted, fontSize = 13.sp)
            }
        }
        return
    }

    val canAct = role == "admin" || role == actor
    if (!canAct) {
        Surface(
            color = Panel, shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                Text("بانتظار إجراء: ${Labels.role(actor ?: "")}", color = Muted)
            }
        }
        return
    }

    when (d.status) {
        "new_office" -> SimpleActionButton("استلام السيارة (قسم الصيانة)", onDone) { Net.repo.maintenanceReceive(d.id) }
        "maintenance_received" -> ItemsForm(d.id, onDone)
        "sent_to_purchasing" -> SimpleActionButton("استلام الطلب (المشتريات)", onDone) { Net.repo.purchasingReceive(d.id) }
        "purchasing_received" -> PriceForm(d, onDone)
        "sent_to_admin" -> ApproveForm(d, onDone)
        "admin_approved" -> SimpleActionButton("بدء الشراء (المشتريات)", onDone) { Net.repo.purchasingBuy(d.id) }
        "purchasing_buying" -> DeliverForm(d, onDone)
        "part_delivered" -> SimpleActionButton("استلام القطعة وبدء الإصلاح", onDone) { Net.repo.maintenancePart(d.id) }
        "maintenance_fixing" -> CompleteForm(d.id, onDone)
    }
}

@Composable
private fun SimpleActionButton(label: String, onDone: () -> Unit, run: suspend () -> Outcome<*>) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(
        color = Panel, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            if (error != null) {
                Text(error!!, color = RedStatus, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
            }
            PrimaryButton(label, loading = loading) {
                error = null; loading = true
                scope.launch {
                    when (val r = run()) {
                        is Outcome.Ok -> { loading = false; onDone() }
                        is Outcome.Err -> { loading = false; error = r.message }
                    }
                }
            }
        }
    }
}

/** تنسيق رقم مع فواصل آلاف (يُستخدم في عدة شاشات). */
internal fun fmtNum(v: Double?): String {
    if (v == null) return "—"
    val l = v.toLong()
    return if (v == l.toDouble()) "%,d".format(l) else "%,.2f".format(v)
}
