/*
 * Copyright 2015 Mihnea Cinteza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.keepphone;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mikec on 22/02/15.
 */
public class State
{
	private AtomicReference<StateValues> m_state;

	public State()
	{
		this.m_state = new AtomicReference<>(StateValues.STOPPED);
	}

	public void start()
	{
		this.m_state.set(StateValues.RUNNING);
	}

	public void pause()
	{
		this.pause(false);
	}

	public void pause(final boolean auto)
	{
		if (auto)
		{
			this.m_state.set(StateValues.AUTO_PAUSED);
		}
		else
		{
			this.m_state.set(StateValues.PAUSED);
		}
	}

	public void resume()
	{
		this.m_state.set(StateValues.RUNNING);
	}

	public void stop()
	{
		this.m_state.set(StateValues.STOPPED);
	}

	public boolean isRunning()
	{
		return this.m_state.get().equals(StateValues.RUNNING);
	}

	public boolean isPaused()
	{
		return this.m_state.get().equals(StateValues.PAUSED) || this.m_state.get().equals(StateValues.AUTO_PAUSED);
	}

	public boolean isAutoPaused()
	{
		return this.m_state.get().equals(StateValues.AUTO_PAUSED);
	}

	public boolean isStopped()
	{
		return this.m_state.get().equals(StateValues.STOPPED);
	}

	private enum StateValues
	{
		STOPPED,
		RUNNING,
		PAUSED,
		AUTO_PAUSED,
		WAITING_ON_GPS
	}
}
