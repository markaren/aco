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

import info.laht.aco.core.ComponentMapper;
import info.laht.aco.core.Engine;
import info.laht.aco.core.Entity;
import info.laht.aco.core.EntitySystem;
import info.laht.aco.core.Family;
import info.laht.aco.components.PositionComponent;
import info.laht.aco.components.VisualComponent;
import info.laht.aco.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RenderSystem extends EntitySystem {

	private ImmutableArray<Entity> entities;

	private final SpriteBatch batch;
	private final OrthographicCamera camera;

	private final ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
	private final ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);

	public RenderSystem (OrthographicCamera camera) {
		batch = new SpriteBatch();

		this.camera = camera;
	}

	@Override
	public void addedToEngine (Engine engine) {
		entities = engine.getEntitiesFor(Family.all(PositionComponent.class, VisualComponent.class).get());
	}

	@Override
	public void removedFromEngine (Engine engine) {

	}

	@Override
	public void step(double deltaTime) {
		PositionComponent position;
		VisualComponent visual;

		camera.update();

		batch.begin();
		batch.setProjectionMatrix(camera.combined);

		for (int i = 0; i < entities.size(); ++i) {
			Entity e = entities.get(i);

			position = pm.get(e);
			visual = vm.get(e);

			batch.draw(visual.region, position.x, position.y);
		}

		batch.end();
	}
}
