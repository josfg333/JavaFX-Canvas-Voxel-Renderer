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
import kotlin.math.floor
import kotlin.math.sqrt


class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val root = Pane()

        val canvas = Canvas(1366.0, 768.0)
        root.children.add(canvas)

        val camera = Camera(Position(0.5, 0.5, 0.5), 0.0, )//35.264389682754654)
        camera.fov = 90.0
        camera.dolly = 0.0
        val screen = Screen(canvas, camera)
        val game = Game(screen)
        screen.zoom = 0.0

        // Render Objects

//        screen.modelInstances.addAll(listOf(
//            ModelInstance(Models.CUBE.m, Vec3(0.0, 0.0, 0.0))
//        ))
//

        val octahedronSize = 10
        val octahedronGap = 1
        for (subSize in octahedronSize downTo 0 step octahedronGap) {
            for (i in -subSize..subSize) {
                val r1 = floor(sqrt(subSize*subSize - i.toDouble()*i)+0.5).toInt()
                for (j in -r1..r1) {
                    val y = floor(sqrt(subSize*subSize - i.toDouble()*i - j.toDouble()*j)+0.5).toInt()
                    for (k in 0..y step 1) {
                        val lim1 = sqrt(subSize*subSize - (abs(i)).toDouble()*(abs(i)) - (abs(j)+1).toDouble()*(abs(j)+1))+0.5
                        val lim2 = sqrt(subSize*subSize - (abs(i)+1).toDouble()*(abs(i)+1) - (abs(j)).toDouble()*(abs(j)))+0.5
                        if (
                            k < floor(lim1).toInt()
                            &&
                            k < floor(lim2).toInt()
                        ) continue
                        for (l in -1..1 step 2) {
                            screen.voxels.voxelSet.add(Triple(i, k*l, j))
                            if (k==0) break
                        }
                    }
                }
            }
        }
        screen.updateVoxels()

        val scene = Scene(root, canvas.width, canvas.height, Color.WHITESMOKE)



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
            if (s.isAltDown && s.isControlDown) {
                game.dollyDelta += amount
            } else if (s.isControlDown) {
                game.fovDelta += amount
            } else {
                game.zoomDelta += amount
            }
        }

        // Camera Rotation
        var oldMouseX = 0.0
        var oldMouseY = 0.0
        scene.onMouseMoved = EventHandler { m: MouseEvent ->
            if (m.isAltDown) {
                game.lookDeltaX += (m.screenX - oldMouseX) * MOUSE_SENSITIVITY * LOOK_SPEED
                game.lookDeltaY += (-m.screenY + oldMouseY) * MOUSE_SENSITIVITY * LOOK_SPEED
//                screen.camera.rotateLeft(-(m.screenX - oldMouseX) * MOUSE_SENSITIVITY * LOOK_SPEED)
//                screen.camera.rotateUp((-m.screenY + oldMouseY) * MOUSE_SENSITIVITY * LOOK_SPEED)
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