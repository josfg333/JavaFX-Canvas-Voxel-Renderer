package org.example.javafx

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.text.Font
import kotlin.math.exp
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

const val DEBUG_VIEW_ENABLED: Boolean = false

class Screen (val canvas: Canvas, val camera: Camera = Camera()){
    var zoom: Double = 0.0

    val modelInstances: MutableList<ModelInstance> = mutableListOf()

    private val renderer = Renderer(camera)


    private val colorList = listOf(Color.DARKRED.brighter(), Color.RED, Color.GOLD.brighter(), Color.YELLOW, Color.DARKORANGE, Color.ORANGE, Color.DODGERBLUE, Color.DEEPSKYBLUE, Color.FORESTGREEN, Color.GREEN.brighter(), Color.PURPLE, Color.PURPLE.brighter(), Color.KHAKI, Color.GOLD, Color.MEDIUMPURPLE, Color.MOCCASIN, Color.CADETBLUE, Color.STEELBLUE)

    var lastDuration = 0.seconds

    fun render2D () { lastDuration = measureTime {
        val gc = canvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
        gc.font = Font(17.0)
        gc.stroke = Color.BLACK
        gc.lineWidth = 1.0

        val triSegs = renderer.applyPipe(modelInstances)

        val properZoom = exp(zoom) * canvas.height / 2

        for (i in (0..triSegs.size - 1).sortedByDescending { i ->
            triSegs[i].depth
        }) {

            val t = triSegs[i]
            if (t.a==null && t.b==null && t.c==null) continue

            gc.beginPath()

            for (j in 0..<3) {
                val edge = t.edgeArray[j]
                if (edge == null) continue
                gc.lineTo(
                    edge.first.x * properZoom + 0.5 * canvas.width,
                    -edge.first.y * properZoom + 0.5 * canvas.height
                )
                gc.lineTo(
                    edge.second.x * properZoom + 0.5 * canvas.width,
                    -edge.second.y * properZoom + 0.5 * canvas.height
                )
            }

            gc.closePath()

            gc.stroke = colorList[t.texture]
            gc.fill = colorList[t.texture]

            gc.stroke()
            gc.globalAlpha = 0.2
            gc.fill()
            gc.globalAlpha = 1.0

            if (DEBUG_VIEW_ENABLED) {
                gc.beginPath()
                for (j in 0..<3) {
                    val edge = t.edgeArray[j]
                    if (edge == null) continue
                    val nextEdge = t.edgeArray[(j+1)%3]
                    if (nextEdge == null) continue
                    gc.moveTo(
                        edge.second.x * properZoom + 0.5 * canvas.width,
                        -edge.second.y * properZoom + 0.5 * canvas.height
                    )
                    gc.lineTo(
                        nextEdge.first.x * properZoom + 0.5 * canvas.width,
                        -nextEdge.first.y * properZoom + 0.5 * canvas.height
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
        gc.lineWidth = 0.5
        gc.fill = Color.GRAY
        gc.globalAlpha = 1.0
        val text =
            "x:%.2f y:%.2f z:%.2f\n\u03B8:%0+7.2f \u03B1:%0+6.2f\nFOV:%05.1f  Zoom:%+.2f\n\nLast draw: %7.2f ms".format(
                camera.pos.x, camera.pos.y, camera.pos.z,
                camera.theta, camera.alpha,
                camera.fov, zoom,
                lastDuration.inWholeMicroseconds / 1000.0
            )

        gc.beginPath()
        gc.fillText(text, 20.0, 20.0)
        gc.closePath()
    }
    }
}