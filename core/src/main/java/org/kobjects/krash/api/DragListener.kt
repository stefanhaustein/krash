package org.kobjects.krash.api

interface DragListener {
    enum class DragState {
        START, MOVE, END, CANCEL
    }

    fun drag(state: DragState, x: Float, y: Float): Boolean
}