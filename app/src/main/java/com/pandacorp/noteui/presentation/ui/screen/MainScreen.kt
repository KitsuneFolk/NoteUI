package com.pandacorp.noteui.presentation.ui.screen

import android.graphics.Color
import android.os.Bundle
import android.util.SparseBooleanArray
import android.util.TypedValue
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.search.SearchView.TransitionState
import com.google.android.material.snackbar.Snackbar
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.ScreenMainBinding
import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.presentation.ui.adapter.notes.NotesAdapter
import com.pandacorp.noteui.presentation.utils.helpers.Constants
import com.pandacorp.noteui.presentation.utils.helpers.app
import com.pandacorp.noteui.presentation.utils.helpers.showFabIfHidden
import com.pandacorp.noteui.presentation.utils.helpers.sp
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
        NotesAdapter(requireContext()).apply {
            setOnClickListener(object : NotesAdapter.OnNoteItemClickListener {
                override fun onClick(noteItem: NoteItem, position: Int) {
                    if (actionMode != null) select(position)
                    else {
                        /* Pass an object, but not a reference, to fix the bug when viewmodel updated the note
                        via the function, but not with the observer */
                        currentNoteViewModel.setNote(noteItem.copy())
                        navController.navigate(R.id.nav_note_screen)
                    }
                }

                override fun onLongClick(noteItem: NoteItem, position: Int) {
                    select(position)
                }

                private fun select(position: Int) {
                    val selectedNotesList = notesViewModel.selectedNotes.value!!
                    selectedNotesList.apply {
                        if (get(position, false)) delete(position)
                        else put(position, true)
                        notesViewModel.selectedNotes.postValue(this)
                    }
                }
            })
        }
    }

    private val searchAdapter by lazy {
        NotesAdapter(requireContext()).apply {
            isSelectionEnabled = false
            setOnClickListener(object : NotesAdapter.OnNoteItemClickListener {
                override fun onClick(noteItem: NoteItem, position: Int) {
                    currentNoteViewModel.setNote(noteItem) // TODO: Maybe get the note from viewmodel by position, to resolve the bug when user can leave NoteScreen, and enter it again quickly and the note will not have the made changes
                    navController.navigate(R.id.nav_note_screen)
                }

                override fun onLongClick(noteItem: NoteItem, position: Int) {}
            })
        }
    }

    private var actionModeCallback = ActionModeCallback()
    private var actionMode: ActionMode? = null
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
            binding.apply {
                if (!sp.getBoolean(Constants.Preferences.isShowFabTextKey, true)) addFAB.shrink()
                else addFAB.extend()
            }
            app.isSettingsChanged = false
            return
        }
    }

    override fun onDestroy() {
        _binding = null
        actionMode = null
        super.onDestroy()
    }

    // Class needed for notes selection
    inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_item_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.recyclerview_menu_delete -> {
                    removeSelectedNotes()
                    binding.addFAB.showFabIfHidden()
                    mode.finish()
                    return true
                }

                R.id.recyclerview_menu_select_all -> {
                    val newList = SparseBooleanArray()
                    if (notesViewModel.selectedNotes.value!!.size() == notesAdapter.itemCount) {
                        // Unselect all
                        newList.clear()
                    } else {
                        // Select all
                        newList.apply {
                            repeat(notesAdapter.currentList.size) {
                                put(it, true)
                            }
                        }
                    }
                    notesViewModel.selectedNotes.postValue(newList)
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            notesViewModel.selectedNotes.postValue(SparseBooleanArray())
            actionMode = null
        }

        private fun removeSelectedNotes() {
            val selectedNotesPositions = mutableListOf<Int>().apply {
                repeat(notesViewModel.selectedNotes.value!!.size()) { adapterPosition ->
                    add(notesViewModel.selectedNotes.value!!.keyAt(adapterPosition))
                }
            }

            if (selectedNotesPositions.isEmpty()) return

            val removedNotes: MutableList<Pair<NoteItem, Int>> = mutableListOf<Pair<NoteItem, Int>>().apply {
                for (i in selectedNotesPositions) add(Pair(notesAdapter.currentList[i], i))
            }

            notesViewModel.removeNotes(removedNotes.map { it.first })

            val snackBarUndoTitle = resources.getText(R.string.snackbar_undo_title)
                .toString() + " " + removedNotes.size.toString()
            val tv = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.colorAccent, tv, true)
            val colorAccent = tv.data

            Snackbar.make(binding.addFAB, snackBarUndoTitle, Constants.SNACKBAR_DURATION).apply {
                animationMode = Snackbar.ANIMATION_MODE_SLIDE
                setTextColor(Color.WHITE)
                setActionTextColor(colorAccent)
                setAction(R.string.undo) {
                    notesViewModel.restoreNotes(removedNotes)
                    Snackbar.make(binding.addFAB, snackBarUndoTitle, Constants.SNACKBAR_DURATION).apply {
                        setText(R.string.successfully)
                        duration = 1_000
                        setTextColor(Color.WHITE)
                        show()
                    }
                }
                show()
            }
        }
    }

    private fun initViews(bundle: Bundle?) {
        binding.apply {
            searchBar.apply {
                title = getString(R.string.app_name)
                addMenuProvider(object : MenuProvider {
                    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                        inflateMenu(R.menu.menu_main)
                    }

                    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                        when (menuItem.itemId) {
                            R.id.menu_settings -> navController.navigate(R.id.nav_settings_screen)
                        }
                        return true
                    }
                }, viewLifecycleOwner)
            }
            searchView.apply {
                editText.apply {
                    addTextChangedListener {
                        notesViewModel.searchViewText.postValue(text.toString())
                    }
                }

                val onBackPressedCallback: OnBackPressedCallback =
                    object : OnBackPressedCallback(false) {
                        override fun handleOnBackPressed() {
                            hide()
                        }
                    }
                requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)
                addTransitionListener { _, _, newState ->
                    onBackPressedCallback.isEnabled = newState == TransitionState.SHOWN
                }
                if (bundle == null) searchBar.startOnLoadAnimation()
            }
            notesRecyclerView.adapter = notesAdapter
            searchRecyclerView.adapter = searchAdapter
            addFAB.setOnClickListener {
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
            if (!sp.getBoolean(Constants.Preferences.isShowFabTextKey, true)) addFAB.shrink()
            else addFAB.extend()
        }

        lifecycleScope.launch {
            notesViewModel.notesList.collect {
                notesAdapter.submitList(it)

                binding.hintInclude.textView.setText(R.string.emptyRecyclerView)
                if (it.isEmpty()) showEmptyImage(binding.notesRecyclerView, binding.hintInclude.root)
                else hideEmptyImage(binding.notesRecyclerView, binding.hintInclude.root)

            }
        }

        notesViewModel.selectedNotes.observe(viewLifecycleOwner) {
            notesAdapter.selectList(it.clone()) // Set a list, but not a reference
            CoroutineScope(Dispatchers.Main).launch { // Without the Main coroutine startActionMode returns null
                val count = it.size()

                if (count > 0 && actionMode == null) {
                    actionMode = binding.searchBar.startActionMode(actionModeCallback)
                }

                actionMode?.apply {
                    title = count.toString()
                    if (count == 0) finish()
                    else invalidate()
                }
            }
        }

        notesViewModel.filteredNotes.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                if (notesViewModel.getNotes().isEmpty()) { // No notes to search
                    binding.searchHintInclude.textView.setText(R.string.emptyRecyclerView)
                    showEmptyImage(binding.searchRecyclerView, binding.searchHintInclude.root)
                    return@launch
                }
                binding.searchHintInclude.textView.setText(R.string.notesNotFound)
                if (it == null) { // User cleared the SearchView, show all notes
                    searchAdapter.submitList(notesViewModel.getNotes())
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
                    val filteredNotes = getFilteredNotes(it, notesViewModel.getNotes())
                    notesViewModel.filteredNotes.postValue(filteredNotes)
                }
            }
        }
    }

    private fun getFilteredNotes(text: String?, notesList: List<NoteItem>): MutableList<NoteItem>? {
        return if (text.isNullOrEmpty())
            null // Clear and show all notes
        else {
            val filteredList = mutableListOf<NoteItem>()
            for (noteItem in notesList) {
                val parsedTitle =
                    JSONObject(noteItem.title).getString(com.pandacorp.noteui.domain.utils.Constants.text)
                if (parsedTitle.lowercase().contains(text.lowercase(Locale.getDefault())))
                    filteredList.add(noteItem)
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