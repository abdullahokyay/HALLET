package com.example.hallet

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class BildirimReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val gorevId = intent.getIntExtra("GOREV_ID", 0)
        val baslik = intent.getStringExtra("GOREV_BASLIK") ?: "Görev Hatırlatıcısı"
        val aciklama = intent.getStringExtra("GOREV_ACIKLAMA").orEmpty()

        val kanalId = "hallet_hatirlatici_kanali"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val kanal = NotificationChannel(
                kanalId,
                "Görev Hatırlatıcıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Zamanı gelen görevler için bildirim gönderir"
            }
            notificationManager.createNotificationChannel(kanal)
        }

        val acilisIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            gorevId,
            acilisIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bildirim = NotificationCompat.Builder(context, kanalId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(baslik)
            .setContentText(aciklama.ifBlank { "Planlanan görev zamanı geldi!" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(gorevId, bildirim)
    }
}