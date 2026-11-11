package com.logix.optiflow.presentation.patient.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.navigation.goPatientTab
import com.logix.optiflow.presentation.patient.CatalogViewModel
import com.logix.optiflow.presentation.patient.components.EyePositions
import com.logix.optiflow.presentation.patient.components.FaceCamera
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.patient.components.drawGlasses
import com.logix.optiflow.presentation.patient.components.image
import com.logix.optiflow.presentation.patient.components.priceLabel
import com.logix.optiflow.presentation.patient.components.shapeLabel
import com.logix.optiflow.presentation.patient.components.tryOnColor
import com.logix.optiflow.presentation.patient.components.tryOnStyle
import com.logix.optiflow.presentation.search.screens.SearchField
import com.logix.optiflow.presentation.shared.components.BellButton
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.IconTile
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientBottomBar
import com.logix.optiflow.presentation.shared.components.PatientLightTopBar
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.Pill
import com.logix.optiflow.presentation.shared.components.SegmentChip
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.BellChip
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Emerald700
import com.logix.optiflow.presentation.theme.InfoBanner
import com.logix.optiflow.presentation.theme.Mint100
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.PatientHeader
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.Teal700
import com.logix.optiflow.presentation.theme.White

private val FrameBorder = Color(0xFF9AA8C7)
private val Ink = Color(0xFF111827)

