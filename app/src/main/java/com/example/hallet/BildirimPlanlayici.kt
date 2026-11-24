package com.example.hallet

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object BildirimPlanlayici {

    fun bildirimKur(
        context: Context,
        gorevId: Int,
        baslik: String,
        aciklama: String,
        zamanMillis: Long
    ) {
        if (zamanMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BildirimReceiver::class.java).apply {
            putExtra("GOREV_ID", gorevId)
            putExtra("GOREV_BASLIK", baslik)
            putExtra("GOREV_ACIKLAMA", aciklama)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            gorevId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tamZamanIzniVar = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (tamZamanIzniVar) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                zamanMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                zamanMillis,
                pendingIntent
            )
        }
    }

    fun bildirimIptalEt(context: Context, gorevId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BildirimReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            gorevId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}