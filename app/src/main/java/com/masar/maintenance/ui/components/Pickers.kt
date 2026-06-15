package com.masar.maintenance.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.masar.maintenance.data.UploadFile
import com.masar.maintenance.data.Uploads
import com.masar.maintenance.ui.imageUrl
import com.masar.maintenance.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** حقل اختيار صورة/ملف مع معاينة وزر حذف. */
@Composable
fun PhotoPickerField(
    label: String,
    picked: UploadFile?,
    onPicked: (UploadFile?) -> Unit,
    modifier: Modifier = Modifier,
    allowPdf: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    val mime = if (allowPdf) "*/*" else "image/*"

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            working = true
            scope.launch {
                val uf = withContext(Dispatchers.IO) { Uploads.prepare(context, uri) }
                working = false
                onPicked(uf)
            }
        }
    }

    Column(modifier.fillMaxWidth()) {
        Text(label, color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Surface(
            color = Ink2,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Line2),
            modifier = Modifier.fillMaxWidth().clickable(enabled = !working) { launcher.launch(mime) }
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                when {
                    working -> {
                        CircularProgressIndicator(color = Red, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("جارٍ التجهيز…", color = Muted)
                    }
                    picked != null -> {
                        if (picked.mime.startsWith("image/")) {
                            AsyncImage(
                                model = picked.file,
                                contentDescription = null,
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("📄", fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(picked.fileName.take(24), color = Txt, modifier = Modifier.weight(1f), fontSize = 13.sp)
                        TextButton(onClick = { onPicked(null) }) { Text("حذف", color = RedStatus) }
                    }
                    else -> {
                        Text("＋", fontSize = 20.sp, color = Red, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(10.dp))
                        Text(if (allowPdf) "اضغط لاختيار صورة أو PDF" else "اضغط لاختيار صورة", color = Muted)
                    }
                }
            }
        }
    }
}

/** صورة من الخادم (مسار نسبي uploads/..) مع بديل عند غيابها. */
@Composable
fun RemoteImage(
    path: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: String = "⛍"
) {
    val url = imageUrl(path)
    if (url.isNullOrBlank()) {
        Box(modifier.background(Ink2), contentAlignment = Alignment.Center) {
            Text(placeholder, color = Muted, fontSize = 22.sp)
        }
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
