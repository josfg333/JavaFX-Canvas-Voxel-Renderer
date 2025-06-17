package org.example.javafx

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

abstract class SegmentShader {
    abstract fun transform (t: TriSeg): TriSeg
}

abstract class VertexShader {
    abstract fun transform (v: Vertex): Vertex
}

class WorldToView(val camera: Camera): VertexShader() {
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

class SquareDistanceShader(val camera: Camera): VertexShader() {
    override fun transform(v: Vertex): Vertex {
        return Vertex(v.x, v.y, v.x*v.x+v.y*v.y + v.z*v.z)
    }
}

class Perspective(val camera: Camera): VertexShader() {

    override fun transform(v: Vertex): Vertex {
        // Camera pos in projection point
        val t = tan(degreesToRadians(camera.fov/2))
        val x = v.x / (v.z * t)
        val y = v.y / (v.z * t)
        val z = v.x*v.x + v.y*v.y + v.z*v.z
        // Camera pos in projection plane
//        val x = v.x / (v.z * tan(degreesToRadians(fov/2)) + 1)
//        val y = v.y / (v.z * tan(degreesToRadians(fov/2)) + 1)

        return Vertex(x, y, z)

    }
}

class ClipTri(val camera: Camera): SegmentShader() {
    override fun transform(t: TriSeg): TriSeg {
        val starts: Array<Vertex> = arrayOf(t.a1, t.b1, t.c1)
        val ends: Array<Vertex> = arrayOf(t.a2, t.b2, t.c2)
        val show: Array<Boolean> = arrayOf(t.aShow, t.bShow, t.cShow)
        for (i in 0 ..< 3) {
            if (!show[i]) continue
            val v1 = starts[i]
            val v2 = ends[i]

            val z1 = v1.z - camera.nearPlaneDistance
            val z2 = v2.z - camera.nearPlaneDistance

            if (-EPSILON <= z1-z2 && z1-z2 <= EPSILON) {
                if (z1 >= 0) {
                    starts[i] = v1
                    ends[i] = v2
                } else {
                    starts[i] = Vertex(z=camera.nearPlaneDistance)
                    ends[i] = Vertex(z=camera.nearPlaneDistance)
                    show[i] = false
                }
            }

            val frac = z1 / (z1 - z2)
            if (z1 >= 0) {
                starts[i] = v1
                if (z2 >= 0) {
                    ends[i] = v2
                } else {
                    ends[i] = Vertex(v1.x+(v2.x-v1.x)*frac, v1.y+(v2.y-v1.y)*frac, camera.nearPlaneDistance)
                }
            } else {
                if (z2 >= 0) {
                    ends[i] = v2
                    starts[i] = Vertex(v1.x+(v2.x-v1.x)*frac, v1.y+(v2.y-v1.y)*frac, camera.nearPlaneDistance)
                } else {
                    starts[i] = Vertex(z=camera.nearPlaneDistance)
                    ends[i] = Vertex(z=camera.nearPlaneDistance)
                    show[i] = false
                }
            }
        }
        return TriSeg(
            starts[0], ends[0], show[0],
            starts[1], ends[1], show[1],
            starts[2], ends[2], show[2]
        )
    }
}

class TriPerspective (val vertexShader: VertexShader): SegmentShader() {
    override fun transform(t: TriSeg): TriSeg {
        val a1 = if (t.aShow) vertexShader.transform(t.a1) else t.a1
        val a2 = if (t.aShow) vertexShader.transform(t.a2) else t.a2
        val b1 = if (t.bShow) vertexShader.transform(t.b1) else t.b1
        val b2 = if (t.bShow) vertexShader.transform(t.b2) else t.b2
        val c1 = if (t.cShow) vertexShader.transform(t.c1) else t.c1
        var c2 = if (t.cShow) vertexShader.transform(t.c2) else t.c2

        if (t.cShow) {
            c2 = Vertex(c2.x, c2.y,(t.c1.x+t.c2.x)*(t.c1.x+t.c2.x) + (t.c1.y+t.c2.y)*(t.c1.y+t.c2.y) + (t.c1.z+t.c2.z)*(t.c1.z+t.c2.z))
        } else {
            c2 = Vertex(c2.x, c2.y,(t.a1.x+t.b2.x)*(t.a1.x+t.b2.x) + (t.a1.y+t.b2.y)*(t.a1.y+t.b2.y) + (t.a1.z+t.b2.z)*(t.a1.z+t.b2.z))
        }

        return TriSeg (
            a1, a2, t.aShow,
            b1, b2, t.bShow,
            c1, c2, t.cShow
        )
    }
}



class Renderer (camera: Camera){
    val vertexPipeline: List<VertexShader> = listOf(WorldToView(camera))
    val segmentPipeline: List<SegmentShader> = listOf(ClipTri(camera), TriPerspective(Perspective(camera)))

    fun applyPipe(vertices: List<Vertex>, tris: List<Tri>) : Pair<List<Vertex>, List<TriSeg>> {
        var vertices = vertices
        for (shader in vertexPipeline) {
            vertices = vertices.map { v -> shader.transform(v) }
        }

        var triSegs: List<TriSeg> = tris.map {tri ->
            TriSeg(
                vertices[tri.a], vertices[tri.b], true,
                vertices[tri.b], vertices[tri.c], true,
                vertices[tri.c], vertices[tri.a], true
            )
        }
        for (shader in segmentPipeline) {
            triSegs = triSegs.map { t-> shader.transform(t) }
        }



        return Pair(vertices, triSegs)
    }
}