package org.example.javafx

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.abs


class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val root = Pane()

        val canvas = Canvas(1000.0, 600.0)
        root.children.add(canvas)

        val camera = Camera(Position(0.5, 0.5, 0.5), 90.0, )//35.264389682754654)
        camera.fov = 90.0
        camera.dolly = 0.0
        val screen = Screen(canvas, camera)
        val game = Game(screen)
        screen.zoom = 0.0

        // Render Objects

//        screen.modelInstances.addAll(listOf(
//            ModelInstance(Models.CUBE.m, Vec3(0.0, 0.0, 0.0))
//        ))
        val octahedronSize = 6
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

        val scene = Scene(root, canvas.width, canvas.height, Color.BLACK)



        // #############
        // # Listeners #
        // #############

        // Window Resize
        stage.widthProperty().addListener { observable, oldValue, newValue ->
            canvas.width = stage.width
        }
        stage.heightProperty().addListener { observable, oldValue, newValue ->
            canvas.height = stage.height
        }

        // Zoom/FOV
        scene.onScroll = EventHandler { s->
            val amount = s.deltaX + s.deltaY
            if (s.isAltDown) {
                game.fovDelta += amount
            } else if (s.isControlDown) {
                game.dollyDelta += amount
            } else {
                game.zoomDelta += amount
            }
        }

        // Camera Rotation
        var oldMouseX = 0.0
        var oldMouseY = 0.0
        scene.onMouseMoved = EventHandler { m: MouseEvent ->
            if (m.isAltDown) {
//                game.lookDeltaX += (m.screenX - oldMouseX) * MOUSE_SENSITIVITY * LOOK_SPEED
//                game.lookDeltaY += (-m.screenY + oldMouseY) * MOUSE_SENSITIVITY * LOOK_SPEED
                screen.camera.rotateLeft(-(m.screenX - oldMouseX) * MOUSE_SENSITIVITY * LOOK_SPEED)
                screen.camera.rotateUp((-m.screenY + oldMouseY) * MOUSE_SENSITIVITY * LOOK_SPEED)
            }
            oldMouseX = m.screenX
            oldMouseY = m.screenY
        }
        scene.onMouseDragged = EventHandler { m: MouseEvent ->
            game.lookDeltaX += (m.screenX - oldMouseX) * MOUSE_SENSITIVITY * LOOK_SPEED
            game.lookDeltaY += (-m.screenY + oldMouseY) * MOUSE_SENSITIVITY * LOOK_SPEED

            oldMouseX = m.screenX
            oldMouseY = m.screenY
        }

        // Keypresses
        scene.onKeyPressed = EventHandler { k ->
            game.handleKeyDown(k.code)
        }

        scene.onKeyReleased = EventHandler {k ->
            game.handleKeyUp(k.code)
        }


        stage.title = "CUBES"
        stage.scene = scene
        game.animation.start()

        stage.isMaximized = true
        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}