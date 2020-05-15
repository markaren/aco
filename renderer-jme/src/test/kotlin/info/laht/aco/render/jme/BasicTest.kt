package info.laht.aco.render.jme

import info.laht.aco.core.Engine
import info.laht.aco.core.Entity
import info.laht.aco.core.Family
import info.laht.aco.math.Color
import info.laht.aco.render.TransformComponent
import info.laht.aco.render.geometry.BoxShape
import info.laht.aco.render.geometry.GeometryComponent
import info.laht.aco.utils.Clock
import java.util.concurrent.atomic.AtomicBoolean


fun main() {

    Engine().also { engine ->

        val renderSystem = JmeRenderSystem()
        renderSystem.moveSpeed = 100f
        engine.addSystem(renderSystem)

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
        engine.addSystem(SineMoverSystem())

        engine.init()

        val stop = AtomicBoolean(false)

        val clock = Clock()
        Thread {
            while (!stop.get()) {
                engine.update(clock.getDelta())
                Thread.sleep(16)
            }
        }.start()

        Thread.sleep(5000)
        stop.set(true)

    }

}

