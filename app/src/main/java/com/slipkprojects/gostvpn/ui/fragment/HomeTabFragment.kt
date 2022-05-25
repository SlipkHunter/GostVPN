package com.slipkprojects.gostvpn.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.blacksquircle.ui.editorkit.model.UndoStack
import com.blacksquircle.ui.language.base.parser.LanguageParser
import com.blacksquircle.ui.language.json.JsonLanguage
import com.slipkprojects.gostvpn.R
import com.slipkprojects.gostvpn.databinding.FragmentHomeTabBinding
import com.slipkprojects.gostvpn.domain.model.GostSettings
import com.slipkprojects.gostvpn.ui.Utils
import com.slipkprojects.gostvpn.ui.activity.AboutActivity
import com.slipkprojects.gostvpn.ui.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class HomeTabFragment: Fragment() {
    private val viewModel by activityViewModels<HomeViewModel>()

    private var binding: FragmentHomeTabBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = FragmentHomeTabBinding.inflate(inflater)
        binding = view
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUi()
        setViews()
    }

    override fun onPause() {
        super.onPause()
        saveSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miAbout -> {
                Intent(activity, AboutActivity::class.java).apply {
                    startActivity(this)
                }
                return true
            }
            R.id.miSettingImportFromClipboard -> {
                if (viewModel.isEnabledGostService.value != true) {
                    val text = Utils.getLastFromClipboard(requireContext())
                    if (text != null) {
                        binding?.editor?.setTextContent(text.trim())
                    }
                } else {
                    Toast.makeText(context, "Desconecte antes para editar a configuração", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeUi() {
        viewModel.gostSettings.observe(this) {
            binding?.editor?.apply {
                setTextContent(it.settings)
                undoStack = UndoStack()
                redoStack = UndoStack()
            }
        }
        viewModel.isEnabledGostService.observe(this) {
            binding?.editor?.isEnabled = !it
            binding?.buttonStarter?.setText(if (it) R.string.action_stop else R.string.action_start)
        }
    }

    private fun setViews() {
        binding?.editor?.language = JsonLanguage()
        binding?.buttonStarter?.setOnClickListener {
            saveSettings()?.invokeOnCompletion {
                try {
                    Json.parseToJsonElement(binding?.editor?.text.toString())

                    viewModel.startOrStopGostService()
                } catch (e: SerializationException) {
                    Toast.makeText(context, "Json malformed or invalid. ${e.localizedMessage}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun saveSettings(): Job? {
        val gostSettings = binding?.editor?.text

        return gostSettings?.let {
            val jsonEncoded = it.toString().trim()

            viewModel.updateGostSettings(
                GostSettings(jsonEncoded)
            )
        }
    }
}