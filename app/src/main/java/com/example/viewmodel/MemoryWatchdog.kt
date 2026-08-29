package com.example.viewmodel

import android.content.Context

enum class MemoryPressure { NORMAL, MODERATE, HIGH, CRITICAL }

class MemoryWatchdog(private val context: Context) {

    private val runtime = Runtime.getRuntime()
    private val maxMemory = runtime.maxMemory()

    fun checkMemoryPressure(): MemoryPressure {
        val used = runtime.totalMemory() - runtime.freeMemory()
        val ratio = used.toFloat() / maxMemory.toFloat()

        return when {
            ratio > 0.85f -> {
                emergencyCleanup()
                MemoryPressure.CRITICAL
            }
            ratio > 0.70f -> {
                moderateCleanup()
                MemoryPressure.HIGH
            }
            ratio > 0.55f -> {
                MemoryPressure.MODERATE
            }
            else -> {
                MemoryPressure.NORMAL
            }
        }
    }

    private fun moderateCleanup() {
        // Clear image cache (simplified representation)
        System.gc() // Nudge
    }

    private fun emergencyCleanup() {
        // Critical cleanup
        System.gc() // Nudge
    }
}
