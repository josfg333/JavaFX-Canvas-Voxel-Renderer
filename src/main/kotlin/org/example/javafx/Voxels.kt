package org.example.javafx

class Voxels {
    val voxelSet: MutableSet<Triple<Int, Int, Int>> = mutableSetOf()

    fun getModelInstances(): List<ModelInstance> {
        return voxelSet.map {t->
            val (x, y, z) = t
            val mask = MutableList(12){true}
            if (Triple(x, y, z-1) in voxelSet) {
                mask[0] = false; mask[1] = false
            }
            if (Triple(x, y, z+1) in voxelSet) {
                mask[2] = false; mask[3] = false
            }
            if (Triple(x-1, y, z) in voxelSet) {
                mask[4] = false; mask[5] = false
            }
            if (Triple(x+1, y, z) in voxelSet) {
                mask[6] = false; mask[7] = false
            }
            if (Triple(x, y-1, z) in voxelSet) {
                mask[8] = false; mask[9] = false
            }
            if (Triple(x, y+1, z) in voxelSet) {
                mask[10] = false; mask[11] = false
            }
            ModelInstance(Models.CUBE.m, Vec3(x.toDouble(), y.toDouble(), z.toDouble()), mask)
        }
    }
}