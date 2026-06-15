package com.masar.maintenance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.masar.maintenance.data.*
import com.masar.maintenance.ui.components.*
import com.masar.maintenance.ui.theme.*
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

/* بند صيانة دورية (حالة قابلة للتعديل) */
private class PerItem {
    var kind by mutableStateOf("oil_change")
    var name by mutableStateOf("")
    var supplierId by mutableStateOf(0)
    var amount by mutableStateOf("")
    var nextKm by mutableStateOf("")
    var donePhoto by mutableStateOf<UploadFile?>(null)
    var invoicePhoto by mutableStateOf<UploadFile?>(null)
}

private val KIND_OPTIONS = listOf(
    "oil_change" to "غيار زيت",
    "tire_change" to "تغيير كفرات",
    "battery" to "تغيير بطارية",
    "other_periodic" to "بند آخر"
)

@Composable
fun PeriodicScreen(nav: NavController) {
    val scope = rememberCoroutineScope()

    var cars by remember { mutableStateOf<List<Car>>(emptyList()) }
    var suppliers by remember { mutableStateOf<List<Company>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    // حالة التدفّق
    var car by remember { mutableStateOf<Car?>(null) }
    var requestId by remember { mutableStateOf(0) }          // يُملأ بعد الفتح
    var opening by remember { mutableStateOf(false) }
    var showWarn by remember { mutableStateOf(false) }

    val items = remember { mutableStateListOf(PerItem()) }
    var odometerOut by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var closePhoto by remember { mutableStateOf<UploadFile?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        val cr = Net.repo.cars(state = "available")      // السيارات المتاحة للصيانة الدورية
        val sr = Net.repo.companies()
        when (cr) { is Outcome.Ok -> cars = cr.data; is Outcome.Err -> loadError = cr.message }
        when (sr) { is Outcome.Ok -> suppliers = sr.data; is Outcome.Err -> {} }
        loading = false
    }

    MasarScaffold(title = "صيانة دورية", onBack = { nav.popBackStack() }) { pad ->
        if (loading) { LoadingBox(); return@MasarScaffold }
        if (loadError != null) { ErrorBox(loadError!!); return@MasarScaffold }

        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            error?.let { Text(it, color = RedStatus, fontSize = 13.sp); Spacer(Modifier.height(10.dp)) }

            // (1) اختيار السيارة
            Surface(color = Panel, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("اختر السيارة", color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    PhotoCarSelector(cars, car, enabled = requestId == 0) { car = it }
                    if (requestId == 0 && car != null) {
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton("متابعة", loading = opening) { showWarn = true }
                    }
                    if (requestId != 0) {
                        Spacer(Modifier.height(8.dp))
                        Badge("السيارة محوّلة إلى حالة الصيانة الدورية", Yellow)
                    }
                }
            }

            // (2) البنود + الإغلاق (تظهر بعد فتح الطلب)
            if (requestId != 0) {
                Spacer(Modifier.height(16.dp))
                Text("بنود الصيانة الدورية", color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                items.forEachIndexed { i, it ->
                    PerItemCard(i, it, suppliers, canDelete = items.size > 1) { items.removeAt(i) }
                    Spacer(Modifier.height(10.dp))
                }
                GhostButton("＋ بند آخر", onClick = { items.add(PerItem()) }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(16.dp))
                Surface(color = Panel, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("إغلاق وإعادة السيارة", color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        MasarField(odometerOut, { odometerOut = it }, "قراءة العداد عند الإنهاء", keyboard = KeyboardType.Number)
                        Spacer(Modifier.height(12.dp))
                        MasarField(note, { note = it }, "ملاحظة (اختياري)", singleLine = false)
                        Spacer(Modifier.height(12.dp))
                        PhotoPickerField("صورة الإغلاق (اختياري)", closePhoto, { closePhoto = it })
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton("إغلاق وإعادة السيارة للنظام", loading = submitting) {
                            error = null
                            submitting = true
                            scope.launch {
                                val arr = JsonArray()
                                val parts = mutableListOf<MultipartBody.Part>()
                                items.forEachIndexed { i, it ->
                                    val o = JsonObject()
                                    o.addProperty("kind", it.kind)
                                    o.addProperty("name", it.name.ifBlank { KIND_OPTIONS.firstOrNull { k -> k.first == it.kind }?.second ?: "بند" })
                                    if (it.supplierId > 0) o.addProperty("supplier_id", it.supplierId)
                                    if (it.amount.isNotBlank()) o.addProperty("invoice_amount", it.amount)
                                    if (it.nextKm.isNotBlank()) o.addProperty("odometer_next", it.nextKm)
                                    arr.add(o)
                                    it.donePhoto?.let { uf -> parts.add(Net.repo.quotePart("done_photo_$i", uf)) }
                                    it.invoicePhoto?.let { uf -> parts.add(Net.repo.quotePart("invoice_photo_$i", uf)) }
                                }
                                closePhoto?.let { uf -> parts.add(Net.repo.quotePart("completion_photo", uf)) }
                                when (val r = Net.repo.periodicComplete(requestId, arr.toString(), odometerOut, note, parts)) {
                                    is Outcome.Ok -> { submitting = false; nav.popBackStack() }
                                    is Outcome.Err -> { submitting = false; error = r.message }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    // تحذير التحويل لحالة الصيانة الدورية
    if (showWarn && car != null) {
        AlertDialog(
            onDismissRequest = { showWarn = false },
            containerColor = Ink2,
            title = { Text("تنبيه", color = Txt) },
            text = { Text("سيتم تحويل السيارة «${car!!.name}» إلى حالة الصيانة الدورية في النظام. متابعة؟", color = Muted) },
            confirmButton = {
                TextButton(onClick = {
                    showWarn = false
                    opening = true
                    scope.launch {
                        when (val r = Net.repo.periodicSelf(car!!.id)) {
                            is Outcome.Ok -> { opening = false; requestId = r.data.get("id")?.asInt ?: 0 }
                            is Outcome.Err -> { opening = false; error = r.message }
                        }
                    }
                }) { Text("موافق", color = Red) }
            },
            dismissButton = { TextButton(onClick = { showWarn = false }) { Text("إلغاء", color = Muted) } }
        )
    }
}

/* بطاقة بند دوري واحد */
@Composable
private fun PerItemCard(index: Int, item: PerItem, suppliers: List<Company>, canDelete: Boolean, onDelete: () -> Unit) {
    Surface(color = Panel, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("بند #${index + 1}", color = Txt, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (canDelete) TextButton(onClick = onDelete) { Text("حذف", color = RedStatus) }
            }
            LabeledDropdown("النوع", KIND_OPTIONS, item.kind) { item.kind = it }
            Spacer(Modifier.height(10.dp))
            MasarField(item.name, { item.name = it }, "اسم/وصف البند (اختياري)")
            Spacer(Modifier.height(10.dp))
            LabeledDropdown(
                "المورّد / البنشر",
                listOf("0" to "— اختر المورّد —") + suppliers.map { it.id.toString() to it.name },
                item.supplierId.toString()
            ) { item.supplierId = it.toIntOrNull() ?: 0 }
            Spacer(Modifier.height(10.dp))
            Row {
                Box(Modifier.weight(1f)) {
                    MasarField(item.amount, { item.amount = it }, "مبلغ الفاتورة (ر.س)", keyboard = KeyboardType.Number)
                }
                if (item.kind == "oil_change" || item.kind == "tire_change") {
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f)) {
                        MasarField(item.nextKm, { item.nextKm = it }, "العداد القادم (كم)", keyboard = KeyboardType.Number)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            PhotoPickerField("صورة التنفيذ", item.donePhoto, { item.donePhoto = it })
            Spacer(Modifier.height(10.dp))
            PhotoPickerField("صورة الفاتورة (اختياري)", item.invoicePhoto, { item.invoicePhoto = it }, allowPdf = true)
        }
    }
}

/* منتقي سيارة يعرض الصور + بحث */
@Composable
private fun PhotoCarSelector(cars: List<Car>, selected: Car?, enabled: Boolean, onSelect: (Car) -> Unit) {
    var open by remember { mutableStateOf(false) }
    GhostButton(
        text = selected?.let { "${it.name} — ${it.plateFull ?: ""}" } ?: "— اختر السيارة —",
        onClick = { if (enabled) open = true },
        modifier = Modifier.fillMaxWidth()
    )
    if (open) {
        var q by remember { mutableStateOf("") }
        val filtered = remember(q, cars) {
            if (q.isBlank()) cars
            else cars.filter {
                it.name.contains(q, true) || (it.plateFull ?: "").contains(q, true) ||
                (it.carCode ?: "").contains(q, true) || it.id.toString().contains(q)
            }
        }
        AlertDialog(
            onDismissRequest = { open = false },
            containerColor = Ink2,
            title = { Text("اختر السيارة", color = Txt) },
            text = {
                Column {
                    MasarField(q, { q = it }, "ابحث بالاسم أو اللوحة…")
                    Spacer(Modifier.height(10.dp))
                    if (filtered.isEmpty()) EmptyBox("لا توجد سيارات متاحة", "∅")
                    else LazyColumn(Modifier.fillMaxWidth().heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered, key = { it.id }) { c ->
                            Surface(
                                color = Panel, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Line),
                                modifier = Modifier.fillMaxWidth().clickable { onSelect(c); open = false }
                            ) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RemoteImage(c.photo, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(c.name, color = Txt, fontWeight = FontWeight.Bold)
                                        Text(c.plateFull ?: "—", color = Muted, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { open = false }) { Text("إغلاق", color = Red) } }
        )
    }
}
