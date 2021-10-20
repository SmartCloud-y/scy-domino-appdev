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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import com.cloud_y.dominoext.classloaders.DominoClassLoaderBuilder;
import com.cloud_y.dominoext.classloaders.DominoClassLoaderManager;
import com.cloud_y.dominoext.classloaders.ExtendedClassLoaderException;
import com.cloud_y.dominoext.classloaders.ParentLastClassLoader;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

@DisabledIfSystemProperty(named = "testClassLoader",matches = "false")
public class TestDominoClassLoaderManager extends AbstractDominoTest {

    private void dumpClasspath(ClassLoader loader) {

	if (loader instanceof ParentLastClassLoader) {
	    System.out.println("Classloader " + loader + ":");
	    ParentLastClassLoader ucl = (ParentLastClassLoader) loader;

	    URL[] urls = ucl.getURLs();

	    for (URL url : urls) {
		System.out.println("\t" + url.toExternalForm());
	    }

	} else if (loader instanceof URLClassLoader) {
	    System.out.println("Classloader " + loader + ":");
	    URLClassLoader ucl = (URLClassLoader) loader;

	    URL[] urls = ucl.getURLs();

	    for (URL url : urls) {
		System.out.println("\t" + url.toExternalForm());
	    }
	} else
	    System.out.println("\t(cannot display components as not a known type): " + loader.getClass().getName());

	if (loader.getParent() != null)
	    dumpClasspath(loader.getParent());
    }

    @Test
    public void testClassLoaderManager(@TempDir Path tmpDir)
	    throws NotesException, ExtendedClassLoaderException, IOException, ClassNotFoundException {

	System.out.println(tmpDir.toString());

	Session session = NotesFactory.createSession();
	Database db = session.getDatabase("", "dev/smartcloud-y/appdev-open.nsf");

	DominoClassLoaderManager manager = DominoClassLoaderManager.getInstance();

	DominoClassLoaderBuilder builder = manager.newBuilder(session, db);

	builder.addScriptLibrary("dominoext-e2e-runnable", true);
	builder.addScriptLibrary("enhanced-agent-demos", true);
	builder.addScriptLibrary("mssql-driver", false);
	builder.addScriptLibrary("slf4j-domino", false);

	ClassLoader originalClassLoader = getClass().getClassLoader();
	ParentLastClassLoader extClassLoader = builder.buildAndGetClassLoader(originalClassLoader);
	assertNotNull(extClassLoader);

	Thread.currentThread().setContextClassLoader(extClassLoader);

	dumpClasspath(extClassLoader);

	assertNotNull(Class.forName("dominoExtE2eRunnable", true, extClassLoader));
	assertNotNull(Class.forName("DemoSyncMSSQL", true, extClassLoader));
	assertNotNull(Class.forName("lotus.domino.Database", true, extClassLoader));

	Thread.currentThread().setContextClassLoader(originalClassLoader);

	extClassLoader.close();

    }

}
