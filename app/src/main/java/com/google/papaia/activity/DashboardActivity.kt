package com.google.papaia.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.papaia.R

class DashboardActivity : AppCompatActivity() {
    private lateinit var navHome: LinearLayout
    private lateinit var navScan: LinearLayout
    private lateinit var navProfile: LinearLayout

    private lateinit var iconHome: ImageView
    private lateinit var iconScan: ImageView
    private lateinit var iconProfile: ImageView

    private lateinit var labelHome: TextView
    private lateinit var labelScan: TextView
    private lateinit var labelProfile: TextView

    private lateinit var scanButtonFrame: FrameLayout

    // Fragments
    private lateinit var homeFragment: HomeFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var scanFragment: ScanFragment
    private var activeFragment: Fragment? = null

    private var selectedTab = 0 // 0 = Home, 1 = Scan, 2 = Profile
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        initFragments()
        setupClickListeners()

        selectTab(0) // Select Home by default
    }

    private fun initViews() {
        // Navigation items
        navHome = findViewById(R.id.nav_home)
        navScan = findViewById(R.id.nav_scan)
        navProfile = findViewById(R.id.nav_profile)

        // Icons
        iconHome = findViewById(R.id.icon_home)
        iconScan = findViewById(R.id.icon_scan)
        iconProfile = findViewById(R.id.icon_profile)

        // Labels
        labelHome = findViewById(R.id.label_home)
        labelScan = findViewById(R.id.label_scan)
        labelProfile = findViewById(R.id.label_profile)

        // Scan button frame
        scanButtonFrame = navScan.getChildAt(0) as FrameLayout
    }

    private fun initFragments() {
        homeFragment = HomeFragment.newInstance("", "")
        profileFragment = ProfileFragment.newInstance("", "")
        scanFragment = ScanFragment.newInstance()

        // Add both, but show only home
        supportFragmentManager.beginTransaction()
            .add(R.id.container, profileFragment, "PROFILE")
            .hide(profileFragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.container, homeFragment, "HOME")
            .commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.container, scanFragment, "SCAN")
            .hide(scanFragment)
            .commit()

        activeFragment = homeFragment
    }

    private fun setupClickListeners() {
        navHome.setOnClickListener { selectTab(0) }
        navScan.setOnClickListener { selectTab(1) }
        navProfile.setOnClickListener { selectTab(2) }
    }

    private fun selectTab(tabIndex: Int) {
        selectedTab = tabIndex

        when (tabIndex) {
            0 -> {
                resetOtherTabs(0)
                animateTabSelection(iconHome, labelHome)
                switchFragment(homeFragment)
            }
            1 -> {
                resetOtherTabs(1)
                animateScanSelection()
                switchFragment(scanFragment)
            }
            2 -> {
                resetOtherTabs(2)
                animateTabSelection(iconProfile, labelProfile)
                switchFragment(profileFragment)
            }
        }
    }
    private fun switchFragment(targetFragment: Fragment) {
        if (activeFragment != targetFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment!!)
                .show(targetFragment)
                .commit()
            activeFragment = targetFragment
        }
    }

    private fun resetOtherTabs(selectedTabIndex: Int) {
        // Reset Home tab only if it's not selected
        if (selectedTabIndex != 0) {
            iconHome.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray))
            labelHome.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            resetScale(iconHome)
            resetScale(labelHome)
        }

        // Reset Profile tab only if it's not selected
        if (selectedTabIndex != 2) {
            iconProfile.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray))
            labelProfile.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            resetScale(iconProfile)
            resetScale(labelProfile)
        }

        // Reset Scan button only if it's not selected
        if (selectedTabIndex != 1) {
            val orangeDrawable = ContextCompat.getDrawable(this, R.drawable.scan_button_background)?.mutate()
            orangeDrawable?.setTint(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            scanButtonFrame.background = orangeDrawable
            labelScan.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            resetScale(scanButtonFrame)
            resetScale(iconScan)
            resetScale(labelScan)
        }
    }

    private fun resetScale(view: View) {
        view.scaleX = 1.0f
        view.scaleY = 1.0f
        view.translationY = 0f
    }

    private fun animateTabSelection(icon: ImageView, label: TextView) {
        val selectedColor = ContextCompat.getColor(this, android.R.color.holo_green_dark) // #4CAF50 equivalent

        // Pop-up animation for icon and label
        val popUpSet = AnimatorSet()

        // Icon animation
        val iconScaleX = ObjectAnimator.ofFloat(icon, "scaleX", 1.0f, 1.3f, 1.1f)
        val iconScaleY = ObjectAnimator.ofFloat(icon, "scaleY", 1.0f, 1.3f, 1.1f)
        val iconTranslationY = ObjectAnimator.ofFloat(icon, "translationY", 0f, -8f, -4f)

        // Label animation
        val labelScaleX = ObjectAnimator.ofFloat(label, "scaleX", 1.0f, 1.1f, 1.05f)
        val labelScaleY = ObjectAnimator.ofFloat(label, "scaleY", 1.0f, 1.1f, 1.05f)

        // Color change animation
        val iconColorAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(this, android.R.color.darker_gray),
            selectedColor
        )
        iconColorAnimator.addUpdateListener { animation ->
            icon.setColorFilter(animation.animatedValue as Int)
        }

        val textColorAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(this, android.R.color.darker_gray),
            selectedColor
        )
        textColorAnimator.addUpdateListener { animation ->
            label.setTextColor(animation.animatedValue as Int)
        }

        popUpSet.playTogether(
            iconScaleX, iconScaleY, iconTranslationY,
            labelScaleX, labelScaleY,
            iconColorAnimator, textColorAnimator
        )

        popUpSet.duration = 300
        popUpSet.interpolator = OvershootInterpolator(1.2f)
        popUpSet.start()
    }

    private fun animateScanSelection() {
        // Scan button selection animation
        val scanAnimSet = AnimatorSet()

        // Scale animation for the circular background
        val backgroundScaleX = ObjectAnimator.ofFloat(scanButtonFrame, "scaleX", 1.0f, 1.2f, 1.1f)
        val backgroundScaleY = ObjectAnimator.ofFloat(scanButtonFrame, "scaleY", 1.0f, 1.2f, 1.1f)

        // Icon animation
        val iconScaleX = ObjectAnimator.ofFloat(iconScan, "scaleX", 1.0f, 1.3f, 1.0f)
        val iconScaleY = ObjectAnimator.ofFloat(iconScan, "scaleY", 1.0f, 1.3f, 1.0f)
        val rotation = ObjectAnimator.ofFloat(iconScan, "rotation", 0f, 360f)

        // Label animation
        val labelScaleX = ObjectAnimator.ofFloat(labelScan, "scaleX", 1.0f, 1.2f, 1.1f)
        val labelScaleY = ObjectAnimator.ofFloat(labelScan, "scaleY", 1.0f, 1.2f, 1.1f)

        // Color animation for the background - change to green when selected
        val backgroundColorAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(this, android.R.color.holo_orange_dark), // From orange
            ContextCompat.getColor(this, android.R.color.holo_green_dark)    // To green
        )
        backgroundColorAnimator.addUpdateListener { animation ->
            val drawable = ContextCompat.getDrawable(this, R.drawable.scan_button_background)?.mutate()
            drawable?.setTint(animation.animatedValue as Int)
            scanButtonFrame.background = drawable
        }

        // Label color animation to match background
        val labelColorAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(this, android.R.color.holo_orange_dark), // From orange
            ContextCompat.getColor(this, android.R.color.holo_green_dark)    // To green
        )
        labelColorAnimator.addUpdateListener { animation ->
            labelScan.setTextColor(animation.animatedValue as Int)
        }

        scanAnimSet.playTogether(
            backgroundScaleX, backgroundScaleY,
            iconScaleX, iconScaleY, rotation,
            labelScaleX, labelScaleY,
            backgroundColorAnimator, labelColorAnimator
        )

        scanAnimSet.duration = 400
        scanAnimSet.interpolator = OvershootInterpolator(1.2f)
        scanAnimSet.start()

        // Add camera functionality here
        scanButtonFrame.postDelayed({
            // Your camera logic here
            openCamera()
        }, 200)
    }

    private fun openCamera() {
        // Add your camera opening logic here
        // For example:
        // val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    // Method to programmatically change tabs
    fun changeTab(tabIndex: Int) {
        if (tabIndex in 0..2) {
            selectTab(tabIndex)
        }
    }

    // Getter for current selected tab
    fun getSelectedTab(): Int = selectedTab

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
}