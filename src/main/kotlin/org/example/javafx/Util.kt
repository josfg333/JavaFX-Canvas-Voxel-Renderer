package org.example.javafx

import kotlin.math.PI

const val EPSILON: Double = 0.000000001

fun degreesToRadians(degrees: Double) : Double {
    return PI * degrees / 180.0
}