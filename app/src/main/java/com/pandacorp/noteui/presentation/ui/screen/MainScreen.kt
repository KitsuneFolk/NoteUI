package com.pandacorp.noteui.presentation.ui.screen

import android.os.Bundle
import android.util.SparseBooleanArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.util.isEmpty
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.pandacorp.dropspinner.DropDownView
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.ScreenMainBinding
import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.presentation.ui.adapter.notes.NotesAdapter
import com.pandacorp.noteui.presentation.utils.ViewAdapter
import com.pandacorp.noteui.presentation.utils.helpers.Constants
import com.pandacorp.noteui.presentation.utils.helpers.app
import com.pandacorp.noteui.presentation.utils.helpers.sp
import com.pandacorp.noteui.presentation.viewModels.CurrentNoteViewModel
import com.pandacorp.noteui.presentation.viewModels.NotesViewModel
import com.pandacorp.searchbar.searchview.SearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class MainScreen : Fragment() {
    private var _binding: ScreenMainBinding? = null
    private val binding get() = _binding!!

    private val notesViewModel: NotesViewModel by viewModel()
    private val currentNoteViewModel: CurrentNoteViewModel by activityViewModel()

    private val navController by lazy { findNavController() }

    private val notesAdapter by lazy {
        NotesAdapter().apply {
            setOnClickListener(
                object : NotesAdapter.OnNoteItemClickListener {
                    override fun onClick(
                        noteItem: NoteItem,
                        position: Int
                    ) {
                        if (binding.searchBar.isCountModeEnabled) {
                            select(position)
                        } else {
                            currentNoteViewModel.setNote(notesViewModel.getNoteById(noteItem.id))
                            navController.navigate(R.id.nav_note_screen)
                        }
                    }

                    override fun onLongClick(
                        noteItem: NoteItem,
                        position: Int
                    ) {
                        select(position)
                    }

                    private fun select(position: Int) {
                        val selectedNotesList = notesViewModel.selectedNotes.value!!
                        selectedNotesList.apply {
                            if (get(position, false)) {
                                delete(position)
                            } else {
                                put(position, true)
                            }
                            notesViewModel.selectedNotes.postValue(this)
                        }
                    }
                },
            )
        }
    }

    private val searchAdapter by lazy {
        NotesAdapter().apply {
            isSelectionEnabled = false
            setOnClickListener(
                object : NotesAdapter.OnNoteItemClickListener {
                    override fun onClick(
                        noteItem: NoteItem,
                        position: Int
                    ) {
                        currentNoteViewModel.setNote(notesViewModel.getNoteById(noteItem.id))
                        navController.navigate(R.id.nav_note_screen)
                    }

                    override fun onLongClick(
                        noteItem: NoteItem,
                        position: Int
                    ) {
                    }
                },
            )
        }
    }

    private val filterSpinner by lazy {
        LayoutInflater.from(requireContext()).inflate(R.layout.filter_view, binding.root, false) as DropDownView
    }

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ScreenMainBinding.inflate(inflater, container, false)

        initViews()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        currentNoteViewModel.clearData()
        if (app.isSettingsChanged) {
            val isShowFab =
                sp.getBoolean(
                    Constants.Preferences.Key.SHOW_FAB,
                    Constants.Preferences.DefaultValue.SHOW_FAB,
                )
            if (!isShowFab) {
                binding.addFAB.shrink()
            } else {
                binding.addFAB.extend()
            }
            app.isSettingsChanged = false
            return
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun initViews() {
        binding.searchBar.post {
            binding.searchBar.setHint(
                hint = resources.getString(R.string.search_hint),
                withAnimation = false,
                moveDown = false,
            )
        }
        binding.searchBar.menu.clear()
        binding.searchBar.inflateMenu(R.menu.menu_main)
        binding.searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_settings -> {
                    navController.navigate(R.id.nav_settings_screen)
                }

                R.id.menu_select_all -> {
                    val newList = SparseBooleanArray()
                    // Select all notes, else unselect all
                    if (notesViewModel.selectedNotes.value!!.size() != notesAdapter.itemCount) {
                        repeat(notesAdapter.currentList.size) {
                            newList.put(it, true)
                        }
                    }
                    notesViewModel.selectedNotes.postValue(newList)
                }

                R.id.menu_remove -> {
                    val notes = notesViewModel.selectedNotes.value!!
                    if (notes.isEmpty()) return@setOnMenuItemClickListener true

                    val selectedNotesPositions =
                        mutableListOf<Int>().apply {
                            repeat(notes.size()) { adapterPosition ->
                                add(notes.keyAt(adapterPosition))
                            }
                        }

                    val removedNotes =
                        mutableListOf<NoteItem>().apply {
                            selectedNotesPositions.forEach { i ->
                                add(notesAdapter.currentList.getOrNull(i) ?: return@apply)
                            }
                        }
                    notesViewModel.removeNotes(removedNotes)
                    notesViewModel.selectedNotes.postValue(SparseBooleanArray())

                    val snackBarUndoTitle =
                        resources.getText(R.string.snackbar_undo_title)
                            .toString() + " " + removedNotes.size.toString()

                    Snackbar.make(binding.addFAB, snackBarUndoTitle, Constants.SNACKBAR_DURATION).apply {
                        animationMode = Snackbar.ANIMATION_MODE_SLIDE
                        val tv = TypedValue()
                        requireContext().theme.resolveAttribute(android.R.attr.colorAccent, tv, true)
                        setActionTextColor(tv.data)
                        setAction(R.string.undo) {
                            notesViewModel.restoreNotes(removedNotes)
                            Snackbar.make(binding.addFAB, snackBarUndoTitle, Constants.SNACKBAR_DURATION).apply {
                                setText(R.string.successfully)
                                duration = 1_000
                                show()
                            }
                        }
                        show()
                    }
                }
            }
            true
        }
        val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    binding.searchView.hide()
                }
            }
        binding.searchView.apply {
            setupWithSearchBar(binding.searchBar) // Setup programmatically, because we don't use CoordinatorLayout in xml
            editText.addTextChangedListener {
                notesViewModel.searchViewText.postValue(binding.searchView.text.toString())
            }
            addTransitionListener { _, _, newState ->
                onBackPressedCallback.isEnabled = newState == SearchView.TransitionState.SHOWN
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)
        binding.notesRecyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (binding.searchBar.isCountModeEnabled) {
                        return
                    }
                    if (dy > 10) {
                        binding.addFAB.hide()
                    }
                    if (dy < -5) {
                        binding.addFAB.show()
                    }
                }
            },
        )
        val viewAdapter = ViewAdapter(filterSpinner)
        binding.notesRecyclerView.adapter = ConcatAdapter(viewAdapter, notesAdapter)
        binding.searchRecyclerView.adapter = searchAdapter
        filterSpinner.setItemClickListener { position, _ ->
            notesViewModel.filter.value = position
            val filteredNotes = getFilteredNotes(notesViewModel.notesList.value ?: emptyList())
            notesAdapter.submitList(filteredNotes)
            searchAdapter.submitList(filteredNotes)
        }
        binding.addFAB.setOnClickListener {
            val tv = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.colorBackground, tv, true)

            CoroutineScope(Dispatchers.IO).launch {
                val noteItem = NoteItem(background = tv.data.toString())
                noteItem.id = notesViewModel.addNote(noteItem)
                currentNoteViewModel.setNote(noteItem)
                withContext(Dispatchers.Main) {
                    navController.navigate(R.id.nav_note_screen)
                }
            }
        }
        if (!sp.getBoolean(
                Constants.Preferences.Key.SHOW_FAB,
                Constants.Preferences.DefaultValue.SHOW_FAB,
            )
        ) {
            binding.addFAB.shrink()
        } else {
            binding.addFAB.extend()
        }

        notesViewModel.notesList.observe(viewLifecycleOwner) { list ->
            val filteredList = getFilteredNotes(list)
            if (filteredList.isEmpty()) {
                filterSpinner.visibility = View.GONE
            } else {
                filterSpinner.visibility = View.VISIBLE
            }
            notesAdapter.submitList(filteredList)
            binding.hintInclude.textView.setText(R.string.emptyRecyclerView)
            if (filteredList.isEmpty()) {
                showEmptyImage(binding.notesRecyclerView, binding.hintInclude.root)
            } else {
                hideEmptyImage(binding.notesRecyclerView, binding.hintInclude.root)
            }
        }

        notesViewModel.selectedNotes.apply {
            // Variable to not animate the count mode on rotation the first time the observer is called
            var startWithAnimation = false
            var previousCount = 0
            observe(viewLifecycleOwner) { selectedNotes ->
                val count = selectedNotes.size()
                val isEmpty = count == 0

                // Hide/show the FAB based on selection
                binding.addFAB.isEnabled = isEmpty
                if (isEmpty) {
                    binding.addFAB.show()
                } else {
                    binding.addFAB.hide()
                }

                notesAdapter.selectList(selectedNotes.clone())

                val moveDown = previousCount < count
                previousCount = count
                binding.searchBar.post { // Use inside of post to ensure correct work after rotation
                    val searchBar = binding.searchBar
                    if (isEmpty) {
                        if (searchBar.isCountModeEnabled) {
                            val restoredText = notesViewModel.searchViewText.value
                            val restoredHint = ContextCompat.getString(requireContext(), R.string.search_hint)
                            searchBar.stopCountMode(restoredText, restoredHint, R.menu.menu_main)
                        }
                    } else {
                        if (!searchBar.isCountModeEnabled) {
                            searchBar.startCountMode(startWithAnimation, count, R.menu.menu_notes_selection) {
                                notesViewModel.selectedNotes.postValue(SparseBooleanArray())
                            }
                        } else {
                            searchBar.setHint(count.toString(), true, moveDown)
                        }
                        searchBar.setNavigationOnClickListener {
                            notesViewModel.selectedNotes.postValue(SparseBooleanArray())
                        }
                    }
                    filterSpinner.isEnabled = isEmpty
                    startWithAnimation = true // Always animate after the first time observer is called
                }
            }
        }

        notesViewModel.searchedNotes.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                val filteredSearchedList = if (it == null) null else getFilteredNotes(it)
                if (notesViewModel.notesList.value.isNullOrEmpty()) { // No notes to search
                    binding.searchHintInclude.textView.setText(R.string.emptyRecyclerView)
                    showEmptyImage(binding.searchRecyclerView, binding.searchHintInclude.root)
                    return@launch
                }
                binding.searchHintInclude.textView.setText(R.string.notesNotFound)
                if (filteredSearchedList == null) { // User cleared the SearchView, show all notes
                    val filteredNotesList = getFilteredNotes(notesViewModel.notesList.value)
                    searchAdapter.submitList(filteredNotesList)
                    hideEmptyImage(binding.searchRecyclerView, binding.searchHintInclude.root)
                } else if (filteredSearchedList.isEmpty()) { // Couldn't find the searched notes
                    searchAdapter.submitList(filteredSearchedList)
                    showEmptyImage(binding.searchRecyclerView, binding.searchHintInclude.root)
                } else { // Notes were found
                    searchAdapter.submitList(filteredSearchedList)
                    if (binding.searchHintInclude.root.visibility != View.VISIBLE) return@launch
                    hideEmptyImage(binding.searchRecyclerView, binding.searchHintInclude.root)
                }
            }
        }

        notesViewModel.searchViewText.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                binding.searchBar.post {
                    binding.searchBar.setText(it, false)
                }
                searchJob?.cancel()
                searchJob =
                    CoroutineScope(Dispatchers.Main).launch {
                        val filteredNotes = getSearchedNotes(it, notesViewModel.notesList.value)
                        notesViewModel.searchedNotes.postValue(filteredNotes)
                    }
            }
        }
    }

    private fun getFilteredNotes(notesList: List<NoteItem>?): List<NoteItem> {
        val filteredList =
            when (notesViewModel.filter.value!!) {
                Constants.Filter.OLDEST -> {
                    notesList?.sortedBy { it.id }
                }

                Constants.Filter.NEWEST -> {
                    notesList?.sortedByDescending { it.id }
                }

                Constants.Filter.MOST_TEXT -> {
                    notesList?.sortedByDescending { it.content.length + it.title.length }
                }

                Constants.Filter.LEAST_TEXT -> {
                    notesList?.sortedBy { it.content.length + it.title.length }
                }

                else -> {
                    notesList?.sortedBy { it.id }
                }
            }
        return filteredList ?: emptyList()
    }

    private fun getSearchedNotes(
        text: String?,
        notesList: List<NoteItem>?
    ): MutableList<NoteItem>? {
        return if (text.isNullOrEmpty()) {
            null // Clear and show all notes
        } else {
            val searchedList = mutableListOf<NoteItem>()
            if (notesList == null) return searchedList
            for (noteItem in notesList) {
                val title = noteItem.title
                val parsedTitle =
                    if (title.isEmpty()) { // Sometimes can be empty though idk why, can't reproduce anymore
                        ""
                    } else {
                        JSONObject(title).getString(com.pandacorp.noteui.domain.utils.Constants.TEXT)
                    }
                if (parsedTitle.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                    searchedList.add(noteItem)
                }
            }
            searchedList
        }
    }

    private fun showEmptyImage(
        recyclerView: RecyclerView,
        hint: View
    ) {
        val transition =
            Fade().apply {
                duration = Constants.ANIMATION_DURATION
                addTarget(recyclerView)
                addTarget(hint)
            }
        TransitionManager.beginDelayedTransition(binding.root, transition)
        recyclerView.visibility = View.GONE
        hint.visibility = View.VISIBLE
    }

    private fun hideEmptyImage(
        recyclerView: RecyclerView,
        hint: View
    ) {
        val transition =
            Fade().apply {
                duration = Constants.ANIMATION_DURATION
                addTarget(recyclerView)
                addTarget(hint)
            }
        TransitionManager.beginDelayedTransition(binding.root, transition)
        recyclerView.visibility = View.VISIBLE
        hint.visibility = View.GONE
    }
}