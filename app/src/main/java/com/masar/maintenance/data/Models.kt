package com.masar.maintenance.data

import com.google.gson.annotations.SerializedName

/* ===== المستخدم ===== */
data class User(
    val id: Int = 0,
    val name: String = "",
    val role: String = "",
    val permissions: String? = null,
    val photo: String? = null
)

/* ===== السيارة ===== */
data class Car(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("car_code") val carCode: String? = null,
    val model: String? = null,
    @SerializedName("plate_full") val plateFull: String? = null,
    @SerializedName("plate_letters") val plateLetters: String? = null,
    @SerializedName("plate_number") val plateNumber: String? = null,
    val odometer: Int? = null,
    @SerializedName("registration_expiry") val registrationExpiry: String? = null,
    @SerializedName("insurance_expiry") val insuranceExpiry: String? = null,
    @SerializedName("next_oil_change_km") val nextOilChangeKm: Int? = null,
    @SerializedName("next_tire_change_km") val nextTireChangeKm: Int? = null,
    @SerializedName("inspection_expiry") val inspectionExpiry: String? = null,
    @SerializedName("reg_days") val regDays: Int? = null,
    @SerializedName("ins_days") val insDays: Int? = null,
    @SerializedName("inspection_days") val inspectionDays: Int? = null,
    @SerializedName("oil_due") val oilDue: Int? = null,
    @SerializedName("tire_due") val tireDue: Int? = null,
    val photo: String? = null,
    @SerializedName("plate_photo") val platePhoto: String? = null,
    @SerializedName("registration_photo") val registrationPhoto: String? = null,
    val state: String = "available",
    val color: String? = null,
    @SerializedName("open_serial") val openSerial: String? = null
)

/* ===== الموظف ===== */
data class Employee(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("employee_code") val employeeCode: String? = null,
    val photo: String? = null,
    @SerializedName("iqama_expiry") val iqamaExpiry: String? = null,
    @SerializedName("iqama_days") val iqamaDays: Int? = null,
    val username: String = "",
    val role: String = "",
    @SerializedName("role_label") val roleLabel: String? = null,
    val permissions: String? = null,
    val status: String = "active"
)

/* ===== فرع الشركة ===== */
data class Branch(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("maps_url") val mapsUrl: String? = null
)

/* ===== الشركة ===== */
data class Company(
    val id: Int = 0,
    val name: String = "",
    val logo: String? = null,
    @SerializedName("contact_primary") val contactPrimary: String? = null,
    @SerializedName("contact_secondary") val contactSecondary: String? = null,
    @SerializedName("bank_details") val bankDetails: String? = null,
    val address: String? = null,
    @SerializedName("unified_number") val unifiedNumber: String? = null,
    val branches: List<Branch> = emptyList()
)

