/* 
 * 
 * PROJECT
 *     Name
 *         SimplifiedAnnotationProcessor
 *     
 *     Code Version
 *         1.1
 *     
 *     Description
 *         An abstract annotation processor base class that simplifies the annotation
 *         processing, but also limits it slightly. It is however good enough for most
 *         cases and makes things a bit easier and clearer.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2013 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2013-07-15: Created!
 *         
 */
package se.natusoft.annotation.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be used on String constants that points out a classpath resource, and have a
 * processor that compile-time verifies that the resource does exist. This will first check in the compile
 * classpath, and if not found then check for a file under src/main/resources which will work if built by
 * maven. The last is probably redundant since I think maven includes the resources on the classpath during
 * compile. Note that this takes the resource from the String constant. This annotation is probably more useful
 * than @MavenResource.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface ResourcePath {}
