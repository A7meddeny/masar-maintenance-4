package com.masar.maintenance.ui

import androidx.compose.ui.graphics.Color
import com.masar.maintenance.data.Net
import com.masar.maintenance.ui.theme.Blue
import com.masar.maintenance.ui.theme.Green
import com.masar.maintenance.ui.theme.Muted
import com.masar.maintenance.ui.theme.RedStatus
import com.masar.maintenance.ui.theme.Yellow

object Labels {

    fun role(r: String): String = when (r) {
        "admin" -> "الإدارة"; "office" -> "موظف المكتب"
        "maintenance" -> "قسم الصيانة"; "purchasing" -> "قسم المشتريات"
        "manager" -> "مدير"
        else -> r
    }

    fun status(s: String): String = when (s) {
        "new_office" -> "طلب جديد — بانتظار الصيانة"
        "maintenance_received" -> "الصيانة استلمت السيارة"
        "sent_to_purchasing" -> "محوّل للمشتريات"
        "purchasing_received" -> "المشتريات استلم الطلب"
        "sent_to_admin" -> "بانتظار اعتماد الإدارة"
        "admin_approved" -> "اعتُمد — جارٍ الشراء"
        "purchasing_buying" -> "المشتريات يشتري القطعة"
        "part_delivered" -> "سُلّمت القطعة للصيانة"
        "maintenance_fixing" -> "الصيانة تُصلح السيارة"
        "completed" -> "مكتمل — عادت السيارة"
        "periodic_self" -> "صيانة دورية — قيد التنفيذ"
        "rejected" -> "مرفوض من الإدارة"
        else -> s
    }

    fun stage(s: String): String = when (s) {
        "new_office" -> "طلب جديد (المكتب)"; "periodic_self" -> "بدء صيانة دورية"
        "maintenance_received" -> "استلام الصيانة"; "sent_to_purchasing" -> "الإرسال للمشتريات"
        "purchasing_received" -> "استلام المشتريات"; "sent_to_admin" -> "الإرسال للإدارة"
        "admin_approved" -> "اعتماد الإدارة"; "purchasing_buying" -> "بدء الشراء"
        "part_delivered" -> "تسليم القطعة"; "maintenance_fixing" -> "بدء الإصلاح"
        "completed" -> "الإنهاء"; "rejected" -> "رفض الإدارة"; else -> s
    }

    fun itemKind(k: String): String = when (k) {
        "general" -> "عام"; "oil_change" -> "تغيير زيت"
        "tire_change" -> "تغيير إطارات"; "other_periodic" -> "دوري آخر"; else -> k
    }

    fun tirePos(p: String?): String = when (p) {
        "front" -> "أمامي"; "rear" -> "خلفي"; "both" -> "الكل"; else -> (p ?: "")
    }

    fun statusColor(s: String): Color = when (s) {
        "completed" -> Green
        "sent_to_admin" -> Blue
        else -> Yellow
    }

    /** لون حسب الأيام المتبقية (سالب/قريب = أحمر) */
    fun daysColor(days: Int?): Color {
        val n = days ?: return Muted
        return when { n < 0 -> RedStatus; n <= 7 -> RedStatus; n <= 30 -> Yellow; else -> Green }
    }

    fun carColor(c: String?): Color = when (c) {
        "green" -> Green; "red" -> RedStatus; "yellow" -> Yellow; else -> Muted
    }
}

/** يحوّل المسار النسبي (uploads/..) إلى رابط كامل حسب رابط الخادم. */
fun imageUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    return Net.session.baseUrl + path.trimStart('/')
}
