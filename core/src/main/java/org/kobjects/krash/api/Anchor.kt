package org.kobjects.krash.api

interface Anchor {
    /**
     * Returns the normalized width of the view. For the screen, this value is negative.
     */
    val width: Float

    /**
     * Returns the normalized height of the view. For the screen, this value is negative.
     */
    val height: Float
    val tag: Any?
}