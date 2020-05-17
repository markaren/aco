package info.laht.aco.render.jme

import info.laht.aco.core.Engine
import info.laht.aco.core.Entity
import info.laht.aco.math.Color
import info.laht.aco.render.TransformComponent
import info.laht.aco.render.geometry.BoxShape
import info.laht.aco.render.geometry.GeometryComponent
import info.laht.aco.utils.Clock
import java.util.concurrent.atomic.AtomicBoolean


fun main() {

    val engine = Engine()

    Entity().apply {
        add(TransformComponent().apply {
            setLocalTranslation(0.0, 0.0, 0.0)
        })
        add(GeometryComponent(BoxShape()).apply {
            color.set(Color.blue)
        })
        add(SineMoverComponent(A=4.0))
        engine.addEntity(this)
    }
    Entity().apply {
        add(TransformComponent().apply {
            setLocalTranslation(5.0, 0.0, 0.0)
        })
        add(GeometryComponent(BoxShape()).apply {
            color.set(Color.yellow)
        })
        add(SineMoverComponent(A=3.0))
        engine.addEntity(this)
    }
    engine.addSystem(SineMoverSystem(0.01))

    val runner = JmeEngineRunner(engine)
    runner.start()

}

