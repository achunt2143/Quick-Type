package com.achunt.justtype

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

data class WebSearchEngine(
    val id: String,
    val name: String,
    val iconRes: Int,
    val executeSearch: (Context, String) -> Unit
)

object WebSearchManager {

    private val allEngines = listOf(
        WebSearchEngine(
            id = "google",
            name = "Google Web Search",
            iconRes = R.drawable.search,
            executeSearch = { context, query ->
                try {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, query)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    openBrowserUrl(context, "https://www.google.com/search?q=" + encode(query))
                }
            }
        ),
        WebSearchEngine(
            id = "maps",
            name = "Google Maps",
            iconRes = R.drawable.map,
            executeSearch = { context, query ->
                try {
                    val geoUri = Uri.parse("geo:0,0?q=" + encode(query))
                    val intent = Intent(Intent.ACTION_VIEW, geoUri)
                    context.startActivity(intent)
                } catch (_: Exception) {
                    openBrowserUrl(context, "https://maps.google.com/?q=" + encode(query))
                }
            }
        ),
        WebSearchEngine(
            id = "youtube",
            name = "YouTube",
            iconRes = R.drawable.youtube,
            executeSearch = { context, query ->
                try {
                    val intent = Intent(Intent.ACTION_SEARCH).apply {
                        `package` = "com.google.android.youtube"
                        putExtra("query", query)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    openBrowserUrl(context, "https://www.youtube.com/results?search_query=" + encode(query))
                }
            }
        ),
        WebSearchEngine(
            id = "wikipedia",
            name = "Wikipedia",
            iconRes = R.drawable.wiki,
            executeSearch = { context, query ->
                try {
                    val intent = Intent(Intent.ACTION_SEARCH).apply {
                        `package` = "org.wikipedia"
                        putExtra("query", query)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    openBrowserUrl(context, "https://en.wikipedia.org/wiki/Special:Search?search=" + encode(query))
                }
            }
        ),
        WebSearchEngine(
            id = "duckduckgo",
            name = "DuckDuckGo",
            iconRes = R.drawable.ic_globe,
            executeSearch = { context, query ->
                openBrowserUrl(context, "https://duckduckgo.com/?q=" + encode(query))
            }
        ),
        WebSearchEngine(
            id = "reddit",
            name = "Reddit",
            iconRes = R.drawable.ic_globe,
            executeSearch = { context, query ->
                openBrowserUrl(context, "https://www.reddit.com/search/?q=" + encode(query))
            }
        ),
        WebSearchEngine(
            id = "github",
            name = "GitHub",
            iconRes = R.drawable.ic_globe,
            executeSearch = { context, query ->
                openBrowserUrl(context, "https://github.com/search?q=" + encode(query))
            }
        ),
        WebSearchEngine(
            id = "amazon",
            name = "Amazon",
            iconRes = R.drawable.ic_globe,
            executeSearch = { context, query ->
                openBrowserUrl(context, "https://www.amazon.com/s?k=" + encode(query))
            }
        )
    )

    fun getEngines(context: Context): List<WebSearchEngine> {
        // Return default top 4 plus any enabled in preferences
        return allEngines.filter { engine ->
            val isDefault = engine.id in listOf("google", "maps", "youtube", "wikipedia")
            SharedPreferencesHelper.getBoolean(context, "engine_${engine.id}", isDefault)
        }
    }

    private fun openBrowserUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No browser found to open link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encode(query: String): String {
        return try {
            URLEncoder.encode(query, "UTF-8")
        } catch (_: Exception) {
            query
        }
    }
}
