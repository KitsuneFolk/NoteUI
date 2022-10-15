package com.pandacorp.domain.repositories

import com.pandacorp.domain.models.ListItem

interface DataRepositoryInterface {
    fun getDatabaseItems(table: String): MutableList<ListItem>
    fun add(table: String, listItem: ListItem)
    fun removeById(table: String, id: Int)
    fun getDatabaseItemIdByRecyclerViewItemId(table: String, id: Int): Int?
}