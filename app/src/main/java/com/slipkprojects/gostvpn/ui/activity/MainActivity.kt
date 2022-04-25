package com.slipkprojects.gostvpn.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.slipkprojects.gostvpn.R
import com.slipkprojects.gostvpn.databinding.ActivityMainBinding
import com.slipkprojects.gostvpn.ui.viewmodel.HomeViewModel
import com.slipkprojects.gostvpn.ui.adapter.ScreenSlidePagerAdapter
import com.slipkprojects.gostvpn.ui.fragment.HomeTabFragment
import com.slipkprojects.gostvpn.ui.fragment.LogsTabFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<HomeViewModel>()

    private lateinit var tabAdapter: ScreenSlidePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycle.addObserver(viewModel)

        // set ScreenSlidePagerAdapter
        tabAdapter = ScreenSlidePagerAdapter(this)
        tabAdapter.addTab(R.string.tab_home, HomeTabFragment::class.java)
        tabAdapter.addTab(R.string.tab_logs, LogsTabFragment::class.java)
        // set adapter to pager
        binding.pager.adapter = tabAdapter
        // attach tab to pager
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = tabAdapter.getTitleByPosition(position)
        }.attach()


        subscribeUi()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miAbout) {
            Intent(this, AboutActivity::class.java).apply {
                startActivity(this)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }*/


    private fun subscribeUi() {
        viewModel.promptMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.also {
                Toast.makeText(this, it, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


}