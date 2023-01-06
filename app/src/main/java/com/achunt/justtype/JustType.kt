package com.achunt.justtype

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*


class JustType : androidx.fragment.app.Fragment(),
    androidx.appcompat.widget.SearchView.OnQueryTextListener {
    lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var jt: EditText
    private lateinit var wbjt: TextView
    private lateinit var wbimg: ImageView
    lateinit var jtAdapter: JTAdapter
    lateinit var cAdapter: ContactsAdapter

    @Volatile
    var contactName = ""

    @Volatile
    var contactNumber = ""
    private var numberTest = ""

    @Volatile
    var cNameTest: ArrayList<String?> = ArrayList()

    @Volatile
    var cNumberTest: ArrayList<String?> = ArrayList()

    @Volatile
    var cPhotoTest: ArrayList<Uri?> = ArrayList()

    @Volatile
    var cName: ArrayList<String> = ArrayList()

    @Volatile
    var cNumber: ArrayList<String> = ArrayList()

    @Volatile
    var cPhoto: ArrayList<Uri> = ArrayList()
    private var intent: Intent? = null
    private var b = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.justtype_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jt = view.findViewById(R.id.jtInput)
        runBlocking {
            val job = this.launch { contactsSearch() }
            job.join()
            jtAdapter = JTAdapter(requireContext(), jt.text.toString())
        }
        recyclerView = view.findViewById(R.id.justtype_view)
        layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.visibility = View.INVISIBLE
        recyclerView.adapter = jtAdapter

        jt.requestFocus()
        jt.isFocusableInTouchMode
        jt.isFocusable = true
        if (jt.requestFocus()) {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(jt, InputMethodManager.SHOW_IMPLICIT)
        }
        val w: Window = requireActivity().window
        w.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.status)
        wbjt = view.findViewById(R.id.webText)
        wbimg = view.findViewById(R.id.webSearch)

        jt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (jt.text.toString().isNotEmpty()) {
                    if (jt.text.toString().startsWith("call")) {
                        b = true
                        cAdapter.isCall = true
                        recyclerView.adapter = cAdapter
                        if (jt.text.toString().length > 4) {
                            recyclerView.visibility = View.VISIBLE
                            val contact: Array<String> =
                                jt.text.toString().split(" ").toTypedArray()
                            if (contact[1].isNotEmpty()) {

                                onQueryTextChange(contact[1])
                            }
                        } else {
                            recyclerView.visibility = View.INVISIBLE
                            wbjt.visibility = View.VISIBLE
                            wbimg.visibility = View.VISIBLE
                        }
                    } else if (jt.text.toString().startsWith("text")){
                        b = true
                        cAdapter.isCall = false
                        recyclerView.adapter = cAdapter
                        if (jt.text.toString().length > 4) {

                            recyclerView.visibility = View.VISIBLE
                            val contact: Array<String> =
                                jt.text.toString().split(" ").toTypedArray()
                            if (contact[1].isNotEmpty()) {
                                onQueryTextChange(contact[1])
                            }
                        } else {
                            recyclerView.visibility = View.INVISIBLE
                            wbjt.visibility = View.VISIBLE
                            wbimg.visibility = View.VISIBLE
                        }
                    } else {
                        b = false
                        recyclerView.adapter = jtAdapter
                        onQueryTextChange(jt.text.toString())
                    }
                    recyclerView.visibility = View.VISIBLE
                    textChange(view)
                } else {
                    wbjt.text = "Search the web for"
                    recyclerView.visibility = View.INVISIBLE
                }
            }
        })
    }

    private fun textChange(view: View) {
        wbjt = view.findViewById(R.id.webText)
        wbjt.text = "Search the web for " + jt.text.toString()
        wbjt.setOnClickListener { webSearch() }
        numberTest = wbjt.text.toString()
        if (jt.text.toString().lowercase(Locale.getDefault()).contains("text")) {
            if (wbjt.text.toString().lowercase(Locale.getDefault())
                    .contains("search the web for ")
            ) {
                var temp = wbjt.text.toString()
                val t: Array<String> = temp.split("Search the web for ").toTypedArray()
                temp = t[1]
                wbjt.text = temp
            }

            if (wbjt.text.toString().lowercase(Locale.getDefault())
                    .startsWith(contactName)
            ) {
                sendText(wbjt.text.toString(), view)
            } else if (Character.isDigit(numberTest[0])) {
                sendText(wbjt.text.toString(), view)
            }

        }
        if (jt.text.toString().lowercase(Locale.getDefault()).contains("youtube")) {
            searchYoutube(wbjt.text.toString())
        }
        if (jt.text.toString().lowercase(Locale.getDefault()).contains("maps")) {
            searchMaps(wbjt.text.toString())
        }
        if (jt.text.toString().lowercase(Locale.getDefault()).startsWith("call")) {
            doCall(wbjt.text.toString(), view)
        }
        if (jt.text.toString().lowercase(Locale.getDefault()).startsWith("wikipedia")) {
            searchWikipedia(wbjt.text.toString())
        }
    }

    private fun contactsSearch() {
        val phones: Cursor? = this.requireContext().contentResolver
            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        var idColumn: Int
        var lookupColumn: Int
        var contactUri: Uri?
        var i = 0
        if (phones != null) {
            while (phones.moveToNext()) {
                contactName =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) shr 0)
                contactNumber =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) shr 0)
                idColumn = phones.getColumnIndex(ContactsContract.Contacts._ID)
                lookupColumn = phones.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
                contactUri = ContactsContract.Contacts.getLookupUri(
                    phones.getLong(idColumn),
                    phones.getString(lookupColumn)
                )
                if (i > 0) {
                    if (!cNameTest[i - 1]?.contains(contactName)!!) {
                        cNameTest.add(i, contactName)
                        cNumberTest.add(i, contactNumber)
                        cPhotoTest.add(i, contactUri)
                        i++
                    }
                } else {
                    cNameTest.add(i, contactName)
                    cNumberTest.add(i, contactNumber)
                    cPhotoTest.add(i, contactUri)
                    i++
                }
            }
        }
        cName = cNameTest as ArrayList<String>
        cNumber = cNumberTest as ArrayList<String>
        cPhoto = cPhotoTest as ArrayList<Uri>
        contactNumber = ""
        contactName = ""
        phones?.close()
        cAdapter = ContactsAdapter(b, cName, cNumber, cPhoto)
    }

    private fun webSearch() {
        intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent!!.putExtra(SearchManager.QUERY, jt.text.toString())
        startActivity(intent)
    }

    private fun sendText(text: String, view: View) {
        wbjt = view.findViewById(R.id.webText)
        wbimg = view.findViewById(R.id.webSearch)
        var toSend = ""
        val split1: Array<String> = text.split("text ").toTypedArray()
        if (split1.size>2){
        val m1 = split1[1]
        val split2: Array<String> = m1.split(" ").toTypedArray()
        val m2 = split2[0]
        var m3 = StringBuilder()
        var cont = ""
        for (t in 1 until split2.size) {
            m3.append(split2[t]).append(" ")
        }
        m3.toString().trim { it <= ' ' }
        for (t in cName.indices) {
            cont = m2.substring(0, 1).uppercase(Locale.getDefault()) + m2.substring(1)
            if (cName[t].startsWith(cont)) {
                wbjt.visibility = View.INVISIBLE
                wbimg.visibility = View.INVISIBLE
                contactName = cName[t]
                contactNumber = cNumber[t]
            }
        }
        if (contactName.startsWith(cont)) {
            if (m2.isNotEmpty()) {
                toSend = m3.toString()
                toSend.uppercase(Locale.getDefault())
            }
        }
        if (m1.length > 3) {
            if (Character.isDigit(m2[0])) {
                wbimg.visibility = View.VISIBLE
                wbjt.visibility = View.VISIBLE
                m3 = StringBuilder()
                val separate: Array<String> = m2.split(" ").toTypedArray()
                contactNumber = separate[0]
                for (t in 1 until split2.size) {
                    m3.append(split2[t]).append(" ")
                }
                toSend = m3.toString()
            }
        }
        }
        wbjt.setOnClickListener {
            val uri = Uri.parse("smsto:$contactNumber")
            intent = Intent(Intent.ACTION_SENDTO, uri)
            intent!!.putExtra("sms_body", toSend)
            startActivity(intent)
        }
    }

    private fun searchYoutube(text: String) {
        var temp = text
        var m1 = ""
        if (text.contains("Search the web for ")) {
            val t: Array<String> = temp.split("Search the web for ").toTypedArray()
            temp = t[1]
            wbjt.text = temp
        }
        val split1: Array<String> = temp.split("youtube ").toTypedArray()
        if (split1.size > 1) {
            m1 = split1[1]
        }
        val finalM = m1
        wbjt.setOnClickListener {
            try {
                intent = Intent(Intent.ACTION_SEARCH)
                intent!!.`package` = "com.google.android.youtube"
                intent!!.putExtra("query", finalM)
                intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } catch (e: Exception) {
                webSearch()
            }
        }
    }

    private fun searchWikipedia(text: String) {
        var temp = text
        var m1 = ""
        if (text.contains("Search the web for ")) {
            val t: Array<String> = temp.split("Search the web for ").toTypedArray()
            temp = t[1]
            wbjt.text = temp
        }
        val split1: Array<String> = temp.split("wikipedia ").toTypedArray()
        if (split1.size > 1) {
            m1 = split1[1]
        }
        val finalM = m1
        wbjt.setOnClickListener {
            try {
                intent = Intent(Intent.ACTION_SEARCH)
                intent!!.`package` = "org.wikipedia"
                intent!!.putExtra("query", finalM)
                intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } catch (e: Exception) {
                val url = "https://en.wikipedia.org/wiki/Special:Search?search=${finalM}"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }
    }

    private fun searchMaps(text: String) {
        var temp = text
        var m1 = ""
        if (text.contains("Search the web for ")) {
            val t: Array<String> = temp.split("Search the web for ").toTypedArray()
            temp = t[1]
            wbjt.text = temp
        }
        val split1: Array<String> = temp.split("maps ").toTypedArray()
        if (split1.size > 1) {
            m1 = split1[1]
        }
        val geo = Uri.parse("geo:0,0?q=$m1")
        wbjt.setOnClickListener {
            try {
                intent = Intent(Intent.ACTION_VIEW)
                intent!!.data = geo
                startActivity(intent)
            } catch (e: Exception) {
                webSearch()
            }
        }
    }

    private fun doCall(text: String, view: View) {
        wbjt = view.findViewById(R.id.webText)
        wbimg = view.findViewById(R.id.webSearch)
        var temp = text
        val m1 = arrayOf("")
        if (text.contains("Search the web for ")) {
            val t: Array<String> = temp.split("Search the web for ").toTypedArray()
            temp = t[1]
            wbjt.text = temp
        }
        val finalTemp = temp
        val split1: Array<String> = finalTemp.split("call ").toTypedArray()
        if (split1.size > 2) {
            m1[0] = split1[1]
            for (c in cName.indices) {
                val cont: String =
                    m1[0].substring(0, 1).uppercase(Locale.getDefault()) + m1[0].substring(1)
                if (cName[c].startsWith(cont)) {
                    wbjt.visibility = View.INVISIBLE
                    wbimg.visibility = View.INVISIBLE
                    contactName = cName[c]
                    contactNumber = cNumber[c]
                }
            }
            if (m1[0].length > 3) {
                if (Character.isDigit(m1[0][0])) {
                    contactNumber = m1[0]
                    wbimg.visibility = View.VISIBLE
                    wbjt.visibility = View.VISIBLE
                }
            }
        }
        wbjt.setOnClickListener {
            intent = Intent(Intent.ACTION_DIAL)
            intent!!.data = Uri.parse("tel:$contactNumber")
            startActivity(intent)
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (b) {
            cAdapter.filter.filter(query)
        } else {
            jtAdapter.filter.filter(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (b) {
            cAdapter.filter.filter(newText)
        } else {
            jtAdapter.filter.filter(newText)
        }
        return true
    }
}