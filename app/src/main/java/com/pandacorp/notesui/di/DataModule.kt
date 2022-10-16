package com.pandacorp.notesui.di

import androidx.room.Room
import com.pandacorp.data.database.NoteDao
import com.pandacorp.data.database.NoteDatabase
import com.pandacorp.data.repositories.DataRepository
import com.pandacorp.domain.repositories.DataRepositoryInterface
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
                get(),
                NoteDatabase::class.java,
                "note-database"
        ).build()
    }
    single<NoteDao> {
        val database = get<NoteDatabase>()
        database.noteDao()
    }
    single<DataRepositoryInterface>() {
        DataRepository(
                get()
        )
    }
    
}