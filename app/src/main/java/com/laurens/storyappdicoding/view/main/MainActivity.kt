package com.laurens.storyappdicoding.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.laurens.storyappdicoding.MenambahkanCerita.MenambahkanCeritaActivity
import com.laurens.storyappdicoding.R
import com.laurens.storyappdicoding.databinding.ActivityMainBinding
import com.laurens.storyappdicoding.view.LoadingPaging.LoadingStateAdapter
import com.laurens.storyappdicoding.view.ModelFacotry.ViewModelFactory
import com.laurens.storyappdicoding.view.maps.MapsActivity
import com.laurens.storyappdicoding.view.welcome.WelcomeActivity


class MainActivity : AppCompatActivity() {
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding  = ActivityMainBinding.inflate(layoutInflater)
        val layoutManager = LinearLayoutManager(this)
        mainBinding .rvStories.layoutManager = layoutManager
        setContentView(mainBinding .root)

        mainViewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
        setupView()
        setupMainAction()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        loadStories()
        supportActionBar?.hide()
    }


    private fun loadStories() {
        val storyAdapter = MainAdapter()
        val extraToken = intent.getStringExtra(EXTRA_TOKEN).toString()
        mainBinding.rvStories.adapter = storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                storyAdapter.retry()
            }
        )
        mainViewModel.getSession().observe(this) { user ->
            var token = user.token
            if (token == null) {
                token = extraToken
            }
            mainViewModel.getCerita(token).observe(this) { pagingData ->
                storyAdapter.submitData(lifecycle, pagingData)
            }
        }
    }

    private fun setupMainAction() {
        mainBinding .topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    mainViewModel.logout()
                    true

                }

                R.id.menu_language -> {
                    navigateToLocaleSettings()
                    true
                }

                R.id.maps -> {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        mainBinding .btnAdd.setOnClickListener {
            startActivity(Intent(this, MenambahkanCeritaActivity::class.java))
        }
    }

    private fun navigateToLocaleSettings() {
        startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
    }

    override fun onResume() {
        super.onResume()
        loadStories()
    }

    companion object {
        const val EXTRA_TOKEN = "extra_token"
    }
}