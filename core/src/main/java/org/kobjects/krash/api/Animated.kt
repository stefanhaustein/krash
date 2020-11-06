package org.kobjects.krash.api

interface Animated {
    fun animate(dt: Float, propertiesChanged: Boolean)
}