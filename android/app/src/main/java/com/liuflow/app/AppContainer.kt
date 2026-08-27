package com.liuflow.app

import android.content.Context
import android.util.Log
import com.liuflow.app.auths.data.AuthManager
import com.liuflow.app.auths.data.CloudBaseAuthApi
import com.liuflow.app.data.db.AppDatabase
import com.liuflow.app.data.prefs.SettingsRepository
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.timer.TimerController
import com.liuflow.app.ui.session.SessionStateMachine
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

    /** Session state machine — encapsulates the 4 transitions of the
     *  start/running/rest state graph and the timer pause / resume /
     *  restart helpers. View models call into this and never touch
     *  [TimerController] directly. */
    val sessionStateMachine = SessionStateMachine(timer)

    /**
     * Wires the foreground service to the timer; instantiated eagerly so
     * the observer is collecting from process start.
     */
    @Suppress("unused")
    val timerServiceController = TimerServiceController(appContext, timer)

    // ----------------------------------------------------------------
    // V0.2.1 auths/：完全按 docs/腾讯云开发接入指引2026.md 文档示范风格
    //   - 一个 CloudBaseAuthApi（OkHttp + Gson 通用客户端）
    //   - 一个 AuthManager（SharedPreferences 存 token + 业务方法）
    //   - 删了 AuthRepository / AuthTokenStore / AuthInterceptor / 所有 DTO sealed class
    // ----------------------------------------------------------------
    val authApi = CloudBaseAuthApi()
    val authManager = AuthManager(appContext, authApi).also {
        // 启动时把本地存的 accessToken 注入 cloudbase 实例（让业务请求自动带 Authorization）
        it.restoreAccessToken()
        Log.i("AuthFlow", "[Init] AuthManager restored, hasToken=${it.accessToken() != null}, username='${it.username()}'")
    }

    init {
        Log.i("AuthFlow", "[Init] AppContainer init: BuildConfig.TCB_ENV_ID='${BuildConfig.TCB_ENV_ID}' length=${BuildConfig.TCB_ENV_ID.length} TCB_REGION='${BuildConfig.TCB_REGION}'")
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
