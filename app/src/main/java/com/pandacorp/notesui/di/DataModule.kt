package com.pandacorp.notesui.di

import androidx.room.Room
import com.pandacorp.data.database.Database
import com.pandacorp.data.database.NoteDao
import com.pandacorp.data.repositories.DataRepository
import com.pandacorp.domain.repositories.DataRepositoryInterface
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
                get(),
                Database::class.java,
                "noteDatabase"
        ).build()
    }
    single<NoteDao> {
        val database = get<Database>()
        database.noteDao()
    }
    single<DataRepositoryInterface>() {
        DataRepository(
                get()
        )
    }
    
}