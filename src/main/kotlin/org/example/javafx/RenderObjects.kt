package org.example.javafx


data class Vertex (val x: Double = 0.0, val y:Double = 0.0, val z:Double = 0.0) {
    val vec
        get() = Vec3(x, y, z)
}

data class Tri (val a: Int, val b: Int, val c: Int, val texture: Int) {
    fun toTriSeg(vertices: List<Vertex>) = TriSeg(
        a=Pair(vertices[a], vertices[b]),
        b=Pair(vertices[b], vertices[c]),
        c=Pair(vertices[c], vertices[a]),
        depth = {
            val v = (vertices[a].vec + vertices[c].vec) * 0.5
            v.x * v.x + v.y * v.y + v.z * v.z
        }(),
        texture=texture
    )
}

data class TriSeg (val a: Pair<Vertex, Vertex>?, val b: Pair<Vertex, Vertex>?, val c: Pair<Vertex, Vertex>?, var depth: Double = Double.POSITIVE_INFINITY, val texture: Int=0) {
    constructor(array: Array<Pair<Vertex, Vertex>?>, depth: Double = Double.POSITIVE_INFINITY, texture: Int=0): this (array[0], array[1], array[2], depth,texture)
    val edgeArray = arrayOf(a, b, c)
}


data class Model (val vertices: List<Vertex>, val tris: List<Tri>){

}


class ModelInstance(val model: Model, val transform: Vec3) {
    fun transformedVertices(): List<Vertex> {
        return model.vertices.map({v ->
            (v.vec + transform).toVertex()
        })
    }
}
