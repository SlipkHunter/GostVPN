package com.slipkprojects.gostvpn.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.slipkprojects.gostvpn.R
import com.slipkprojects.gostvpn.databinding.ActivityMainBinding
import com.slipkprojects.gostvpn.adapter.ScreenSlidePagerAdapter
import com.slipkprojects.gostvpn.fragment.HomeTabFragment
import com.slipkprojects.gostvpn.fragment.LogsTabFragment
import com.slipkprojects.gostvpn.viewmodel.HomeViewModel
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

    private fun subscribeUi() {
        viewModel.promptMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.also {
                Toast.makeText(this, it, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}