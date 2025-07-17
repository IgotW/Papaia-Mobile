package com.google.papaia.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.papaia.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val bottomAppBar = findViewById<BottomAppBar>(R.id.bottomAppBar)
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        val navigateTo = intent.getStringExtra("navigateTo")

        if (navigateTo == "profile") {
            bottomNavigationView.selectedItemId = R.id.nav_profile
            loadFragment(ProfileFragment())
        } else {
            bottomNavigationView.selectedItemId = R.id.nav_home
            loadFragment(HomeFragment())
        }

//        bottomNavigationView.selectedItemId = R.id.nav_home
//        val badgeDrawable: BadgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_profile)
//        badgeDrawable.number = 99
//        badgeDrawable.isVisible = true
//        loadFragment(HomeFragment())

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        fab.setOnClickListener {
            // Handle FAB click
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}