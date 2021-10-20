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
package com.cloud_y.dominoext.classloaders;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * @author Daniele Vistalli <daniele.vistalli@factor-y.com>
 *
 */

public class DominoClassLoaderBuilder {

    private static Logger log = Logger.getLogger(DominoClassLoaderBuilder.class.getName());

    class ScriptLibraryOptions {

	private String libraryName;
	private boolean includeLocalSources = false;

	public ScriptLibraryOptions(String libraryName, boolean includeLocalSources) {
	    this.libraryName = libraryName;
	    this.includeLocalSources = includeLocalSources;
	}

	public String getLibraryName() {
	    return libraryName;
	}

	public boolean isIncludeLocalSources() {
	    return includeLocalSources;
	}

    }

    private HashMap<String, ScriptLibraryOptions> scriptLibraries = new HashMap<>(); // List of script libraries
										     // required by the agent

    private Session session;
    private Database db;
    private DominoClassLoaderManager parent;

    public DominoClassLoaderBuilder(Session session, Database db, DominoClassLoaderManager dominoClassLoaderManager) {
	this.session = session;
	this.db = db;
	this.parent = dominoClassLoaderManager;
    }

    public void addScriptLibrary(String libraryName, boolean includeLocalSources) {
	log.info("Adding script library: " + libraryName + " with sources: " + includeLocalSources);
	scriptLibraries.put(libraryName, new ScriptLibraryOptions(libraryName, includeLocalSources));
    }

    public ParentLastClassLoader buildAndGetClassLoader(ClassLoader parentClassLoader)
	    throws ExtendedClassLoaderException, NotesException {
	// Deploy Java Script Libraries from DB to filesystem to serve the agent

	List<ScriptLibraryClassLoaderEntry> libraries = scriptLibraries.values().stream().map(libraryEntry -> {

	    log.info("Processing & installing library: " + libraryEntry.libraryName);
	    
	    try {
		return parent.installScriptLibrary(session, db, libraryEntry.libraryName,
			libraryEntry.includeLocalSources);
	    } catch (NotesException e) {
		e.printStackTrace();
		return null; // Failure installing library
	    }

	}).collect(Collectors.toList());

	return new ParentLastClassLoader(libraries, parentClassLoader);

    }

}
