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

import info.laht.aco.core.EntitySystem;

/**
 * A simple {@link EntitySystem} that does not run its update logic every call to {@link EntitySystem#update(float)}, but after a
 * given interval. The actual logic should be placed in {@link IntervalSystem#updateInterval()}.
 * @author David Saltares
 */
public abstract class IntervalSystem extends EntitySystem {

	private double interval;
	private double accumulator;

	/**
	 * @param interval time in seconds between calls to {@link IntervalSystem#updateInterval()}.
	 */
	public IntervalSystem (double interval) {
		this(interval, 0);
	}

	/**
	 * @param interval time in seconds between calls to {@link IntervalSystem#updateInterval()}.
	 * @param priority
	 */
	public IntervalSystem (double interval, int priority) {
		super(priority);
		this.interval = interval;
		this.accumulator = 0;
	}

	public double getInterval() {
		return interval;
	}

	@Override
	public final void update (double deltaTime) {
		accumulator += deltaTime;

		while (accumulator >= interval) {
			accumulator -= interval;
			updateInterval();
		}
	}

	/**
	 * The processing logic of the system should be placed here.
	 */
	protected abstract void updateInterval ();

}