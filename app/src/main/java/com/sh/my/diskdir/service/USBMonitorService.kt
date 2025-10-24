package com.sh.my.diskdir.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sh.my.diskdir.R
import com.sh.my.diskdir.utils.FileSystemUtils
import kotlinx.coroutines.*

class USBMonitorService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val usbReceiver = USBReceiver()
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "usb_monitor_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerUSBReceiver()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
        serviceScope.cancel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "USB Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Мониторинг подключения USB устройств"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("DiskDir - Мониторинг USB")
        .setContentText("Отслеживание подключения внешних накопителей")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)
        .build()
    
    private fun registerUSBReceiver() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(Intent.ACTION_MEDIA_MOUNTED)
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addAction(Intent.ACTION_MEDIA_EJECT)
            addDataScheme("file")
        }
        registerReceiver(usbReceiver, filter)
    }
    
    private inner class USBReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    device?.let {
                        onUSBDeviceConnected(it)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    device?.let {
                        onUSBDeviceDisconnected(it)
                    }
                }
                Intent.ACTION_MEDIA_MOUNTED -> {
                    val data = intent.data
                    data?.let {
                        onMediaMounted(it.path ?: "")
                    }
                }
                Intent.ACTION_MEDIA_UNMOUNTED -> {
                    val data = intent.data
                    data?.let {
                        onMediaUnmounted(it.path ?: "")
                    }
                }
            }
        }
    }
    
    private fun onUSBDeviceConnected(device: UsbDevice) {
        serviceScope.launch {
            // Логика обработки подключения USB устройства
            // Здесь можно добавить уведомления или обновление списка устройств
        }
    }
    
    private fun onUSBDeviceDisconnected(device: UsbDevice) {
        serviceScope.launch {
            // Логика обработки отключения USB устройства
        }
    }
    
    private fun onMediaMounted(path: String) {
        serviceScope.launch {
            // Логика обработки подключения носителя
            // Сканирование содержимого нового устройства
            if (FileSystemUtils.isDeviceAccessible(path)) {
                // Обновить список устройств
            }
        }
    }
    
    private fun onMediaUnmounted(path: String) {
        serviceScope.launch {
            // Логика обработки отключения носителя
        }
    }
}
