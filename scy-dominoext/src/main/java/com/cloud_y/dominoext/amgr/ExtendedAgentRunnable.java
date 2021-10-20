/*
 * Copyright © Oct 1, 2021 Factor-y S.r.l. (daniele.vistalli@factor-y.com)
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
package com.cloud_y.dominoext.amgr;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import lotus.domino.AgentBase;
import lotus.domino.Session;

/**
 * A decorator class that's invoked by an ExtendedAgentBase agent to use code 
 * from the extended classloader
 * 
 * @author Daniele Vistalli <daniele.vistalli@factor-y.com>
 *
 */
public abstract class ExtendedAgentRunnable implements Runnable, IAgentBase  {

	private AgentBase base;

	public ExtendedAgentRunnable() {
		this(null);
	}
	
	public ExtendedAgentRunnable(AgentBase base) {
		this.base = base;
	}

	@Override
	public abstract void NotesMain();

	@Override
	public void dbgMsg(String msg) {
		base.dbgMsg(msg);
	}

	@Override
	public void dbgMsg(String msg, PrintStream ps) {
		base.dbgMsg(msg, ps);
	}

	@Override
	public void dbgMsg(String msg, PrintWriter pw) {
		base.dbgMsg(msg, pw);
	}

	@Override
	public PrintWriter getAgentOutput() {
		return base.getAgentOutput();
	}

	@Override
	public OutputStream getAgentOutputStream() {
		return base.getAgentOutputStream();
	}

	@Override
	public Session getSession() {
		return base.getSession();
	}

	@Override
	public boolean isRestricted() {
		return base.isRestricted();
	}

	@Override
	public void setDebug(boolean debug) {
		base.setDebug(debug);
	}

	@Override
	public void setTrace(boolean trace) {
		base.setTrace(trace);
	}

	@Override
	public void run() {
		NotesMain();
	}

	@Override
	public void setBase(AgentBase base) {
		this.base = base;
	}

}
