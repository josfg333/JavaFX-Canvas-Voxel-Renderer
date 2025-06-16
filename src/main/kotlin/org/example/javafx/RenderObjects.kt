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
import kotlin.math.sqrt
import kotlin.math.tan

class Vertex (val x: Double, val y:Double, val z:Double)

class Tri (val a: Int, val b: Int, val c: Int)

class Position (var x: Double, var y:Double, var z: Double) {
    fun update(x: Double, y:Double, z: Double) {
        this.x=x; this.y=y; this.z=z
    }
}

fun degreesToRadians(degrees: Double) : Double {
    return PI * degrees / 180.0
}

class Camera (var pos: Position = Position(0.0,0.0,0.0),
              theta: Double = 0.0, alpha: Double = 0.0) {
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

    var fov: Double
        get() = renderer.perspectiveShader.fov
        set(degrees) {renderer.perspectiveShader.fov = degrees}

    val colorList = listOf(Color.DARKRED.brighter(), Color.RED, Color.GOLD.brighter(), Color.YELLOW, Color.DARKORANGE, Color.ORANGE, Color.DODGERBLUE, Color.DEEPSKYBLUE, Color.FORESTGREEN, Color.GREEN.brighter(), Color.PURPLE.brighter(), Color.MEDIUMPURPLE, Color.KHAKI, Color.GOLD, Color.MEDIUMPURPLE, Color.MOCCASIN, Color.CADETBLUE, Color.STEELBLUE)

    fun render2D () {
        val gc = canvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
        gc.font = Font(17.0)
        gc.stroke = Color.BLACK
        gc.lineWidth = 1.0

        val vertices = renderer.applyPipe(vertices)
        val paths = tris.map {tri-> arrayOf(vertices[tri.a], vertices[tri.b], vertices[tri.c])}

        val properZoom = exp(zoom) * canvas.height / 2
        for (i in (0..paths.size-1).sortedByDescending {i -> paths[i][0].z + paths[i][2].z}) {
            val path = paths[i]
            gc.beginPath()
//            gc.stroke = colorList[i%colorList.size]
            gc.fill = colorList[i%colorList.size]

            gc.moveTo(path[2].x * properZoom + 0.5*canvas.width, -path[2].y * properZoom + 0.5*canvas.height)
            for (j in 0..2) {
                gc.lineTo(path[j].x * properZoom + 0.5*canvas.width, -path[j].y * properZoom + 0.5*canvas.height)
            }
            gc.globalAlpha = 1.0
            gc.stroke()
            gc.globalAlpha = 0.75
            gc.fill()
            gc.closePath()
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
            fov, zoom)

        gc.beginPath()
        gc.fillText(text, 20.0, 20.0)
        gc.closePath()
    }
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

class Perspective(fov: Double): VertexShader() {
    var fov = fov
        set(degrees) {field = max(1.0, min(179.0, degrees))}
    override fun transform(v: Vertex): Vertex {
        // Camera pos in projection point
        val x = v.x / (v.z * tan(degreesToRadians(fov/2)))
        val y = v.y / (v.z * tan(degreesToRadians(fov/2)))

        // Camera pos in projection plane
//        val x = v.x / (v.z * tan(degreesToRadians(fov/2)) + 1)
//        val y = v.y / (v.z * tan(degreesToRadians(fov/2)) + 1)

        return Vertex(x, y, v.z)

    }
}

class Renderer (camera: Camera){
    val perspectiveShader =  Perspective(130.0)
    val shaderPipeline: List<VertexShader> = listOf(WorldToView(camera), perspectiveShader)

    fun applyPipe(vertices: List<Vertex>) : List<Vertex>{
        var vertices = vertices
        for (shader in shaderPipeline) {
            vertices = vertices.map { v -> shader.transform(v) }
        }
        return vertices
    }
}