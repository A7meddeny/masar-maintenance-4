package com.masar.maintenance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.masar.maintenance.data.Car
import com.masar.maintenance.data.Net
import com.masar.maintenance.data.Outcome
import com.masar.maintenance.data.UploadFile
import com.masar.maintenance.ui.components.*
import com.masar.maintenance.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CarFormScreen(nav: NavController, id: Int) {
    val editing = id > 0
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(editing) }
    var loadError by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var carCode by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var letters by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }
    var nextOil by remember { mutableStateOf("") }
    var regExpiry by remember { mutableStateOf("") }
    var insExpiry by remember { mutableStateOf("") }

    var photo by remember { mutableStateOf<UploadFile?>(null) }
    var platePhoto by remember { mutableStateOf<UploadFile?>(null) }
    var regPhoto by remember { mutableStateOf<UploadFile?>(null) }
    var current by remember { mutableStateOf<Car?>(null) }

    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        if (!editing) return@LaunchedEffect
        loading = true
        when (val r = Net.repo.car(id)) {
            is Outcome.Ok -> {
                val c = r.data
                current = c
                name = c.name
                carCode = c.carCode ?: ""
                model = c.model ?: ""
                letters = c.plateLetters ?: ""
                number = c.plateNumber ?: ""
                odometer = c.odometer?.toString() ?: ""
                nextOil = c.nextOilChangeKm?.toString() ?: ""
                regExpiry = c.registrationExpiry ?: ""
                insExpiry = c.insuranceExpiry ?: ""
            }
            is Outcome.Err -> loadError = r.message
        }
        loading = false
    }

    MasarScaffold(
        title = if (editing) "تعديل سيارة" else "إضافة سيارة",
        onBack = { nav.popBackStack() }
    ) { pad ->
        when {
            loading -> LoadingBox()
            loadError != null -> ErrorBox(loadError!!)
            else -> Column(
                Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                Surface(
                    color = Panel, shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("بيانات السيارة", color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        if (error != null) {
                            Text(error!!, color = RedStatus, fontSize = 13.sp)
                            Spacer(Modifier.height(10.dp))
                        }

                        MasarField(name, { name = it }, "اسم السيارة *")
                        Spacer(Modifier.height(10.dp))
                        MasarField(carCode, { carCode = it }, "كود السيارة")
                        Spacer(Modifier.height(10.dp))
                        MasarField(model, { model = it }, "الموديل / سنة الصنع")
                        Spacer(Modifier.height(10.dp))
                        Row {
                            MasarField(letters, { letters = it }, "حروف اللوحة", modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(10.dp))
                            MasarField(number, { number = it }, "رقم اللوحة *", keyboard = KeyboardType.Number, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        MasarField(odometer, { odometer = it }, "قراءة العداد (كم)", keyboard = KeyboardType.Number)
                        Spacer(Modifier.height(10.dp))
                        MasarField(nextOil, { nextOil = it }, "كم تغيير الزيت القادم", keyboard = KeyboardType.Number)
                        Spacer(Modifier.height(10.dp))
                        MasarField(regExpiry, { regExpiry = it }, "انتهاء الاستمارة (YYYY-MM-DD)")
                        Spacer(Modifier.height(10.dp))
                        MasarField(insExpiry, { insExpiry = it }, "انتهاء التأمين (YYYY-MM-DD)")
                        Spacer(Modifier.height(14.dp))

                        PhotoPickerField(
                            if (editing && !current?.photo.isNullOrBlank()) "صورة السيارة (لتغييرها)" else "صورة السيارة",
                            photo, { photo = it }
                        )
                        Spacer(Modifier.height(12.dp))
                        PhotoPickerField(
                            if (editing && !current?.platePhoto.isNullOrBlank()) "صورة اللوحة (لتغييرها)" else "صورة اللوحة",
                            platePhoto, { platePhoto = it }
                        )
                        Spacer(Modifier.height(12.dp))
                        PhotoPickerField(
                            if (editing && !current?.registrationPhoto.isNullOrBlank()) "صورة الاستمارة (لتغييرها)" else "صورة الاستمارة",
                            regPhoto, { regPhoto = it }
                        )
                        Spacer(Modifier.height(18.dp))

                        PrimaryButton(if (editing) "حفظ التعديلات" else "إضافة السيارة", loading = submitting) {
                            error = null
                            when {
                                name.isBlank() -> error = "اسم السيارة مطلوب"
                                number.isBlank() -> error = "رقم اللوحة مطلوب"
                                else -> {
                                    submitting = true
                                    val fields = buildMap<String, String?> {
                                        put("action", if (editing) "update" else "create")
                                        if (editing) put("id", id.toString())
                                        put("name", name)
                                        put("car_code", carCode)
                                        put("model", model)
                                        put("plate_letters", letters)
                                        put("plate_number", number)
                                        put("odometer", odometer.ifBlank { "0" })
                                        put("next_oil_change_km", nextOil)
                                        put("registration_expiry", regExpiry)
                                        put("insurance_expiry", insExpiry)
                                    }
                                    scope.launch {
                                        when (val r = Net.repo.saveCar(fields, photo, platePhoto, regPhoto)) {
                                            is Outcome.Ok -> { submitting = false; nav.popBackStack() }
                                            is Outcome.Err -> { submitting = false; error = r.message }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
