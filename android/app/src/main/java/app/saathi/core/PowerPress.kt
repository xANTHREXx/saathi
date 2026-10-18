package app.saathi.core

/**
 * Counts screen on/off toggles caused by power-button presses.
 * Fires when [needed] toggles land within [windowS] seconds. [needed] = 0 turns it off.
 */
class PowerPress(var needed: Int = 3, private val windowS: Double = 2.0) {
    private val times = ArrayDeque<Long>()

    fun onToggle(atMs: Long): Boolean {
        if (needed <= 0) return false
        times.addLast(atMs)
        val windowMs = (windowS * 1000).toLong()
        while (times.isNotEmpty() && atMs - times.first() > windowMs) times.removeFirst()
        if (times.size < needed) return false
        times.clear()
        return true
    }
}