/* ===== الطلب (في القائمة) ===== */
data class RequestRow(
    val id: Int = 0,
    @SerializedName("serial_no") val serialNo: String = "",
    @SerializedName("car_id") val carId: Int = 0,
    val type: String = "general",
    val origin: String = "office",
    val status: String = "",
    @SerializedName("status_label") val statusLabel: String = "",
    @SerializedName("car_name") val carName: String? = null,
    @SerializedName("plate_full") val plateFull: String? = null,
    @SerializedName("car_photo") val carPhoto: String? = null,
    @SerializedName("maint_name") val maintName: String? = null,
    @SerializedName("purch_name") val purchName: String? = null,
    @SerializedName("office_name") val officeName: String? = null,
    @SerializedName("age_days") val ageDays: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

/* ===== ملف عرض السعر ===== */
data class QuoteFile(
    val id: Int = 0,
    @SerializedName("item_id") val itemId: Int = 0,
    @SerializedName("company_id") val companyId: Int? = null,
    @SerializedName("file_path") val filePath: String = "",
    @SerializedName("file_type") val fileType: String = "image",
    @SerializedName("company_name") val companyName: String? = null
)

/* ===== بند الطلب ===== */
data class RequestItem(
    val id: Int = 0,
    @SerializedName("request_id") val requestId: Int = 0,
    @SerializedName("item_kind") val itemKind: String = "general",
    val name: String = "",
    val note: String? = null,
    val photo: String? = null,
    @SerializedName("odometer_current") val odometerCurrent: Int? = null,
    @SerializedName("odometer_next") val odometerNext: Int? = null,
    @SerializedName("tire_count") val tireCount: Int? = null,
    @SerializedName("tire_position") val tirePosition: String? = null,
    @SerializedName("service_company") val serviceCompany: String? = null,
    val price: Double? = null,
    @SerializedName("company_id") val companyId: Int? = null,
    @SerializedName("company_name") val companyName: String? = null,
    @SerializedName("admin_approved") val adminApproved: Int = 0,
    @SerializedName("supplier_id") val supplierId: Int? = null,
    @SerializedName("supplier_name") val supplierName: String? = null,
    @SerializedName("branch_id") val branchId: Int? = null,
    @SerializedName("branch_name") val branchName: String? = null,
    @SerializedName("branch_maps_url") val branchMapsUrl: String? = null,
    @SerializedName("branch_note") val branchNote: String? = null,
    @SerializedName("branch_map_url") val branchMapUrl: String? = null,
    @SerializedName("invoice_amount") val invoiceAmount: Double? = null,
    @SerializedName("invoice_photo") val invoicePhoto: String? = null,
    @SerializedName("done_photo") val donePhoto: String? = null,
    val quotes: List<QuoteFile> = emptyList()
)

/* ===== عنصر خط الزمن ===== */
data class TimelineEntry(
    val id: Int = 0,
    val stage: String = "",
    @SerializedName("actor_name") val actorName: String? = null,
    @SerializedName("actor_role") val actorRole: String? = null,
    val note: String? = null,
    val photo: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

/* ===== سيارة مختصرة داخل تفاصيل الطلب ===== */
data class CarBrief(
    val id: Int = 0,
    val name: String? = null,
    @SerializedName("plate_full") val plateFull: String? = null,
    val photo: String? = null,
    val odometer: Int? = null
)

/* ===== تفاصيل الطلب الكاملة ===== */
data class RequestDetail(
    val id: Int = 0,
    @SerializedName("serial_no") val serialNo: String = "",
    @SerializedName("car_id") val carId: Int = 0,
    val type: String = "general",
    val status: String = "",
    @SerializedName("status_label") val statusLabel: String = "",
    @SerializedName("problem_description") val problemDescription: String? = null,
    @SerializedName("problem_photo") val problemPhoto: String? = null,
    @SerializedName("purchasing_photo") val purchasingPhoto: String? = null,
    @SerializedName("reject_reason") val rejectReason: String? = null,
    @SerializedName("odometer_out") val odometerOut: Int? = null,
    @SerializedName("completion_note") val completionNote: String? = null,
    @SerializedName("completion_photo") val completionPhoto: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val car: CarBrief? = null,
    val items: List<RequestItem> = emptyList(),
    val timeline: List<TimelineEntry> = emptyList()
)

/* ===== متابعة الموظفين ===== */
data class StaffMember(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("employee_code") val employeeCode: String? = null,
    val photo: String? = null,
    val role: String = "",
    @SerializedName("role_label") val roleLabel: String? = null,
    val total: Int = 0,
    val open: Int = 0,
    @SerializedName("oldest_days") val oldestDays: Int? = null,
    val overdue: Boolean = false
)

/* ===== لوحة المؤشرات ===== */
data class ByStatus(val count: Int = 0, val label: String = "")
data class CarAlert(
    val id: Int = 0, val name: String? = null,
    @SerializedName("plate_full") val plateFull: String? = null,
    val days: Int? = null, val remaining: Int? = null
)
data class IqamaAlert(
    val id: Int = 0, val name: String? = null,
    @SerializedName("employee_code") val employeeCode: String? = null,
    val days: Int? = null
)
data class Kpis(
    @SerializedName("total_cars") val totalCars: Int = 0,
    val available: Int = 0,
    @SerializedName("under_maintenance") val underMaintenance: Int = 0,
    @SerializedName("open_requests") val openRequests: Int = 0,
    @SerializedName("pending_admin") val pendingAdmin: Int = 0,
    @SerializedName("red_cars") val redCars: Int = 0,
    val employees: Int = 0,
    val companies: Int = 0,
    @SerializedName("by_status") val byStatus: Map<String, ByStatus> = emptyMap(),
    @SerializedName("reg_alerts") val regAlerts: List<CarAlert> = emptyList(),
    @SerializedName("ins_alerts") val insAlerts: List<CarAlert> = emptyList(),
    @SerializedName("iqama_alerts") val iqamaAlerts: List<IqamaAlert> = emptyList(),
    @SerializedName("oil_alerts") val oilAlerts: List<CarAlert> = emptyList(),
    @SerializedName("inspection_alerts") val inspectionAlerts: List<CarAlert> = emptyList(),
    @SerializedName("tire_alerts") val tireAlerts: List<CarAlert> = emptyList()
)

/* ===== مغلّف نتيجة عام ===== */
sealed class Outcome<out T> {
    data class Ok<T>(val data: T) : Outcome<T>()
    data class Err(val message: String) : Outcome<Nothing>()
}
