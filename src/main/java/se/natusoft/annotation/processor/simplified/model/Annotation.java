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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Wraps an AnnotationMirror.
 */
public class Annotation {
    //
    // Private Members
    //

    /** The mirror image of the annotation. */
    private AnnotationMirror annotationMirror;

    //
    // Constructors
    //

    /**
     * Creates a new Annotation.
     *
     * @param annotationMirror The AnnotationMirror to wrap.
     */
    public Annotation(AnnotationMirror annotationMirror) {
        this.annotationMirror = annotationMirror;
    }

    //
    // Methods
    //

    /**
     * Returns the value for named annotation method.
     *
     * @param name The annotation method to get the value for.
     */
    public AnnotationValue getAnnotationValueFor(String name) {
        AnnotationValue value = null;

        for (ExecutableElement executableElement : this.annotationMirror.getElementValues().keySet()) {
            if (executableElement.getSimpleName().contentEquals(name)) {
                value = this.annotationMirror.getElementValues().get(executableElement);
                break;
            }
        }

        // If value still is null we have to resolve the default value.
        if (value == null) {
            TypeElement annotationTypeElement = (TypeElement)this.annotationMirror.getAnnotationType().asElement();
            Type annotationType = new Type(annotationTypeElement);
            Executable annotationMethod = annotationType.getMethodByName(name);
            value = annotationMethod.getAnnotationDefaultValue();
        }

        return value;
    }

    /**
     * Returns the value for named annotation method.
     *
     * @param name The annotation method to get the value for.
     */
    public AObject getValueFor(String name) {
        AnnotationValue av = getAnnotationValueFor(name);
        if (av != null) {
            return new AObject(av.getValue());
        }

        return null;
    }

    //
    // Inner Classes
    //

    public static class AObject {
        //
        // Private Members
        //

        private Object object;

        //
        // Constructors
        //

        public AObject(Object object) {
            this.object = object;
        }

        //
        // Methods
        //

        public Object toObject() {
            return this.object;
        }

        public String toString() {
            return this.object.toString();
        }

        public int toInt() {
            return (Integer)this.object;
        }

        public long toLong() {
            return (Long)this.object;
        }

        public float toFloat() {
            return (Float)this.object;
        }

        public double toDouble() {
            return (Double)this.object;
        }

        public boolean toBoolean() {
            return (Boolean)this.object;
        }

        public byte toByte() {
            return (Byte)this.object;
        }

        public char toChar() {
            return (Character)this.object;
        }
    }
}
