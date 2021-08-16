package dev.ahmedmourad.tvmanager.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.ahmedmourad.tvmanager.databinding.ActivityMainBinding
import dev.ahmedmourad.tvmanager.di.injector

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        injector.inject(this)
    }
}
