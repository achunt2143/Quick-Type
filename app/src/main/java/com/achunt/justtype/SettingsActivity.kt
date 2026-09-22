package com.achunt.justtype

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_activity_settings)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Reset launch history preference
            val resetHistoryPref = findPreference<Preference>("reset_history")
            resetHistoryPref?.setOnPreferenceClickListener {
                AppRepository.resetLaunchCounts(requireContext())
                Toast.makeText(requireContext(), "App launch history cleared", Toast.LENGTH_SHORT).show()
                true
            }

            // webOS tribute link
            val tributePref = findPreference<Preference>("about_tribute")
            tributePref?.setOnPreferenceClickListener {
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://en.wikipedia.org/wiki/WebOS#Just_Type")
                    )
                    startActivity(intent)
                } catch (_: Exception) {}
                true
            }

            // Handle "Enable Logs" preference
            val enableLogsSwitch = findPreference<SwitchPreferenceCompat>("enable_logs")
            enableLogsSwitch?.setOnPreferenceChangeListener { _, newValue ->
                val isLoggingEnabled = newValue as Boolean
                Toast.makeText(
                    requireContext(),
                    if (isLoggingEnabled) "Logging Enabled" else "Logging Disabled",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("SettingsFragment", "Logging has been ${if (isLoggingEnabled) "enabled" else "disabled"}.")
                true
            }

            // Handle "Collect Logs" preference
            val collectLogsPreference = findPreference<Preference>("collect_logs")
            collectLogsPreference?.setOnPreferenceClickListener {
                collectLogs()
                true
            }
        }

        /**
         * Collect logs using logcat and save them to a file.
         */
        private fun collectLogs() {
            try {
                val process = Runtime.getRuntime().exec("logcat -d")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val logs = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    logs.append(line).append("\n")
                }
                reader.close()

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "App Logs")
                    putExtra(Intent.EXTRA_TEXT, logs.toString())
                }

                startActivity(Intent.createChooser(intent, "Share logs via"))
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Error collecting logs", e)
                Toast.makeText(requireContext(), "Error collecting logs", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
