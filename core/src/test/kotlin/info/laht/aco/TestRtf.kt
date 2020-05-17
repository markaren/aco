package info.laht.aco

import info.laht.aco.core.HeadlessEngineRunner
import info.laht.aco.core.Engine
import info.laht.aco.core.Entity
import info.laht.aco.render.TransformComponent
import info.laht.aco.render.geometry.BoxShape
import info.laht.aco.render.geometry.GeometryComponent
import info.laht.aco.render.geometry.SphereShape
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
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

    engine.addSystem(SineMoverSystem(0.1))

    val runner = HeadlessEngineRunner(engine)
    engine.realtimeFactor = 2.0

    val stop = 5.0
    measureTime {
        runner.runUntil(stop).get()
    }.also { t ->
        println("Simulation finished. Simulated ${engine.currentTime}s in ${t.inSeconds}s.. ")
    }

}
