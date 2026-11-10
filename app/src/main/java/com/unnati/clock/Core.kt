package com.unnati.clock
import android.app.*
import android.content.*
import org.json.*
import java.util.Calendar

/** days: bitmask bit0=Sun..bit6=Sat, 127=daily, 0=once */
data class Alarm(val id: Int, var h: Int, var m: Int, var name: String, var days: Int, var on: Boolean)

object Store {
    private fun sp(c: Context) = c.getSharedPreferences("u", 0)
    fun all(c: Context): MutableList<Alarm> {
        val a = JSONArray(sp(c).getString("al", "[]"))
        return MutableList(a.length()) { val o = a.getJSONObject(it); Alarm(o.getInt("id"), o.getInt("h"), o.getInt("m"), o.getString("n"), o.getInt("d"), o.getBoolean("on")) }
    }
    fun save(c: Context, l: List<Alarm>) {
        val a = JSONArray()
        l.forEach { a.put(JSONObject().put("id", it.id).put("h", it.h).put("m", it.m).put("n", it.name).put("d", it.days).put("on", it.on)) }
        sp(c).edit().putString("al", a.toString()).apply()
    }
    fun ring(c: Context): String? = sp(c).getString("ring", null)
    fun setRing(c: Context, u: String) = sp(c).edit().putString("ring", u).apply()
    fun snooze(c: Context) = sp(c).getInt("snz", 5)
    fun vol(c: Context) = sp(c).getFloat("vol", 1f)
    fun vib(c: Context) = sp(c).getBoolean("vib", true)
}

object Scheduler {
    fun next(a: Alarm): Long {
        val now = Calendar.getInstance()
        for (d in 0..7) {
            val c = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, d); set(Calendar.HOUR_OF_DAY, a.h); set(Calendar.MINUTE, a.m); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val bit = (a.days shr (c.get(Calendar.DAY_OF_WEEK) - 1)) and 1
            if (c.after(now) && (a.days == 0 || bit == 1)) return c.timeInMillis
        }
        return 0
    }
    private fun pi(c: Context, code: Int, id: Int, name: String) = PendingIntent.getBroadcast(
        c, code, Intent(c, AlarmReceiver::class.java).putExtra("id", id).putExtra("name", name),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun at(c: Context, t: Long, code: Int, id: Int, name: String) {
        val show = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        c.getSystemService(AlarmManager::class.java).setAlarmClock(AlarmManager.AlarmClockInfo(t, show), pi(c, code, id, name))
    }
    fun set(c: Context, a: Alarm) { if (a.on) { val t = next(a); if (t > 0) at(c, t, a.id, a.id, a.name) } }
    fun cancel(c: Context, a: Alarm) = c.getSystemService(AlarmManager::class.java).cancel(pi(c, a.id, a.id, a.name))
    fun all(c: Context) = Store.all(c).forEach { set(c, it) }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val id = i.getIntExtra("id", 0)
        val l = Store.all(c)
        l.find { it.id == id }?.let { if (it.days == 0) { it.on = false; Store.save(c, l) } else Scheduler.set(c, it) }
        c.startForegroundService(Intent(c, AlarmService::class.java).putExtra("id", id).putExtra("name", i.getStringExtra("name")))
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) { Scheduler.all(c) }
}
