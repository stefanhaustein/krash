package org.kobjects.krash.api

interface Bubble : Content {
    var content: Content
    var fillColor : Int
    var lineColor : Int
    var lineWidth : Float
    var cornerRadius : Float
    var padding : Float
    var dX: Float
    var dY: Float

}