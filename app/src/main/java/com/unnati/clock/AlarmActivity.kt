package com.unnati.clock
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.*

class AlarmActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val n = intent.getStringExtra("name") ?: "Alarm"
        val id = intent.getIntExtra("id", 0)
        val sp = getSharedPreferences("u", 0)
        val k = sp.getInt("k", 0); sp.edit().putInt("k", k + 1).apply()   // different Krishna image every alarm
        val r = resources.getIdentifier("krishna_${k % 5 + 1}", "drawable", packageName)
        val img = ImageView(this).apply { scaleType = ImageView.ScaleType.FIT_CENTER; adjustViewBounds = true; if (r != 0) setImageResource(r) }  // FIT_CENTER = no crop
        fun send(a: String) { startService(Intent(this, AlarmService::class.java).setAction(a).putExtra("id", id).putExtra("name", n)); finish() }
        fun btn(t: String, a: String) = Button(this).apply { text = t; textSize = 20f; setOnClickListener { send(a) } }
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER; addView(btn("😴 Snooze", "SNOOZE")); addView(btn("⏹ Stop", "STOP")) }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setBackgroundColor(0xFFFFE4EE.toInt()); gravity = Gravity.CENTER; setPadding(24, 48, 24, 48)
            addView(TextView(context).apply { text = n; textSize = 28f; gravity = Gravity.CENTER; setTextColor(Color.rgb(107, 31, 69)) })
            addView(img, LinearLayout.LayoutParams(-1, 0, 1f))
            addView(row)
        })
    }
}
