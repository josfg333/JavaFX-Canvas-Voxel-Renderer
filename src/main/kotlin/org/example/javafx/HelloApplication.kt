package org.example.javafx

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage


const val MOUSE_SENSITIVITY = 0.75
const val MOUSE_WHEEL_SENSITIVITY = 0.75
const val SPEED = 0.2

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val root = Pane()

        val canvas = Canvas(600.0, 400.0)
        root.children.add(canvas)

        val screen = Screen(canvas)
        screen.vertices = listOf(
            Vertex(1.0, 1.0, -1.0), Vertex(-1.0, 1.0, -1.0), Vertex(-1.0, -1.0, -1.0), Vertex(1.0, -1.0, -1.0),
            Vertex(1.0, 1.0, 1.0), Vertex(-1.0, 1.0, 1.0), Vertex(-1.0, -1.0, 1.0), Vertex(1.0, -1.0, 1.0)
        )
        screen.tris = listOf(
            Tri(0, 1, 2), Tri(2, 3, 0),
            Tri(5, 4, 7), Tri(7, 6, 5),
            Tri(1, 5, 6), Tri(6, 2, 1),
            Tri(4, 0, 3), Tri(3, 7, 4),
            Tri(4, 5, 1), Tri(1, 0, 4),
            Tri(3, 2, 6), Tri(6, 7, 3)
        )

        screen.render2D()
        val scene = Scene(root, canvas.width, canvas.height+30.0, Color.WHITESMOKE)

        stage.widthProperty().addListener { observable, oldValue, newValue ->
            canvas.width = stage.width
            screen.render2D()
        }
        stage.heightProperty().addListener { observable, oldValue, newValue ->
            canvas.height = stage.height - 30.0
            screen.render2D()
        }

        canvas.setOnScroll {s->
            screen.zoom += MOUSE_WHEEL_SENSITIVITY * s.deltaY/100.0
            screen.render2D()
        }

        var oldMouseX = 0.0
        var oldMouseY = 0.0
        canvas.setOnMouseDragged { m ->
            screen.camera.rotateLeft((-m.screenX + oldMouseX) * MOUSE_SENSITIVITY)
            screen.camera.rotateUp((-m.screenY + oldMouseY) * MOUSE_SENSITIVITY)

            screen.render2D()

            oldMouseX = m.screenX
            oldMouseY = m.screenY
//            println(m)
        }
        canvas.setOnMouseMoved { m ->
            oldMouseX = m.screenX
            oldMouseY = m.screenY
        }

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
//            println(k)
        }


        stage.title = "Lines"
        stage.scene = scene

        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}