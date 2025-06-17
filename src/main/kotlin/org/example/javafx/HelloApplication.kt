package org.example.javafx

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.E
import kotlin.math.log


const val MOUSE_SENSITIVITY = 0.2
const val MOUSE_WHEEL_SENSITIVITY = 0.75
const val SPEED = 0.2

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val root = Pane()

        val canvas = Canvas(1000.0, 600.0)
        root.children.add(canvas)

        val camera = Camera(Position(-1.2, 1.2, -1.2), -45.0, -35.26438968)
        camera.fov = 30.0
        val screen = Screen(canvas, camera)
        screen.zoom = log(0.2, E)

        // Render Objects

//        screen.modelInstances.addAll(listOf(
//            ModelInstance(Models.CUBE.m, Vec3(0.0, 0.0, 0.0))
//        ))

        for (i in 0..<10){
            for (j in 0..<10) {
                for (k in 0..(i+j)) {
                    if (i < 9 && i > 0 && j < 9 && j > 0 && k > 0 && k < (i + j)) continue
                    screen.modelInstances.add(
                        ModelInstance(
                            Models.CUBE.m, Vec3(
                                i.toDouble(), k.toDouble(), j.toDouble()
                            )
                        )
                    )
                }
            }
        }

        screen.render2D()

        val scene = Scene(root, canvas.width, canvas.height, Color.BLACK)

        // #############
        // # Listeners #
        // #############

        // Window Resize
        stage.widthProperty().addListener { observable, oldValue, newValue ->
            canvas.width = stage.width
            screen.render2D()
        }
        stage.heightProperty().addListener { observable, oldValue, newValue ->
            canvas.height = stage.height
            screen.render2D()
        }

        // Zoom/FOV
        canvas.setOnScroll {s->
            if (!s.isShiftDown) {
                screen.camera.fov += -MOUSE_WHEEL_SENSITIVITY * s.deltaY/20.0
            } else {
                screen.zoom += MOUSE_WHEEL_SENSITIVITY * s.deltaX / 100.0
            }
            screen.render2D()
        }

        // Camera Rotation
        var oldMouseX = 0.0
        var oldMouseY = 0.0
        canvas.setOnMouseMoved { m: MouseEvent ->
            if (m.isShiftDown) {
                screen.camera.rotateLeft((-m.screenX + oldMouseX) * MOUSE_SENSITIVITY)
                screen.camera.rotateUp((-m.screenY + oldMouseY) * MOUSE_SENSITIVITY)
                screen.render2D()
            }
            oldMouseX = m.screenX
            oldMouseY = m.screenY
        }
        canvas.setOnMouseDragged { m: MouseEvent ->
            screen.camera.rotateLeft((-m.screenX + oldMouseX) * MOUSE_SENSITIVITY)
            screen.camera.rotateUp((-m.screenY + oldMouseY) * MOUSE_SENSITIVITY)
            screen.render2D()

            oldMouseX = m.screenX
            oldMouseY = m.screenY
        }

        // Keypresses
        scene.setOnKeyPressed {k ->
            when (k.text) {
                "w" -> screen.camera.forward(SPEED)
                "s" -> screen.camera.forward(-SPEED)
                "a" -> screen.camera.right(-SPEED)
                "d" -> screen.camera.right(SPEED)
                " " -> screen.camera.up(SPEED)
                "z" -> screen.camera.up(-SPEED)
            }
            screen.render2D()
        }


        stage.title = "Lines"
        stage.scene = scene

        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}