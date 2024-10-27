package com.gummybearstudio.tflitetester

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gummybearstudio.tflitetester.databinding.RootActivityBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: RootActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = RootActivityBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }
}
