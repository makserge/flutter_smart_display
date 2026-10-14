package com.smsoft.smartdisplay.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The topic settings of the push button, doorbell, message alert and proximity light. */
class MqttTopicTest {

    @Test
    fun withoutWildcardsOnlyTheSameTopicMatches() {
        assertTrue(mqttTopicMatches("doorbell", "doorbell"))
        assertFalse(mqttTopicMatches("doorbell", "doorbell/x"))
        assertFalse(mqttTopicMatches("doorbell", "Doorbell"))
        assertFalse(mqttTopicMatches("stat/plug/POWER", "stat/plug"))
    }

    @Test
    fun plusMatchesExactlyOneLevel() {
        assertTrue(mqttTopicMatches("stat/+/POWER", "stat/plug1/POWER"))
        assertFalse(mqttTopicMatches("stat/+/POWER", "stat/POWER"))
        assertFalse(mqttTopicMatches("stat/+/POWER", "stat/a/b/POWER"))
        assertFalse(mqttTopicMatches("stat/+", "stat/a/b"))
    }

    @Test
    fun hashMatchesTheLevelsBelowAndTheParent() {
        assertTrue(mqttTopicMatches("stat/#", "stat/plug1/POWER"))
        assertTrue(mqttTopicMatches("stat/#", "stat"))
        assertTrue(mqttTopicMatches("#", "anything/at/all"))
        assertFalse(mqttTopicMatches("stat/#", "cmnd/plug1/POWER"))
    }

    @Test
    fun aHashNotLastIsNoFilter() {
        assertFalse(mqttTopicMatches("stat/#/POWER", "stat/plug1/POWER"))
    }

    @Test
    fun brokerTopicsAreNotMatchedByAFirstLevelWildcard() {
        assertFalse(mqttTopicMatches("+/info", "\$SYS/info"))
        assertFalse(mqttTopicMatches("#", "\$SYS/broker/uptime"))
        assertTrue(mqttTopicMatches("\$SYS/#", "\$SYS/broker/uptime"))
    }

    @Test
    fun anEmptyFilterMatchesNothing() {
        assertFalse(mqttTopicMatches("", ""))
        assertFalse(mqttTopicMatches("", "stat/plug"))
    }
}
