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

import info.laht.aco.core.EntitySystem;
import info.laht.aco.core.PooledEngine;

public class IgnoreSystemTest {

	public static void main (String[] args) {
		PooledEngine engine = new PooledEngine();

		CounterSystem counter = new CounterSystem();
		IgnoredSystem ignored = new IgnoredSystem();

		engine.addSystem(counter);
		engine.addSystem(ignored);

		for (int i = 0; i < 10; i++) {
			engine.update(0.25);
		}
	}

	private static class CounterSystem extends EntitySystem {
		@Override
		public void update (double deltaTime) {
			log("Running " + getClass().getSimpleName());
		}
	}

	private static class IgnoredSystem extends EntitySystem {

		int counter = 0;

		@Override
		public boolean checkProcessing () {
			counter = 1 - counter;
			return counter == 1;
		}

		@Override
		public void update (double deltaTime) {
			log("Running " + getClass().getSimpleName());
		}
	}

	public static void log (String string) {
		System.out.println(string);
	}

}
