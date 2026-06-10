package com.nfc.blockage

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var nm: NotificationManager

    private val apps = listOf(
        "com.google.android.youtube",
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.reddit.frontpage",
        "com.twitter.android",
        "com.facebook.katana",
        "com.snapchat.android"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("blocage", Context.MODE_PRIVATE)
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("ch", "Blocage", NotificationManager.IMPORTANCE_HIGH)
        )
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(i: Intent?) {
        val a = i?.action ?: return
        if (a == NfcAdapter.ACTION_TAG_DISCOVERED || a == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            toggle()
            finish()
        }
    }

    private fun toggle() {
        val actif = prefs.getBoolean("actif", false)
        val nouvelEtat = !actif
        prefs.edit().putBoolean("actif", nouvelEtat).apply()

        val etat = if (nouvelEtat)
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
        else
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        for (pkg in apps) {
            try {
                packageManager.setApplicationEnabledSetting(pkg, etat, 0)
            } catch (e: Exception) { }
        }

        val msg = if (nouvelEtat) "🔴 Mode concentration activé" else "🟢 Applis débloquées"
        val notif = NotificationCompat.Builder(this, "ch")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try { nm.notify(1, notif) } catch (e: Exception) { }
    }
}
