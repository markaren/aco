package info.laht.aco.render.jme

import com.badlogic.gdx.math.Vector3
import info.laht.aco.core.Component
import info.laht.aco.core.ComponentMapper
import info.laht.aco.core.Entity
import info.laht.aco.core.Family
import info.laht.aco.render.TransformComponent
import info.laht.aco.systems.IteratingSystem
import org.joml.Vector3d
import kotlin.math.PI
import kotlin.math.sin

data class SineMoverComponent(
    val A: Double = 1.0,
    val f: Double = 0.1,
    val phi: Double = 0.0
) : Component

class SineMoverSystem : IteratingSystem(
    Family.all(SineMoverComponent::class.java, TransformComponent::class.java).get()
) {

    private val sm = ComponentMapper.getFor(SineMoverComponent::class.java)
    private val tm = ComponentMapper.getFor(TransformComponent::class.java)

    private val tmp = Vector3d()

    override fun processEntity(entity: Entity, currentTime: Double, deltaTime: Double) {

        val s = sm.get(entity)
        val t = tm.get(entity)

        val pos = t.getLocalTranslation(tmp)
        t.setLocalTranslation(pos.x, s.A * sin(2 * PI * s.f * currentTime + s.phi), pos.z)

    }
}
