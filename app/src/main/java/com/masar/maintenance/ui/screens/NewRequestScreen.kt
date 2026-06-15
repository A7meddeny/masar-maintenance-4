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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.masar.maintenance.data.Car
import com.masar.maintenance.data.Net
import com.masar.maintenance.data.Outcome
import com.masar.maintenance.data.User
import com.masar.maintenance.ui.components.*
import com.masar.maintenance.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NewRequestScreen(nav: NavController) {
    val role = Net.session.userRole
    val isPeriodic = role == "maintenance"

    MasarScaffold(
        title = if (isPeriodic) "صيانة دورية" else "طلب صيانة جديد",
        onBack = { nav.popBackStack() }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            if (isPeriodic) PeriodicForm(nav) else OfficeCreateForm(nav)
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ============ نموذج المكتب: إنشاء طلب صيانة ============ */
@Composable
private fun OfficeCreateForm(nav: NavController) {
    val scope = rememberCoroutineScope()

    var cars by remember { mutableStateOf<List<Car>>(emptyList()) }
    var staff by remember { mutableStateOf<List<User>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var loadingLists by remember { mutableStateOf(true) }

    var car by remember { mutableStateOf<Car?>(null) }
    var maintId by remember { mutableStateOf(0) }
    var desc by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<com.masar.maintenance.data.UploadFile?>(null) }

    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loadingLists = true
        val cr = Net.repo.cars(state = "available")
        val sr = Net.repo.staff("maintenance")
        when (cr) {
            is Outcome.Ok -> cars = cr.data
            is Outcome.Err -> loadError = cr.message
        }
        when (sr) {
            is Outcome.Ok -> staff = sr.data
            is Outcome.Err -> if (loadError == null) loadError = sr.message
        }
        loadingLists = false
    }

    if (loadingLists) { LoadingBox(); return }
    if (loadError != null) { ErrorBox(loadError!!); return }

    Surface(
        color = Panel, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("بيانات الطلب", color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            if (error != null) {
                Text(error!!, color = RedStatus, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
            }

            CarSelector(cars, car) { car = it }
            Spacer(Modifier.height(12.dp))

            if (staff.isEmpty()) {
                Text("لا يوجد موظفو صيانة مسجّلون بعد. أضِفهم من قسم الموظفين.", color = Yellow, fontSize = 13.sp)
            } else {
                LabeledDropdown(
                    "موظف الصيانة المسؤول",
                    listOf("0" to "— اختر الموظف —") + staff.map { it.id.toString() to it.name },
                    maintId.toString()
                ) { maintId = it.toInt() }
            }
            Spacer(Modifier.height(12.dp))

            MasarField(desc, { desc = it }, "وصف العطل", singleLine = false)
            Spacer(Modifier.height(12.dp))
            MasarField(odometer, { odometer = it }, "قراءة العداد عند التسليم (اختياري)", keyboard = KeyboardType.Number)
            Spacer(Modifier.height(12.dp))
            PhotoPickerField("صورة العطل (اختياري)", photo, { photo = it })
            Spacer(Modifier.height(16.dp))

            PrimaryButton("إنشاء الطلب", loading = submitting) {
                error = null
                when {
                    car == null -> error = "اختر السيارة أولاً"
                    maintId <= 0 -> error = "اختر موظف الصيانة المسؤول"
                    desc.isBlank() -> error = "اكتب وصف العطل"
                    else -> {
                        submitting = true
                        scope.launch {
                            when (val r = Net.repo.officeCreate(car!!.id, maintId, desc, odometer, photo)) {
                                is Outcome.Ok -> { submitting = false; nav.popBackStack() }
                                is Outcome.Err -> { submitting = false; error = r.message }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ============ نموذج الصيانة: بدء صيانة دورية ============ */
@Composable
private fun PeriodicForm(nav: NavController) {
    val scope = rememberCoroutineScope()

    var cars by remember { mutableStateOf<List<Car>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var loadingLists by remember { mutableStateOf(true) }

    var car by remember { mutableStateOf<Car?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loadingLists = true
        when (val cr = Net.repo.cars(state = "available")) {
            is Outcome.Ok -> cars = cr.data
            is Outcome.Err -> loadError = cr.message
        }
        loadingLists = false
    }

    if (loadingLists) { LoadingBox(); return }
    if (loadError != null) { ErrorBox(loadError!!); return }

    Surface(
        color = Panel, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("بدء صيانة دورية لسيارة", color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text("اختر السيارة وسيتم فتح طلب صيانة دورية باسمك مباشرةً.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(14.dp))

            if (error != null) {
                Text(error!!, color = RedStatus, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
            }

            CarSelector(cars, car) { car = it }
            Spacer(Modifier.height(16.dp))

            PrimaryButton("بدء الصيانة الدورية", loading = submitting) {
                error = null
                if (car == null) { error = "اختر السيارة أولاً"; return@PrimaryButton }
                submitting = true
                scope.launch {
                    when (val r = Net.repo.periodicSelf(car!!.id)) {
                        is Outcome.Ok -> { submitting = false; nav.popBackStack() }
                        is Outcome.Err -> { submitting = false; error = r.message }
                    }
                }
            }
        }
    }
}

/* ============ منتقي السيارة (بحث + قائمة) ============ */
@Composable
internal fun CarSelector(cars: List<Car>, selected: Car?, onSelect: (Car) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Column {
        Text("السيارة", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        GhostButton(
            text = selected?.let { "${it.name} — ${it.plateFull ?: ""}" } ?: "— اختر السيارة —",
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (open) {
        var q by remember { mutableStateOf("") }
        val filtered = remember(q, cars) {
            if (q.isBlank()) cars
            else cars.filter {
                it.name.contains(q, true) ||
                (it.plateFull ?: "").contains(q, true) ||
                (it.carCode ?: "").contains(q, true) ||
                it.id.toString().contains(q)
            }
        }
        AlertDialog(
            onDismissRequest = { open = false },
            containerColor = Ink2,
            title = { Text("اختر السيارة", color = Txt) },
            text = {
                Column {
                    MasarField(q, { q = it }, "ابحث بالاسم أو اللوحة أو الكود…")
                    Spacer(Modifier.height(10.dp))
                    if (filtered.isEmpty()) {
                        EmptyBox("لا توجد سيارات متاحة مطابقة", "∅")
                    } else {
                        LazyColumn(
                            Modifier.fillMaxWidth().heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered, key = { it.id }) { c ->
                                Surface(
                                    color = Panel, shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Line),
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        onSelect(c); open = false
                                    }
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(c.name, color = Txt, fontWeight = FontWeight.Bold)
                                        Text(
                                            buildString {
                                                append(c.plateFull ?: "—")
                                                c.carCode?.let { append(" · كود: $it") }
                                            },
                                            color = Muted, fontSize = 12.sp
                                        )
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
