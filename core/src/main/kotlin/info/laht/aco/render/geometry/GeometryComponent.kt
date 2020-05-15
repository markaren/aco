package info.laht.aco.render.geometry

import info.laht.aco.core.Component
import info.laht.aco.math.Color

class GeometryComponent(val shape: Shape) : Component {
    val color = Color(Color.white)
    var visible = true
    var wireframe = false
}
