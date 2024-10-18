package com.abdushakoor12.image_file_caching_playground

import android.app.Application

class App: Application() {

    val imageDownloader by lazy {
        ImageDownloader(this.applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
    }
}