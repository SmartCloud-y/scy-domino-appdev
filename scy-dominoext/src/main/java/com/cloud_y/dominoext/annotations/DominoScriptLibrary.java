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
package com.cloud_y.dominoext.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;

/**
 * @author Daniele Vistalli <daniele.vistalli@factor-y.com>
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(DominoScriptLibraries.class)
public @interface DominoScriptLibrary {

    /**
     * The name of a Java Script library in a domino database to be added to
     * the class path as required dependency
     * @return the name of the Java Script library
     */
    String libraryName() default "";
    
    
    /**
     * Defines if java code in a shared library (local java classes, non artifact) 
     * need to be added to the Classpath.
     * 
     * @return true if the "local" java code must be added to classpath
     */
    boolean useLocalCode() default false;
    
}
