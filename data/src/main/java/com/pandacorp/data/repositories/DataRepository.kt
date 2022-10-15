package com.pandacorp.data.repositories

import android.content.ContentValues
import com.pandacorp.data.database.DBHelper
import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.repositories.DataRepositoryInterface

class DataRepository(private val dbHelper: DBHelper) : DataRepositoryInterface {
    
    override fun getDatabaseItems(table: String): MutableList<ListItem> {
        val notesList = mutableListOf<ListItem>()
        
        val cursor = dbHelper.writableDatabase.rawQuery("SELECT * FROM $table", null)
        
        val HEADER_COL = cursor.getColumnIndex(DBHelper.HEADER_COL)
        val CONTENT_COL = cursor.getColumnIndex(DBHelper.CONTENT_COL)
        if (cursor.moveToFirst()) {
            do {
                val note = ListItem(
                        cursor.getString(HEADER_COL),
                        cursor.getString(CONTENT_COL)
                
                )
                
                notesList.add(note)
                
                
            } while (cursor.moveToNext())
            
        }
        cursor.close()
        return notesList
    }
    
    override fun add(table: String, listItem: ListItem) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues()
    
        cv.put(DBHelper.HEADER_COL, listItem.header)
        cv.put(DBHelper.CONTENT_COL, listItem.content)
    
        db.insert(table, null, cv)
    }
    
    override fun removeById(table: String, id: Int) {
        val db = dbHelper.writableDatabase
        val deletedPosition = getDatabaseItemIdByRecyclerViewItemId(table, id)
    
        db.delete(table, DBHelper.ID_COL + "=?", arrayOf("$deletedPosition"))
    
    }
    
    override fun getDatabaseItemIdByRecyclerViewItemId(table: String, id: Int): Int? {
        val cursor = dbHelper.writableDatabase.rawQuery("SELECT * FROM $table", null)
    
        //var of Id number in Table to understand how many there is elements
        var numberOfIds = 0
        //var of position of the deleted item
        val deletedPosition: Int?
    
        if (cursor.moveToFirst()) {
            val ID_COL = cursor.getColumnIndex(DBHelper.ID_COL)
            do {
                numberOfIds++
                if (numberOfIds == id + 1) {
                
                    deletedPosition = cursor.getInt(ID_COL)
                    cursor.close()
                    return deletedPosition
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return null
    
    }
    
}
