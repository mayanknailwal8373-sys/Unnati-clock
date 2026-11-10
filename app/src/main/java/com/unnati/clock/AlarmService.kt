package com.unnati.clock
import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.media.*
import android.net.Uri
import android.os.*

class AlarmService : Service() {
    private var mp: MediaPlayer? = null
    private val aa = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        val name = i?.getStringExtra("name") ?: "Alarm"
        val id = i?.getIntExtra("id", 0) ?: 0
        when (i?.action) {
            "STOP" -> { end(); return START_NOT_STICKY }
            "SNOOZE" -> {
                // Snooze: sound stops now; alarm re-fires after N min and restarts from the fixed intro MP3
                Scheduler.at(this, System.currentTimeMillis() + Store.snooze(this) * 60000L, id + 1000000, id, name)
                end(); return START_NOT_STICKY
            }
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("al", "Alarms", NotificationManager.IMPORTANCE_HIGH))
        val ai = PendingIntent.getActivity(this, 1, Intent(this, AlarmActivity::class.java).putExtra("name", name).putExtra("id", id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(this, "al").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(name).setContentText("UNNATI CLOCK")
            .setCategory(Notification.CATEGORY_ALARM).setFullScreenIntent(ai, true).setContentIntent(ai).setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(7, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(7, n)
        play()
        if (Store.vib(this)) getSystemService(Vibrator::class.java).vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 400), 0))
        return START_NOT_STICKY
    }

    private fun go(src: (MediaPlayer) -> Unit, loop: Boolean, next: (() -> Unit)?) {
        try {
            val p = MediaPlayer(); p.setAudioAttributes(aa); src(p); p.isLooping = loop
            val v = Store.vol(this); p.setVolume(v, v)
            p.setOnCompletionListener { it.release(); if (mp === it) mp = null; next?.invoke() }
            p.setOnErrorListener { _, _, _ -> next?.invoke(); true }
            p.setOnPreparedListener { it.start() }
            mp = p; p.prepareAsync()
        } catch (e: Exception) { next?.invoke() }
    }
    // Custom ringtone loops until Stop/Snooze (default alarm tone if none chosen)
    private fun main() {
        val u = Store.ring(this)?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        go({ it.setDataSource(this, u) }, true, null)
    }
    // Fixed intro MP3 (res/raw/fixed_intro_ringtone.mp3) ALWAYS plays first, then main()
    private fun play() {
        val r = resources.getIdentifier("fixed_intro_ringtone", "raw", packageName)
        if (r != 0) go({ val a = resources.openRawResourceFd(r); it.setDataSource(a.fileDescriptor, a.startOffset, a.length); a.close() }, false) { main() } else main()
    }
    private fun end() {
        mp?.release(); mp = null
        getSystemService(Vibrator::class.java).cancel()
        stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
    }
    override fun onDestroy() { mp?.release(); mp = null; super.onDestroy() }
}
