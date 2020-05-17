package info.laht.aco.render.jme

import info.laht.aco.core.Engine
import info.laht.aco.core.Entity
import info.laht.aco.render.TransformComponent
import info.laht.aco.render.geometry.BoxShape
import info.laht.aco.render.geometry.GeometryComponent
import info.laht.aco.render.geometry.SphereShape

fun main() {

    val engine = Engine()
    val box = Entity() .apply {
        add(GeometryComponent(BoxShape()))
        add(TransformComponent().apply {
            setLocalTranslation(0.0, 0.0, 0.0)
        })
        add(SineMoverComponent())
    }
    engine.addEntity(box)
    val sphere = Entity() .apply {
        add(GeometryComponent(SphereShape()))
        add(TransformComponent().apply {
            setLocalTranslation(2.0, 0.0, 0.0)
        })
        add(SineMoverComponent())
    }
    engine.addEntity(sphere)

    engine.addSystem(SineMoverSystem(0.01))

    val runner = JmeEngineRunner(engine)
    runner.start()

}
