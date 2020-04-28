package dev.gregwhatley.cse3310.team3animal

class SessionStatistics {
    companion object {
        var counts: MutableMap<String, Int> = HashMap<String, Int>().apply {
            Classifier.labels.forEach { put(it, 0) }
        }
        var correct = 0
        var incorrect = 0
        val total
            get() = correct + incorrect
        val correctRatio
            get() = if (total == 0) 0f else correct.toFloat() / total.toFloat()

        fun correct(label: String) {
            correct++
            counts[label] = (counts[label] ?: 0) + 1
        }

        fun incorrect(label: String) {
            incorrect++
        }
    }
}