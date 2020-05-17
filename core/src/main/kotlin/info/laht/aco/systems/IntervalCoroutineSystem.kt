package info.laht.aco.systems

import info.laht.aco.core.Engine
import info.laht.aco.core.Entity
import info.laht.aco.core.Family
import info.laht.aco.utils.ImmutableArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


abstract class IntervalCoroutineSystem(
    val family: Family,
    interval: Double
): IntervalSystem(interval) {

    private lateinit var entities: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        entities = engine.getEntitiesFor(family)
    }

    override fun updateInterval(currentTime: Double) {
        runBlocking(Dispatchers.Default) {
            entities.forEach { entity ->
                launch {
                    processEntity(entity, currentTime, interval)
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
    protected abstract suspend fun processEntity(entity: Entity, currentTime:Double, deltaTime: Double)

}

