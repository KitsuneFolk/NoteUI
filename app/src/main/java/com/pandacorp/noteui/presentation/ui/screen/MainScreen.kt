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
import androidx.core.util.isNotEmpty
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.ScreenMainBinding
import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.presentation.ui.adapter.notes.NotesAdapter
import com.pandacorp.noteui.presentation.utils.helpers.Constants
import com.pandacorp.noteui.presentation.utils.helpers.animateAlpha
import com.pandacorp.noteui.presentation.utils.helpers.app
import com.pandacorp.noteui.presentation.utils.helpers.sp
import com.pandacorp.noteui.presentation.utils.views.searchbar.searchview.SearchView
import com.pandacorp.noteui.presentation.viewModels.CurrentNoteViewModel
import com.pandacorp.noteui.presentation.viewModels.NotesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class MainScreen : Fragment() {
    private var _binding: ScreenMainBinding? = null
    private val binding get() = _binding!!

    private val notesViewModel: NotesViewModel by viewModel()
    private val currentNoteViewModel: CurrentNoteViewModel by sharedViewModel()

    private val navController by lazy { findNavController() }

    private val notesAdapter by lazy {
        NotesAdapter().apply {
            setOnClickListener(object : NotesAdapter.OnNoteItemClickListener {
                override fun onClick(noteItem: NoteItem, position: Int) {
                    if (binding.searchBar.isCountModeEnabled) {
                        select(position)
                    } else {
                        currentNoteViewModel.setNote(notesViewModel.getNoteById(noteItem.id))
                        navController.navigate(R.id.nav_note_screen)
                    }
                }

                override fun onLongClick(noteItem: NoteItem, position: Int) {
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
            })
        }
    }

    private val searchAdapter by lazy {
        NotesAdapter().apply {
            isSelectionEnabled = false
            setOnClickListener(object : NotesAdapter.OnNoteItemClickListener {
                override fun onClick(noteItem: NoteItem, position: Int) {
                    currentNoteViewModel.setNote(notesViewModel.getNoteById(noteItem.id))
                    navController.navigate(R.id.nav_note_screen)
                }

                override fun onLongClick(noteItem: NoteItem, position: Int) {}
            })
        }
    }

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ScreenMainBinding.inflate(inflater, container, false)

        initViews(savedInstanceState)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        currentNoteViewModel.clearData()
        if (app.isSettingsChanged) {
            val isShowFab = sp.getBoolean(
                Constants.Preferences.isShowFabTextKey,
                Constants.Preferences.isShowFabTextDefaultValue,
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

    private fun initViews(bundle: Bundle?) {
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

                    val selectedNotesPositions = mutableListOf<Int>().apply {
                        repeat(notes.size()) { adapterPosition ->
                            add(notes.keyAt(adapterPosition))
                        }
                    }

                    val removedNotes = mutableListOf<NoteItem>().apply {
                        selectedNotesPositions.forEach { i ->
                            add(notesAdapter.currentList.getOrNull(i) ?: return@apply)
                        }
                    }
                    notesViewModel.removeNotes(removedNotes)
                    notesViewModel.selectedNotes.postValue(SparseBooleanArray())

                    val snackBarUndoTitle = resources.getText(R.string.snackbar_undo_title)
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
        binding.searchView.editText.addTextChangedListener {
            notesViewModel.searchViewText.postValue(binding.searchView.text.toString())
        }

        val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    binding.searchView.hide()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)
        binding.searchView.addTransitionListener { _, _, newState ->
            onBackPressedCallback.isEnabled = newState == SearchView.TransitionState.SHOWN
        }
        if (bundle == null) binding.searchBar.startOnLoadAnimation()
        binding.notesRecyclerView.adapter = notesAdapter
        binding.searchRecyclerView.adapter = searchAdapter
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
                Constants.Preferences.isShowFabTextKey,
                Constants.Preferences.isShowFabTextDefaultValue,
            )
        ) {
            binding.addFAB.shrink()
        } else {
            binding.addFAB.extend()
        }

        notesViewModel.notesList.observe(viewLifecycleOwner) {
            notesAdapter.submitList(it)
            searchAdapter.submitList(it)

            binding.hintInclude.textView.setText(R.string.emptyRecyclerView)
            if (it.isEmpty()) {
                showEmptyImage(binding.notesRecyclerView, binding.hintInclude.root)
            } else {
                hideEmptyImage(binding.notesRecyclerView, binding.hintInclude.root)
            }
        }

        notesViewModel.selectedNotes.observe(viewLifecycleOwner) {
            val count = it.size()
            // Hide the FAB if there is selection
            binding.addFAB.isEnabled = it.isEmpty()
            if (it.isNotEmpty()) {
                binding.addFAB.hide()
            } else {
                binding.addFAB.show()
            }

            notesAdapter.selectList(it.clone()) // Set a list, but not a reference

            binding.searchBar.post { // Use inside of post to resolve the bug when searchbar doesn't respond after rotation
                if (count <= 0) {
                    if (binding.searchBar.isCountModeEnabled) {
                        binding.searchBar.stopCountMode()
                        binding.searchBar.setNavigationOnClickListener(null)
                        binding.searchBar.textView.animateAlpha(1f, 0f, 200) {
                            binding.searchBar.hint =
                                ContextCompat.getString(requireContext(), R.string.search_hint)
                            binding.searchBar.textView.animateAlpha(0f, 1f, 200)
                        }
                        // Alpha animation for the Menu Icons
                        val menuIcons = binding.searchBar.getChildAt(binding.searchBar.childCount - 1)
                        menuIcons.animateAlpha(1f, 0f, 200) {
                            binding.searchBar.menu.clear()
                            binding.searchBar.inflateMenu(R.menu.menu_main)
                            menuIcons.animateAlpha(0f, 1f, 200)
                        }
                    }
                } else {
                    if (!binding.searchBar.isCountModeEnabled) {
                        binding.searchBar.startCountMode()
                        binding.searchBar.setNavigationOnClickListener {
                            notesViewModel.selectedNotes.postValue(SparseBooleanArray())
                        }
                        binding.searchBar.textView.animateAlpha(1f, 0f, 200) {
                            binding.searchBar.hint = count.toString()
                            binding.searchBar.textView.animateAlpha(0f, 1f, 200)
                        }
                        // Alpha animation for the Menu Icons
                        val menuIcons = binding.searchBar.getChildAt(binding.searchBar.childCount - 1)
                        menuIcons.animateAlpha(1f, 0f, 200) {
                            binding.searchBar.menu.clear()
                            binding.searchBar.inflateMenu(R.menu.menu_notes_selection)
                            menuIcons.animateAlpha(0f, 1f, 200)
                        }
                    } else {
                        binding.searchBar.hint = count.toString()
                    }
                }
            }
        }

        notesViewModel.filteredNotes.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                if (notesViewModel.notesList.value.isNullOrEmpty()) { // No notes to search
                    binding.searchHintInclude.textView.setText(R.string.emptyRecyclerView)
                    showEmptyImage(binding.searchRecyclerView, binding.searchHintInclude.root)
                    return@launch
                }
                binding.searchHintInclude.textView.setText(R.string.notesNotFound)
                if (it == null) { // User cleared the SearchView, show all notes
                    searchAdapter.submitList(notesViewModel.notesList.value)
                    hideEmptyImage(binding.searchRecyclerView, binding.searchHintInclude.root)
                } else if (it.isEmpty()) { // Couldn't find the searched notes
                    searchAdapter.submitList(it)
                    showEmptyImage(binding.searchRecyclerView, binding.searchHintInclude.root)
                } else { // Notes were found
                    searchAdapter.submitList(it)
                    if (binding.searchHintInclude.root.visibility != View.VISIBLE) return@launch
                    hideEmptyImage(binding.searchRecyclerView, binding.searchHintInclude.root)
                }
            }
        }

        notesViewModel.searchViewText.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                binding.searchBar.text = it
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    val filteredNotes = getFilteredNotes(it, notesViewModel.notesList.value)
                    notesViewModel.filteredNotes.postValue(filteredNotes)
                }
            }
        }
    }

    private fun getFilteredNotes(text: String?, notesList: List<NoteItem>?): MutableList<NoteItem>? {
        return if (text.isNullOrEmpty()) {
            null // Clear and show all notes
        } else {
            val filteredList = mutableListOf<NoteItem>()
            if (notesList == null) return filteredList
            for (noteItem in notesList) {
                val title = noteItem.title
                val parsedTitle =
                    if (title.isEmpty()) { // Sometimes can be empty though idk why, can't reproduce anymore
                        ""
                    } else {
                        JSONObject(title).getString(com.pandacorp.noteui.domain.utils.Constants.text)
                    }
                if (parsedTitle.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                    filteredList.add(noteItem)
                }
            }
            filteredList
        }
    }

    private fun showEmptyImage(recyclerView: RecyclerView, hint: View) {
        val transition = Fade().apply {
            duration = Constants.ANIMATION_DURATION
            addTarget(recyclerView)
            addTarget(hint)
        }
        TransitionManager.beginDelayedTransition(binding.root, transition)
        recyclerView.visibility = View.GONE
        hint.visibility = View.VISIBLE
    }

    private fun hideEmptyImage(recyclerView: RecyclerView, hint: View) {
        val transition = Fade().apply {
            duration = Constants.ANIMATION_DURATION
            addTarget(recyclerView)
            addTarget(hint)
        }
        TransitionManager.beginDelayedTransition(binding.root, transition)
        recyclerView.visibility = View.VISIBLE
        hint.visibility = View.GONE
    }
}