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
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achunt.justtype.databinding.JusttypeSearchBinding
import kotlinx.coroutines.*

class JustType : Fragment(), androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var jt: EditText
    private lateinit var cAdapter: ContactsAdapter
    private val jtAdapter by lazy { JTAdapter(requireContext()) }
    private val searchAdapter by lazy { context?.let { SearchAdapter(it, qSearch) } }
    private lateinit var binding: JusttypeSearchBinding

    private var qSearch: MutableList<String> = MutableList(4) { "web" }
    private var b = false
    private var debounceJob: Job? = null

    private val contacts = mutableListOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = JusttypeSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jt = binding.jtInput

        initializeRecyclerView(view)
        configureKeyboardAndStatusBar(view)

        CoroutineScope(Dispatchers.IO).launch {
            contactsSearch()

            withContext(Dispatchers.Main) {
                recyclerView.adapter = jtAdapter
                searchRecyclerView.adapter = searchAdapter
            }
        }

        jt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                debounce {
                    handleCommandInput(s.toString())
                }
            }
        })
    }

    private fun initializeRecyclerView(view: View) {
        recyclerView = view.findViewById<RecyclerView>(R.id.justtype_view).apply {
            layoutManager = LinearLayoutManager(requireContext())
            visibility = View.INVISIBLE
        }

        searchRecyclerView = view.findViewById<RecyclerView>(R.id.justtype_search).apply {
            layoutManager = LinearLayoutManager(requireContext())
            visibility = View.INVISIBLE
        }
    }

    private fun configureKeyboardAndStatusBar(view: View) {
        view.clearFocus() // Clear focus from other views.
        jt.requestFocus()
        jt.post {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(jt, InputMethodManager.SHOW_IMPLICIT)
        }
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.status)
    }



    private fun handleCommandInput(input: String) {
        when {
            input.startsWith("call") -> handleCallCommand(input)
            input.startsWith("text") -> handleTextCommand(input)
            input.isNotEmpty() -> handleSearchCommand(input)
            else -> resetViewStates()
        }
    }

    private fun handleCallCommand(input: String) {
        b = true
        cAdapter.isCall = true
        recyclerView.adapter = cAdapter

        val query = input.split(" ").getOrNull(1).orEmpty()
        if (query.isNotEmpty()) {
            onQueryTextChange(query)
            toggleRecyclerViews(showRecyclerView = true, showSearchView = false)
        } else {
            toggleRecyclerViews(showRecyclerView = false, showSearchView = true)
        }
    }

    private fun handleTextCommand(input: String) {
        b = true
        cAdapter.isCall = false
        recyclerView.adapter = cAdapter

        val query = input.split(" ").getOrNull(1).orEmpty()
        textSend = input

        if (query.isNotEmpty()) {
            onQueryTextChange(query)
            toggleRecyclerViews(showRecyclerView = true, showSearchView = false)
        } else {
            toggleRecyclerViews(showRecyclerView = false, showSearchView = true)
        }
    }

    private fun handleSearchCommand(input: String) {
        b = false
        recyclerView.adapter = jtAdapter
        qSearch.clear()
        repeat(4) { qSearch.add(input) }
        onQueryTextChange(input)
        toggleRecyclerViews(showRecyclerView = true, showSearchView = true)
    }

    private fun resetViewStates() {
        toggleRecyclerViews(showRecyclerView = false, showSearchView = false)
    }

    private fun toggleRecyclerViews(showRecyclerView: Boolean, showSearchView: Boolean) {
        recyclerView.visibility = if (showRecyclerView) View.VISIBLE else View.INVISIBLE
        searchRecyclerView.visibility = if (showSearchView) View.VISIBLE else View.INVISIBLE
    }

    private fun contactsSearch() {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Contacts.PHOTO_URI
        )

        val phones: Cursor? = context?.contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
        )

        phones?.use { cursor ->
            val uniqueContacts = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                val contactName = cursor.getString(0)
                val contactNumber = normalizePhoneNumber(cursor.getString(1))
                val contactPhotoUri = cursor.getString(2)?.let { Uri.parse(it) }

                if (uniqueContacts.add(contactNumber)) {
                    contacts.add(Contact(contactName, contactNumber, contactPhotoUri))
                }
            }
        }
        cAdapter = ContactsAdapter(b, contacts)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        filterAdapters(query)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        filterAdapters(newText)
        return true
    }

    private fun filterAdapters(query: String) {
        if (b) {
            cAdapter.filter.filter(query)
        } else {
            jtAdapter.filter.filter(query)
            searchAdapter?.filter?.filter(query)
        }
    }

    private fun debounce(delay: Long = 300L, action: () -> Unit) {
        debounceJob?.cancel()
        debounceJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(delay)
            action()
        }
    }


    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.filter { it.isDigit() }
    }

    companion object {
        var textSend: String = ""
    }
}
