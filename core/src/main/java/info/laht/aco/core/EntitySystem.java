/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package info.laht.aco.core;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for processing sets of {@link Entity} objects.
 *
 * @author Stefan Bachmann
 */
public abstract class EntitySystem {

    /**
     * Use this to set the priority of the system. Lower means it'll get executed first.
     */
    public int priority;

    private boolean enabled;
    private Engine engine;

    /**
     * Default constructor that will initialise an EntitySystem with priority 0.
     */
    public EntitySystem() {
        this(0);
    }

    /**
     * Initialises the EntitySystem with the priority specified.
     *
     * @param priority The priority to execute this system with (lower means higher priority).
     */
    public EntitySystem(int priority) {
        this.priority = priority;
        this.enabled = true;
    }

    /**
     * Called when this EntitySystem is added to an {@link Engine}.
     *
     * @param engine The {@link Engine} this system was added to.
     */
    protected void addedToEngine(@NotNull Engine engine) {
    }

    /**
     * Called when this EntitySystem is removed from an {@link Engine}.
     *
     * @param engine The {@link Engine} the system was removed from.
     */
    protected void removedFromEngine(@NotNull Engine engine) {
    }

    protected void preInit() {
    }

    protected void postInit() {
    }

    /**
     * The update method called every tick.
     *
     * @param deltaTime The time passed since last frame in seconds.
     */
    protected void step(double deltaTime) {
    }

    protected void fixedStep(double deltaTime) {
    }

    protected void postStep() {
    }

    public void terminate() {
    }

    /**
     * @return Whether or not the system should be processed.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether or not the system should be processed by the {@link Engine}.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return engine instance the system is registered to.
     * It will be null if the system is not associated to any engine instance.
     */
    public Engine getEngine() {
        return engine;
    }

    final void addedToEngineInternal(Engine engine) {
        this.engine = engine;
        addedToEngine(engine);
    }

    final void removedFromEngineInternal(Engine engine) {
        this.engine = null;
        removedFromEngine(engine);
    }

}
