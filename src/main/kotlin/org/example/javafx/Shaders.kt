package org.example.javafx

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

abstract class Shader<V> {
    val cache: MutableMap<V, V> = mutableMapOf()
    abstract fun transform (v:V): V
}




class WorldToView(val camera: Camera): Shader<Vertex>() {
    override fun transform(v: Vertex): Vertex {
//        val out = cache[v]
//        if (out!=null) return out

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

        val ret = Vertex(vertX, vertY, vertZ)
//        cache[v] = ret
        return ret
    }
}

class SquareDistanceShader(val camera: Camera): Shader<Vertex>() {
    override fun transform(v: Vertex): Vertex {
        return Vertex(v.x, v.y, v.x*v.x+v.y*v.y + v.z*v.z)
    }
}

class Perspective(val camera: Camera): Shader<Vertex>() {
    override fun transform(v: Vertex): Vertex {
//        val out = cache[v]
//        if (out!=null) return out

        val t = tan(degreesToRadians(camera.fov/2))
        val z = v.z

        // x / (z - dolly*d) = newx / d
        // newx = d*x/(z-dolly*d) = x/(z/d - dolly) = x/(z*t - dolly)
        val x = v.x / (v.z * t - camera.dolly)
        val y = v.y / (v.z * t - camera.dolly)


        val ret = Vertex(x, y, z)
//        cache[v] = ret
        return ret

    }
}

class CullTri(val plane: Plane, val segment: Boolean = false): Shader<TriSeg?>() {
    override fun transform(v: TriSeg?): TriSeg? {
        v!!
        val edges = v.edgeArray

        for (i in 0..<3) {
            val edge = edges[i]
            if (edge == null) continue
            val v1 = edge.first
            val v2 = edge.second
            val z1 = plane.getSignedDistance(v1.vec)
            val z2 = plane.getSignedDistance(v2.vec)
            if (z1 >= 0 || z2 >= 0) {
                return v
            }
        }
        return null
    }
}

class ClipTri(val plane: Plane): Shader<TriSeg?>() {
    override fun transform(v: TriSeg?): TriSeg? {
        v!!
        val edges = v.edgeArray
        val newEdges: Array<Pair<Vertex, Vertex>?> = Array(3) {null}
        var visible = false

        for (i in 0 ..< 3) {
            val edge = edges[i]
            if (edge == null) continue
            val v1 = edge.first
            val v2 = edge.second

//            val z1 = v1.z - camera.nearPlaneDistance
//            val z2 = v2.z - camera.nearPlaneDistance

            val z1 = plane.getSignedDistance(v1.vec)
            val z2 = plane.getSignedDistance(v2.vec)

            if (-EPSILON <= z1-z2 && z1-z2 <= EPSILON) {
                if (z1 >= 0) {
                    newEdges[i] = Pair(v1, v2)
                    visible = true
                }
                continue
            }

            val frac = z1 / (z1 - z2)
            if (z1 >= 0) {
                visible = true
                if (z2 >= 0) {
                    newEdges[i] = Pair(v1, v2)
                } else {
                    val p1 = plane.proj(v1.vec)
                    val p2 = plane.proj(v2.vec)
                    newEdges[i] = Pair(v1, (p1 + (p2-p1)*frac).toVertex())
                }
            } else {
                if (z2 >= 0) {
                    visible = true

                    val p1 = plane.proj(v1.vec)
                    val p2 = plane.proj(v2.vec)
                    newEdges[i] = Pair((p1 + (p2-p1)*frac).toVertex(), v2)
                }
            }
        }
        return if (visible) TriSeg(newEdges, v.depth, v.texture) else null
    }
}

class TriPerspective (val vertexShader: Shader<Vertex>): Shader<TriSeg?>() {
    override fun transform(v: TriSeg?): TriSeg? {
        v!!
        val newEdges: Array<Pair<Vertex, Vertex>?> = Array(3){null}
        for (i in 0..<3) {
            val edge = v.edgeArray[i]
            if (edge != null) newEdges[i] = Pair(
                vertexShader.transform(edge.first),
                vertexShader.transform(edge.second)
            )
        }

        return TriSeg(newEdges, v.depth,v.texture)
    }
}



class Renderer (val camera: Camera, val modelInstances: List<ModelInstance>){
    val vertexPipeline: List<Shader<Vertex>> = listOf(WorldToView(camera))


    var worldModel: Model = Model(listOf(), listOf())

    fun aggregateModelInstances() {
        val vertices: MutableList<Vertex> = mutableListOf()
        val tris: MutableList<Tri> = mutableListOf()

        val uniqueVertices: MutableMap<Vertex, Int> = mutableMapOf()
        var totalVertices = 0
        for (instance in modelInstances) {
            val indexMap: MutableList<Int> = mutableListOf()
            val newVertices = instance.transformedVertices()
            totalVertices += newVertices.size
            newVertices.forEachIndexed {i,v ->
                val index = uniqueVertices[v]
                if (index != null) {
                    indexMap.add(index)
                } else {
                    val index = vertices.size
                    indexMap.add(index)
                    uniqueVertices[v] = index
                    vertices.add(v)
                }
            }
            tris.addAll(instance.model.tris.mapIndexedNotNull{i,t ->
                if(i >= instance.mask.size || instance.mask[i])
                    Tri(indexMap[t.a], indexMap[t.b], indexMap[t.c], t.texture)
                else null
            })
        }
        worldModel = Model(vertices, tris)
        println("Vertices: %d / %d".format(uniqueVertices.size, totalVertices))
    }

    fun applyPipe() : List<TriSeg> {
        var vertices = worldModel.vertices
        val tris = worldModel.tris
        for (shader in vertexPipeline) {
            shader.cache.clear()
            vertices = vertices.map { v -> shader.transform(v) }
        }

        val tangent = tan(degreesToRadians(camera.fov/2))
        val depthFunc = {t: Tri ->
            val v = (vertices[t.a].vec + vertices[t.c].vec) * 0.5 + Vec3(0.0, 0.0, -camera.dolly/tangent)
            v.x * v.x + v.y * v.y + v.z * v.z
        }
        var triSegs: List<TriSeg> = tris.map {tri ->
            tri.toTriSeg(vertices, depthFunc)
        }

        val segmentPipeline: List<Shader<TriSeg?>> = listOf(
            ClipTri(camera.nearPlaneLocal),
            CullTri(camera.bottomFrustumLocal),
            CullTri(camera.topFrustumLocal),
            CullTri(camera.leftFrustumLocal),
            CullTri(camera.rightFrustumLocal),

            TriPerspective(Perspective(camera))
        )
        for (shader in segmentPipeline) {
            shader.cache.clear()
            triSegs = triSegs.mapNotNull { t-> shader.transform(t) }
        }

        return triSegs
    }
}