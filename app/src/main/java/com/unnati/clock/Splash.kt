package com.unnati.clock
import android.app.Activity
import android.content.*
import android.graphics.*
import android.os.*
import android.view.*
import android.widget.*
import java.util.*

/** Live fireworks: rockets rise, burst (circle / ring / heart), sparks fall with trails. Tap = launch rocket. */
class FireworksView(c: Context) : View(c) {
    private class P(var x: Float, var y: Float, var vx: Float, var vy: Float, var l: Float, val d: Float, val col: Int)
    private class Rk(var x: Float, var y: Float, val ty: Float, var vy: Float, val col: Int)
    private val ps = ArrayList<P>(); private val rs = ArrayList<Rk>()
    private val pt = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 5f; strokeCap = Paint.Cap.ROUND }
    private val cols = intArrayOf(0xFFFF2D87.toInt(), 0xFFA855F7.toInt(), 0xFFFF9F1C.toInt(), 0xFF22D3EE.toInt(), 0xFFEF4444.toInt(), 0xFF22C55E.toInt(), 0xFFFACC15.toInt())
    private val rnd = Random(); private val dn = resources.displayMetrics.density; private var last = 0L
    private fun rc() = cols[rnd.nextInt(cols.size)]
    fun launch(tx: Float? = null, ty: Float? = null) {
        rs.add(Rk(tx ?: width * (.15f + rnd.nextFloat() * .7f), height.toFloat(), ty ?: height * (.12f + rnd.nextFloat() * .33f), -(11 + rnd.nextFloat() * 3) * dn, rc()))
    }
    private fun boom(x: Float, y: Float, col: Int) {
        val type = rnd.nextInt(3); val n = if (type == 1) 90 else 70; val c2 = rc()
        for (i in 0 until n) {
            val vx: Float; val vy: Float; val a = rnd.nextDouble() * 6.283
            when (type) {
                0 -> { val s = 1.5 + rnd.nextDouble() * 4; vx = (Math.cos(a) * s).toFloat(); vy = (Math.sin(a) * s).toFloat() }
                1 -> { val t = i / n.toDouble() * 6.283
                    vx = (16 * Math.pow(Math.sin(t), 3.0) * .22).toFloat()
                    vy = (-(13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t)) * .22).toFloat() }
                else -> { vx = (Math.cos(a) * 3.4).toFloat(); vy = (Math.sin(a) * 3.4).toFloat() }
            }
            ps.add(P(x, y, vx * dn, vy * dn, 1f, .011f + rnd.nextFloat() * .01f, if (i % 3 != 0) col else c2))
        }
    }
    override fun onDraw(c: Canvas) {
        val now = SystemClock.uptimeMillis()
        if (width > 0 && now - last > 320) { launch(); last = now }
        val ri = rs.iterator()
        while (ri.hasNext()) {
            val r = ri.next(); r.y += r.vy; pt.color = r.col; pt.alpha = 255; c.drawCircle(r.x, r.y, 3 * dn, pt)
            if (r.y <= r.ty) { boom(r.x, r.y, r.col); ri.remove() }
        }
        val pi = ps.iterator()
        while (pi.hasNext()) {
            val p = pi.next(); val ox = p.x; val oy = p.y
            p.x += p.vx; p.y += p.vy; p.vx *= .985f; p.vy = p.vy * .985f + .045f * dn; p.l -= p.d
            if (p.l <= 0) { pi.remove(); continue }
            pt.color = p.col; pt.alpha = (255 * minOf(1f, p.l * 1.4f)).toInt(); c.drawLine(ox, oy, p.x, p.y, pt)
        }
        postInvalidateOnAnimation()
    }
    override fun onTouchEvent(e: MotionEvent): Boolean { if (e.action == MotionEvent.ACTION_DOWN) launch(e.x, e.y); return true }
}

class SplashActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val msgs = arrayOf("Aaj tum bahut achhe lag rahe ho!", "✨ Tumhari smile bahut special hai.", "🌸 Aaj ka din tumhare liye bahut khoobsurat ho.",
            "💖 Khud par bharosa rakho, tum sab kar sakte ho.", "🦚 Krishna ji hamesha tumhara bhala karein.", "🌷 Tum jitna sochte ho, usse kahin zyada special ho.")
        val m = msgs[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % 6]   // daily: same all day, new tomorrow
        var mv: TextView? = null
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(48, 48, 48, 48)
            addView(TextView(context).apply { text = "Aapke Husband aapse bahut pyaar karte hain! ❤️"; textSize = 26f; gravity = Gravity.CENTER; setTextColor(0xFF6B1F45.toInt()) })
            addView(TextView(context).apply { mv = this; alpha = 0f; text = m; textSize = 19f; gravity = Gravity.CENTER; setTextColor(0xFF6B1F45.toInt()); setPadding(0, 32, 0, 0) })
        }
        setContentView(FrameLayout(this).apply {
            setBackgroundColor(0xFFFFE4EE.toInt()); addView(col, -1, -1); addView(FireworksView(context), -1, -1)
        })
        val h = Handler(Looper.getMainLooper())
        h.postDelayed({ mv?.animate()?.alpha(1f)?.setDuration(800)?.start() }, 2000)   // motivation line appears after the husband message
        h.postDelayed({ startActivity(Intent(this, MainActivity::class.java)); finish() }, 5000)   // clock opens at 5 sec
    }
}
