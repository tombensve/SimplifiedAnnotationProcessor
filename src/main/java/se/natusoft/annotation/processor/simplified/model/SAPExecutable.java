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

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * This wraps an ExecutableElement and represents a constructor or method.
 */
public class SAPExecutable extends SAPBaseElement {
    //
    // Cosntructors
    //

    /**
     * Creates a new SAPExecutable.
     *
     * @param element The element representing the field.
     */
    public SAPExecutable(Element element) {
        super(element);
    }

    /**
     * Creates a new SAPExecutable.
     *
     * @param element The element representing the field.
     * @param parent The parent element,
     */
    public SAPExecutable(Element element, Element parent) {
        super(element, parent);
    }

    //
    // Methods
    //

    /**
     * Returns the element as a VariableElement.
     */
    public ExecutableElement getExecutable() {
        return (ExecutableElement)super.getElement();
    }

    /**
     * Returns the generic type parameters.
     */
    public List<? extends TypeParameterElement> getTypeParameters() {
        return getExecutable().getTypeParameters();
    }

    /**
     * Returns the return type of the executable.
     */
    public String getReturnTypeAsString() {
        return getExecutable().getReturnType().toString();
    }

    /**
     * Returns parameters.
     */
    public List<SAPVariable> getParameters() {
        List<SAPVariable> parameters = new ArrayList<SAPVariable>();

        for (VariableElement elem : getExecutable().getParameters()) {
            parameters.add(new SAPVariable(elem, getElement()));
        }

        return parameters;
    }

    /**
     * Returns the throws list as a list of names.
     */
    public List<String> getThrowsListAsStringList() {
        List<String> throwsList = new ArrayList<String>();

        for (TypeMirror typeMirror : getExecutable().getThrownTypes()) {
            throwsList.add(typeMirror.toString());
        }

        return throwsList;
    }

    /**
     * This only applies to annotation methods.
     * <p>
     * Returns the default value for the annotation.
     */
    public AnnotationValue getAnnotationDefaultValue() {
        return getExecutable().getDefaultValue();
    }

    /**
     * This only applies to annotation methods.
     * <p>
     * Convenience method for getAnnotationDefaultValue().toString().
     */
    public String getAnnotationValueAsString() {
        AnnotationValue annValue = getExecutable().getDefaultValue();
        return annValue.toString();
    }

    /**
     * Returns the parent as a SAPType.
     */
    public SAPType getParentType() {
        return new SAPType(getParent());
    }
}
