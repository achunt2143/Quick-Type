package com.achunt.justtype

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.widget.Toast

data class QuickAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val execute: (Context) -> Unit
)

object QuickActionsManager {

    fun getActionsForQuery(query: String): List<QuickAction> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val actions = mutableListOf<QuickAction>()

        // 1. Direct Dial if numeric phone number
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.length in 7..15 && trimmed.matches(Regex("""^[\d\s\-+().]+$"""))) {
            actions.add(
                QuickAction(
                    id = "action_dial",
                    title = "Dial $trimmed",
                    subtitle = "Open phone dialer",
                    iconRes = R.drawable.ic_call,
                    execute = { ctx ->
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digitsOnly"))
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Could not open dialer", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            )
        }

        // 2. Timer detection (e.g. "timer 5m", "timer 10 min", "timer 45s", "timer 2h")
        val timerRegex = Regex("""^timer\s+(\d+)\s*(m|min|mins|minutes|s|sec|secs|seconds|h|hr|hours)?$""", RegexOption.IGNORE_CASE)
        val timerMatch = timerRegex.matchEntire(trimmed)
        if (timerMatch != null) {
            val amount = timerMatch.groupValues[1].toIntOrNull() ?: 1
            val unit = timerMatch.groupValues[2].lowercase()
            val seconds = when {
                unit.startsWith("s") -> amount
                unit.startsWith("h") -> amount * 3600
                else -> amount * 60 // default to minutes
            }
            val formattedDuration = when {
                seconds >= 3600 -> "${seconds / 3600} hr"
                seconds >= 60 -> "${seconds / 60} min"
                else -> "$seconds sec"
            }
            actions.add(
                QuickAction(
                    id = "action_timer",
                    title = "Start Timer ($formattedDuration)",
                    subtitle = "Set countdown timer in Clock",
                    iconRes = R.drawable.ic_alarm,
                    execute = { ctx ->
                        try {
                            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                                putExtra(AlarmClock.EXTRA_MESSAGE, "Quick Type Timer")
                                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                            }
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Could not set timer", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            )
        }

        // 3. Alarm detection (e.g. "alarm 7am", "alarm 7:30pm", "alarm 06:45")
        val alarmRegex = Regex("""^alarm\s+(\d{1,2})(?::(\d{2}))?\s*(am|pm)?$""", RegexOption.IGNORE_CASE)
        val alarmMatch = alarmRegex.matchEntire(trimmed)
        if (alarmMatch != null) {
            var hour = alarmMatch.groupValues[1].toIntOrNull() ?: 0
            val minute = alarmMatch.groupValues[2].toIntOrNull() ?: 0
            val ampm = alarmMatch.groupValues[3].lowercase()

            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0

            val formattedTime = String.format("%02d:%02d", hour, minute)
            actions.add(
                QuickAction(
                    id = "action_alarm",
                    title = "Set Alarm for $formattedTime",
                    subtitle = "Set alarm in Clock",
                    iconRes = R.drawable.ic_alarm,
                    execute = { ctx ->
                        try {
                            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                                putExtra(AlarmClock.EXTRA_HOUR, hour)
                                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                            }
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Could not set alarm", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            )
        }

        // 4. New Note / Memo (The hallmark of webOS Quick Actions)
        actions.add(
            QuickAction(
                id = "action_note",
                title = "New Note: \"$trimmed\"",
                subtitle = "Save to Keep, Notes, or text apps",
                iconRes = R.drawable.ic_note,
                execute = { ctx ->
                    try {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, trimmed)
                        }
                        ctx.startActivity(Intent.createChooser(intent, "Create note with"))
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Could not create note", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        )

        // 5. New Calendar Event
        actions.add(
            QuickAction(
                id = "action_event",
                title = "New Event: \"$trimmed\"",
                subtitle = "Add to Calendar",
                iconRes = R.drawable.ic_event,
                execute = { ctx ->
                    try {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, trimmed)
                        }
                        ctx.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Could not open Calendar", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        )

        // 6. New Email
        actions.add(
            QuickAction(
                id = "action_email",
                title = "Email: \"$trimmed\"",
                subtitle = "Compose email draft",
                iconRes = R.drawable.ic_email,
                execute = { ctx ->
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                            putExtra(Intent.EXTRA_SUBJECT, trimmed)
                        }
                        ctx.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Could not open email app", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        )

        // 7. Search Play Store
        actions.add(
            QuickAction(
                id = "action_play_store",
                title = "Search Play Store",
                subtitle = "Look up apps, games & books for \"$trimmed\"",
                iconRes = R.drawable.ic_play_store,
                execute = { ctx ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$trimmed"))
                        ctx.startActivity(intent)
                    } catch (e: Exception) {
                        val webIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/search?q=$trimmed")
                        )
                        ctx.startActivity(webIntent)
                    }
                }
            )
        )

        return actions
    }
}
