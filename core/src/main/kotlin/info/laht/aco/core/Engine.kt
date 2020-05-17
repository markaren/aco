/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.laht.aco.core

import com.badlogic.gdx.utils.reflect.ClassReflection
import com.badlogic.gdx.utils.reflect.ReflectionException
import info.laht.aco.core.ComponentOperationHandler.BooleanInformer
import info.laht.aco.signals.Listener
import info.laht.aco.signals.Signal
import info.laht.aco.utils.ImmutableArray
import java.io.Closeable
import java.io.IOException

/**
 * The heart of the Entity framework. It is responsible for keeping track of [Entity] and
 * managing [EntitySystem] objects. The Engine should be updated every tick via the [.step] method.
 *
 *
 * With the Engine you can:
 *
 *
 *  * Add/Remove [Entity] objects
 *  * Add/Remove [EntitySystem]s
 *  * Obtain a list of entities for a specific [Family]
 *  * Update the main loop
 *  * Register/unregister [EntityListener] objects
 *
 *
 * @author Stefan Bachmann
 */
open class Engine @JvmOverloads constructor(
    val startTime: Double = 0.0
) : Closeable {

    private val componentAdded: Listener<Entity> = ComponentListener()
    private val componentRemoved: Listener<Entity> = ComponentListener()
    private val systemManager = SystemManager(EngineSystemListener())
    private val entityManager = EntityManager(EngineEntityListener())
    private val componentOperationHandler = ComponentOperationHandler(EngineDelayedInformer())
    private val familyManager = FamilyManager(entityManager.entities)

    private var updating = false
    var isInitialized = false
        private set
    var stepNumber: Long = 0
        private set
    var currentTime: Double = startTime
        private set
    var realtimeFactor = 1.0

    /**
     * Creates a new Entity object.
     *
     * @return @[Entity]
     */
    open fun createEntity(): Entity {
        return Entity()
    }

    /**
     * Creates a new [Component]. To use that method your components must have a visible no-arg constructor
     */
    open fun <T : Component> createComponent(componentType: Class<T>): T? {
        return try {
            ClassReflection.newInstance(componentType)
        } catch (e: ReflectionException) {
            null
        }
    }

    /**
     * Adds an entity to this Engine.
     * This will throw an IllegalArgumentException if the given entity
     * was already registered with an engine.
     */
    fun addEntity(entity: Entity) {
        val delayed = updating || familyManager.notifying()
        entityManager.addEntity(entity, delayed)
    }

    /**
     * Removes an entity from this Engine.
     */
    fun removeEntity(entity: Entity) {
        val delayed = updating || familyManager.notifying()
        entityManager.removeEntity(entity, delayed)
    }

    /**
     * Removes all entities of the given [Family].
     */
    fun removeAllEntities(family: Family) {
        val delayed = updating || familyManager.notifying()
        entityManager.removeAllEntities(getEntitiesFor(family), delayed)
    }

    /**
     * Removes all entities registered with this Engine.
     */
    fun removeAllEntities() {
        val delayed = updating || familyManager.notifying()
        entityManager.removeAllEntities(delayed)
    }

    /**
     * Returns an [ImmutableArray] of [Entity] that is managed by the the Engine
     * but cannot be used to modify the state of the Engine. This Array is not Immutable in
     * the sense that its contents will not be modified, but in the sense that it only reflects
     * the state of the engine.
     *
     *
     * The Array is Immutable in the sense that you cannot modify its contents through the API of
     * the [ImmutableArray] class, but is instead "Managed" by the Engine itself. The engine
     * may add or remove items from the array and this will be reflected in the returned array.
     *
     *
     * This is an important note if you are looping through the returned entities and calling operations
     * that may add/remove entities from the engine, as the underlying iterator of the returned array
     * will reflect these modifications.
     *
     *
     * The returned array will have entities removed from it if they are removed from the engine,
     * but there is no way to introduce new Entities through the array's interface, or remove
     * entities from the engine through the array interface.
     *
     *
     * Discussion of this can be found at https://github.com/libgdx/ashley/issues/224
     *
     * @return An unmodifiable array of entities that will match the state of the entities in the
     * engine.
     */
    val entities: ImmutableArray<Entity>
        get() = entityManager.entities

    /**
     * Adds the [EntitySystem] to this Engine.
     * If the Engine already had a system of the same class,
     * the new one will replace the old one.
     */
    fun addSystem(system: EntitySystem) {
        systemManager.addSystem(system)
    }

    /**
     * Removes the [EntitySystem] from this Engine.
     */
    fun removeSystem(system: EntitySystem) {
        systemManager.removeSystem(system)
    }

    /**
     * Removes all systems from this Engine.
     */
    fun removeAllSystems() {
        systemManager.removeAllSystems()
    }

    /**
     * Quick [EntitySystem] retrieval.
     */
    fun <T : EntitySystem?> getSystem(systemType: Class<T>): T {
        return systemManager.getSystem(systemType)
    }

    /**
     * @return immutable array of all entity systems managed by the [Engine].
     */
    val systems: ImmutableArray<EntitySystem>
        get() = systemManager.systems

    /**
     * Returns immutable collection of entities for the specified [Family]. Will return the same instance every time.
     */
    fun getEntitiesFor(family: Family): ImmutableArray<Entity> {
        return familyManager.getEntitiesFor(family)
    }

    /**
     * Adds an [EntityListener].
     *
     *
     * The listener will be notified every time an entity is added/removed to/from the engine.
     */
    fun addEntityListener(listener: EntityListener) {
        addEntityListener(empty, 0, listener)
    }

    /**
     * Adds an [EntityListener]. The listener will be notified every time an entity is added/removed
     * to/from the engine. The priority determines in which order the entity listeners will be called. Lower
     * value means it will get executed first.
     */
    fun addEntityListener(priority: Int, listener: EntityListener) {
        addEntityListener(empty, priority, listener)
    }

    /**
     * Adds an [EntityListener] for a specific [Family].
     *
     *
     * The listener will be notified every time an entity is added/removed to/from the given family.
     */
    fun addEntityListener(family: Family, listener: EntityListener) {
        addEntityListener(family, 0, listener)
    }

    /**
     * Adds an [EntityListener] for a specific [Family]. The listener will be notified every time an entity is
     * added/removed to/from the given family. The priority determines in which order the entity listeners will be called. Lower
     * value means it will get executed first.
     */
    fun addEntityListener(family: Family, priority: Int, listener: EntityListener) {
        familyManager.addEntityListener(family, priority, listener)
    }

    /**
     * Removes an [EntityListener]
     */
    fun removeEntityListener(listener: EntityListener) {
        familyManager.removeEntityListener(listener)
    }

    fun init() {
        if (!this.isInitialized) {
            this.isInitialized = true
            for (system in systemManager.systems) {
                if (system.isEnabled) {
                    system.preInit()
                }
            }
            for (system in systems) {
                if (system.isEnabled) {
                    system.postInit()
                }
            }
        }
    }

    /**
     * Steps all the systems in this Engine.
     *
     * @param deltaTime The time passed since the last frame.
     */
    fun step(deltaTime: Double) {
        check(!updating) { "Cannot call step() on an Engine that is already stepping." }
        if (!this.isInitialized) {
            init()
        }
        val scaledDeltaTime = deltaTime * realtimeFactor
        updating = true
        try {
            for (system in systems) {
                if (system.isEnabled) {
                    system.step(scaledDeltaTime)
                }
                while (componentOperationHandler.hasOperationsToProcess() || entityManager.hasPendingOperations()) {
                    componentOperationHandler.processOperations()
                    entityManager.processPendingOperations()
                }
            }
            currentTime += scaledDeltaTime
            stepNumber += 1
            for (system in systems) {
                if (system.isEnabled) {
                    system.postStep()
                }
            }
        } finally {
            updating = false
        }
    }

    fun terminate() {
        check(!updating) { "Cannot call terminate() on an Engine that is updating." }
        for (system in systems) {
            if (system.isEnabled) {
                system.terminate()
            }
        }
    }

    override fun close() {
        terminate()
    }

    protected fun addEntityInternal(entity: Entity) {
        entity.componentAdded.add(componentAdded)
        entity.componentRemoved.add(componentRemoved)
        entity.componentOperationHandler = componentOperationHandler
        familyManager.updateFamilyMembership(entity)
    }

    protected open fun removeEntityInternal(entity: Entity) {
        familyManager.updateFamilyMembership(entity)
        entity.componentAdded.remove(componentAdded)
        entity.componentRemoved.remove(componentRemoved)
        entity.componentOperationHandler = null
    }

    private inner class ComponentListener : Listener<Entity> {
        override fun receive(
            signal: Signal<Entity>,
            `object`: Entity
        ) {
            familyManager.updateFamilyMembership(`object`)
        }
    }

    private inner class EngineSystemListener : SystemManager.SystemListener {
        override fun systemAdded(system: EntitySystem) {
            system.addedToEngineInternal(this@Engine)
        }

        override fun systemRemoved(system: EntitySystem) {
            system.removedFromEngineInternal(this@Engine)
        }
    }

    private inner class EngineEntityListener : EntityListener {
        override fun entityAdded(entity: Entity) {
            addEntityInternal(entity)
        }

        override fun entityRemoved(entity: Entity) {
            removeEntityInternal(entity)
        }
    }

    private inner class EngineDelayedInformer : BooleanInformer {
        override fun value(): Boolean {
            return updating
        }
    }

    companion object {
        private val empty = Family.all().get()
    }

}
