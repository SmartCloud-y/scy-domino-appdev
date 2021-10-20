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

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;

import com.cloud_y.dominoext.annotations.DominoAgentExecutable;
import com.cloud_y.dominoext.annotations.DominoScriptLibraries;
import com.cloud_y.dominoext.classloaders.DominoClassLoaderBuilder;
import com.cloud_y.dominoext.classloaders.DominoClassLoaderManager;
import com.cloud_y.dominoext.classloaders.ExtendedClassLoaderException;
import com.cloud_y.dominoext.classloaders.ParentLastClassLoader;

import lotus.domino.AgentBase;
import lotus.domino.NotesError;
import lotus.domino.NotesException;

/**
 * 
 * EnhancedAgentBase provides an enhnanced AgentBase implementation that is able
 * to extend agent operationts by:
 * 
 * - Detaching shared libraries to disk (instead of loading in memory) -
 * Optimizing the class-library by only extracting libraries if changed since
 * last run - FUTURE: caching / optimizing ClassLoaders needed by a Domino agent
 * for reuse/cleaning up on resource change - Creating an enriched ClassLoader
 * where "agent libraries" are loaded before parent classloaders'
 * 
 * 
 * <java.io.tmp>/amgr-enanced/<replicaid>/<files>.jar
 * 
 * @author Daniele Vistalli <daniele.vistalli@factor-y.com>
 *
 */

public class ExtendedAgentBase extends AgentBase {

    private boolean extendedAgentDebugEnabled = false;
    private boolean extendedAgentTraceEnabled = false;

    private ClassLoader originalAgentManagerClassLoader = null;

//    private String fileSystemStoragePath = null; // Where the file based classpath needs to be built
    private String extendedAgentClassName; // The name of the class implementing the EnangedAgentRunnable

    private DominoClassLoaderBuilder classLoaderBuilder = null;

    public void setDebug(boolean debug) {
	super.setDebug(debug);
	this.extendedAgentDebugEnabled = debug;
    }

    public void setTrace(boolean trace) {
	super.setTrace(trace);
	this.extendedAgentTraceEnabled = trace;
    }

    public void addScriptLibrary(String libraryName) {
	classLoaderBuilder.addScriptLibrary(libraryName, false);
    }

    public void addScriptLibrary(String libraryName, boolean includeSources) {
	classLoaderBuilder.addScriptLibrary(libraryName, includeSources);
    }

    @Override
    public void initThread() {
	super.initThread();
    }

    public void setRunnableClass(String className) {
	this.extendedAgentClassName = className;
    }

    @Override
    public void NotesMain() {

	Class<?> enhancedAgentclazz = this.getClass();

	try {
	    classLoaderBuilder = DominoClassLoaderManager.getInstance().newBuilder(getAgentSession(),
		    getAgentSession().getAgentContext().getCurrentDatabase());
	} catch (NotesException e) {
	    throw new RuntimeException("getCurrent database faild when building new classloader", e);
	}

	if (enhancedAgentclazz.isAnnotationPresent(DominoScriptLibraries.class)) {
	    System.out.println("DominoScriptLibraries annotation found");

	    DominoScriptLibraries scriptLibraries = enhancedAgentclazz.getAnnotation(DominoScriptLibraries.class);

	    Arrays.stream(scriptLibraries.value()).forEach(lib -> {
		System.out.println("Adding: " + lib.libraryName() + " with internal code: " + lib.useLocalCode());
		this.addScriptLibrary(lib.libraryName(), lib.useLocalCode());
	    });

	    DominoAgentExecutable executable = enhancedAgentclazz.getAnnotation(DominoAgentExecutable.class);
	    if (executable != null) {
		System.out.println("Class: " + executable.agentExecutableClass() + " - Debug: " + executable.debug()
			+ " - Trace: " + executable.trace());
		setRunnableClass(executable.agentExecutableClass());
		setDebug(executable.debug());
		setTrace(executable.trace());
	    }

	} else {
	    System.out.println("DominoScriptLibraries annotation NOT found");
	}

	if (extendedAgentDebugEnabled) {
	    System.out.println("SmartAgentBase initThread");
	}

	ClassLoader originalClassLoader = currentThread().getContextClassLoader(); // Get current class loader for agent

	ParentLastClassLoader newClassloader;

	try {
	    newClassloader = AccessController.doPrivileged(new PrivilegedExceptionAction<ParentLastClassLoader>() {

		@Override
		public ParentLastClassLoader run() throws Exception {
		    try {
			final ParentLastClassLoader smartClassLoader = classLoaderBuilder
				.buildAndGetClassLoader(originalClassLoader);
			currentThread().setContextClassLoader(smartClassLoader);
			return smartClassLoader;
		    } catch (NotesException | ExtendedClassLoaderException e2) {
			e2.printStackTrace();
		    }
		    return null;
		}

	    });
	} catch (PrivilegedActionException e1) {
	    e1.printStackTrace();
	    throw new RuntimeException(new NotesException(NotesError.NOTES_ERR_FAILURE,
		    "Privileged action not allowed: " + e1.getMessage()));
	}

	ExtendedAgentRunnable runnable = null;

	try {

	    runnable = (ExtendedAgentRunnable) Class.forName(extendedAgentClassName, true, newClassloader)
		    .newInstance();

	} catch (IllegalAccessException e) {
	    throw new RuntimeException(new NotesException(NotesError.NOTES_ERR_FAILURE,
		    "Enhanced Agent Manager reports an illegal access error for class: " + extendedAgentClassName, e));
	} catch (InstantiationException e) {
	    throw new RuntimeException(new NotesException(NotesError.NOTES_ERR_FAILURE,
		    "Enhanced Agent Manager cannot instantiate class: " + extendedAgentClassName, e));
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(new NotesException(NotesError.NOTES_ERR_FAILURE,
		    "Enhanced Agent Manager cannot find class: " + extendedAgentClassName, e));
	}
	runnable.setBase(this);
	runnable.run();

	// Revert back to old class-loader and free up resources.

	currentThread().setContextClassLoader(originalClassLoader);

	// Cleanup ExtendedClassLoader
	
	try {
	    newClassloader.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    @Override
    public void termThread() {
	currentThread().setContextClassLoader(originalAgentManagerClassLoader);
	super.termThread();
    }

}
