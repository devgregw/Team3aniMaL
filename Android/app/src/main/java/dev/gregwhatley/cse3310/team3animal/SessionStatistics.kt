package dev.gregwhatley.cse3310.team3animal

class SessionStatistics {
    companion object {
        // Map of classification attempts per label
        var counts: MutableMap<String, Int> = HashMap<String, Int>().apply {
            Classifier.labels.forEach { put(it, 0) }
        }

        var correct = 0
        var incorrect = 0

        // Sum of classification attempts (does not include attempts without feedback)
        val total
            get() = correct + incorrect

        // Percentage of correct classifications
        val correctRatio
            get() = if (total == 0) 0f else correct.toFloat() / total.toFloat()

        // Increments the correct counter and label counter
        fun correct(label: String) {
            correct++
            counts[label] = (counts[label] ?: 0) + 1
        }

        // Increments the incorrect counter and label counter
        fun incorrect(label: String) {
            incorrect++
            counts[label] = (counts[label] ?: 0) + 1
        }
    }
}