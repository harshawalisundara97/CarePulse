package com.carepulse.app

import android.app.Application

/**
 * Application entry. Kept minimal — repository state is held in a singleton
 * object, so no DI graph is required for this mock-data build.
 */
class CarePulseApplication : Application()
