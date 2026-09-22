package com.achunt.justtype

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object ContactRepository {

    private val cachedContacts = mutableListOf<Contact>()
    private var isLoaded = false

    suspend fun getContacts(context: Context, forceReload: Boolean = false): List<Contact> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }

        if (!isLoaded || forceReload) {
            val list = mutableListOf<Contact>()
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            )

            val cursor: Cursor? = try {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
            } catch (_: Exception) {
                null
            }

            cursor?.use { c ->
                val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                val uniqueNumbers = mutableSetOf<String>()

                while (c.moveToNext()) {
                    val name = if (nameIdx != -1) c.getString(nameIdx).orEmpty() else ""
                    val rawNumber = if (numberIdx != -1) c.getString(numberIdx).orEmpty() else ""
                    val normalizedNumber = rawNumber.filter { it.isDigit() }
                    val photoUri = if (photoIdx != -1) c.getString(photoIdx)?.let { Uri.parse(it) } else null

                    if (normalizedNumber.isNotEmpty() && uniqueNumbers.add(normalizedNumber)) {
                        list.add(Contact(name = name, number = rawNumber, photoUri = photoUri))
                    }
                }
            }

            synchronized(cachedContacts) {
                cachedContacts.clear()
                cachedContacts.addAll(list)
                isLoaded = true
            }
        }

        synchronized(cachedContacts) {
            cachedContacts.toList()
        }
    }

    fun searchContacts(query: String, limit: Int = 5): List<Contact> {
        val trimmed = query.trim().lowercase(Locale.getDefault())
        if (trimmed.isEmpty()) return emptyList()

        val digits = trimmed.filter { it.isDigit() }
        val all = synchronized(cachedContacts) { cachedContacts.toList() }

        return all.filter { contact ->
            contact.name.lowercase(Locale.getDefault()).contains(trimmed) ||
                    (digits.isNotEmpty() && contact.number.filter { it.isDigit() }.contains(digits))
        }.take(limit)
    }
}
