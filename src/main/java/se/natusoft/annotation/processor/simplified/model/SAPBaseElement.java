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
package se.natusoft.annotation.processor.simplified.model;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Base class wrapping Element.
 */
public class SAPBaseElement {
    //
    // Private Members
    //

    /** The base element. */
    private Element element = null;

    /** The parent of the element if any. */
    private Element parent = null;

    //
    // Constructor
    //

    /**
     * Creates a new SAPBaseElement.
     *
     * @param element The element to wrap.
     */
    public SAPBaseElement(Element element) {
        this.element = element;
    }

    /**
     * Creates a new SAPBaseElement.
     *
     * @param element The element to wrap.
     */
    public SAPBaseElement(Element element, Element parent) {
        this(element);
        this.parent = parent;
    }

    //
    // Methods
    //

    /**
     * Returns the element.
     */
    protected Element getElement() {
        return this.element;
    }

    /**
     * Returns the parent element.
     */
    public Element getParent() {
        return this.parent;
    }

    /**
     * Returns true if this element has a parent element.
     */
    public boolean hasParent() {
        return this.parent != null;
    }

    /**
     * Returns the simple name as a String.
     */
    public String getSimpleName() {
        return this.element.getSimpleName().toString();
    }

    /**
     * Returns the kind of element.
     */
    public ElementKind getKind() {
        return this.element.getKind();
    }

    /**
     * Returns true if there are annotations avialable.
     */
    public boolean hasAnnotations() {
        return !this.element.getAnnotationMirrors().isEmpty();
    }

    /**
     * Returns any annotations.
     */
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return this.element.getAnnotationMirrors();
    }

    /**
     * Returns the annotations as Elements.
     */
    public List<TypeElement> getAnnotationsAsElementFromMirror() {
        List<TypeElement> annotations = new ArrayList<TypeElement>();
        for (AnnotationMirror am : getAnnotationMirrors()) {
            TypeElement annElement = (TypeElement)am.getAnnotationType().asElement();
            annotations.add(annElement);
        }

        return annotations;
    }

    /**
     * Returns named annotation.
     *
     * @param annotation A fully qualified name of the annotation.
     */
    public AnnotationMirror getAnnotationMirrorByName(String annotation) {
        AnnotationMirror annotationMirror = null;

        for (AnnotationMirror am : getAnnotationMirrors()) {
            if (am.getAnnotationType().toString().equals(annotation)) {
                annotationMirror = am;
            }
            else if (((TypeElement)am.getAnnotationType().asElement()).getSimpleName().contentEquals(annotation)) {
                annotationMirror = am;
                break;
            }
        }

        return annotationMirror;
    }

    /**
     * Returns the annotations as wrapped SAPAnnotation objects.
     */
    public List<SAPAnnotation> getAnnotations() {
        List<SAPAnnotation> annotations = new ArrayList<SAPAnnotation>();

        for (AnnotationMirror annotationMirror : getAnnotationMirrors()) {
            SAPAnnotation annotation = new SAPAnnotation(annotationMirror, this.element);
            annotations.add(annotation);
        }

        return annotations;
    }

    /**
     * Returns an SAPAnnotation wrapper for the specified annotation.
     *
     * @param annotation The annotation name to get the SAPAnnotation wrapper for.
     */
    public SAPAnnotation getAnnotationByName(String annotation) {
        SAPAnnotation ann = null;

        AnnotationMirror annMirror = getAnnotationMirrorByName(annotation);
        if (annMirror != null) {
            ann = new SAPAnnotation(annMirror, this.element);
        }

        return ann;
    }

    /**
     * Returns the specified annotation or null if not available.
     *
     * @param annClass The annotation class to get.
     *
     * @return
     */
    public SAPAnnotation getAnnotationByClass(Class annClass) {
        return getAnnotationByName(annClass.getName());
    }


    @Override
    public int hashCode() {
        return this.element.getSimpleName().hashCode() + this.element.getKind().toString().hashCode() + this.element.asType().toString().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof SAPBaseElement)) {
            return false;
        }

        SAPBaseElement bmObject = (SAPBaseElement)object;
        return this.element.getSimpleName().equals(bmObject.element.getSimpleName()) &&
                this.element.getKind() == bmObject.getKind() &&
                this.element.asType().toString().equals(bmObject.element.asType().toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("simpleName: ");
        sb.append(this.element.getSimpleName());
        sb.append(", kind: ");
        sb.append(this.element.getKind().toString());
        sb.append(", type: ");
        sb.append(this.element.asType().toString());

        return sb.toString();
    }
}
