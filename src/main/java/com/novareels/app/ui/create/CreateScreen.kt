@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.novareels.app.ui.create

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.novareels.app.data.Repo
import com.novareels.app.ui.icons.NIcons
import com.novareels.app.ui.theme.Montserrat
import com.novareels.app.ui.theme.NovaAccent
import com.novareels.app.ui.theme.NovaGlass
import com.novareels.app.ui.theme.NovaGlassBorder
import com.novareels.app.ui.theme.NovaPink
import com.novareels.app.ui.theme.NovaSurface
import com.novareels.app.ui.theme.NovaSurfaceHigh
import com.novareels.app.ui.theme.NovaTextDim
import com.novareels.app.ui.theme.novaGradient
import com.novareels.app.util.Files
import com.novareels.app.util.Haptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CreateScreen(onDone: (String?) -> Unit) {
    val ctx = LocalContext.current
    var picked by remember { mutableStateOf<File?>(null) }
    var cameraMode by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf(EditResult(0, 0, 0)) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            picked = Files.copyFromUri(ctx, it, Files.videosDir(ctx), "mp4")
        }
    }

    if (cameraMode) {
        CameraCapture(
            onClose = { cameraMode = false },
            onRecorded = { f ->
                picked = f
                cameraMode = false
                editing = true
            }
        )
    } else if (picked != null && editing) {
        EditorScreen(
            file = picked!!,
            initialFilter = edit.filter,
            onCancel = { editing = false },
            onApply = { r ->
                edit = r
                editing = false
            }
        )
    } else if (picked == null) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Crear reel",
                fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, fontFamily = Montserrat,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Graba en la máxima calidad de tu cámara (hasta 4K) o importa un vídeo.",
                color = NovaTextDim, fontSize = 13.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            val camPerm = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { grants ->
                if (grants[Manifest.permission.CAMERA] == true) cameraMode = true
            }
            BigAction(NIcons.Camera, "Grabar ahora", "Cámara en calidad máxima") {
                camPerm.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                )
            }
            Spacer(Modifier.height(16.dp))
            BigAction(NIcons.Image, "Elegir de la galería", "Vídeos de tu dispositivo") {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            }
            Spacer(Modifier.height(24.dp))
            val draft = remember { Repo.loadDraft(ctx) }
            if (draft != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NovaSurface)
                        .border(1.dp, NovaGlassBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(NIcons.Edit, "Borrador", Modifier.size(20.dp), tint = NovaAccent)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Borrador guardado", color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp
                        )
                        Text(
                            draft.caption.ifBlank { "Sin descripción" },
                            color = NovaTextDim, fontSize = 11.sp, maxLines = 1
                        )
                    }
                    Text(
                        "Descartar",
                        color = NovaTextDim, fontSize = 11.5.sp,
                        modifier = Modifier.clickable {
                            Repo.clearDraft(ctx)
                            Repo.toastFlow.value = "Borrador descartado"
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Abrir",
                        color = NovaAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            val f = File(draft.path)
                            if (f.exists()) picked = f
                            else Repo.toastFlow.value = "El vídeo del borrador ya no existe"
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            Text("Cancelar", color = NovaTextDim, modifier = Modifier.clickable { onDone(null) })
        }
    } else {
        PublishForm(
            file = picked!!,
            edit = edit,
            onEdit = { editing = true },
            onDone = onDone
        )
    }
}

@Composable
private fun BigAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NovaGlass)
            .border(1.dp, NovaGlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SolidColor(NovaAccent)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(24.dp), tint = Color.White)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = NovaTextDim, fontSize = 12.sp)
        }
    }
}

