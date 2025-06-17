package org.example.javafx

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

interface Shader<V> {
    fun transform (v:V): V
}




class WorldToView(val camera: Camera): Shader<Vertex> {
    override fun transform(v: Vertex): Vertex {
        val pos = camera.pos
        val translateX = v.x - pos.x
        val translateY = v.y - pos.y
        val translateZ = v.z - pos.z

        val horX = translateX * cos(camera.thetaRad) + translateZ * sin(camera.thetaRad)
        val horY = translateY
        val horZ = translateX * -sin(camera.thetaRad) + translateZ * cos(camera.thetaRad)

        val vertX = horX
        val vertY = horY * cos(camera.alphaRad) + horZ * -sin(camera.alphaRad)
        val vertZ = horY * sin(camera.alphaRad) + horZ * cos(camera.alphaRad)

        return Vertex(vertX, vertY, vertZ)
    }
}

class SquareDistanceShader(val camera: Camera): Shader<Vertex> {
    override fun transform(v: Vertex): Vertex {
        return Vertex(v.x, v.y, v.x*v.x+v.y*v.y + v.z*v.z)
    }
}

class Perspective(val camera: Camera): Shader<Vertex> {

    override fun transform(v: Vertex): Vertex {
        // Camera pos in projection point
        val t = tan(degreesToRadians(camera.fov/2))
        val x = v.x / (v.z * t)
        val y = v.y / (v.z * t)
        val z = v.z
        // Camera pos in projection plane
//        val x = v.x / (v.z * tan(degreesToRadians(fov/2)) + 1)
//        val y = v.y / (v.z * tan(degreesToRadians(fov/2)) + 1)

        return Vertex(x, y, z)

    }
}

class ClipTri(val camera: Camera): Shader<TriSeg> {
    override fun transform(v: TriSeg): TriSeg {
        val edges = v.edgeArray
        val newEdges: Array<Pair<Vertex, Vertex>?> = Array(3) {null}

        for (i in 0 ..< 3) {
            val edge = edges[i]
            if (edge == null) continue
            val v1 = edge.first
            val v2 = edge.second

            val z1 = v1.z - camera.nearPlaneDistance
            val z2 = v2.z - camera.nearPlaneDistance

            if (-EPSILON <= z1-z2 && z1-z2 <= EPSILON) {
                if (z1 >= 0) {
                    newEdges[i] = Pair(v1, v2)
                }
                continue
            }

            val frac = z1 / (z1 - z2)
            if (z1 >= 0) {
                if (z2 >= 0) {
                    newEdges[i] = Pair(v1, v2)
                } else {
                    val x = v1.x+(v2.x-v1.x)*frac
                    val y = v1.y+(v2.y-v1.y)*frac
                    val z = camera.nearPlaneDistance
                    newEdges[i] = Pair(v1, Vertex(x, y, z))
                }
            } else {
                if (z2 >= 0) {
                    val x = v1.x+(v2.x-v1.x)*frac
                    val y = v1.y+(v2.y-v1.y)*frac
                    val z = camera.nearPlaneDistance
                    newEdges[i] = Pair(Vertex(x, y, z), v2)
                }
            }
        }
        return TriSeg(newEdges, v.texture)
    }
}

class TriPerspective (val vertexShader: Shader<Vertex>): Shader<TriSeg> {
    override fun transform(v: TriSeg): TriSeg {
        val newEdges: Array<Pair<Vertex, Vertex>?> = Array(3){null}
        for (i in 0..<3) {
            val edge = v.edgeArray[i]
            if (edge != null) newEdges[i] = Pair(
                vertexShader.transform(edge.first),
                vertexShader.transform(edge.second)
            )
        }

        return TriSeg(newEdges, v.texture)
    }
}



class Renderer (camera: Camera){
    val vertexPipeline: List<Shader<Vertex>> = listOf(WorldToView(camera))
    val segmentPipeline: List<Shader<TriSeg>> = listOf(ClipTri(camera), TriPerspective(Perspective(camera)))

    fun applyPipe(vertices: List<Vertex>, tris: List<Tri>) : Pair<List<Vertex>, List<TriSeg>> {
        var vertices = vertices
        for (shader in vertexPipeline) {
            vertices = vertices.map { v -> shader.transform(v) }
        }

        var triSegs: List<TriSeg> = tris.map {tri ->
            tri.toTriSeg(vertices)
        }
        for (shader in segmentPipeline) {
            triSegs = triSegs.map { t-> shader.transform(t) }
        }



        return Pair(vertices, triSegs)
    }
}