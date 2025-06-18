package org.example.javafx

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Camera (var pos: Position = Position(0.0,0.0,0.0),
              theta: Double = 0.0, alpha: Double = 0.0, fov: Double = 30.0, var dolly: Double = 0.0) {

    var aspectRatio: Double = 1.0
    var nearPlaneDistance = 0.00001

    var theta = theta
        set(degrees) {
            field = ((degrees % 360.0)+360.0)%360
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
        set(degrees) {field = max(0.001, min(179.999, degrees)) }

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
        pos.update(pos.x+units*-sin(thetaRad), pos.y, pos.z+units* cos(thetaRad))
    }

    fun right(units: Double) {
        pos.update(pos.x+units* cos(thetaRad), pos.y, pos.z+units* sin(thetaRad))
    }

    fun up(units: Double) {
        pos.update(pos.x, pos.y+units, pos.z)
    }
}

class Position (override var x: Double, override var y:Double, override var z: Double): Vec3(x, y, z) {
    fun update(x: Double, y:Double, z: Double) {
        this.x=x; this.y=y; this.z=z
    }
}