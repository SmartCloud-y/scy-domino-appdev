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

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A parent-last classloader that will try the child classloader first and then
 * the parent. This takes a fair bit of doing because java really prefers
 * parent-first.
 * 
 * For those not familiar with class loading trickery, be wary
 */
public class ParentLastClassLoader extends ClassLoader implements Closeable {

    private ChildURLClassLoader childClassLoader;
    private URL[] urls;

    public ParentLastClassLoader(URL[] urls, ClassLoader parent) {
	super(Thread.currentThread().getContextClassLoader());
	this.urls = urls;
	childClassLoader = new ChildURLClassLoader(urls, new FindClassClassLoader(parent), this);
    }

    public ParentLastClassLoader(List<ScriptLibraryClassLoaderEntry> libraries, ClassLoader parent) {
	super(Thread.currentThread().getContextClassLoader());
	
	System.out.println("Collecting urls for classloader");

	ArrayList<URL> urlsToAdd = new ArrayList<>();
	
	libraries.forEach(lib -> {
	    urlsToAdd.addAll(Arrays.asList(lib.getUrls()));
	});
	
	urlsToAdd.forEach(url -> {
	    System.out.println(url);
	});

	System.out.println("Setting up");

	this.urls = urlsToAdd.toArray(new URL[] {});

	childClassLoader = new ChildURLClassLoader(urls, new FindClassClassLoader(parent), this);
    }

    /**
     * This class allows me to call findClass on a classloader
     */
    private static class FindClassClassLoader extends ClassLoader {
	public FindClassClassLoader(ClassLoader parent) {
	    super(parent);
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
	    return super.findClass(name);
	}
    }

    /**
     * This class delegates (child then parent) for the findClass method for a
     * URLClassLoader. We need this because findClass is protected in URLClassLoader
     */
    private static class ChildURLClassLoader extends URLClassLoader {
	private FindClassClassLoader realParent;

	public ChildURLClassLoader(URL[] urls, FindClassClassLoader realParent,
		ParentLastClassLoader parentLastURLClassLoader) {
	    super(urls, parentLastURLClassLoader);

	    this.realParent = realParent;
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
	    try {
		Class<?> loaded = super.findLoadedClass(name);
		if (loaded != null)
		    return loaded;
		// first try to use the URLClassLoader findClass
		return super.findClass(name);
	    } catch (ClassNotFoundException e) {
		// if that fails, we ask our real parent classloader to load the
		// class (we give up)
		return realParent.loadClass(name);
	    }
	}
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
	try {

	    // first we try to find a class inside the child classloader
	    return childClassLoader.findClass(name);
	} catch (ClassNotFoundException e) {
	    // didn't find it, try the parent
	    return super.loadClass(name, resolve);
	}
    }

    public void close() throws IOException {
	// Close the child URL classloader
	childClassLoader.close();
    }

    public URL[] getURLs() {
	return urls;
    }
}