@Composable
fun CatalogScreen(nav: NavController, vm: CatalogViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    val catalogFilters = listOf("Todos", "Favoritos") + state.shapes
    val frames =
        state.frames
            .filter {
                when (filter) {
                    0 -> true
                    1 -> it.sku in state.favorites
                    else -> it.shape == catalogFilters.getOrNull(filter)
                }
            }
            .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }

    PatientScreen(
        nav = nav,
        tab = PatientTab.SEARCH,
        topBar = {
            PatientLightTopBar("Explorar Monturas", onBack = { nav.popBackStack() }) {
                BellButton(onClick = { nav.navigate(Routes.P_NOTIFICATIONS) })
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VSpace(4.dp)
            SearchField(query, { query = it }, "Buscar armazón, estilo...")
            VSpace(20.dp)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                catalogFilters.forEachIndexed { index, label ->
                    SegmentChip(
                        text = label,
                        selected = filter == index,
                        onClick = { filter = index },
                        selectedBg = PatientHeader,
                        selectedText = White,
                        unselectedText = DeepNavy,
                        border = FrameBorder,
                        fontSize = 14,
                        height = 34.dp,
                        weight = FontWeight.Bold,
                    )
                }
            }
            VSpace(20.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(InfoBanner)
                    .border(1.dp, SkyTint, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(R.drawable.lucide_ic_star, White, Electric, size = 42.dp, iconSize = 22.dp, radius = 12.dp)
                HSpace(14.dp)
                Column {
                    Txt("Encuentra tu estilo", 13, weight = FontWeight.Bold, color = DeepNavy)
                    Txt(
                        "Selecciona una montura y te mostraremos qué ópticas cercanas la tienen.",
                        11,
                        color = Slate500,
                        lineHeight = 15.sp,
                    )
                }
            }
            VSpace(20.dp)
            if (frames.isEmpty()) {
                Txt(
                    when {
                        state.isLoading -> "Cargando monturas..."
                        state.errorMessage != null -> state.errorMessage.orEmpty()
                        filter == 1 -> "Aún no tienes monturas favoritas. Toca el corazón en una montura para guardarla."
                        else -> "No hay monturas en esta categoría por ahora."
                    },
                    13,
                    color = Slate500,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
            frames.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { frame ->
                        CatalogCard(frame, Modifier.weight(1f)) { nav.navigate(Routes.frame(frame.sku)) }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                VSpace(16.dp)
            }
            VSpace(8.dp)
        }
    }
}

@Composable
private fun CatalogCard(frame: FrameModel, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .card(radius = 16.dp, border = FrameBorder)
            .tap(onClick)
            .padding(8.dp),
    ) {
        Image(
            painter = painterResource(frame.image),
            contentDescription = frame.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(116.dp).clip(RoundedCornerShape(8.dp)),
        )
        VSpace(14.dp)
        Txt(frame.shapeLabel, 11, color = Electric)
        VSpace(4.dp)
        Txt(frame.name, 14, weight = FontWeight.Bold, color = Ink, maxLines = 1)
        VSpace(4.dp)
        Txt(frame.priceLabel, 14, weight = FontWeight.Bold, color = Teal700)
        VSpace(10.dp)
    }
}

@Composable
fun FrameDetailScreen(nav: NavController, sku: String, vm: CatalogViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(sku) { vm.loadStock(sku) }
    val frame = state.frame(sku)
    if (frame == null) {
        if (!state.isLoading) LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    val stock = state.stock
    val units = stock?.sumOf { it.quantity } ?: 0
    val favorite = sku in state.favorites

    PatientScreen(
        nav = nav,
        tab = PatientTab.SEARCH,
        topBar = {
            PatientLightTopBar("Detalles de montura", onBack = { nav.popBackStack() }) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(BellChip).tap { vm.toggleFavorite(sku) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (favorite) {
                        Icon(Icons.Filled.Favorite, null, tint = PatientHeader, modifier = Modifier.size(20.dp))
                    } else {
                        LIcon(R.drawable.lucide_ic_heart, PatientHeader, 20.dp)
                    }
                }
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VSpace(8.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, FrameBorder, RoundedCornerShape(16.dp)),
            ) {
                Image(
                    painter = painterResource(frame.image),
                    contentDescription = frame.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (units > 0) {
                    Pill(
                        text = "DISPONIBLE HOY",
                        background = Mint100,
                        color = Emerald700,
                        icon = R.drawable.lucide_ic_circle_check,
                        modifier = Modifier.padding(14.dp),
                        padding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                        iconSize = 14.dp,
                    )
                }
            }
            VSpace(12.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .card(radius = 16.dp, border = FrameBorder)
                    .padding(horizontal = 17.dp, vertical = 16.dp),
            ) {
                Txt("${frame.shapeLabel} · UNISEX", 11, weight = FontWeight.Bold, color = Electric)
                VSpace(6.dp)
                Txt(frame.name, 24, weight = FontWeight.ExtraBold, color = Ink)
                VSpace(10.dp)
                Txt(listOfNotNull(frame.material, frame.color).joinToString(" · "), 13, color = Slate500)
                VSpace(18.dp)
                Txt(frame.priceLabel, 22, weight = FontWeight.ExtraBold, color = Electric)
                VSpace(14.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(R.drawable.lucide_ic_package_check, Mint100, Emerald700, size = 30.dp, iconSize = 16.dp, radius = 15.dp)
                    HSpace(8.dp)
                    Column {
                        Txt(
                            when {
                                stock == null -> "Consultando stock..."
                                units > 0 -> "En stock · $units unidades"
                                else -> "Sin stock por ahora"
                            },
                            13,
                            weight = FontWeight.Bold,
                            color = Emerald700,
                        )
                        Txt("Disponible para prueba y reserva inmediata", 11, color = Slate500)
                    }
                }
                VSpace(12.dp)
                Divider(SkyTint)
                VSpace(12.dp)
                Txt("Características principales", 13, weight = FontWeight.Bold, color = Ink)
                VSpace(10.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOfNotNull(frame.material, frame.shape).forEachIndexed { index, feature ->
                        Pill(
                            text = feature,
                            background = SkyTint,
                            color = NavyBlue,
                            icon = if (index == 0) R.drawable.lucide_ic_gem else R.drawable.lucide_ic_leaf,
                            padding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                            iconSize = 14.dp,
                        )
                    }
                }
            }
            VSpace(20.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(InfoBanner)
                    .tap { nav.navigate(Routes.P_SEARCH) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(R.drawable.lucide_ic_map_pin, White, NavyBlue, size = 40.dp, iconSize = 20.dp, radius = 12.dp)
                HSpace(14.dp)
                Column {
                    val stores = stock.orEmpty()
                    Txt(
                        when (stores.size) {
                            0 -> "Consulta la disponibilidad en tu óptica"
                            1 -> "Disponible en 1 óptica"
                            else -> "Disponible en ${stores.size} ópticas"
                        },
                        13,
                        weight = FontWeight.Bold,
                        color = DeepNavy,
                    )
                    Txt(
                        stores.firstOrNull()?.let { "Por ejemplo, en ${it.store.name}." } ?: "Toca para ver las ópticas.",
                        11,
                        color = Slate500,
                    )
                }
            }
            VSpace(52.dp)
            if (frame.tryOnStyle != null) {
                SolidButton(
                    text = "Probar virtualmente",
                    onClick = { nav.navigate(Routes.tryOn(frame.sku)) },
                    background = White,
                    contentColor = Electric,
                    border = Electric,
                    leadingIcon = R.drawable.lucide_ic_glasses,
                    fontSize = 15,
                    weight = FontWeight.SemiBold,
                )
                VSpace(12.dp)
            }
            SolidButton(
                text = "Ver ópticas con disponibilidad",
                onClick = { nav.navigate(Routes.book()) },
                leadingIcon = R.drawable.lucide_ic_map_pin,
                fontSize = 15,
                weight = FontWeight.SemiBold,
            )
            VSpace(16.dp)
        }
    }
}

@Composable
fun TryOnScreen(nav: NavController, sku: String, vm: CatalogViewModel = hiltViewModel()) {
    com.logix.optiflow.presentation.shared.components.StatusBarIcons(lightIcons = true)
    val context = LocalContext.current
    val state by vm.uiState.collectAsStateWithLifecycle()
    val frame = state.frame(sku)
    val style = frame?.tryOnStyle
    if (frame == null || style == null) {
        // Sin modelo de prueba virtual no hay probador (US21, escenario 2).
        if (!state.isLoading) LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    var hasCamera by remember { mutableStateOf(hasCameraPermission(context)) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }
    LaunchedEffect(Unit) { if (!hasCamera) permission.launch(Manifest.permission.CAMERA) }
    LifecycleResumeEffect(Unit) {
        // Por si el permiso se dio desde Ajustes.
        hasCamera = hasCameraPermission(context)
        onPauseOrDispose {}
    }
    var eyes by remember { mutableStateOf<EyePositions?>(null) }
    val glassesColor = frame.tryOnColor

    Column(Modifier.fillMaxSize().background(Color(0xFF14295A))) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(listOf(Color(0xFF2B4F96), Color(0xFF14295A), Color(0xFF0D1C44))),
                ),
        ) {
            if (hasCamera) {
                FaceCamera(
                    onEyes = { found ->
                        // Se suaviza el movimiento para que la montura no tiemble.
                        eyes = found?.let { new ->
                            eyes?.let { old -> EyePositions(lerp(old.left, new.left, 0.5f), lerp(old.right, new.right, 0.5f)) } ?: new
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                Canvas(Modifier.fillMaxSize()) { eyes?.let { drawGlasses(style, glassesColor, it) } }
            } else {
                Txt(
                    "Toca aquí para permitir la cámara (o actívala en Ajustes).",
                    13,
                    color = White,
                    align = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp).tap { permission.launch(Manifest.permission.CAMERA) },
                )
            }
            if (hasCamera && eyes == null) CameraGuide()
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CameraButton(R.drawable.lucide_ic_chevron_left) { nav.popBackStack() }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Txt("Prueba virtual", 16, weight = FontWeight.Bold, color = White)
                    Txt("${frame.name} · Cámara AR", 11, color = White.copy(alpha = 0.7f))
                }
                Spacer(Modifier.size(40.dp))
            }
            if (hasCamera) {
                Row(
                    Modifier
                        .statusBarsPadding()
                        .padding(start = 20.dp, top = 70.dp)
                        .clip(CircleShape)
                        .background(Color(0x40000000))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(if (eyes != null) Color(0xFF60A5FA) else Slate400))
                    HSpace(7.dp)
                    Txt(if (eyes != null) "Rostro detectado" else "Buscando tu rostro…", 11, weight = FontWeight.SemiBold, color = White)
                }
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp)
                        .clip(CircleShape)
                        .background(Color(0x40000000))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LIcon(R.drawable.lucide_ic_eye, White, 14.dp)
                    HSpace(8.dp)
                    Txt("Mira de frente y mantén el rostro dentro de la guía", 11, color = White)
                }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(White),
        ) {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(frame.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(62.dp).clip(RoundedCornerShape(12.dp)),
                )
                HSpace(14.dp)
                Column(Modifier.weight(1f)) {
                    Txt("PROBANDO AHORA", 11, weight = FontWeight.Bold, color = Electric)
                    Txt(frame.name, 18, weight = FontWeight.ExtraBold, color = DeepNavy, maxLines = 1)
                    Txt(listOfNotNull(frame.color, frame.priceLabel).joinToString(" · "), 12, color = Slate500)
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SkyTint)
                        .tap { nav.navigate(Routes.frame(frame.sku)) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Txt("Ver stock", 12, weight = FontWeight.Bold, color = Electric)
                }
            }
            PatientBottomBar(PatientTab.SEARCH) { nav.goPatientTab(it) }
        }
    }
}

private fun hasCameraPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

@Composable
private fun CameraButton(icon: Int, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33000000))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .tap(onClick),
        contentAlignment = Alignment.Center,
    ) {
        LIcon(icon, White, 20.dp)
    }
}

/** Guía de rostro (rejilla y óvalo punteado) mientras no se detecta una cara. */
@Composable
private fun CameraGuide() {
    Canvas(Modifier.fillMaxSize()) {
        val grid = Color(0x22FFFFFF)
        drawLine(grid, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx())
        drawLine(grid, Offset(0f, size.height * 0.52f), Offset(size.width, size.height * 0.52f), 1.dp.toPx())
        val ovalW = size.width * 0.44f
        val ovalH = size.height * 0.6f
        val center = Offset(size.width / 2, size.height * 0.55f)
        drawOval(
            color = Color(0x99FFFFFF),
            topLeft = Offset(center.x - ovalW / 2, center.y - ovalH / 2),
            size = Size(ovalW, ovalH),
            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
        )
    }
}
