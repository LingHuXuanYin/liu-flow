package com.liuflow.app

import android.content.Context
import android.util.Log
import com.liuflow.app.data.db.AppDatabase
import com.liuflow.app.data.prefs.SettingsRepository
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.timer.TimerController
import com.liuflow.app.timer.TimerServiceController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * Manual service-locator. We intentionally avoid Hilt to keep the project
 * easy to read and to remove a build-time annotation processor.
 */
class AppContainer(appContext: Context) {

    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val db = AppDatabase.get(appContext)

    val settingsRepository = SettingsRepository(appContext)
    val flowRepository = FlowRepository(db.sessionDao(), db.dailyGoalDao())

    val timer = TimerController(appScope)

    /**
     * Wires the foreground service to the timer; instantiated eagerly so
     * the observer is collecting from process start.
     */
    @Suppress("unused")
    val timerServiceController = TimerServiceController(appContext, timer)

    init {
        logStorageLocations(appContext)
    }

    /**
     * Print the actual on-disk locations of the DataStore and Room
     * files so the user can sanity-check that auto-backup rules cover
     * them. Filtered to debug builds in release builds, the log
     * statement is still cheap but won't show in Logcat by default.
     */
    private fun logStorageLocations(appContext: Context) {
        val ds = File(appContext.filesDir, "datastore/flow_settings.preferences_pb")
        val dbFile = appContext.getDatabasePath("flow.db")
        Log.i(TAG, "DataStore: $ds (exists=${ds.exists()})")
        Log.i(TAG, "Room: $dbFile (exists=${dbFile.exists()})")
        Log.i(TAG, "Backup coverage: domain=file path=datastore/ -> covers DataStore")
        Log.i(TAG, "Backup coverage: domain=database path=.   -> covers Room")
    }

    companion object {
        private const val TAG = "FlowInit"
    }
}
