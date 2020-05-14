package info.laht.aco.core;

import info.laht.aco.core.ComponentOperationHandler.BooleanInformer;
import info.laht.aco.signals.Listener;
import info.laht.aco.signals.Signal;
import org.junit.Assert;
import org.junit.Test;

public class ComponentOperationHandlerTests {

	private static class BooleanInformerMock implements BooleanInformer {

		public boolean delayed = false;
		
		@Override
		public boolean value () {
			return delayed;
		}
	}
	
	private static class ComponentSpy implements Listener<Entity> {
		public boolean called;
		
		@Override
		public void receive(Signal<Entity> signal, Entity object) {
			called = true;
		}
	}
	
	@Test
	public void add() {
		ComponentSpy spy = new ComponentSpy();
		BooleanInformerMock informer = new BooleanInformerMock();
		ComponentOperationHandler handler = new ComponentOperationHandler(informer);
		
		Entity entity = new Entity();
		entity.componentOperationHandler = handler;
		entity.componentAdded.add(spy);
		
		handler.add(entity);
		
		Assert.assertTrue(spy.called);
	}

	@Test
	public void addDelayed() {
		ComponentSpy spy = new ComponentSpy();
		BooleanInformerMock informer = new BooleanInformerMock();
		ComponentOperationHandler handler = new ComponentOperationHandler(informer);
		
		informer.delayed = true;
		
		Entity entity = new Entity();
		entity.componentOperationHandler = handler;
		entity.componentAdded.add(spy);
		
		handler.add(entity);
		
		Assert.assertFalse(spy.called);
		handler.processOperations();
		Assert.assertTrue(spy.called);
	}
	
	@Test
	public void remove() {
		ComponentSpy spy = new ComponentSpy();
		BooleanInformerMock informer = new BooleanInformerMock();
		ComponentOperationHandler handler = new ComponentOperationHandler(informer);
		
		Entity entity = new Entity();
		entity.componentOperationHandler = handler;
		entity.componentRemoved.add(spy);
		
		handler.remove(entity);
		
		Assert.assertTrue(spy.called);
	}
	
	@Test
	public void removeDelayed() {
		ComponentSpy spy = new ComponentSpy();
		BooleanInformerMock informer = new BooleanInformerMock();
		ComponentOperationHandler handler = new ComponentOperationHandler(informer);
		
		informer.delayed = true;
		
		Entity entity = new Entity();
		entity.componentOperationHandler = handler;
		entity.componentRemoved.add(spy);
		
		handler.remove(entity);
		
		Assert.assertFalse(spy.called);
		handler.processOperations();
		Assert.assertTrue(spy.called);
	}
}
