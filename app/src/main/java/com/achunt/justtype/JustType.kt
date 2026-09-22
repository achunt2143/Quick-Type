package com.achunt.justtype

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.achunt.justtype.databinding.JusttypeSearchBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JustType : Fragment() {

    private var _binding: JusttypeSearchBinding? = null
    private val binding get() = _binding!!

    // Adapters
    private lateinit var calculationAdapter: CalculationAdapter
    private lateinit var appsHeaderAdapter: SectionHeaderAdapter
    private lateinit var appsAdapter: JTAdapter
    private lateinit var contactsHeaderAdapter: SectionHeaderAdapter
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var actionsHeaderAdapter: SectionHeaderAdapter
    private lateinit var actionsAdapter: QuickActionsAdapter
    private lateinit var webHeaderAdapter: SectionHeaderAdapter
    private lateinit var webAdapter: SearchAdapter

    private var debounceJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = JusttypeSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchInput()
        setupButtons()
        setupWindowInsets()
        configureKeyboardAndStatusBar()

        // Load data in background and present initial view
        viewLifecycleOwner.lifecycleScope.launch {
            val context = requireContext()
            AppRepository.getApps(context)
            ContactRepository.getContacts(context)
            updateResults(binding.jtInput.text.toString())
        }
    }

    private fun setupRecyclerView() {
        calculationAdapter = CalculationAdapter()
        appsHeaderAdapter = SectionHeaderAdapter(getString(R.string.section_applications))
        appsAdapter = JTAdapter(requireContext())
        contactsHeaderAdapter = SectionHeaderAdapter(getString(R.string.section_contacts))
        contactsAdapter = ContactsAdapter()
        actionsHeaderAdapter = SectionHeaderAdapter(getString(R.string.section_quick_actions))
        actionsAdapter = QuickActionsAdapter()
        webHeaderAdapter = SectionHeaderAdapter(getString(R.string.section_search_everywhere))
        webAdapter = SearchAdapter()

        val concatAdapter = ConcatAdapter(
            calculationAdapter,
            appsHeaderAdapter,
            appsAdapter,
            contactsHeaderAdapter,
            contactsAdapter,
            actionsHeaderAdapter,
            actionsAdapter,
            webHeaderAdapter,
            webAdapter
        )

        binding.unifiedRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = concatAdapter
            itemAnimator = null // Avoid flicker during fast typing
        }
    }

    private fun setupSearchInput() {
        binding.jtInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnClear.isVisible = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString().orEmpty()
                if (text == "20090606") {
                    Toast.makeText(
                        requireContext(),
                        "Palm Pre Launch: June 6, 2009 🌴 - webOS Just Type",
                        Toast.LENGTH_LONG
                    ).show()
                }

                debounce(120) {
                    updateResults(text)
                }
            }
        })
    }

    private fun setupButtons() {
        binding.btnClear.setOnClickListener {
            binding.jtInput.text?.clear()
            binding.jtInput.requestFocus()
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateResults(query: String) {
        if (!isAdded) return
        val context = context ?: return

        val enableCalc = SharedPreferencesHelper.getBoolean(context, "category_calc", true)
        val enableApps = SharedPreferencesHelper.getBoolean(context, "category_apps", true)
        val enableContacts = SharedPreferencesHelper.getBoolean(context, "category_contacts", true)
        val enableActions = SharedPreferencesHelper.getBoolean(context, "category_actions", true)
        val enableWeb = SharedPreferencesHelper.getBoolean(context, "category_web", true)

        val trimmed = query.trim()

        if (trimmed.isEmpty()) {
            // Empty state: show Frequently Used Apps
            calculationAdapter.calculation = null

            val recentApps = if (enableApps) AppRepository.getFrequentlyUsedApps(6) else emptyList()
            appsHeaderAdapter.isVisible = recentApps.isNotEmpty()
            appsAdapter.submitList(recentApps)

            contactsHeaderAdapter.isVisible = false
            contactsAdapter.submitList(emptyList())

            actionsHeaderAdapter.isVisible = false
            actionsAdapter.submitList(emptyList())

            webHeaderAdapter.isVisible = false
            webAdapter.submitList(emptyList())

            binding.emptyStatePrompt.isVisible = recentApps.isEmpty()
        } else {
            binding.emptyStatePrompt.isVisible = false

            // 1. Calculator
            val calcResult = if (enableCalc) CalculatorEvaluator.evaluate(trimmed) else null
            calculationAdapter.calculation = calcResult

            // 2. Apps
            val matchedApps = if (enableApps) AppRepository.searchApps(trimmed) else emptyList()
            appsHeaderAdapter.isVisible = matchedApps.isNotEmpty()
            appsAdapter.submitList(matchedApps)

            // 3. Contacts
            val matchedContacts = if (enableContacts) ContactRepository.searchContacts(trimmed) else emptyList()
            contactsHeaderAdapter.isVisible = matchedContacts.isNotEmpty()
            contactsAdapter.submitList(matchedContacts)

            // 4. Quick Actions
            val actions = if (enableActions) QuickActionsManager.getActionsForQuery(trimmed) else emptyList()
            actionsHeaderAdapter.isVisible = actions.isNotEmpty()
            actionsAdapter.submitList(actions)

            // 5. Web Search
            val engines = if (enableWeb) WebSearchManager.getEngines(context) else emptyList()
            val webItems = engines.map { WebSearchItem(it, trimmed) }
            webHeaderAdapter.isVisible = webItems.isNotEmpty()
            webAdapter.submitList(webItems)
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.searchJT) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.searchJT.updatePadding(
                left = insets.left,
                top = insets.top,
                right = insets.right,
                bottom = insets.bottom
            )
            windowInsets
        }
    }

    private fun configureKeyboardAndStatusBar() {
        binding.jtInput.requestFocus()
        binding.jtInput.post {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(binding.jtInput, InputMethodManager.SHOW_IMPLICIT)
        }
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        }
    }

    private fun debounce(delayMs: Long = 120L, action: () -> Unit) {
        debounceJob?.cancel()
        debounceJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(delayMs)
            action()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh when returning from settings or app launches
        updateResults(binding.jtInput.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        debounceJob?.cancel()
        _binding = null
    }
}
