package com.pandacorp.notesui

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pandacorp.notesui.adapter.ListItem

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
    
    fun getCursor(TABLE_NAME: String): Cursor =
        writableDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null)
    
    fun add(table: String, listItem: ListItem) {
        val db = this.writableDatabase
        val cv = ContentValues()
        
        cv.put(HEADER_COL, listItem.header)
        cv.put(CONTENT_COL, listItem.content)
        
        db.insert(table, null, cv)
        
        
    }
    
    fun removeById(table: String, id: Int) {
        
        val db = this.writableDatabase
        val deletedPosition = getDatabaseItemIdByRecyclerViewItemId(table, id)
        
        db.delete(table, DBHelper.ID_COL + "=?", arrayOf("$deletedPosition"))
        
        
    }
    
    fun getDatabaseItemIdByRecyclerViewItemId(table: String, id: Int): Int? {
        val cursor = getCursor(table)
        //var of Id number in Timer_Table to understand how much there elements is
        var numberOfIds = 0
        //var of position of the deleted item
        var deletedPosition: Int?
        
        if (cursor!!.moveToFirst()) {
            val ID_COL = cursor.getColumnIndex(DBHelper.ID_COL)
            do {
                numberOfIds++
                if (numberOfIds == id + 1) {
                    
                    deletedPosition = cursor.getInt(ID_COL)
                    if (deletedPosition == null) {
                        throw Exception("deletedPosition cannot be null!")
                    }
                    return deletedPosition
                }
            } while (cursor.moveToNext())
        }
        return null
        
        
    }
    
    fun getDatabaseItems(): MutableList<ListItem> {
        val notesList = mutableListOf<ListItem>()
        
        //Creating WritableDatabase object
        
        //Creating Cursor object
        val cursor = getCursor(NOTES_TABLE)
        
        //Uploading the timers when opening the app
        val HEADER_COL = cursor.getColumnIndex(DBHelper.HEADER_COL)
        val CONTENT_COL = cursor.getColumnIndex(DBHelper.CONTENT_COL)
        if (cursor.moveToFirst()) {
            do {
                val stopwatch = ListItem(
                        cursor.getString(HEADER_COL),
                        cursor.getString(CONTENT_COL)
                
                )
                
                notesList.add(stopwatch)
                
                
            } while (cursor.moveToNext())
            
        }
        return notesList
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