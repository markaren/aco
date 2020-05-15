package info.laht.aco.render.geometry

import org.joml.Vector3f
import org.joml.Vector3fc

class BoxShape(
    private val extents: Vector3f
) : Shape {

    @JvmOverloads
    constructor(extents: Float = 1f) : this(Vector3f(extents, extents, extents)) {
    }

    fun getExtents(): Vector3fc {
        return extents
    }

    val width: Float
        get() = extents.x

    val height: Float
        get() = extents.y

    val depth: Float
        get() = extents.z

}
