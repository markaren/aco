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

package info.laht.aco.systems;

import info.laht.aco.core.*;
import info.laht.aco.utils.ImmutableArray;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class IntervalIteratingTest {

	private static final double deltaTime = 0.1;

	private static class IntervalComponentSpy implements Component {
		public int numUpdates = 0;
	}

	private static class IntervalIteratingSystemSpy extends IntervalIteratingSystem {

		private final ComponentMapper<IntervalComponentSpy> im;

		public IntervalIteratingSystemSpy () {
			super(Family.all(IntervalComponentSpy.class).get(), deltaTime * 2);

			im = ComponentMapper.getFor(IntervalComponentSpy.class);
		}

		@Override
		protected void processEntity (@NotNull Entity entity, double currentTime, double deltaTime) {
			im.get(entity).numUpdates++;
		}
	}

	@Test
	public void intervalSystem () {
		Engine engine = new Engine();
		IntervalIteratingSystemSpy intervalSystemSpy = new IntervalIteratingSystemSpy();
		ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(IntervalComponentSpy.class).get());
		ComponentMapper<IntervalComponentSpy> im = ComponentMapper.getFor(IntervalComponentSpy.class);

		engine.addSystem(intervalSystemSpy);

		for (int i = 0; i < 10; ++i) {
			Entity entity = new Entity();
			entity.add(new IntervalComponentSpy());
			engine.addEntity(entity);
		}

		for (int i = 1; i <= 10; ++i) {
			engine.step(deltaTime);

			for (int j = 0; j < entities.size(); ++j) {
				Assert.assertEquals(i / 2, im.get(entities.get(j)).numUpdates);
			}
		}
	}
}
