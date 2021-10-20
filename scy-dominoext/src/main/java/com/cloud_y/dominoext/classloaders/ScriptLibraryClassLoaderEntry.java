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

import java.net.URL;
import java.util.Date;

public class ScriptLibraryClassLoaderEntry {
    
    private String replicaId;
    private String libraryName;
    private String libraryDesignElementNoteId;
    private Date libraryDesignElementLastUpdate;
    private URL[] urls;
    
    public ScriptLibraryClassLoaderEntry(URL[] urlsArray, String libraryName) {
	this.urls = urlsArray;
	this.libraryName = libraryName;
    }

    public String getReplicaId() {
        return replicaId;
    }
    public void setReplicaId(String replicaId) {
        this.replicaId = replicaId;
    }
    public String getLibraryName() {
        return libraryName;
    }
    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }
    public String getLibraryDesignElementNoteId() {
        return libraryDesignElementNoteId;
    }
    public void setLibraryDesignElementNoteId(String libraryDesignElementNoteId) {
        this.libraryDesignElementNoteId = libraryDesignElementNoteId;
    }
    public Date getLibraryDesignElementLastUpdate() {
        return libraryDesignElementLastUpdate;
    }
    public void setLibraryDesignElementLastUpdate(Date libraryDesignElementLastUpdate) {
        this.libraryDesignElementLastUpdate = libraryDesignElementLastUpdate;
    }

    public URL[] getUrls() {
	return urls;
    }

}
