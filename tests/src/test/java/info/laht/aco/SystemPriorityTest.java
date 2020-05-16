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

import info.laht.aco.core.Engine;
import info.laht.aco.core.EntitySystem;

public class SystemPriorityTest {
    public static void main(String[] args) {
        Engine engine = new Engine();

        engine.addSystem(new SystemA(10));
        engine.addSystem(new SystemB(5));
        engine.addSystem(new SystemA(2));

        engine.update(0);
    }

    public static class SystemA extends EntitySystem {
        public SystemA(int priority) {
            super(priority);
        }

        @Override
        public void update(double deltaTime) {
            System.out.println("SystemA");
        }

    }

    public static class SystemB extends EntitySystem {
        public SystemB(int priority) {
            super(priority);
        }

        @Override
        public void update(double deltaTime) {
            System.out.println("SystemB");
        }
    }

}
