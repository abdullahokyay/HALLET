package com.example.hallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GorevFiltresi(val etiket: String) {
    TUMU("Tümü"),
    AKTIF("Yapılacak"),
    TAMAMLANAN("Biten")
}

class GorevViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val dao = AppDatabase.veritabaniniGetir(application).gorevDao()

    private val _seciliFiltre = MutableStateFlow(GorevFiltresi.TUMU)
    val seciliFiltre: StateFlow<GorevFiltresi> = _seciliFiltre.asStateFlow()

    val tumGorevler: StateFlow<List<GorevEntity>> = dao.tumGorevleriGetir()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filtrelenmisGorevler: StateFlow<List<GorevEntity>> = combine(
        tumGorevler,
        _seciliFiltre
    ) { liste, filtre ->
        when (filtre) {
            GorevFiltresi.TUMU -> liste
            GorevFiltresi.AKTIF -> liste.filter { !it.tamamlandiMi }
            GorevFiltresi.TAMAMLANAN -> liste.filter { it.tamamlandiMi }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun filtreDegistir(yeniFiltre: GorevFiltresi) {
        _seciliFiltre.value = yeniFiltre
    }

    fun gorevEkle(
        baslik: String,
        aciklama: String,
        tarihSaatMillis: Long?,
        bildirimAktifMi: Boolean
    ) {
        val temizBaslik = baslik.trim()
        if (temizBaslik.isEmpty()) return

        val temizAciklama = aciklama.trim()
        val bildirimKurulsunMu = bildirimAktifMi && tarihSaatMillis != null

        viewModelScope.launch {
            val yeniId = dao.gorevEkle(
                GorevEntity(
                    baslik = temizBaslik,
                    aciklama = temizAciklama,
                    tarihSaatMillis = tarihSaatMillis,
                    bildirimAktifMi = bildirimKurulsunMu
                )
            )
            if (bildirimKurulsunMu && tarihSaatMillis != null) {
                BildirimPlanlayici.bildirimKur(
                    context = context,
                    gorevId = yeniId.toInt(),
                    baslik = temizBaslik,
                    aciklama = temizAciklama,
                    zamanMillis = tarihSaatMillis
                )
            }
        }
    }

    fun gorevGuncelle(
        mevcutGorev: GorevEntity,
        yeniBaslik: String,
        yeniAciklama: String,
        yeniTarihSaatMillis: Long?,
        yeniBildirimAktifMi: Boolean
    ) {
        val temizBaslik = yeniBaslik.trim()
        if (temizBaslik.isEmpty()) return

        val temizAciklama = yeniAciklama.trim()
        val bildirimKurulsunMu = yeniBildirimAktifMi && yeniTarihSaatMillis != null

        val guncelGorev = mevcutGorev.copy(
            baslik = temizBaslik,
            aciklama = temizAciklama,
            tarihSaatMillis = yeniTarihSaatMillis,
            bildirimAktifMi = bildirimKurulsunMu
        )

        viewModelScope.launch {
            dao.gorevGuncelle(guncelGorev)
            BildirimPlanlayici.bildirimIptalEt(context, mevcutGorev.id)
            if (!guncelGorev.tamamlandiMi && bildirimKurulsunMu && yeniTarihSaatMillis != null) {
                BildirimPlanlayici.bildirimKur(
                    context = context,
                    gorevId = guncelGorev.id,
                    baslik = temizBaslik,
                    aciklama = temizAciklama,
                    zamanMillis = yeniTarihSaatMillis
                )
            }
        }
    }

    fun durumDegistir(gorev: GorevEntity) {
        val yeniDurum = !gorev.tamamlandiMi
        viewModelScope.launch {
            dao.gorevGuncelle(gorev.copy(tamamlandiMi = yeniDurum))
            if (yeniDurum) {
                BildirimPlanlayici.bildirimIptalEt(context, gorev.id)
            } else if (gorev.bildirimAktifMi && gorev.tarihSaatMillis != null) {
                BildirimPlanlayici.bildirimKur(
                    context = context,
                    gorevId = gorev.id,
                    baslik = gorev.baslik,
                    aciklama = gorev.aciklama,
                    zamanMillis = gorev.tarihSaatMillis
                )
            }
        }
    }

    fun gorevSil(gorev: GorevEntity) {
        viewModelScope.launch {
            BildirimPlanlayici.bildirimIptalEt(context, gorev.id)
            dao.gorevSil(gorev)
        }
    }
}