package org.kobjects.krash.api

interface Bubble<T> : Content where T : Content {
    var content: T
    var fillColor : Int
    var lineColor : Int
    var lineWidth : Float
    var cornerRadius : Float
    var padding : Float
    var dX: Float
    var dY: Float

}