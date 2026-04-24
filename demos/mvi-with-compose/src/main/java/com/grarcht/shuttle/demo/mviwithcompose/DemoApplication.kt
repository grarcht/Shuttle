package com.grarcht.shuttle.demo.mviwithcompose

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The application class for the MVI with Compose demo. It initializes Hilt for
 * dependency injection across the demo module.
 */
@HiltAndroidApp
class DemoApplication : Application()
