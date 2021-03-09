package com.showmethe.myapplication

import android.app.Application
import com.show.blurlayout.Util

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Util.init(this)
    }
}