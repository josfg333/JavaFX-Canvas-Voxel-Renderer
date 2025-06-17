package org.example.javafx


data class Model (val vertices: List<Vertex>, val tris: List<Tri>, val textures: List<Int>){

}


class ModelInstance(val model: Model, val transform: Array<Double>) {

}
