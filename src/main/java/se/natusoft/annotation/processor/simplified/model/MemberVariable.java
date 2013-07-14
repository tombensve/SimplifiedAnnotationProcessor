/* 
 * 
 * PROJECT
 *     Name
 *         SimplifiedAnnotationProcessor
 *     
 *     Code Version
 *         1.0
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
 *
 */
package se.natusoft.annotation.processor.simplified.model;

import javax.lang.model.element.Element;

/**
 *
 */
public class MemberVariable extends Variable {

    //
    // Cosntructors
    //

    /**
     * Creates a new Variable.
     *
     * @param element The element representing the field.
     */
    public MemberVariable(Element element) {
        super(element);
    }

    //
    // Methods
    //

    /**
     * Returns the type this member is a member of.
     */
    public Type getMemberOf() {
        return new Type(getElement().getEnclosingElement());
    }
}
