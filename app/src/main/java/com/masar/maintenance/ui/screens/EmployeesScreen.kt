package com.masar.maintenance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.masar.maintenance.data.Employee
import com.masar.maintenance.data.Net
import com.masar.maintenance.data.Outcome
import com.masar.maintenance.data.UploadFile
import com.masar.maintenance.ui.Labels
import com.masar.maintenance.ui.components.*
import com.masar.maintenance.ui.theme.*
import kotlinx.coroutines.launch

private val ROLE_OPTIONS = listOf(
    "office" to "موظف المكتب",
    "maintenance" to "قسم الصيانة",
    "purchasing" to "قسم المشتريات",
    "admin" to "الإدارة"
)

@Composable
fun EmployeesScreen(nav: NavController) {
    var query by remember { mutableStateOf("") }
    var reload by remember { mutableIntStateOf(0) }
    var editTarget by remember { mutableStateOf<Employee?>(null) }
    var showForm by remember { mutableStateOf(false) }

    MasarScaffold(title = "الموظفون والمستخدمون", onBack = { nav.popBackStack() }) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
                    MasarField(query, { query = it }, "ابحث بالاسم أو الكود أو اسم الدخول…")
                }
                RemoteContent(
                    reloadKey = "$query|$reload",
                    load = { Net.repo.employees(q = query) }
                ) { list ->
                    if (list.isEmpty()) {
                        EmptyBox("لا يوجد موظفون مطابقون", "∅")
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
                        ) {
                            items(list, key = { it.id }) { e ->
                                EmployeeCard(e) { editTarget = e; showForm = true }
                            }
                        }
                    }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { editTarget = null; showForm = true },
                containerColor = Red, contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("إضافة موظف") },
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
            )
        }
    }

    if (showForm) {
        EmployeeFormDialog(
            existing = editTarget,
            onDismiss = { showForm = false },
            onSaved = { showForm = false; reload++ }
        )
    }
}

@Composable
private fun EmployeeCard(e: Employee, onEdit: () -> Unit) {
    Surface(
        color = Panel, shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!e.photo.isNullOrBlank()) {
                RemoteImage(e.photo, modifier = Modifier.size(46.dp).clip(RoundedCornerShape(23.dp)), placeholder = "👤")
            } else {
                Avatar(e.name.take(1).ifBlank { "؟" }, size = 46)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(e.name, color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (e.status != "active") {
                        Spacer(Modifier.width(8.dp))
                        Badge("موقوف", RedStatus)
                    }
                }
                Text(
                    buildString {
                        append(e.roleLabel ?: Labels.role(e.role))
                        if (e.username.isNotBlank()) append(" · ${e.username}")
                    },
                    color = Muted, fontSize = 13.sp
                )
                if (e.iqamaDays != null) {
                    Spacer(Modifier.height(4.dp))
                    val txt = if (e.iqamaDays < 0) "الإقامة منتهية منذ ${-e.iqamaDays} يوم" else "الإقامة تنتهي خلال ${e.iqamaDays} يوم"
                    Badge(txt, Labels.daysColor(e.iqamaDays))
                }
            }
            Text("تعديل ›", color = Red, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmployeeFormDialog(existing: Employee?, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val scope = rememberCoroutineScope()
    val editing = existing != null

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var code by remember { mutableStateOf(existing?.employeeCode ?: "") }
    var username by remember { mutableStateOf(existing?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(existing?.role ?: "office") }
    var iqama by remember { mutableStateOf(existing?.iqamaExpiry ?: "") }
    var active by remember { mutableStateOf((existing?.status ?: "active") == "active") }
    var photo by remember { mutableStateOf<UploadFile?>(null) }

    var submitting by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!submitting && !deleting) onDismiss() },
        containerColor = Ink2,
        title = { Text(if (editing) "تعديل موظف" else "إضافة موظف", color = Txt) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (error != null) {
                    Text(error!!, color = RedStatus, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                }
                MasarField(name, { name = it }, "الاسم *")
                Spacer(Modifier.height(10.dp))
                MasarField(code, { code = it }, "كود الموظف")
                Spacer(Modifier.height(10.dp))
                MasarField(username, { username = it }, "اسم الدخول *")
                Spacer(Modifier.height(10.dp))
                MasarField(
                    password, { password = it },
                    if (editing) "كلمة المرور (اتركها فارغة لإبقائها)" else "كلمة المرور *",
                    password = true
                )
                Spacer(Modifier.height(10.dp))
                LabeledDropdown("الدور", ROLE_OPTIONS, role) { role = it }
                Spacer(Modifier.height(10.dp))
                MasarField(iqama, { iqama = it }, "انتهاء الإقامة (YYYY-MM-DD)")
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = active, onCheckedChange = { active = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Red, checkedTrackColor = Red.copy(alpha = 0.5f))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (active) "نشِط" else "موقوف", color = Txt)
                }
                Spacer(Modifier.height(12.dp))
                PhotoPickerField(
                    if (editing && !existing?.photo.isNullOrBlank()) "صورة الموظف (لتغييرها)" else "صورة الموظف",
                    photo, { photo = it }
                )
                if (editing) {
                    Spacer(Modifier.height(14.dp))
                    if (confirmDelete) {
                        Text("تأكيد حذف الموظف؟", color = RedStatus, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Row {
                            TextButton(
                                onClick = {
                                    deleting = true; error = null
                                    scope.launch {
                                        when (val r = Net.repo.deleteEmployee(existing!!.id)) {
                                            is Outcome.Ok -> { deleting = false; onSaved() }
                                            is Outcome.Err -> { deleting = false; error = r.message; confirmDelete = false }
                                        }
                                    }
                                }
                            ) { Text(if (deleting) "جارٍ الحذف…" else "نعم، احذف", color = RedStatus) }
                            TextButton(onClick = { confirmDelete = false }) { Text("تراجع", color = Muted) }
                        }
                    } else {
                        TextButton(onClick = { confirmDelete = true }) { Text("حذف الموظف", color = RedStatus) }
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButtonCompact(if (editing) "حفظ" else "إضافة", loading = submitting) {
                error = null
                when {
                    name.isBlank() -> error = "الاسم مطلوب"
                    username.isBlank() -> error = "اسم الدخول مطلوب"
                    !editing && password.isBlank() -> error = "كلمة المرور مطلوبة"
                    else -> {
                        submitting = true
                        val fields = buildMap<String, String?> {
                            put("action", if (editing) "update" else "create")
                            if (editing) put("id", existing!!.id.toString())
                            put("name", name)
                            put("employee_code", code)
                            put("username", username)
                            if (password.isNotBlank()) put("password", password)
                            put("role", role)
                            put("iqama_expiry", iqama)
                            put("status", if (active) "active" else "inactive")
                        }
                        scope.launch {
                            when (val r = Net.repo.saveEmployee(fields, photo)) {
                                is Outcome.Ok -> { submitting = false; onSaved() }
                                is Outcome.Err -> { submitting = false; error = r.message }
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting && !deleting) { Text("إلغاء", color = Muted) }
        }
    )
}
