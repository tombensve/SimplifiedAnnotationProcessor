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
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2013-07-15: Created!
 *         
 */
package se.natusoft.annotation.processor.simplified.model;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;

/**
 * This wraps a VariableElement and represents a field or parameter.
 * <p>
 * For a field consider using the MemberVariable subclass instead!
 */
public class Variable extends BaseElement {
    //
    // Cosntructors
    //

    /**
     * Creates a new Variable.
     *
     * @param element The element representing the field.
     */
    public Variable(Element element) {
        super(element);
    }

    //
    // Methods
    //

    /**
     * Returns the element as a VariableElement.
     */
    public VariableElement getVariable() {
        return (VariableElement)super.getElement();
    }

    /**
     * Returns true if this is an enum constant.
     */
    public boolean isEnumConstant() {
        return getVariable().getKind() == ElementKind.ENUM_CONSTANT;
    }

    /**
     * Returns the constant value if this is a contant.
     */
    public Object getConstantValue() {
        return getVariable().getConstantValue();
    }

    public String getTypeAsString() {
        return getVariable().asType().toString();
    }


}
