package org.example.javafx

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.transform.Affine
import kotlin.math.exp
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

const val DEBUG_VIEW_ENABLED: Boolean = false

class Screen (val canvas: Canvas, val camera: Camera = Camera()){
    var displayHud = true
    var canvasZoom = 0.0
    val properCanvasZoom: Double
        get() = exp(canvasZoom)

    val modelInstances: MutableList<ModelInstance> = mutableListOf()

    val voxels = Voxels()

    private val renderer = Renderer(camera, modelInstances)


    private val colorList = listOf(Color.DARKRED.brighter(), Color.RED, Color.GOLD.brighter(), Color.YELLOW, Color.DARKORANGE, Color.ORANGE, Color.DODGERBLUE, Color.DEEPSKYBLUE, Color.PURPLE, Color.PURPLE.brighter(), Color.FORESTGREEN, Color.GREEN.brighter(), Color.KHAKI, Color.GOLD, Color.MEDIUMPURPLE, Color.MOCCASIN, Color.CADETBLUE, Color.STEELBLUE)

    var lastDuration = 0.seconds
    var externalLastDuration = 0.seconds

    fun updateWorldModel () {renderer.aggregateModelInstances()}
    fun updateVoxels() {
        modelInstances.clear()
        modelInstances.addAll(voxels.getModelInstances())
        updateWorldModel()
    }
    fun render2D () { lastDuration = measureTime {
        val gc = canvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
        gc.font = Font(18.0)
        gc.save()

        val triSegs = renderer.applyPipe()


        // Main Draw
        val properZoom = camera.properZoom * properCanvasZoom * canvas.height / 2
        gc.transform = Affine(
            properZoom, 0.0, 0.5*canvas.width,
            0.0, -properZoom, 0.5*canvas.height
            )

        for (i in (0..triSegs.size - 1).sortedByDescending { i ->
            triSegs[i].depth
        }) {

            val t = triSegs[i]


            gc.beginPath()

            for (j in 0..<3) {
                val edge = t.edgeArray[j]
                if (edge == null) continue

                gc.lineTo(
                    edge.first.x,
                    edge.first.y,
                )
                gc.lineTo(
                    edge.second.x,
                    edge.second.y,
                )
            }
            gc.closePath()

            gc.stroke = colorList[t.texture]
            gc.lineWidth = 1.0 / (canvas.height/2)
            gc.globalAlpha = 0.5
            gc.stroke()

            gc.fill = colorList[t.texture]
            gc.globalAlpha = 0.2
            gc.fill()

            if (DEBUG_VIEW_ENABLED) {

                gc.beginPath()
                var firstIndex = -1
                for (j in 0..<3) {
                    val edge = t.edgeArray[j]
                    if (edge == null) continue
                    firstIndex = j
                    gc.moveTo(edge.second.x, edge.second.y)
                    break
                }

                for (j in 0..<3) {
                    val edge = t.edgeArray[(firstIndex+j+1)%3]
                    if (edge == null) continue
                    gc.lineTo(edge.first.x, edge.first.y)
                    gc.moveTo(edge.second.x, edge.second.y)
                }

                gc.lineWidth = 3.0/(camera.properZoom*canvas.height/2)
                gc.stroke = Color.WHITE
                gc.globalAlpha = 0.5
                gc.stroke()
            }

        }

        gc.restore()
        gc.save()

        if (displayHud) {

            // Outline
            gc.transform = Affine(
                0.5*canvas.width*properCanvasZoom, 0.0, canvas.width/2,
                0.0, -0.5*canvas.height*properCanvasZoom, canvas.height/2)

            gc.beginPath()
            gc.moveTo(-1.0, 1.0)
            gc.lineTo(1.0, 1.0)
            gc.lineTo(1.0, -1.0)
            gc.lineTo(-1.0, -1.0)
            gc.closePath()

            gc.stroke = Color.WHITE
            gc.lineWidth = 2.0 / (canvas.height / 2)
            gc.globalAlpha = 1.0
            gc.stroke()

            gc.restore()
            gc.save()


            // Crosshair

            gc.beginPath()
            gc.moveTo(canvas.width / 2 - 5, canvas.height / 2)
            gc.lineTo(canvas.width / 2 + 5, canvas.height / 2)
            gc.moveTo(canvas.width / 2, canvas.height / 2 - 5)
            gc.lineTo(canvas.width / 2, canvas.height / 2 + 5)
            gc.stroke = Color.GRAY
            gc.lineWidth = 2.0
            gc.globalAlpha = 0.75
            gc.stroke()

            gc.restore()
            gc.save()

            // Debug
            val text =
                "x:%.2f y:%.2f z:%.2f\n\u03B8:%0+7.2f \u03B1:%0+6.2f\nFOV:%05.1f  Zoom:%+.2f  Dolly:%+.2f\n\nLast draw: %7.2f ms / %7.2f ms".format(
                    camera.pos.x, camera.pos.y, camera.pos.z,
                    camera.theta, camera.alpha,
                    camera.fov, camera.zoom, camera.dolly,
                    lastDuration.inWholeMicroseconds / 1000.0,
                    externalLastDuration.inWholeMicroseconds / 1000.0
                )

            gc.globalAlpha = 1.0
            gc.fill = Color.GRAY
            gc.fillText(text, 20.0, 20.0)
            gc.lineWidth=0.5
            gc.stroke = Color.BLACK
            gc.strokeText(text, 20.0, 20.0)

            gc.restore()
            gc.save()
        }
        gc.restore()
    }
    }
}