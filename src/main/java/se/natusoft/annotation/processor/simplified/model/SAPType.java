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

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * Wraps a TypeElement and represents a type.
 */
public class SAPType extends SAPBaseElement {
    //
    // Private Members
    //

    /** Utils provided with java.lang.model. */
    public static Elements elementUtils = null;

    //
    // Constructors
    //

    /**
     * Creates a new SAPType instance.
     *
     * @param typeElement The TypeElement to wrap.
     */
    public SAPType(Element typeElement) {
        super(typeElement);
    }

    //
    // Methods
    //

    /**
     * Returns the element as a TypeElement.
     */
    public TypeElement getTypeElement() {
        return (TypeElement)super.getElement();
    }

    /**
     * Returns the qualified name as a String.
     */
    public String getQualifiedName() {
        return getTypeElement().getQualifiedName().toString();
    }

    /**
     * Returns the type this type extends.
     */
    public String getExtends() {
        return getTypeElement().getSuperclass().toString();
    }

    /**
     * Returns the type this type extends without eventual package part.
     */
    public String getExtendsSimpleName() {
        String ext = getExtends();
        int ix = ext.lastIndexOf('.');
        if (ix >= 0) {
            ext = ext.substring(ix + 1);
        }

        return ext;
    }

    /**
     * Returns the interfaces implemented by this type.
     */
    public List<SAPType> getInterfaces() {
        List<SAPType> ifs = new LinkedList<SAPType>();

        for (TypeMirror tm : getTypeElement().getInterfaces()) {
            DeclaredType dt = (DeclaredType)tm;
            ifs.add(new SAPType(dt.asElement()));
        }

        return ifs;
    }

    /**
     * Returns the package.
     */
    public String getPackage() {
        String qualifiedName = getQualifiedName();
        String pkg = qualifiedName.substring(0, qualifiedName.length() - getSimpleName().length() - 1);

        // Only include the parts up to a part that starts with an uppercase character. That is, stop at a class.
        // This will happen when an annotated class is an inner class. In that case we only want the package of
        // the top level class. We do this by splitting on '.' and checking for an uppercase char at the beginning
        // of every part. A found uppercase char breaks the loop and does thus not append its part.
        String[] parts = pkg.split("\\.");
        StringBuilder pkgBuilder = new StringBuilder();
        String dot = "";
        for (String part : parts) {
            if (Character.isUpperCase(part.charAt(0))) {
                break;
            }
            pkgBuilder.append(dot);
            pkgBuilder.append(part);
            dot = ".";
        }

        return pkgBuilder.toString();
    }

    /**
     * Returns the fields.
     */
    public List<SAPVariable> getFields() {
        List<SAPVariable> fields = new ArrayList<SAPVariable>();

        for (Element elem : getTypeElement().getEnclosedElements()) {
            if (elem.getKind().isField()) {
                fields.add(new SAPVariable(elem, getElement()));
            }
        }

        return fields;
    }

    /**
     * Returns the constructors.
     */
    public List<SAPExecutable> getConstructors() {
        List<SAPExecutable> constructors = new ArrayList<SAPExecutable>();

        for (Element elem : getTypeElement().getEnclosedElements()) {
            if (elem.getKind() == ElementKind.CONSTRUCTOR) {
                constructors.add(new SAPExecutable(elem, getElement()));
            }
        }

        return constructors;
    }

    /**
     * Returns the methods.
     */
    public List<SAPExecutable> getMethods() {
        List<SAPExecutable> methods = new ArrayList<SAPExecutable>();

        for (Element elem : getTypeElement().getEnclosedElements()) {
            if (elem.getKind() == ElementKind.METHOD) {
                methods.add(new SAPExecutable(elem, getElement()));
            }
        }

        return methods;
    }

    /**
     * Returns all methods including inherited.
     */
    public List<SAPExecutable> getAllMethods() {
        List<SAPExecutable> methods = new ArrayList<SAPExecutable>();

        for (Element elem : elementUtils.getAllMembers(getTypeElement())) {
            if (elem.getKind() == ElementKind.METHOD) {
                methods.add(new SAPExecutable(elem, getElement()));
            }
        }

        return methods;
    }

    /**
     * Returns a method by name.
     *
     * @param name The name of the  method to get.
     */
    public SAPExecutable getMethodByName(String name) {
        SAPExecutable method = null;

        for (SAPExecutable executable : getAllMethods()) {
            //System.out.println("#### if (\"" + executable.getSimpleName() + "\".equals(\"" + name + "\") = " + executable.getSimpleName().equals(name));
            if (executable.getSimpleName().equals(name)) {
                method = executable;
                break;
            }
        }

        return method;
    }

    /**
     * Returns the generic type parameters.
     */
    public List<? extends TypeParameterElement> getTypeParameters() {
        return getTypeElement().getTypeParameters();
    }

    /**
     * Returns the inner interfaces.
     */
    public List<SAPType> getInnerInterfaces() {
        List<SAPType> types = new ArrayList<SAPType>();

        for (Element element : super.getElement().getEnclosedElements()) {
            if (element.getKind() == ElementKind.INTERFACE) {
                types.add(new SAPType(element));
            }
        }

        return types;
    }

    public void printType() {
        elementUtils.printElements(new OutputStreamWriter(System.out), getTypeElement());
    }

    @Override
    public int hashCode() {
        return getTypeElement().getQualifiedName().hashCode() + getTypeElement().getKind().toString().hashCode() + getTypeElement().asType().toString().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof SAPType)) {
            return false;
        }

        TypeElement typeElement = getTypeElement();
        TypeElement bmObject = ((SAPType)object).getTypeElement();
        return typeElement.getQualifiedName().equals(bmObject.getQualifiedName()) &&
                typeElement.getKind() == bmObject.getKind() &&
                typeElement.asType().toString().equals(bmObject.asType().toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        TypeElement typeElement = getTypeElement();
        sb.append("simpleName: ");
        sb.append(typeElement.getQualifiedName());
        sb.append(", kind: ");
        sb.append(typeElement.getKind().toString());
        sb.append(", type: ");
        sb.append(typeElement.asType().toString());

        return sb.toString();
    }

}
