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

package test.java.com.badlogic.ashley.core;

import static org.junit.Assert.*;

import main.java.com.badlogic.ashley.core.Component;
import main.java.com.badlogic.ashley.core.ComponentMapper;
import main.java.com.badlogic.ashley.core.ComponentType;
import main.java.com.badlogic.ashley.core.Entity;
import org.junit.Test;

import main.java.com.badlogic.ashley.signals.Listener;
import main.java.com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.utils.Bits;

public class EntityTests {

	private static class ComponentA implements Component {
	}

	private static class ComponentB implements Component {
	}

	private static class EntityListenerMock implements Listener<Entity> {

		public int counter = 0;

		@Override
		public void receive (Signal<Entity> signal, Entity object) {
			++counter;

			Assert.assertNotNull(signal);
			Assert.assertNotNull(object);
		}
	}

	private ComponentMapper<ComponentA> am = ComponentMapper.getFor(ComponentA.class);
	private ComponentMapper<ComponentB> bm = ComponentMapper.getFor(ComponentB.class);

	@Test
	public void addAndReturnComponent(){
		Entity entity = new Entity();
		ComponentA componentA = new ComponentA();
		ComponentB componentB = new ComponentB();

		Assert.assertEquals(componentA, entity.addAndReturn(componentA));
		Assert.assertEquals(componentB, entity.addAndReturn(componentB));

		Assert.assertEquals(2, entity.getComponents().size());
	}

	@Test
	public void noComponents () {
		Entity entity = new Entity();

		Assert.assertEquals(0, entity.getComponents().size());
		Assert.assertTrue(entity.getComponentBits().isEmpty());
		Assert.assertNull(am.get(entity));
		Assert.assertNull(bm.get(entity));
		Assert.assertFalse(am.has(entity));
		Assert.assertFalse(bm.has(entity));
	}

	@Test
	public void addAndRemoveComponent () {
		Entity entity = new Entity();

		entity.add(new ComponentA());

		Assert.assertEquals(1, entity.getComponents().size());

		Bits componentBits = entity.getComponentBits();
		int componentAIndex = ComponentType.getIndexFor(ComponentA.class);

		for (int i = 0; i < componentBits.length(); ++i) {
			Assert.assertEquals(i == componentAIndex, componentBits.get(i));
		}

		Assert.assertNotNull(am.get(entity));
		Assert.assertNull(bm.get(entity));
		Assert.assertTrue(am.has(entity));
		Assert.assertFalse(bm.has(entity));

		entity.remove(ComponentA.class);

		Assert.assertEquals(0, entity.getComponents().size());

		for (int i = 0; i < componentBits.length(); ++i) {
			Assert.assertFalse(componentBits.get(i));
		}

		Assert.assertNull(am.get(entity));
		Assert.assertNull(bm.get(entity));
		Assert.assertFalse(am.has(entity));
		Assert.assertFalse(bm.has(entity));
	}
	
	@Test
	public void removeUnexistingComponent () throws Exception {
		// ensure remove unexisting component work with
		// new component type at default bag limits (64)
		Entity entity = new Entity();
		
		ComponentClassFactory cl = new ComponentClassFactory();
		
		for(int i=0 ; i<65 ; i++){
			Class<? extends Component> type = cl.createComponentType("Component" + i);
			entity.remove(type);
			entity.add(type.newInstance());
		}
	}	

	@Test
	public void addAndRemoveAllComponents () {
		Entity entity = new Entity();

		entity.add(new ComponentA());
		entity.add(new ComponentB());

		Assert.assertEquals(2, entity.getComponents().size());

		Bits componentBits = entity.getComponentBits();
		int componentAIndex = ComponentType.getIndexFor(ComponentA.class);
		int componentBIndex = ComponentType.getIndexFor(ComponentB.class);

		for (int i = 0; i < componentBits.length(); ++i) {
			Assert.assertEquals(i == componentAIndex || i == componentBIndex, componentBits.get(i));
		}

		Assert.assertNotNull(am.get(entity));
		Assert.assertNotNull(bm.get(entity));
		Assert.assertTrue(am.has(entity));
		Assert.assertTrue(bm.has(entity));

		entity.removeAll();

		Assert.assertEquals(0, entity.getComponents().size());

		for (int i = 0; i < componentBits.length(); ++i) {
			Assert.assertFalse(componentBits.get(i));
		}

		Assert.assertNull(am.get(entity));
		Assert.assertNull(bm.get(entity));
		Assert.assertFalse(am.has(entity));
		Assert.assertFalse(bm.has(entity));
	}

	@Test
	public void addSameComponent () {
		Entity entity = new Entity();

		ComponentA a1 = new ComponentA();
		ComponentA a2 = new ComponentA();

		entity.add(a1);
		entity.add(a2);

		Assert.assertEquals(1, entity.getComponents().size());
		Assert.assertTrue(am.has(entity));
		Assert.assertNotEquals(a1, am.get(entity));
		Assert.assertEquals(a2, am.get(entity));
	}

	@Test
	public void componentListener () {
		EntityListenerMock addedListener = new EntityListenerMock();
		EntityListenerMock removedListener = new EntityListenerMock();

		Entity entity = new Entity();
		entity.componentAdded.add(addedListener);
		entity.componentRemoved.add(removedListener);

		Assert.assertEquals(0, addedListener.counter);
		Assert.assertEquals(0, removedListener.counter);

		entity.add(new ComponentA());

		Assert.assertEquals(1, addedListener.counter);
		Assert.assertEquals(0, removedListener.counter);

		entity.remove(ComponentA.class);

		Assert.assertEquals(1, addedListener.counter);
		Assert.assertEquals(1, removedListener.counter);

		entity.add(new ComponentB());

		Assert.assertEquals(2, addedListener.counter);

		entity.remove(ComponentB.class);

		Assert.assertEquals(2, removedListener.counter);
	}

	@Test
	public void getComponentByClass () {
		ComponentA compA = new ComponentA();
		ComponentB compB = new ComponentB();

		Entity entity = new Entity();
		entity.add(compA).add(compB);

		ComponentA retA = entity.getComponent(ComponentA.class);
		ComponentB retB = entity.getComponent(ComponentB.class);

		Assert.assertNotNull(retA);
		Assert.assertNotNull(retB);

		Assert.assertTrue(retA == compA);
		Assert.assertTrue(retB == compB);
	}
}
