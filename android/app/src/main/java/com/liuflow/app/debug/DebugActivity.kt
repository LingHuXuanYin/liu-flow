package com.liuflow.app.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.liuflow.app.FlowApp
import com.liuflow.app.ui.theme.FlowAppTheme

/**
 * CloudBase 调试面板入口（仅 DEBUG build 可见）。
 * 独立 Activity，不走 NavHost，方便直接看 12 个原始接口的响应。
 */
class DebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as FlowApp).container
        setContent {
            FlowAppTheme {
                DebugScreen(
                    container = container,
                    onBack = { finish() },
                )
            }
        }
    }
}
