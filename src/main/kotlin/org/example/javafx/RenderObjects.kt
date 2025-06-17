package org.example.javafx

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.text.Font
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

const val EPSILON: Double = 0.000000001
const val DEBUG_VIEW_ENABLED: Boolean = false

class Vec3 (val x: Double = 0.0, val y: Double=0.0, val z: Double=0.0) {

    val vertex
        get() = Vertex(x, y, z)

    operator fun plus(other: Vec3): Vec3 {
        return Vec3(x+other.x, y+other.y, z+other.z)
    }

    operator fun times(other: Vec3): Vec3 {
        return Vec3(y*other.z - z*other.y, z*other.x-x*other.z, x*other.y-y*other.x)
    }

    operator fun times(scalar: Double): Vec3 {
        return Vec3(scalar*x, scalar*y, scalar*z)
    }

    fun dot(other: Vec3): Double {
        return x*other.x + y*other.y + z*other.z
    }

}

class Vertex (val x: Double = 0.0, val y:Double = 0.0, val z:Double = 0.0) {
    val vec
        get() = Vec3(x, y, z)
}

class Tri (val a: Int, val b: Int, val c: Int)

class TriSeg (val a1: Vertex, val a2: Vertex, val aShow: Boolean,
              val b1: Vertex, val b2: Vertex, val bShow: Boolean,
              val c1: Vertex, val c2: Vertex, val cShow: Boolean): Comparable<TriSeg>
{
    fun getSortedZs(): List<Double> {
        val ret = mutableListOf<Double>()
        if (aShow) ret.add(a1.z + a2.z)
        if (bShow) ret.add(b1.z + b2.z)
//        if (cShow) ret.add(c1.z + c2.z)
        ret.sort()
        return ret
    }

    override fun compareTo(other: TriSeg): Int {
        val thisList = getSortedZs()
        val otherList = other.getSortedZs()
        for (i in 0..<min(thisList.size, otherList.size)) {
            if (thisList[i] - otherList[i] > EPSILON) {
                return 1
            } else if (thisList[i] - otherList[i] < -EPSILON) {
                return -1
            }
        }
        return 0
    }
}
class Position (var x: Double, var y:Double, var z: Double) {
    fun update(x: Double, y:Double, z: Double) {
        this.x=x; this.y=y; this.z=z
    }
}

fun degreesToRadians(degrees: Double) : Double {
    return PI * degrees / 180.0
}

class Camera (var pos: Position = Position(0.0,0.0,0.0),
              theta: Double = 0.0, alpha: Double = 0.0, fov: Double = 30.0) {

    var aspectRatio: Double = 1.0
    var nearPlaneDistance = 0.001

    var theta = theta
        set(degrees) {
            field = degrees % 360.0
        }

    var alpha = alpha
        set(degrees) {
            field = min(90.0, max(-90.0, degrees))
        }

    val thetaRad
        get() = degreesToRadians(theta)

    val alphaRad
        get() = degreesToRadians(alpha)

//    init {
//        this.alpha=alpha
//        this.theta=theta
//    }
    var fov = fov
        set(degrees) {field = max(1.0, min(179.0, degrees))}

    val leftFrustumLocalNormal
        get() = {
            val theta = degreesToRadians(fov/2)
            Vec3 (cos(theta), 0.0, sin(theta))
        }


    val bottomFrustumLocalNormal
        get() = {
            val theta = degreesToRadians(fov/2)
            Vec3 (0.0, cos(theta), sin(theta))
        }

    fun rotateUp(degrees: Double = 0.0) {
        this.alpha += degrees
    }

    fun rotateLeft(degrees: Double = 0.0) {
        this.theta += degrees
    }

    fun forward(units: Double) {
        pos.update(pos.x+units*-sin(thetaRad), pos.y, pos.z+units*cos(thetaRad))
    }

    fun right(units: Double) {
        pos.update(pos.x+units*cos(thetaRad), pos.y, pos.z+units*sin(thetaRad))
    }

    fun up(units: Double) {
        pos.update(pos.x, pos.y+units, pos.z)
    }


}

class Screen (val canvas: Canvas, val camera: Camera = Camera()){
    var zoom: Double = 0.0

    var vertices: List<Vertex> = listOf()
    var tris: List<Tri> = listOf()

    private val renderer = Renderer(camera)


