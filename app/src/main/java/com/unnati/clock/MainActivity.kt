package com.unnati.clock
import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
    private lateinit var box: LinearLayout
    override fun onCreate(b: android.os.Bundle?) {
        super.onCreate(b)
        val root = ScrollView(this)
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 64, 32, 32); setBackgroundColor(0xFFFFE4EE.toInt()) }
        root.addView(box); setContentView(root)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        val am = getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms())
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
        draw()
    }
    private fun btn(t: String, f: () -> Unit) { box.addView(Button(this).apply { text = t; setOnClickListener { f() } }) }
    private fun draw() {
        box.removeAllViews()
        box.addView(TextView(this).apply { text = "🌸 UNNATI CLOCK"; textSize = 26f })
        btn("➕ Add alarm") {
            TimePickerDialog(this, { _, h, m ->
                val l = Store.all(this); val a = Alarm((System.currentTimeMillis() % 1000000).toInt(), h, m, "Good Morning", 127, true)
                l.add(a); Store.save(this, l); Scheduler.set(this, a); draw()
            }, 6, 0, false).show()
        }
        btn("🎵 Pick custom ringtone") { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).setType("audio/*").addCategory(Intent.CATEGORY_OPENABLE), 5) }
        btn("🔋 Battery optimisation settings") { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
        val me = this
        Store.all(this).forEach { a ->
            box.addView(Switch(this).apply {
                text = "%02d:%02d  %s  (long-press = delete)".format(a.h, a.m, a.name); textSize = 18f; isChecked = a.on
                setOnCheckedChangeListener { _, c ->
                    val l = Store.all(me); l.find { it.id == a.id }?.on = c; Store.save(me, l)
                    a.on = c; if (c) Scheduler.set(me, a) else Scheduler.cancel(me, a)
                }
                setOnLongClickListener { Scheduler.cancel(me, a); Store.save(me, Store.all(me).filter { it.id != a.id }); draw(); true }
            })
        }
    }
    @Deprecated("simple picker")
    override fun onActivityResult(rc: Int, res: Int, d: Intent?) {
        super.onActivityResult(rc, res, d)
        val u = d?.data ?: return
        if (rc == 5) {
            contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            Store.setRing(this, u.toString()); Toast.makeText(this, "Ringtone set ✓", Toast.LENGTH_SHORT).show()
        }
    }
}
