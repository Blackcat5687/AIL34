package com.notekeep.local.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.notekeep.local.R
import com.notekeep.local.data.AppDatabase
import com.notekeep.local.data.BackupManager
import com.notekeep.local.databinding.ActivitySettingsBinding
import com.notekeep.local.graph.GraphActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val createBackupLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) writeBackup(uri)
        }

    private val openBackupLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) confirmRestore(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rowGraph.setOnClickListener {
            startActivity(Intent(this, GraphActivity::class.java))
        }

        binding.rowBackup.setOnClickListener {
            val stamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
            createBackupLauncher.launch("notes_backup_$stamp.json")
        }

        binding.rowRestore.setOnClickListener {
            openBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        }
    }

    private fun writeBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                val notes = AppDatabase.getInstance(applicationContext).noteDao().getAllOnce()
                val json = BackupManager.toJson(notes)
                contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(this@SettingsActivity, R.string.backup_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, R.string.backup_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmRestore(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle(R.string.restore_choice_title)
            .setPositiveButton(R.string.restore_merge) { _, _ -> performRestore(uri, replace = false) }
            .setNegativeButton(R.string.restore_replace) { _, _ -> performRestore(uri, replace = true) }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    private fun performRestore(uri: Uri, replace: Boolean) {
        lifecycleScope.launch {
            try {
                val text = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: throw IllegalStateException("empty")
                val notes = BackupManager.fromJson(text)
                val dao = AppDatabase.getInstance(applicationContext).noteDao()
                if (replace) dao.deleteAll()
                dao.insertAll(notes)
                Toast.makeText(this@SettingsActivity, R.string.restore_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, R.string.restore_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
