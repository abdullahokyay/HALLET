package com.example.hallet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val AnaRenk = Color(0xFF4F46E5)
private val IkincilRenk = Color(0xFF10B981)
private val ArkaPlanAcik = Color(0xFFF8FAFC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val renkSemasi = lightColorScheme(
                primary = AnaRenk,
                secondary = IkincilRenk,
                background = ArkaPlanAcik,
                surface = Color.White,
                surfaceVariant = Color(0xFFEEF2FF)
            )
            MaterialTheme(colorScheme = renkSemasi) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HalletAnaEkran()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalletAnaEkran(viewModel: GorevViewModel = viewModel()) {
    val context = LocalContext.current
    val tumGorevler by viewModel.tumGorevler.collectAsState()
    val gorevListesi by viewModel.filtrelenmisGorevler.collectAsState()
    val seciliFiltre by viewModel.seciliFiltre.collectAsState()

    var eklemePaneliAcik by remember { mutableStateOf(false) }
    var duzenlenecekGorev by remember { mutableStateOf<GorevEntity?>(null) }

    val izinIsteyici = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val izinVar = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!izinVar) {
                izinIsteyici.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val toplamSayi = tumGorevler.size
    val bitenSayi = tumGorevler.count { it.tamamlandiMi }
    val ilerlemeOrani = if (toplamSayi > 0) bitenSayi.toFloat() / toplamSayi else 0f

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { eklemePaneliAcik = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Yeni Görev", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { icBosluk ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(icBosluk)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AnaRenk)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HALLET",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = bugununTarihi(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "$bitenSayi / $toplamSayi Bitti",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { ilerlemeOrani },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = IkincilRenk,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                GorevFiltresi.entries.forEachIndexed { index, filtre ->
                    SegmentedButton(
                        selected = seciliFiltre == filtre,
                        onClick = { viewModel.filtreDegistir(filtre) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = GorevFiltresi.entries.size
                        ),
                        label = {
                            Text(
                                text = filtre.etiket,
                                fontWeight = if (seciliFiltre == filtre) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))


            if (gorevListesi.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (seciliFiltre) {
                                GorevFiltresi.TUMU -> "Listen tertemiz!"
                                GorevFiltresi.AKTIF -> "Bekleyen işin kalmadı."
                                GorevFiltresi.TAMAMLANAN -> "Henüz tamamlanan görev yok."
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sağ alttaki butondan yeni görev planla",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(gorevListesi, key = { it.id }) { gorev ->
                        ModernGorevKarti(
                            gorev = gorev,
                            onKartaTikla = { duzenlenecekGorev = gorev },
                            onDurumDegistir = { viewModel.durumDegistir(gorev) },
                            onSil = { viewModel.gorevSil(gorev) }
                        )
                    }
                }
            }
        }
    }

    if (eklemePaneliAcik) {
        GorevFormBottomSheet(
            duzenlenecekGorev = null,
            onKapat = { eklemePaneliAcik = false },
            onKaydet = { baslik, aciklama, tarihSaatMillis, bildirimAktif ->
                viewModel.gorevEkle(baslik, aciklama, tarihSaatMillis, bildirimAktif)
                eklemePaneliAcik = false
            }
        )
    }

    duzenlenecekGorev?.let { seciliGorev ->
        GorevFormBottomSheet(
            duzenlenecekGorev = seciliGorev,
            onKapat = { duzenlenecekGorev = null },
            onKaydet = { baslik, aciklama, tarihSaatMillis, bildirimAktif ->
                viewModel.gorevGuncelle(
                    mevcutGorev = seciliGorev,
                    yeniBaslik = baslik,
                    yeniAciklama = aciklama,
                    yeniTarihSaatMillis = tarihSaatMillis,
                    yeniBildirimAktifMi = bildirimAktif
                )
                duzenlenecekGorev = null
            }
        )
    }
}

@Composable
fun ModernGorevKarti(
    gorev: GorevEntity,
    onKartaTikla: () -> Unit,
    onDurumDegistir: () -> Unit,
    onSil: () -> Unit
) {
    val gecikmisMi = !gorev.tamamlandiMi &&
            gorev.tarihSaatMillis != null &&
            gorev.tarihSaatMillis < System.currentTimeMillis()

    val kartRengi by animateColorAsState(
        targetValue = if (gorev.tamamlandiMi) Color(0xFFF1F5F9) else Color.White,
        label = "kartRengi"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onKartaTikla() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = kartRengi),
        border = BorderStroke(
            width = 1.dp,
            color = if (gecikmisMi) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (gorev.tamamlandiMi) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (gorev.tamamlandiMi) IkincilRenk else Color.Transparent)
                    .clickable { onDurumDegistir() },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = if (gorev.tamamlandiMi) IkincilRenk else Color.Transparent,
                    border = BorderStroke(
                        2.dp,
                        if (gorev.tamamlandiMi) IkincilRenk else AnaRenk
                    )
                ) {
                    if (gorev.tamamlandiMi) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gorev.baslik,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (gorev.tamamlandiMi) TextDecoration.LineThrough else null,
                    color = if (gorev.tamamlandiMi) Color(0xFF94A3B8) else Color(0xFF0F172A)
                )

                if (gorev.aciklama.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = gorev.aciklama,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = if (gorev.tamamlandiMi) Color(0xFFCBD5E1) else Color(0xFF475569)
                    )
                }

                if (gorev.tarihSaatMillis != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val etiketArkaPlan = when {
                        gorev.tamamlandiMi -> Color(0xFFE2E8F0)
                        gecikmisMi -> Color(0xFFFEF2F2)
                        else -> Color(0xFFEEF2FF)
                    }
                    val etiketYaziRengi = when {
                        gorev.tamamlandiMi -> Color(0xFF64748B)
                        gecikmisMi -> Color(0xFFDC2626)
                        else -> AnaRenk
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = etiketArkaPlan
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (gorev.bildirimAktifMi) Icons.Default.Notifications
                                else Icons.Default.DateRange,
                                contentDescription = null,
                                tint = etiketYaziRengi,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = tarihSaatFormatla(gorev.tarihSaatMillis),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = etiketYaziRengi
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onSil,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GorevFormBottomSheet(
    duzenlenecekGorev: GorevEntity?,
    onKapat: () -> Unit,
    onKaydet: (String, String, Long?, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val duzenlemeModuMu = duzenlenecekGorev != null

    val baslangicTakvimi = remember(duzenlenecekGorev) {
        duzenlenecekGorev?.tarihSaatMillis?.let { millis ->
            Calendar.getInstance().apply { timeInMillis = millis }
        }
    }

    var baslik by remember(duzenlenecekGorev) {
        mutableStateOf(duzenlenecekGorev?.baslik ?: "")
    }
    var aciklama by remember(duzenlenecekGorev) {
        mutableStateOf(duzenlenecekGorev?.aciklama ?: "")
    }
    var secilenTarihMillis by remember(duzenlenecekGorev) {
        mutableStateOf(duzenlenecekGorev?.tarihSaatMillis)
    }
    var secilenSaat by remember(duzenlenecekGorev) {
        mutableIntStateOf(baslangicTakvimi?.get(Calendar.HOUR_OF_DAY) ?: 9)
    }
    var secilenDakika by remember(duzenlenecekGorev) {
        mutableIntStateOf(baslangicTakvimi?.get(Calendar.MINUTE) ?: 0)
    }
    var saatSecildiMi by remember(duzenlenecekGorev) {
        mutableStateOf(duzenlenecekGorev?.tarihSaatMillis != null)
    }
    var bildirimAktif by remember(duzenlenecekGorev) {
        mutableStateOf(duzenlenecekGorev?.bildirimAktifMi ?: true)
    }

    var tarihDialogAcik by remember { mutableStateOf(false) }
    var saatDialogAcik by remember { mutableStateOf(false) }

    val birlesikZamanMillis: Long? = remember(secilenTarihMillis, secilenSaat, secilenDakika, saatSecildiMi) {
        secilenTarihMillis?.let { tarih ->
            val takvim = Calendar.getInstance().apply {
                timeInMillis = tarih
                set(Calendar.HOUR_OF_DAY, if (saatSecildiMi) secilenSaat else 9)
                set(Calendar.MINUTE, if (saatSecildiMi) secilenDakika else 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            takvim.timeInMillis
        }
    }

    ModalBottomSheet(
        onDismissRequest = onKapat,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (duzenlemeModuMu) "Görevi Düzenle" else "Yeni Görev Planla",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            OutlinedTextField(
                value = baslik,
                onValueChange = { baslik = it },
                label = { Text("Görev Başlığı *") },
                placeholder = { Text("Örn: Proje sunumunu hazırla") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = aciklama,
                onValueChange = { aciklama = it },
                label = { Text("Ne Yapılacak? (Detay / Not)") },
                placeholder = { Text("Adımları veya önemli notları buraya yaz...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                maxLines = 4
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { tarihDialogAcik = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = secilenTarihMillis?.let { sadeceTarihFormatla(it) } ?: "Tarih Seç"
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (secilenTarihMillis == null) {
                            secilenTarihMillis = System.currentTimeMillis()
                        }
                        saatDialogAcik = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (saatSecildiMi) {
                            String.format(Locale.getDefault(), "%02d:%02d", secilenSaat, secilenDakika)
                        } else {
                            "Saat Seç"
                        }
                    )
                }

                if (secilenTarihMillis != null) {
                    IconButton(onClick = {
                        secilenTarihMillis = null
                        saatSecildiMi = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Tarihi temizle")
                    }
                }
            }

            if (birlesikZamanMillis != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = AnaRenk
                            )
                            Text("Zamanı gelince bildirim gönder")
                        }
                        Switch(
                            checked = bildirimAktif,
                            onCheckedChange = { bildirimAktif = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    onKaydet(baslik, aciklama, birlesikZamanMillis, bildirimAktif)
                },
                enabled = baslik.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (duzenlemeModuMu) "Değişiklikleri Kaydet" else "Görevi Kaydet",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    if (tarihDialogAcik) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = yerelTarihiUtcGeceYarisinaCevir(
                secilenTarihMillis ?: System.currentTimeMillis()
            )
        )
        DatePickerDialog(
            onDismissRequest = { tarihDialogAcik = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        secilenTarihMillis = utcGeceYarisiniYerelTariheCevir(utcMillis)
                    }
                    tarihDialogAcik = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { tarihDialogAcik = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (saatDialogAcik) {
        val timePickerState = rememberTimePickerState(
            initialHour = secilenSaat,
            initialMinute = secilenDakika,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { saatDialogAcik = false },
            title = { Text("Saat Seç") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    secilenSaat = timePickerState.hour
                    secilenDakika = timePickerState.minute
                    saatSecildiMi = true
                    saatDialogAcik = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { saatDialogAcik = false }) { Text("İptal") }
            }
        )
    }
}

private fun yerelTarihiUtcGeceYarisinaCevir(yerelMillis: Long): Long {
    val yerel = Calendar.getInstance().apply { timeInMillis = yerelMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(yerel.get(Calendar.YEAR), yerel.get(Calendar.MONTH), yerel.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun utcGeceYarisiniYerelTariheCevir(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun bugununTarihi(): String {
    val format = SimpleDateFormat("d MMMM EEEE", Locale("tr", "TR"))
    return format.format(Date())
}

private fun sadeceTarihFormatla(millis: Long): String {
    val format = SimpleDateFormat("d MMM yyyy", Locale("tr", "TR"))
    return format.format(Date(millis))
}

private fun tarihSaatFormatla(millis: Long): String {
    val format = SimpleDateFormat("d MMM • HH:mm", Locale("tr", "TR"))
    return format.format(Date(millis))
}