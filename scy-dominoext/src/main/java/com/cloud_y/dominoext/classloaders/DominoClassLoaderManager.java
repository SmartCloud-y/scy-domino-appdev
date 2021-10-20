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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.EmbeddedObject;
import lotus.domino.Item;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class DominoClassLoaderManager {

    private static final String SOURCE_JAR_ATTACHMENT_NAME = "%%source%%.jar";
    private static final String OBJECT_JAR_ATTACHMENT_NAME = "%%object%%.jar";

    private static Logger log = Logger.getLogger(DominoClassLoaderBuilder.class.getName());

    private static DominoClassLoaderManager instance = new DominoClassLoaderManager();

    public static DominoClassLoaderManager getInstance() {
	return instance;
    }

    private String dominoExtClassLoaderFileSystemPath;

    // Key = database(replicaid)/dependenciesList(list of script libraries names)
    // Value = classLoaderEntry(last build time / URLClassLoader)

    /*
     * Filesystem manager & layout
     * 
     * - Domino temporary folder (Directory or notes_tempdir from notes.ini) -
     * /<replicaid>/<dependenciesListHash>
     * 
     */

    public DominoClassLoaderManager() {
	super();
	this.dominoExtClassLoaderFileSystemPath = System.getProperty("java.io.tmpdir")
		+ FileSystems.getDefault().getSeparator() + "scy-dominoext";
	log.info("Class loader path: " + dominoExtClassLoaderFileSystemPath);
    }

    private HashMap<String, ClassLoader> cachedClassLoaders;

//    public void addScriptLibrary(String DbReplicaId, String libraryName) {
//
//	String noteId = null;
//	Date lastUpdate = null;
//
//	//
//	new ScriptLibraryClassLoaderEntry(DbReplicaId, libraryName, noteId, lastUpdate);
//
//    }

    /**
     * Creates a new ClassLoader builder in the context of a given session and
     * database
     * 
     * Session and database will be used to access code artifacts for the
     * application.
     * 
     * At the end of collection the global manager will be requested to process
     * dependencies and track usage.
     * 
     * @param session
     * @param db
     * @return
     * 
     */
    public DominoClassLoaderBuilder newBuilder(Session session, Database db) {
	// TODO Auto-generated method stub
	return new DominoClassLoaderBuilder(session, db, this);
    }

    /**
     * Adds a new shared library to the internal library management system
     * 
     * Finds shared library & checks for changes
     * 
     * Extracts to disk if newer (with a name that's qualified by the modification date)
     * 
     * Returns a new ScriptLibraryClassLoader using the files.
     * 
     * @param session
     * @param db
     * @param libraryName
     * @param b
     * @throws NotesException
     */
    protected ScriptLibraryClassLoaderEntry installScriptLibrary(Session session, Database db, String libraryName,
	    boolean includeLocalSources) throws NotesException {

	// 1. Gets a java script library from current DB
	// 2. Deatches %%object.jar%% to disk in a temp folder
	// 3. Add it to URLClassLoader
	// Ideally logic to only refresh when needed
	// Debug mode should detach each time

	log.info("Extracting: " + libraryName);

	String libraryPathFull = dominoExtClassLoaderFileSystemPath + "/" + db.getReplicaID() + "/" + libraryName;

	Path libraryPath = Paths.get(libraryPathFull);

	log.info("Preparing " + libraryPathFull + " as " + libraryPath.toString());
	// Create path needed by library
	try {
	    Files.createDirectories(libraryPath);
	} catch (IOException e2) {
	    // TODO Auto-generated catch block
	    e2.printStackTrace();
	}

	try {
	    NoteCollection ncoll = db.createNoteCollection(false);
	    ncoll.setSelectScriptLibraries(true);
	    ncoll.setSelectionFormula("SELECT $TITLE = \"" + libraryName + "\"");
	    ncoll.buildCollection();

	    log.info("Found " + ncoll.getCount() + " library for " + libraryName);

	    Document docLib = db.getDocumentByID(ncoll.getFirstNoteID());

	    Vector<Item> doclibItems = docLib.getItems();
	    doclibItems.stream().filter(item -> {
		try {
		    return item.getName().equals("$FILE");
		} catch (NotesException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		    return false;
		}
	    }).forEach(it -> {
		try {

		    String filename = it.getValueString();
		    EmbeddedObject attachment = docLib.getAttachment(filename);

		    if (filename.equals(OBJECT_JAR_ATTACHMENT_NAME)) {
			if (includeLocalSources) {
			    // Extract library local sources compiled for runtime
			    log.info("Extracting library local code: " + libraryName);
			    attachment.extractFile(libraryPathFull + "/" + libraryName + "_localcode.jar");
			}
		    } else if (!filename.equals(SOURCE_JAR_ATTACHMENT_NAME)) {
			// Extract any jar that is not the source jars for the library
			log.info("Extracting library JAR: " + libraryName + " " + filename);
			attachment.extractFile(libraryPathFull + "/" + filename);
		    }
		    attachment.recycle();
		    it.remove();
		} catch (NotesException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    });

	    ncoll.recycle();

	} catch (NotesException e) {
	    e.printStackTrace();
	}

	// Build list of files in classpath folder in order to create an URLClassLoader

	File[] files = new File(libraryPathFull).listFiles();

	// List urls = new ArrayList();
	List<URL> classPathURLs = new ArrayList<>(); // List of URLs to be added to classpath
	for (int i = 0; i < files.length; i++) {
	    if (!files[i].isDirectory()) {
		try {
		    log.info("Adding to ClassLoader " + files[i].getAbsolutePath());
		    classPathURLs.add(files[i].toURI().toURL());
		} catch (MalformedURLException e) {
		    log.info("Error parsing " + files[i].getAbsolutePath());
		}
	    }
	}

	URL[] urlsArray = classPathURLs.toArray(new URL[] {});

	return new ScriptLibraryClassLoaderEntry(urlsArray, libraryName);
    }

}
