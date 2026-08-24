package com.liuflow.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrateUtil {
    fun short(context: Context) {
        if (!hasPermission(context)) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(VibratorManager::class.java)
                mgr?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(220, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vib = context.getSystemService(Vibrator::class.java)
                vib?.vibrate(VibrationEffect.createOneShot(220, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    fun pattern(context: Context) {
        if (!hasPermission(context)) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(VibratorManager::class.java)
                mgr?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                val vib = context.getSystemService(Vibrator::class.java)
                vib?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))
            }
        }
    }

    private fun hasPermission(context: Context): Boolean {
        val perm = "android.permission.VIBRATE"
        return context.checkCallingOrSelfPermission(perm) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
