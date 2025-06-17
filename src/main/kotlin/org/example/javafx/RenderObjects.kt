package org.example.javafx


data class Vertex (val x: Double = 0.0, val y:Double = 0.0, val z:Double = 0.0) {
    val vec
        get() = Vec3(x, y, z)
}

data class Tri (val a: Int, val b: Int, val c: Int, val texture: Int) {
    fun toTriSeg(vertices: List<Vertex>) = TriSeg(
        Pair(vertices[a], vertices[b]),
        Pair(vertices[b], vertices[c]),
        Pair(vertices[c], vertices[a]),
        texture
    )
}

data class TriSeg (val a: Pair<Vertex, Vertex>?, val b: Pair<Vertex, Vertex>?, val c: Pair<Vertex, Vertex>?, val texture: Int=0) {
    constructor(array: Array<Pair<Vertex, Vertex>?>, texture: Int=0): this (array[0], array[1], array[2], texture)
    val edgeArray = arrayOf(a, b, c)
}