    private val colorList = listOf(Color.DARKRED.brighter(), Color.RED, Color.GOLD.brighter(), Color.YELLOW, Color.DARKORANGE, Color.ORANGE, Color.DODGERBLUE, Color.DEEPSKYBLUE, Color.FORESTGREEN, Color.GREEN.brighter(), Color.PURPLE.brighter(), Color.ORCHID, Color.KHAKI, Color.GOLD, Color.MEDIUMPURPLE, Color.MOCCASIN, Color.CADETBLUE, Color.STEELBLUE)

    fun updateAspectRatio() {
        camera.aspectRatio = canvas.width / canvas.height
    }

    fun render2D () {
        val gc = canvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
        gc.font = Font(17.0)
        gc.stroke = Color.BLACK
        gc.lineWidth = 1.0

        val triSegs = renderer.applyPipe(vertices, tris)

        val properZoom = exp(zoom) * canvas.height / 2

        for (i in (0..triSegs.size-1).sortedByDescending {i ->
            triSegs[i]
        }) {

            val t = triSegs[i]
            val show = arrayOf(t.aShow, t.bShow, t.cShow)
            if (! (show[0] || show[1] || show[2])) continue

            val path = arrayOf(arrayOf(t.a1, t.a2), arrayOf(t.b1, t.b2), arrayOf(t.c1, t.c2))

            gc.beginPath()

            for (j in 0..<3) {
                if (!show[j]) continue
                gc.lineTo(path[j][0].x * properZoom + 0.5*canvas.width, -path[j][0].y * properZoom + 0.5*canvas.height)
                gc.lineTo(path[j][1].x * properZoom + 0.5*canvas.width, -path[j][1].y * properZoom + 0.5*canvas.height)
            }

            gc.closePath()

            gc.stroke = colorList[i%colorList.size]
            gc.fill = colorList[i%colorList.size]

            gc.stroke()
            gc.globalAlpha = 0.6
            gc.fill()
            gc.globalAlpha = 1.0

            if (DEBUG_VIEW_ENABLED) {
                gc.beginPath()
                for (j in 0..<3) {
                    if (!show[j] || !show[(j + 1) % 3]) continue
                    gc.moveTo(
                        path[j][1].x * properZoom + 0.5 * canvas.width,
                        -path[j][1].y * properZoom + 0.5 * canvas.height
                    )
                    gc.lineTo(
                        path[(j + 1) % 3][0].x * properZoom + 0.5 * canvas.width,
                        -path[(j + 1) % 3][0].y * properZoom + 0.5 * canvas.height
                    )
                }
                gc.closePath()

                gc.lineWidth = 3.0
                gc.stroke = Color.BLACK
                gc.stroke()
                gc.lineWidth = 1.0
            }

        }

        // Crosshair
        gc.stroke = Color.GRAY
        gc.lineWidth = 2.0
        gc.globalAlpha = 0.75
        gc.beginPath()
        gc.moveTo(canvas.width / 2 - 10, canvas.height / 2)
        gc.lineTo(canvas.width / 2 + 10, canvas.height / 2)
        gc.moveTo(canvas.width / 2, canvas.height / 2 - 10)
        gc.lineTo(canvas.width / 2, canvas.height / 2 + 10)
        gc.stroke()
        gc.closePath()

        // Debug Text
        gc.stroke = Color.BLACK
        gc.lineWidth = 1.0
        gc.fill = Color.BLACK.brighter()
        gc.globalAlpha = 1.0
        val text = "x:%.2f y:%.2f z:%.2f\n \u03B8:%0+7.2f \u03B1:%0+6.2f\n FOV:%05.1f  Zoom:%+.2f".format(
            camera.pos.x, camera.pos.y, camera.pos.z,
            camera.theta, camera.alpha,
            camera.fov, zoom)

        gc.beginPath()
        gc.fillText(text, 20.0, 20.0)
        gc.closePath()
    }
}

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

class Perspective(val camera: Camera): VertexShader() {

    override fun transform(v: Vertex): Vertex {
        // Camera pos in projection point
        val x = v.x / (v.z * tan(degreesToRadians(camera.fov/2)))
        val y = v.y / (v.z * tan(degreesToRadians(camera.fov/2)))

        // Camera pos in projection plane
//        val x = v.x / (v.z * tan(degreesToRadians(fov/2)) + 1)
//        val y = v.y / (v.z * tan(degreesToRadians(fov/2)) + 1)

        return Vertex(x, y, v.z)

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
        val c2 = if (t.cShow) vertexShader.transform(t.c2) else t.c2

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

    fun applyPipe(vertices: List<Vertex>, tris: List<Tri>) : List<TriSeg>{
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



        return triSegs
    }
}