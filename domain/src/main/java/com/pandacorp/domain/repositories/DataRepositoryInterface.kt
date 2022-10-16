package com.pandacorp.domain.repositories

import com.pandacorp.domain.models.ListItem

interface DataRepositoryInterface {
    fun getDatabaseItems(): MutableList<ListItem>
    fun add(listItem: ListItem)
    fun remove(listItem: ListItem)
}