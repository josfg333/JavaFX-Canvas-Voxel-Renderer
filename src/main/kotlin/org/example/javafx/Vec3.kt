package org.example.javafx

open class Vec3 (open val x: Double = 0.0, open val y: Double=0.0, open val z: Double=0.0) {

    fun toVertex() = Vertex(x, y, z)
    fun toPosition() = Position(x,y,z)

    operator fun plus(other: Vec3): Vec3 {
        return Vec3(x+other.x, y+other.y, z+other.z)
    }

    operator fun minus(other: Vec3): Vec3 {
        return this + other * -1.0
    }

    operator fun times(other: Vec3): Vec3 {
        return Vec3(y*other.z - z*other.y, z*other.x-x*other.z, x*other.y-y*other.x)
    }

    operator fun times(scalar: Double): Vec3 {
        return Vec3(scalar*x, scalar*y, scalar*z)
    }

    fun dot(other: Vec3): Double {
        return x*other.x + y*other.y + z*other.z
    }

}

operator fun Double.times(other: Vec3): Vec3 {
    return other * this
}
