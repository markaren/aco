package info.laht.aco.systems

import info.laht.aco.core.Engine
import info.laht.aco.core.Entity
import info.laht.aco.core.EntitySystem
import info.laht.aco.core.Family
import info.laht.aco.utils.ImmutableArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class CoroutineSystem @JvmOverloads constructor(
    val family: Family,
    priority: Int = 0
) : EntitySystem(priority) {

    /**
     * @return set of entities processed by the system
     */
    lateinit var entities: ImmutableArray<Entity>
        private set

    override fun addedToEngine(engine: Engine) {
        entities = engine.getEntitiesFor(family)
    }

    override fun step(deltaTime: Double) {
        runBlocking(Dispatchers.Default) {
            entities.forEach { entity ->
                launch {
                    processEntity(entity, deltaTime)
                }
            }
        }
    }

    /**
     * This method is called on every entity on every update call of the EntitySystem. Override this to implement your system's
     * specific processing.
     * @param entity The current Entity being processed
     * @param deltaTime The delta time between the last and current frame
     */
    protected abstract suspend fun processEntity(entity: Entity, deltaTime: Double)

}
