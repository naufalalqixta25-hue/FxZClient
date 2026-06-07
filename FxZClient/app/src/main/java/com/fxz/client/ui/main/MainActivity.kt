package com.fxz.client.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fxz.client.R
import com.fxz.client.databinding.ActivityMainBinding
import com.fxz.client.utils.Extensions.gone
import com.fxz.client.utils.Extensions.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    val viewModel: MainViewModel by viewModels()

    // Fragments that HIDE the bottom nav
    private val hideNavFragments = setOf(
        R.id.downloadFragment,
        R.id.gameSetupFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in hideNavFragments) {
                binding.bottomNav.gone()
            } else {
                binding.bottomNav.visible()
            }
            // Slide nav bar animation
            val translationY = if (destination.id in hideNavFragments) 200f else 0f
            binding.bottomNav.animate()
                .translationY(translationY)
                .setDuration(250)
                .start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
