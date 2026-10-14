package com.smsoft.smartdisplay.utils

/**
 * Whether the concrete topic [topic] of an incoming message is matched by the topic [filter] a
 * setting holds. "+" stands for one level and "#" for all levels below it (and the level itself),
 * as in MQTT 3.1.1 section 4.7, so a setting "stat/+/POWER" follows every device. A filter without
 * wildcards matches only the same topic, as before. Pure function, no Android.
 */
fun mqttTopicMatches(filter: String, topic: String): Boolean {
    if (filter.isEmpty()) {
        return false
    }
    if (filter == topic) {
        return true
    }
    // Topics of the broker itself ("$SYS/...") are not matched by a wildcard in the first level
    if (topic.startsWith('$') && (filter.startsWith('+') || filter.startsWith('#'))) {
        return false
    }
    val filterLevels = filter.split('/')
    val topicLevels = topic.split('/')
    for ((index, level) in filterLevels.withIndex()) {
        when (level) {
            // "#" is only valid as the last level
            "#" -> return index == filterLevels.lastIndex
            "+" -> if (index >= topicLevels.size) {
                return false
            }
            else -> if ((index >= topicLevels.size) || (topicLevels[index] != level)) {
                return false
            }
        }
    }
    return filterLevels.size == topicLevels.size
}
