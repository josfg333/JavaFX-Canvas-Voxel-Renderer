package josfg333.voxel.software_render

enum class Models(val m: Model) {
    CUBE(Model(
        vertices = listOf(
            Vertex(1.0, 1.0, 0.0), Vertex(0.0, 1.0, 0.0), Vertex(0.0, 0.0, 0.0), Vertex(1.0, 0.0, 0.0),
            Vertex(1.0, 1.0, 1.0), Vertex(0.0, 1.0, 1.0), Vertex(0.0, 0.0, 1.0), Vertex(1.0, 0.0, 1.0)
        ),
        tris = listOf(
            Tri(0, 1, 2, 0), Tri(2, 3, 0, 1),   // Front
            Tri(5, 4, 7, 2), Tri(7, 6, 5, 3),   // Back
            Tri(1, 5, 6, 4), Tri(6, 2, 1, 5),   // Left
            Tri(4, 0, 3, 6), Tri(3, 7, 4, 7),   // Right
            Tri(3, 2, 6, 8), Tri(6, 7, 3, 9),   // Bottom
            Tri(4, 5, 1, 10), Tri(1, 0, 4, 11), // Top
        )
    ))
}