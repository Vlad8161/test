package com.example.test

import com.example.test.ui.activity.MainActivity
import dagger.Component
import javax.inject.Singleton

/**
 * Created by vlad on 25.11.18.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun inject(mainActivityViewModel: MainActivity.MainActivityViewModel)
}