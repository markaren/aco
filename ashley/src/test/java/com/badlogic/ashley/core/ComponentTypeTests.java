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

package com.badlogic.ashley.core;

import org.junit.Assert;
import org.junit.Test;

public class ComponentTypeTests {

	private static class ComponentA implements Component {

	}

	private static class ComponentB implements Component {

	}

	@Test
	public void validComponentType () {
		Assert.assertNotNull(ComponentType.getFor(ComponentA.class));
		Assert.assertNotNull(ComponentType.getFor(ComponentB.class));
	}

	@Test
	public void sameComponentType () {
		ComponentType componentType1 = ComponentType.getFor(ComponentA.class);
		ComponentType componentType2 = ComponentType.getFor(ComponentA.class);

		Assert.assertEquals(true, componentType1.equals(componentType2));
		Assert.assertEquals(true, componentType2.equals(componentType1));
		Assert.assertEquals(componentType1.getIndex(), componentType2.getIndex());
		Assert.assertEquals(componentType1.getIndex(), ComponentType.getIndexFor(ComponentA.class));
		Assert.assertEquals(componentType2.getIndex(), ComponentType.getIndexFor(ComponentA.class));
	}

	@Test
	public void differentComponentType () {
		ComponentType componentType1 = ComponentType.getFor(ComponentA.class);
		ComponentType componentType2 = ComponentType.getFor(ComponentB.class);

		Assert.assertEquals(false, componentType1.equals(componentType2));
		Assert.assertEquals(false, componentType2.equals(componentType1));
		Assert.assertNotEquals(componentType1.getIndex(), componentType2.getIndex());
		Assert.assertNotEquals(componentType1.getIndex(), ComponentType.getIndexFor(ComponentB.class));
		Assert.assertNotEquals(componentType2.getIndex(), ComponentType.getIndexFor(ComponentA.class));
	}
}
