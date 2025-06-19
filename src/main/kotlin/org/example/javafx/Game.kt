package org.example.javafx

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.scene.input.KeyCode
import kotlin.time.Duration.Companion.nanoseconds


const val MOUSE_SENSITIVITY = 0.01 * 2.0
const val MOUSE_WHEEL_SENSITIVITY = 0.01 * 0.25

const val LOOK_SPEED = 30.0 // Degrees per second
const val SPEED = 5.0 // Units per second

class Game (val screen: Screen) {

    private var lastFrameT: Long = 0

    private var movementHeading = Vec3()
    private val trigger: Map<KeyCode, (Double) -> Unit> = mapOf(
        Pair(KeyCode.W, {dt -> movementHeading += Vec3(z=1.0)}),
        Pair(KeyCode.S, {dt -> movementHeading += Vec3(z=-1.0)}),
        Pair(KeyCode.D, {dt -> movementHeading += Vec3(x=1.0)}),
        Pair(KeyCode.A, {dt -> movementHeading += Vec3(x=-1.0)}),
        Pair(KeyCode.SPACE, {dt -> movementHeading += Vec3(y=1.0)}),
        Pair(KeyCode.SHIFT, {dt -> movementHeading += Vec3(y=-1.0)}),
        Pair(KeyCode.LEFT, {dt -> screen.camera.rotateLeft(LOOK_SPEED*dt)}),
        Pair(KeyCode.RIGHT, {dt -> screen.camera.rotateLeft(-LOOK_SPEED*dt)}),
        Pair(KeyCode.UP, {dt -> screen.camera.rotateUp(LOOK_SPEED*dt)}),
        Pair(KeyCode.DOWN, {dt -> screen.camera.rotateUp(-LOOK_SPEED*dt)}),
    )

    private val keysDown: MutableMap<KeyCode, Boolean> = mutableMapOf(
        Pair(KeyCode.W, false),
        Pair(KeyCode.S, false),
        Pair(KeyCode.D, false),
        Pair(KeyCode.A, false),
        Pair(KeyCode.SPACE, false),
        Pair(KeyCode.SHIFT, false),
        Pair(KeyCode.LEFT, false),
        Pair(KeyCode.RIGHT, false),
        Pair(KeyCode.UP, false),
        Pair(KeyCode.DOWN, false),
    )
    var lookDeltaX = 0.0
    var lookDeltaY = 0.0
    var fovDelta = 0.0
    var dollyDelta = 0.0
    var zoomDelta = 0.0
    var canvasZoomDelta = 0.0

    private val keyUpEvents: MutableSet<KeyCode> = mutableSetOf()

    fun handleKeyUp(code: KeyCode) {
        if (code in keysDown.keys)
            keyUpEvents.add(code)
    }

    fun handleKeyDown(code: KeyCode) {
        if (code in keysDown.keys)
            keysDown[code] = true
        else when (code) {
            KeyCode.F1 -> screen.displayHud = !screen.displayHud
            else -> null
        }
    }

    val animation = object: AnimationTimer() {
        override fun handle(t: Long) {
            val dt = (t-lastFrameT).toDouble() / 1e9
            for (keyDown in keysDown) {
                if (keyDown.value) {
                    trigger[keyDown.key]?.invoke(dt)
                }
            }
            var xzVel = Vec3(movementHeading.x, 0.0, movementHeading.z)
            val xzVelLength = xzVel.length
            if (xzVelLength > 0)
                xzVel /= xzVelLength
            val vel =(xzVel + Vec3(y=movementHeading.y)) * SPEED * dt
            screen.camera.forward(vel.z)
            screen.camera.right(vel.x)
            screen.camera.up(vel.y)
            movementHeading = Vec3()

            for (keyUp in keyUpEvents) {
                keysDown[keyUp] = false
            }
            keyUpEvents.clear()
            screen.camera.rotateLeft(-lookDeltaX*LOOK_SPEED*MOUSE_SENSITIVITY)
            lookDeltaX = 0.0
            screen.camera.rotateUp(lookDeltaY*LOOK_SPEED*MOUSE_SENSITIVITY)
            lookDeltaY = 0.0
            screen.camera.fov += fovDelta * MOUSE_WHEEL_SENSITIVITY * 10.0
            fovDelta = 0.0
            screen.camera.dolly += dollyDelta * MOUSE_WHEEL_SENSITIVITY * 0.5
            dollyDelta = 0.0
            screen.camera.zoom+=zoomDelta * MOUSE_WHEEL_SENSITIVITY
            zoomDelta = 0.0
            screen.canvasZoom+=canvasZoomDelta * MOUSE_WHEEL_SENSITIVITY
            canvasZoomDelta = 0.0
            screen.externalLastDuration = (t - lastFrameT).nanoseconds
            lastFrameT = t
            screen.render2D()
        }
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}