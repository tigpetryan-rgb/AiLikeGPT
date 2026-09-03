package com.ailikegpt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ailikegpt.app.hardware.DeviceCapabilities
import com.ailikegpt.app.runtime.NativeRuntime
import com.ailikegpt.app.ui.AiLikeGptApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hardware = DeviceCapabilities.read(this)
        val nativeStatus = NativeRuntime.status()

        setContent {
            AiLikeGptApp(
                hardware = hardware,
                nativeStatus = nativeStatus,
            )
        }
    }
}
