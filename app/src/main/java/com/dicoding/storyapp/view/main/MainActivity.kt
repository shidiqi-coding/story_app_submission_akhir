package com.dicoding.storyapp.view.main

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.view.WelcomeActivity
import com.dicoding.storyapp.view.detail.DetailActivity
import com.dicoding.storyapp.view.helper.LocaleHelper
import com.dicoding.storyapp.view.map.MapsActivity
import com.dicoding.storyapp.view.newstory.NewStoryActivity
import com.dicoding.storyapp.view.setting.SettingActivity
import com.dicoding.storyapp.view.setting.SettingPreferences
import com.dicoding.storyapp.view.widget.ImagesBannerWidget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var adapter: ListStoryAdapter

    override fun attachBaseContext(newBase: Context?) {
        val langCode = LocaleHelper.getSavedLanguage(newBase ?: return)
        val contextWithLocale = LocaleHelper.applyLanguage(newBase, langCode)
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        applyThemeFromPreferences()
        setLocaleFromPreferences()

        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeStories()
        setUpGreeting()

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, NewStoryActivity::class.java)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                androidx.core.util.Pair(binding.fabAddStory, "fab_add_story")
            )
            startActivity(intent, options.toBundle())
        }
    }

    private fun setUpGreeting() {
        val userPref = UserPreference.getInstance(this)
        lifecycleScope.launch {
            userPref.getSession().collect { user ->
                val greeting = getString(R.string.greeting, user.name)
                binding.greetingId.text = greeting
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ListStoryAdapter { story, imageView, nameView, descView ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_STORY_ID, story.id)
            }

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                androidx.core.util.Pair(imageView as View, imageView.transitionName),
                androidx.core.util.Pair(nameView as View, nameView.transitionName),
                androidx.core.util.Pair(descView as View, descView.transitionName),
            )

            startActivity(intent, options.toBundle())
        }

        binding.rvStoryList.layoutManager = LinearLayoutManager(this)
        binding.rvStoryList.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener {loadState ->
            binding.menuPB.visibility =
                if (loadState.source.refresh is androidx.paging.LoadState.Loading) View.VISIBLE
            else View.GONE

            val errorState = loadState.source.append as? androidx.paging.LoadState.Error
                ?: loadState.source.prepend as? androidx.paging.LoadState.Error
                ?: loadState.refresh as? androidx.paging.LoadState.Error
                ?: loadState.append as? androidx.paging.LoadState.Error

            errorState?.let {
                android.widget.Toast.makeText(
                    this,
                    "Error: ${it.error.localizedMessage}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun observeStories() {
        lifecycleScope.launch {
            viewModel.stories.collectLatest { pagingData ->
                adapter.submitData(pagingData)
                refreshWidgetStackView()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun refreshWidgetStackView() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, ImagesBannerWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        if (menu != null && menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val method = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_setting -> {
                startActivity(Intent(this, SettingActivity::class.java))
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.action_map -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        val alertDialog = AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.logout_popup_text))
            setMessage(getString(R.string.logout_message))
            setPositiveButton(getString(R.string.yes_button)) { _, _ ->
                lifecycleScope.launch {
                    val userPref = UserPreference.getInstance(this@MainActivity)
                    userPref.logout()
                    val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            setNegativeButton(getString(R.string.cancel_button), null)
        }.create()

        alertDialog.setOnShowListener {
            alertDialog.findViewById<TextView>(android.R.id.message)?.setTextColor(
                ContextCompat.getColor(this, R.color.text_color)
            )

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getColor(R.color.button_color))

            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(getColor(R.color.button_color))
        }

        alertDialog.show()
    }

    private fun applyThemeFromPreferences() {
        val settingPref = SettingPreferences.getInstance(this)
        lifecycleScope.launch {
            settingPref.getThemeSetting().collect { theme ->
                when (theme) {
                    0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
    }

    private fun setLocaleFromPreferences() {
        val settingPref = SettingPreferences.getInstance(this)
        lifecycleScope.launch {
            val langCode = settingPref.getLanguageSetting().first() ?: "en"
            setLocale(this@MainActivity, langCode)
        }
    }

    @Suppress("DEPRECATION")
    private fun setLocale(context: Context , langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
