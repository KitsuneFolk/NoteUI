package com.pandacorp.noteui.presentation.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pandacorp.noteui.data.database.Database
import com.pandacorp.noteui.data.mapper.ColorMapper
import com.pandacorp.noteui.data.mapper.NoteMapper
import com.pandacorp.noteui.data.repository.ColorRepositoryImpl
import com.pandacorp.noteui.data.repository.NoteRepositoryImpl
import com.pandacorp.noteui.domain.repository.ColorRepository
import com.pandacorp.noteui.domain.repository.NoteRepository
import com.pandacorp.noteui.presentation.utils.helpers.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(get(), Database::class.java, "NoteUIDatabase")
            .addCallback(object :
                RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        get<ColorRepository>().insert(Utils.getDefaultColorsList(get()))
                    }
                }
            })
            .build()

    }
    single {
        get<Database>().noteDao()
    }
    single {
        get<Database>().colorDao()
    }
    singleOf(::ColorMapper)
    singleOf(::NoteMapper)

    single<ColorRepository> {
        ColorRepositoryImpl(get(), get())
    }
    single<NoteRepository> {
        NoteRepositoryImpl(get(), get())
    }
}