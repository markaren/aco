package test.java.com.badlogic.ashley.core;

import static org.junit.Assert.*;

import main.java.com.badlogic.ashley.core.Engine;
import main.java.com.badlogic.ashley.core.EntitySystem;
import main.java.com.badlogic.ashley.core.SystemManager;
import org.junit.Test;

import main.java.com.badlogic.ashley.core.SystemManager.SystemListener;
import main.java.com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;

public class SystemManagerTests {

	private static class SystemListenerSpy implements SystemListener {
		public int addedCount = 0;
		public int removedCount = 0;
		
		@Override
		public void systemAdded (EntitySystem system) {
			system.addedToEngine(null);
			++addedCount;
		}

		@Override
		public void systemRemoved (EntitySystem system) {
			system.removedFromEngine(null);
			removedCount++;
		}
	}
	
	private static class EntitySystemMock extends EntitySystem {
		public int addedCalls = 0;
		public int removedCalls = 0;

		private Array<Integer> updates;

		public EntitySystemMock () {
			super();
		}

		public EntitySystemMock (Array<Integer> updates) {
			super();

			this.updates = updates;
		}

		@Override
		public void update (float deltaTime) {
			if (updates != null) {
				updates.add(priority);
			}
		}

		@Override
		public void addedToEngine (Engine engine) {
			++addedCalls;
		}

		@Override
		public void removedFromEngine (Engine engine) {
			++removedCalls;
		}
	}

	private static class EntitySystemMockA extends EntitySystemMock {

		public EntitySystemMockA () {
			super();
		}

		public EntitySystemMockA (Array<Integer> updates) {
			super(updates);
		}
	}

	private static class EntitySystemMockB extends EntitySystemMock {

		public EntitySystemMockB () {
			super();
		}

		public EntitySystemMockB (Array<Integer> updates) {
			super(updates);
		}
	}
	
	@Test
	public void addAndRemoveSystem () {
		EntitySystemMockA systemA = new EntitySystemMockA();
		EntitySystemMockB systemB = new EntitySystemMockB();
		
		SystemListenerSpy systemSpy = new SystemListenerSpy();
		SystemManager manager = new SystemManager(systemSpy);

		Assert.assertNull(manager.getSystem(EntitySystemMockA.class));
		Assert.assertNull(manager.getSystem(EntitySystemMockB.class));

		manager.addSystem(systemA);
		manager.addSystem(systemB);

		Assert.assertNotNull(manager.getSystem(EntitySystemMockA.class));
		Assert.assertNotNull(manager.getSystem(EntitySystemMockB.class));
		Assert.assertEquals(1, systemA.addedCalls);
		Assert.assertEquals(1, systemB.addedCalls);

		manager.removeSystem(systemA);
		manager.removeSystem(systemB);

		Assert.assertNull(manager.getSystem(EntitySystemMockA.class));
		Assert.assertNull(manager.getSystem(EntitySystemMockB.class));
		Assert.assertEquals(1, systemA.removedCalls);
		Assert.assertEquals(1, systemB.removedCalls);

		manager.addSystem(systemA);
		manager.addSystem(systemB);
		manager.removeAllSystems();

		Assert.assertNull(manager.getSystem(EntitySystemMockA.class));
		Assert.assertNull(manager.getSystem(EntitySystemMockB.class));
		Assert.assertEquals(2, systemA.removedCalls);
		Assert.assertEquals(2, systemB.removedCalls);
	}

	@Test
	public void getSystems () {
		SystemListenerSpy systemSpy = new SystemListenerSpy();
		SystemManager manager = new SystemManager(systemSpy);
		EntitySystemMockA systemA = new EntitySystemMockA();
		EntitySystemMockB systemB = new EntitySystemMockB();

		Assert.assertEquals(0, manager.getSystems().size());

		manager.addSystem(systemA);
		manager.addSystem(systemB);

		Assert.assertEquals(2, manager.getSystems().size());
		Assert.assertEquals(2, systemSpy.addedCount);
		
		manager.removeSystem(systemA);
		manager.removeSystem(systemB);
		
		Assert.assertEquals(0, manager.getSystems().size());
		Assert.assertEquals(2, systemSpy.addedCount);
		Assert.assertEquals(2, systemSpy.removedCount);
	}
	
	@Test
	public void addTwoSystemsOfSameClass () {
		SystemListenerSpy systemSpy = new SystemListenerSpy();
		SystemManager manager = new SystemManager(systemSpy);
		EntitySystemMockA system1 = new EntitySystemMockA();
		EntitySystemMockA system2 = new EntitySystemMockA();

		Assert.assertEquals(0, manager.getSystems().size());

		manager.addSystem(system1);
		
		Assert.assertEquals(1, manager.getSystems().size());
		Assert.assertEquals(system1, manager.getSystem(EntitySystemMockA.class));
		Assert.assertEquals(1, systemSpy.addedCount);
		
		manager.addSystem(system2);

		Assert.assertEquals(1, manager.getSystems().size());
		Assert.assertEquals(system2, manager.getSystem(EntitySystemMockA.class));
		Assert.assertEquals(2, systemSpy.addedCount);
		Assert.assertEquals(1, systemSpy.removedCount);
	}

	@Test
	public void systemUpdateOrder () {
		Array<Integer> updates = new Array<Integer>();

		SystemListenerSpy systemSpy = new SystemListenerSpy();
		SystemManager manager = new SystemManager(systemSpy);
		EntitySystemMock system1 = new EntitySystemMockA(updates);
		EntitySystemMock system2 = new EntitySystemMockB(updates);

		system1.priority = 2;
		system2.priority = 1;

		manager.addSystem(system1);
		manager.addSystem(system2);

		ImmutableArray<EntitySystem> systems = manager.getSystems();
		Assert.assertEquals(system2, systems.get(0));
		Assert.assertEquals(system1, systems.get(1));
	}
}
