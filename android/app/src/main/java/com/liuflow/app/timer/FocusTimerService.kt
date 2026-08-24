package com.liuflow.app.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.liuflow.app.FlowApp
import com.liuflow.app.MainActivity
import com.liuflow.app.R
import com.liuflow.app.util.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps the [TimerController] alive while the app
 * is backgrounded and shows an ongoing notification with the remaining
 * time. Tapping the notification brings the user back to the app.
 *
 * Lifecycle:
 *  - Started by [TimerServiceController] whenever the timer enters one of
 *    {RUNNING, PAUSED, RESTING}.
 *  - Stops itself when the timer returns to {IDLE, COMPLETED, ABANDONED}.
 *  - The OS may kill the service under memory pressure; if so, the
 *    TimerController state is lost (acceptable for the MVP).
 */
class FocusTimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Must be called within 5s of onStartCommand; do it before anything else.
        startForeground(NOTIFICATION_ID, buildFocusNotification("准备中", "—"))
        val timer = (application as FlowApp).container.timer
        observerJob = scope.launch {
            timer.state.collect { state ->
                val n = when (state.phase) {
                    TimerController.Phase.RUNNING -> buildFocusNotification(
                        title = state.task.ifBlank { "未命名专注" },
                        text = "剩余 ${TimeFormat.mmss(state.remainingSeconds)}",
                    )
                    TimerController.Phase.PAUSED -> buildFocusNotification(
                        title = state.task.ifBlank { "未命名专注" },
                        text = "已暂停 · 剩余 ${TimeFormat.mmss(state.remainingSeconds)}",
                    )
                    TimerController.Phase.RESTING -> buildRestNotification(
                        text = "剩余 ${TimeFormat.mmss(state.remainingSeconds)}",
                    )
                    TimerController.Phase.COMPLETED -> buildCompleteNotification()
                    TimerController.Phase.ABANDONED,
                    TimerController.Phase.IDLE -> {
                        stopSelf()
                        return@collect
                    }
                }
                NotificationManagerCompat.from(this@FocusTimerService)
                    .notify(NOTIFICATION_ID, n)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        observerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "专注计时",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "专注和休息的倒计时进度"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    private fun buildFocusNotification(title: String, text: String): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_focus)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun buildRestNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_focus)
            .setContentTitle("休息一下")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun buildCompleteNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_focus)
            .setContentTitle("完成")
            .setContentText("一次专注圆满结束")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "flow_focus"
        private const val NOTIFICATION_ID = 1
    }
}
