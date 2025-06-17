package org.example.javafx


data class Vertex (val x: Double = 0.0, val y:Double = 0.0, val z:Double = 0.0) {
    val vec
        get() = Vec3(x, y, z)
}

data class Tri (val a: Int, val b: Int, val c: Int)

data class TriSeg (val a1: Vertex, val a2: Vertex, val aShow: Boolean,
              val b1: Vertex, val b2: Vertex, val bShow: Boolean,
              val c1: Vertex, val c2: Vertex, val cShow: Boolean)


