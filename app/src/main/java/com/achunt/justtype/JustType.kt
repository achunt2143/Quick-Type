package com.achunt.justtype

import android.content.Context
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class JustType : androidx.fragment.app.Fragment(),
    androidx.appcompat.widget.SearchView.OnQueryTextListener {

    lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var jt: EditText
    lateinit var jtAdapter: JTAdapter
    lateinit var cAdapter: ContactsAdapter
    lateinit var searchRecyclerView: RecyclerView
    lateinit var searchAdapter: SearchAdapter
    var qSearch: MutableList<String> = MutableList(4) { "web" }
    private var b = false

    @Volatile
    var contactName = ""

    @Volatile
    var contactNumber = ""

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
            searchAdapter = SearchAdapter(qSearch)
        }
        recyclerView = view.findViewById(R.id.justtype_view)
        layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.visibility = View.INVISIBLE
        recyclerView.adapter = jtAdapter
        val searchLayoutManager = LinearLayoutManager(context)
        searchRecyclerView = view.findViewById(R.id.justtype_search)
        searchRecyclerView.layoutManager = searchLayoutManager
        searchRecyclerView.visibility = View.INVISIBLE
        searchRecyclerView.adapter = searchAdapter

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

        jt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (jt.text.toString().isNotEmpty()) {
                    if (jt.text.toString().startsWith("call")) {
                        b = true
                        cAdapter.isCall = true
                        recyclerView.adapter = cAdapter
                        searchRecyclerView.visibility = View.INVISIBLE
                        if (jt.text.toString().length > 4) {

                            recyclerView.visibility = View.VISIBLE
                            val contact: Array<String> =
                                jt.text.toString().split(" ").toTypedArray()
                            if (contact[1].isNotEmpty()) {
                                onQueryTextChange(contact[1])
                            }
                        } else {
                            searchRecyclerView.visibility = View.VISIBLE
                            recyclerView.visibility = View.INVISIBLE
                        }
                    } else if (jt.text.toString().startsWith("text")){
                        b = true
                        cAdapter.isCall = false
                        recyclerView.adapter = cAdapter
                        if (jt.text.toString().length > 4) {
                            recyclerView.visibility = View.VISIBLE
                            searchRecyclerView.visibility = View.INVISIBLE
                            val contact: Array<String> =
                                jt.text.toString().split(" ").toTypedArray()
                            if (contact[1].isNotEmpty()) {
                                onQueryTextChange(contact[1])
                            }
                            textSend = jt.text.toString()
                        } else {
                            searchRecyclerView.visibility = View.VISIBLE
                            recyclerView.visibility = View.INVISIBLE
                        }
                    } else {
                        b = false
                        recyclerView.adapter = jtAdapter
                        searchRecyclerView.visibility = View.VISIBLE
                        for (i in 0..3) {
                            qSearch.add(i, s.toString())
                        }
                        onQueryTextChange(s.toString())
                    }
                    recyclerView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.INVISIBLE
                    searchRecyclerView.visibility = View.INVISIBLE
                }
            }
        })
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

    override fun onQueryTextSubmit(query: String): Boolean {
        if (b) {
            cAdapter.filter.filter(query)
        } else {
            jtAdapter.filter.filter(query)
            searchAdapter.filter.filter(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (b) {
            cAdapter.filter.filter(newText)
        } else {
            jtAdapter.filter.filter(newText)
            searchAdapter.filter.filter(newText)
        }
        return true
    }

    companion object {
        var textSend = ""
    }

}