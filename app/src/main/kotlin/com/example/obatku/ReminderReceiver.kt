package com.example.obatku

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val obatNama = intent.getStringExtra("OBAT_NAMA") ?: "Paracetamol"
        val obatWaktu = intent.getStringExtra("OBAT_WAKTU") ?: "08:00 WIB"
        val obatId = intent.getStringExtra("OBAT_ID") ?: ""

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "obatku_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Obat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk pengingat jadwal minum obat"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent untuk klik notifikasi -> buka halaman utama
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "Sudah Diminum" (Mockup: Untuk sekarang bisa membuka MainActivity atau intent khusus)
        val sudahIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("MARK_SUDAH", obatId)
        }
        val sudahPendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 1, sudahIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "Nanti"
        val nantiIntent = Intent(context, MainActivity::class.java)
        val nantiPendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 2, nantiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_clock) // Menggunakan icon jam atau logo
            .setContentTitle("💊 Saatnya minum obat!")
            .setContentText("$obatNama\nJadwal: $obatWaktu")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$obatNama\nJadwal: $obatWaktu"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_check, "Sudah Diminum", sudahPendingIntent)
            .addAction(R.drawable.ic_clock, "Nanti", nantiPendingIntent)

        notificationManager.notify(obatId.hashCode(), builder.build())
    }
}