@Composable
fun PublishForm(
    file: File,
    edit: EditResult = EditResult(0, 0, 0),
    onEdit: () -> Unit = {},
    onDone: (String?) -> Unit
) {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var caption by remember { mutableStateOf("") }
    var music by remember { mutableStateOf(Repo.takePendingMusic()) }
    var publishing by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(edit.filter) }
    var showAudio by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(24.dp)
    ) {
        Text(
            "Nuevo reel", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
            fontFamily = Montserrat, color = Color.White
        )
        Spacer(Modifier.height(16.dp))
        AsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(file).decoderFactory(VideoFrameDecoder.Factory()).build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0A0A12))
                .border(1.dp, NovaGlassBorder, RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            placeholder = { Text("Describe tu reel…", color = NovaTextDim) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaAccent,
                unfocusedBorderColor = NovaGlassBorder,
                cursorColor = NovaAccent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            minLines = 2,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Done
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions {
                focusManager.clearFocus()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = music,
            onValueChange = { music = it },
            placeholder = { Text("Nombre de la canción (opcional)", color = NovaTextDim) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Done
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions {
                focusManager.clearFocus()
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaAccent,
                unfocusedBorderColor = NovaGlassBorder,
                cursorColor = NovaAccent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(NovaSurface)
                    .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                    .clickable { showAudio = true }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(NIcons.Music, null, Modifier.size(14.dp), tint = NovaAccent)
                    Spacer(Modifier.width(6.dp))
                    Text("Usar audio", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(NovaSurface)
                    .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                    .clickable { onEdit() }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(NIcons.Edit, null, Modifier.size(14.dp), tint = NovaAccent)
                    Spacer(Modifier.width(6.dp))
                    Text("Editar vídeo", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Mencionar", color = NovaTextDim, fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Repo.mentionPool.take(4).forEach { (handle, _) ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(NovaSurface)
                        .border(1.dp, NovaGlassBorder, RoundedCornerShape(50))
                        .clickable {
                            caption = (caption + " " + handle + " ").trim()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        handle, color = NovaAccent, fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Original" to 0f,
                "Cálido" to 24f,
                "Frío" to 200f,
                "Mono" to 0f
            ).forEachIndexed { i, (name, hue) ->
                val sel = filter == i
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { filter = i }
                ) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when (i) {
                                    0 -> NovaSurfaceHigh
                                    3 -> Color(0xFF6B6B72)
                                    else -> Color.hsl(hue, 0.45f, 0.5f)
                                }
                            )
                            .border(
                                if (sel) 2.dp else 1.dp,
                                if (sel) NovaAccent else NovaGlassBorder,
                                RoundedCornerShape(14.dp)
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        name,
                        color = if (sel) Color.White else NovaTextDim,
                        fontSize = 10.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(NovaSurface)
                    .border(1.dp, NovaGlassBorder, RoundedCornerShape(25.dp))
                    .clickable(enabled = !publishing) {
                        Repo.scope.launch {
                            Repo.saveDraftMulti(file, caption, music, filter)
                            Repo.toastFlow.value = "Borrador guardado"
                            onDone(null)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Guardar borrador", color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 13.5.sp
                )
            }
            Box(
                Modifier
                    .weight(1.4f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(if (publishing) SolidColor(NovaGlass) else SolidColor(NovaAccent))
                    .clickable(enabled = !publishing) {
                        Haptics.strong(haptics)
                        publishing = true
                        Repo.clearDraft(ctx)
                        Repo.scope.launch {
                            Repo.publishVideo(
                                ctx, file, caption.ifBlank { "Mi reel ✨" },
                                music.ifBlank { "Sonido original" }, filter,
                                edit.startMs, edit.endMs
                            )
                            onDone("Reel publicado. Se está subiendo a la nube...")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    publishing,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "p"
                ) {
                    Text(
                        if (it) "Publicando..." else "Publicar reel",
                        color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
                    )
                }
            }
        }
    }

    if (showAudio) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showAudio = false },
            containerColor = NovaSurface,
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
        ) {
            AudioPickerSheet(
                onDismiss = { showAudio = false },
                onPick = { name ->
                    music = name
                    showAudio = false
                    Repo.toastFlow.value = "Audio: $name"
                }
            )
        }
    }
}

@Composable
fun CameraCapture(onClose: () -> Unit, onRecorded: (File) -> Unit) {
    val ctx = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val haptics = LocalHapticFeedback.current
    var recording by remember { mutableStateOf<Recording?>(null) }
    var seconds by remember { mutableStateOf(0) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val videoCaptureRef = remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    val providerFuture = remember { ProcessCameraProvider.getInstance(ctx) }
    val recorder = remember(lensFacing) {
        Recorder.Builder()
            .setQualitySelector(
                QualitySelector.fromOrderedList(listOf(Quality.UHD, Quality.FHD, Quality.HD))
            )
            .build()
    }
    val previewView = remember { mutableStateOf<PreviewView?>(null) }
    val cameraRef = remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    val zoomRange = remember { mutableStateOf(1f) }
    var zoom by remember { mutableStateOf(1f) }
    var grid by remember { mutableStateOf(true) }

    LaunchedEffect(previewView.value, lensFacing) {
        val pv = previewView.value ?: return@LaunchedEffect
        val deferred = kotlinx.coroutines.CompletableDeferred<ProcessCameraProvider>()
        providerFuture.addListener(
            { runCatching { deferred.complete(providerFuture.get()) } },
            ContextCompat.getMainExecutor(ctx)
        )
        val provider = runCatching { deferred.await() }.getOrNull() ?: return@LaunchedEffect
        runCatching {
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(pv.surfaceProvider)
            }
            val selector = CameraSelector.Builder()
                .requireLensFacing(lensFacing).build()
            val videoCapture = VideoCapture.withOutput(recorder)
            provider.unbindAll()
            val bound = provider.bindToLifecycle(lifecycleOwner, selector, preview, videoCapture)
            videoCaptureRef.value = videoCapture
            cameraRef.value = bound
            runCatching { zoomRange.value = bound.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f }
        }
    }

    AndroidView(
        factory = { c -> PreviewView(c).also { previewView.value = it } },
        modifier = Modifier.fillMaxSize()
    )

    if (grid) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val c = Color.White.copy(alpha = 0.18f)
            for (i in 1..2) {
                drawLine(c, androidx.compose.ui.geometry.Offset(w * i / 3f, 0f),
                    androidx.compose.ui.geometry.Offset(w * i / 3f, h), 1f)
                drawLine(c, androidx.compose.ui.geometry.Offset(0f, h * i / 3f),
                    androidx.compose.ui.geometry.Offset(w, h * i / 3f), 1f)
            }
        }
    }

    LaunchedEffect(recording != null) {
        if (recording != null) {
            val t0 = System.currentTimeMillis()
            while (recording != null) {
                seconds = ((System.currentTimeMillis() - t0) / 1000L).toInt()
                delay(500)
            }
        } else {
            seconds = 0
        }
    }

    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                NIcons.Close, "Cerrar",
                Modifier
                    .clickable {
                        recording?.stop()
                        onClose()
                    }
                    .padding(8.dp),
                tint = Color.White
            )
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    if (recording != null) "● REC ${seconds}s · hasta 4K" else "Calidad máxima (4K)",
                    color = if (recording != null) Color(0xFFFF6B6B) else Color.White,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                NIcons.Flip, "Cambiar cámara",
                Modifier
                    .clickable {
                        lensFacing =
                            if (lensFacing == CameraSelector.LENS_FACING_BACK)
                                CameraSelector.LENS_FACING_FRONT
                            else CameraSelector.LENS_FACING_BACK
                    }
                    .padding(8.dp),
                tint = Color.White
            )
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(1f, 2f, 3f).forEach { z ->
                val target = z.coerceAtMost(zoomRange.value)
                val sel = kotlin.math.abs(zoom - target) < 0.05f
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (sel) Color.White else Color.Black.copy(alpha = 0.45f))
                        .clickable {
                            zoom = target
                            runCatching {
                                cameraRef.value?.cameraControl?.setZoomRatio(target)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${target.toInt()}x",
                        color = if (sel) Color.Black else Color.White,
                        fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (grid) Color.White else Color.Black.copy(alpha = 0.45f))
                    .clickable { grid = !grid }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(
                    "Rejilla",
                    color = if (grid) Color.Black else Color.White,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(84.dp)
                .clip(CircleShape)
                .background(if (recording != null) SolidColor(Color.White.copy(alpha = 0.3f)) else novaGradient)
                .border(4.dp, Color.White, CircleShape)
                .clickable {
                    val vc = videoCaptureRef.value ?: return@clickable
                    if (recording == null) {
                        Haptics.strong(haptics)
                        val file = File(Files.videosDir(ctx), "cam_${System.currentTimeMillis()}.mp4")
                        runCatching {
                            recording = vc.output
                                .prepareRecording(ctx, FileOutputOptions.Builder(file).build())
                                .apply { runCatching { withAudioEnabled() } }
                                .start(ContextCompat.getMainExecutor(ctx)) { ev ->
                                    if (ev is VideoRecordEvent.Finalize) {
                                        recording = null
                                        if (!ev.hasError()) onRecorded(file)
                                    }
                                }
                        }
                    } else {
                        recording?.stop()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (recording != null) {
                Text("■", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Text(
            if (recording != null) "Toca para detener" else "Toca para grabar",
            color = Color.White, fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp)
        )
    }
}
