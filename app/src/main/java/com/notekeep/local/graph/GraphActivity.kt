package com.notekeep.local.graph

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.notekeep.local.data.AppDatabase
import com.notekeep.local.databinding.ActivityGraphBinding
import com.notekeep.local.databinding.BottomsheetGraphSettingsBinding
import com.notekeep.local.ui.NoteEditActivity
import kotlinx.coroutines.launch

class GraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding
    private var hideOrphans = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.graphView.onNoteTapped = { noteId ->
            val intent = Intent(this, NoteEditActivity::class.java)
            intent.putExtra(NoteEditActivity.EXTRA_NOTE_ID, noteId)
            startActivity(intent)
        }

        binding.fabGraphSettings.setOnClickListener { showSettingsSheet() }

        loadGraph()
    }

    override fun onResume() {
        super.onResume()
        loadGraph()
    }

    private fun loadGraph() {
        lifecycleScope.launch {
            val notes = AppDatabase.getInstance(applicationContext).noteDao().getAllOnce()
            val graphData = GraphData.build(notes, hideOrphans)
            binding.graphView.data = graphData
            binding.emptyView.visibility =
                if (graphData.nodes.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun showSettingsSheet() {
        val sheet = BottomSheetDialog(this)
        val sheetBinding = BottomsheetGraphSettingsBinding.inflate(layoutInflater)
        sheet.setContentView(sheetBinding.root)

        sheetBinding.switchArrows.setOnCheckedChangeListener { _, checked ->
            binding.graphView.showArrows = checked
            binding.graphView.invalidate()
        }
        sheetBinding.switchOrphans.setOnCheckedChangeListener { _, checked ->
            hideOrphans = checked
            loadGraph()
        }

        fun applyForces() {
            binding.graphView.applyForceSettings(
                sheetBinding.sliderCenter.value,
                sheetBinding.sliderRepel.value,
                sheetBinding.sliderLinkStrength.value,
                sheetBinding.sliderLinkDistance.value
            )
        }

        sheetBinding.sliderCenter.addOnChangeListener { _, _, _ -> applyForces() }
        sheetBinding.sliderRepel.addOnChangeListener { _, _, _ -> applyForces() }
        sheetBinding.sliderLinkStrength.addOnChangeListener { _, _, _ -> applyForces() }
        sheetBinding.sliderLinkDistance.addOnChangeListener { _, _, _ -> applyForces() }

        sheetBinding.sliderNodeSize.addOnChangeListener { _, value, _ ->
            binding.graphView.nodeSizeSetting = value
            binding.graphView.invalidate()
        }
        sheetBinding.sliderLinkThickness.addOnChangeListener { _, value, _ ->
            binding.graphView.linkThicknessSetting = value
            binding.graphView.invalidate()
        }
        sheetBinding.sliderFade.addOnChangeListener { _, value, _ ->
            binding.graphView.fadeThreshold = value
            binding.graphView.invalidate()
        }

        sheetBinding.btnRestart.setOnClickListener {
            applyForces()
            binding.graphView.restart()
        }

        applyForces()
        sheet.show()
    }
}
