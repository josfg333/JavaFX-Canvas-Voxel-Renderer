package org.example.javafx

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.abs


const val MOUSE_SENSITIVITY = 0.1
const val MOUSE_WHEEL_SENSITIVITY = 0.75
const val LOOK_SPEED = 0.25
const val SPEED = 0.125

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val root = Pane()

        val canvas = Canvas(600.0, 600.0)
        root.children.add(canvas)

        val camera = Camera(Position(0.5, 0.5, 0.5), 90.0, 35.264389682754654)
        camera.fov = 0.0
        camera.dolly = -1.0
        val screen = Screen(canvas, camera)
        screen.zoom = -2.5

        // Render Objects

//        screen.modelInstances.addAll(listOf(
//            ModelInstance(Models.CUBE.m, Vec3(0.0, 0.0, 0.0))
//        ))
        val octahedronSize = 9
        val octahedronGap = 3
        for (subSize in octahedronSize downTo 0 step octahedronGap) {
            for (i in -subSize..subSize) {
                for (j in -(subSize - abs(i))..subSize - abs(i)) {
                    val y = subSize - (abs(i) + abs(j))
                    for (k in -1..1 step 2) {
                        screen.modelInstances.add(
                            ModelInstance(
                                Models.CUBE.m, Vec3(
                                    i.toDouble(), y*k.toDouble(), j.toDouble()
                                )
                            )
                        )
                        if (y == 0) break
                    }
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
            val amount = s.deltaX + s.deltaY
            if (s.isShiftDown) {
                screen.camera.fov += MOUSE_WHEEL_SENSITIVITY * -amount / 20.0
            } else if (s.isControlDown) {
                screen.camera.dolly += MOUSE_WHEEL_SENSITIVITY * amount / 400.0
            } else {
                screen.zoom += MOUSE_WHEEL_SENSITIVITY * amount / 100.0
            }
            screen.render2D()
        }

        // Camera Rotation
        var oldMouseX = 0.0
        var oldMouseY = 0.0
        canvas.setOnMouseMoved { m: MouseEvent ->
            if (m.isShiftDown) {
                screen.camera.rotateLeft((-m.screenX + oldMouseX) * MOUSE_SENSITIVITY * LOOK_SPEED)
                screen.camera.rotateUp((-m.screenY + oldMouseY) * MOUSE_SENSITIVITY * LOOK_SPEED)
                screen.render2D()
            }
            oldMouseX = m.screenX
            oldMouseY = m.screenY
        }
        canvas.setOnMouseDragged { m: MouseEvent ->
            screen.camera.rotateLeft((-m.screenX + oldMouseX) * MOUSE_SENSITIVITY * LOOK_SPEED)
            screen.camera.rotateUp((-m.screenY + oldMouseY) * MOUSE_SENSITIVITY * LOOK_SPEED)
            screen.render2D()

            oldMouseX = m.screenX
            oldMouseY = m.screenY
        }

        // Keypresses
        scene.setOnKeyPressed {k ->
            when (k.code) {
                KeyCode.W -> screen.camera.forward(SPEED)
                KeyCode.S -> screen.camera.forward(-SPEED)
                KeyCode.A -> screen.camera.right(-SPEED)
                KeyCode.D -> screen.camera.right(SPEED)
                KeyCode.SPACE -> screen.camera.up(SPEED)
                KeyCode.Z -> screen.camera.up(-SPEED)
                KeyCode.LEFT -> screen.camera.rotateLeft(LOOK_SPEED)
                KeyCode.RIGHT -> screen.camera.rotateLeft(-LOOK_SPEED)
                KeyCode.UP -> screen.camera.rotateUp(LOOK_SPEED)
                KeyCode.DOWN -> screen.camera.rotateUp(-LOOK_SPEED)
                KeyCode.F1 -> screen.displayHud = !screen.displayHud
                else -> Unit
            }
            screen.render2D()
        }


        stage.title = "Lines"
        stage.scene = scene

        class SceneAnimation(): AnimationTimer() {
            override fun handle(time: Long) {
                screen.render2D()
                screen.camera.rotateLeft(0.8)
            }
        }
        SceneAnimation().start()

        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}