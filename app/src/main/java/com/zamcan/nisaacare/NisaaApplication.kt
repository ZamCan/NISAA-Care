package com.zamcan.nisaacare

import android.app.Application
import com.zamcan.nisaacare.core.engine.NisaaCareEngine

class NisaaApplication : Application() {
    lateinit var container: NisaaCareEngine
        private set

    override fun onCreate() {
        super.onCreate()
        container = NisaaCareEngine(this)
    }
}

/**
 * Source-compatibility alias for existing app code. There is only one concrete
 * application composition root: [NisaaCareEngine].
 */
typealias AppContainer = NisaaCareEngine
