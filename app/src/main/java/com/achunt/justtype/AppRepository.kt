package com.achunt.justtype

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object AppRepository {

    private val cachedApps = mutableListOf<AppInfo>()
    private var isLoaded = false

    suspend fun getApps(context: Context, forceReload: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        if (!isLoaded || forceReload) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val activities = pm.queryIntentActivities(intent, 0)
            val list = mutableListOf<AppInfo>()

            for (ri in activities) {
                val label = ri.loadLabel(pm).toString()
                val pkg = ri.activityInfo.packageName
                val icon = try {
                    ri.activityInfo.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }
                val launchCount = SharedPreferencesHelper.getInt(context, "app_launch_$pkg", 0)
                list.add(AppInfo(label = label, packageName = pkg, icon = icon, launchCount = launchCount))
            }

            synchronized(cachedApps) {
                cachedApps.clear()
                cachedApps.addAll(list)
                isLoaded = true
            }
        }

        synchronized(cachedApps) {
            cachedApps.toList()
        }
    }

    fun searchApps(query: String): List<AppInfo> {
        val trimmed = query.trim().lowercase(Locale.getDefault())
        if (trimmed.isEmpty()) {
            return getFrequentlyUsedApps(5)
        }

        val all = synchronized(cachedApps) { cachedApps.toList() }

        return all.mapNotNull { app ->
            val labelLower = app.label.lowercase(Locale.getDefault())
            val score = computeMatchScore(labelLower, trimmed)
            if (score > 0) Pair(app, score) else null
        }
            .sortedWith(
                compareByDescending<Pair<AppInfo, Int>> { it.second }
                    .thenByDescending { it.first.launchCount }
                    .thenBy { it.first.label }
            )
            .map { it.first }
    }

    fun getFrequentlyUsedApps(limit: Int = 5): List<AppInfo> {
        val all = synchronized(cachedApps) { cachedApps.toList() }
        val used = all.filter { it.launchCount > 0 }
            .sortedByDescending { it.launchCount }
            .take(limit)

        // If user hasn't launched apps through Quick Type yet, fallback to top alphabetically
        return if (used.isNotEmpty()) {
            used
        } else {
            all.sortedBy { it.label }.take(limit)
        }
    }

    fun recordLaunch(context: Context, packageName: String) {
        val currentCount = SharedPreferencesHelper.getInt(context, "app_launch_$packageName", 0)
        val newCount = currentCount + 1
        SharedPreferencesHelper.saveInt(context, "app_launch_$packageName", newCount)

        synchronized(cachedApps) {
            val idx = cachedApps.indexOfFirst { it.packageName == packageName }
            if (idx != -1) {
                cachedApps[idx].launchCount = newCount
            }
        }
    }

    fun resetLaunchCounts(context: Context) {
        synchronized(cachedApps) {
            for (app in cachedApps) {
                SharedPreferencesHelper.saveInt(context, "app_launch_${app.packageName}", 0)
                app.launchCount = 0
            }
        }
    }

    private fun computeMatchScore(label: String, query: String): Int {
        // Exact match
        if (label == query) return 100

        // Starts with query
        if (label.startsWith(query)) return 80

        // Word starts with query (e.g. "Calculator" in "Google Calculator")
        val words = label.split(" ", "-", "_")
        if (words.any { it.startsWith(query) }) return 60

        // Acronym / initialisms match (e.g. "yt" -> "YouTube", "gcal" -> "Google Calendar")
        val initials = words.mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
        if (initials.startsWith(query)) return 50

        // Substring contains
        if (label.contains(query)) return 30

        return 0
    }
}
