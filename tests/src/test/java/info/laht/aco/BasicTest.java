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

package info.laht.aco;

import info.laht.aco.components.MovementComponent;
import info.laht.aco.components.PositionComponent;
import info.laht.aco.core.*;
import info.laht.aco.utils.ImmutableArray;

public class BasicTest {

    public static void main(String[] args) {
        PooledEngine engine = new PooledEngine();

        MovementSystem movementSystem = new MovementSystem();
        PositionSystem positionSystem = new PositionSystem();

        engine.addSystem(movementSystem);
        engine.addSystem(positionSystem);

        Listener listener = new Listener();
        engine.addEntityListener(listener);

        for (int i = 0; i < 10; i++) {
            Entity entity = engine.createEntity();
            entity.add(new PositionComponent(10, 0));
            if (i > 5) entity.add(new MovementComponent(10, 2));

            engine.addEntity(entity);
        }

        log("MovementSystem has: " + movementSystem.entities.size() + " entities.");
        log("PositionSystem has: " + positionSystem.entities.size() + " entities.");

        for (int i = 0; i < 10; i++) {
            engine.step(0.25f);

            if (i > 5) engine.removeSystem(movementSystem);
        }

        engine.removeEntityListener(listener);
    }

    public static class PositionSystem extends EntitySystem {
        public ImmutableArray<Entity> entities;

        @Override
        public void addedToEngine(Engine engine) {
            entities = engine.getEntitiesFor(Family.all(PositionComponent.class).get());
            log("PositionSystem added to engine.");
        }

        @Override
        public void removedFromEngine(Engine engine) {
            log("PositionSystem removed from engine.");
            entities = null;
        }
    }

    public static class MovementSystem extends EntitySystem {
        public ImmutableArray<Entity> entities;

        private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
        private ComponentMapper<MovementComponent> mm = ComponentMapper.getFor(MovementComponent.class);

        @Override
        public void addedToEngine(Engine engine) {
            entities = engine.getEntitiesFor(Family.all(PositionComponent.class, MovementComponent.class).get());
            log("MovementSystem added to engine.");
        }

        @Override
        public void removedFromEngine(Engine engine) {
            log("MovementSystem removed from engine.");
            entities = null;
        }

        @Override
        public void step(double deltaTime) {

            for (Entity e : entities) {
                PositionComponent p = pm.get(e);
                MovementComponent m = mm.get(e);

                p.x += m.velocityX * deltaTime;
                p.y += m.velocityY * deltaTime;
            }

            log(entities.size() + " Entities updated in MovementSystem.");
        }
    }

    public static class Listener implements EntityListener {

        @Override
        public void entityAdded(Entity entity) {
            log("Entity added " + entity);
        }

        @Override
        public void entityRemoved(Entity entity) {
            log("Entity removed " + entity);
        }
    }

    public static void log(String string) {
        System.out.println(string);
    }
}
