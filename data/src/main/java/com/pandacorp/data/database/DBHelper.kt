package com.pandacorp.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    private val TAG = "MyLogs"
    
    override fun onCreate(db: SQLiteDatabase) {
        val create_notes_table_query = ("CREATE TABLE " + NOTES_TABLE + " ("
                + ID_COL + " INTEGER PRIMARY KEY, "
                + HEADER_COL + " TEXT,"
                + CONTENT_COL + " TEXT" + ")")
        db.execSQL(create_notes_table_query)
        
    }
    
    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + NOTES_TABLE)
        onCreate(db)
    }
    
    companion object {
        // here we have defined variables for our database
        
        // below is variable for database name
        private const val DATABASE_NAME = "NotesUI"
        
        // below is the variable for database version
        private const val DATABASE_VERSION = 1
        
        // below is the variable for table name
        const val NOTES_TABLE = "NOTES_TABLE"
        
        // below is the variable for id column
        const val ID_COL = "id"
        
        val HEADER_COL = "header"
        val CONTENT_COL = "content"
        
        
    }
}