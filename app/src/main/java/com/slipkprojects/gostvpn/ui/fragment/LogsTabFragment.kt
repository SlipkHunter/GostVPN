package com.slipkprojects.gostvpn.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.slipkprojects.gostvpn.R
import com.slipkprojects.gostvpn.databinding.FragmentLogsTabBinding
import com.slipkprojects.gostvpn.ui.adapter.LogsAdapterList
import com.slipkprojects.gostvpn.ui.viewmodel.LogsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogsTabFragment: Fragment() {
    private val viewModel by viewModels<LogsViewModel>()

    private var binding: FragmentLogsTabBinding? = null
    private val logsAdapterList: LogsAdapterList by lazy { LogsAdapterList() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = FragmentLogsTabBinding.inflate(inflater)
        binding = view
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUi()
        setViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.logs_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miClearLogs) {
            viewModel.clearLogs()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeUi() {
        viewModel.logsGostService.observe(this) {
            logsAdapterList.submitList(it.toList())
        }
    }
    private fun setViews() {
        binding?.rvLogs?.apply {
            adapter = logsAdapterList
            layoutManager = LinearLayoutManager(context)
        }
    }
}