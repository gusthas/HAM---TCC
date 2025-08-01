package com.apol.myapplication

class SelectableItem {
    // SelectableItem.kt
    interface SelectableItem {
        val id: Long
        var isSelected: Boolean
    }
}