package com.aggregateservice.core.location

import android.app.Activity

/**
 * Android implementation of ContextProvider per D-07.
 */
class AndroidContextProvider(override val context: Activity) : ContextProvider