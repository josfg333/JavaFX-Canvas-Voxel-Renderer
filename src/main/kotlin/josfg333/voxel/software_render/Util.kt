package josfg333.voxel.software_render

import kotlin.math.PI

const val EPSILON: Double = 0.01

fun degreesToRadians(degrees: Double) : Double {
    return PI * degrees / 180.0
}

fun radiansToDegrees(radians: Double): Double {
    return 180.0 * radians / PI
}

