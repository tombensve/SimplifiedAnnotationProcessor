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
package se.natusoft.annotation.resource.processor;

import java.io.File;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;
import se.natusoft.annotation.processor.simplified.annotations.AutoDiscovery;

/**
 * This processor validates that the specified classpath resource is available on the classpath.
 */
@AutoDiscovery
@SupportedAnnotationTypes("se.natusoft.annotation.resource.ResourcePath")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ResourcePathProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (!roundEnv.processingOver()) {
            // We only have one annotation, but this is still easier.
            for (TypeElement annotationType : annotations) {
                for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotationType)) {
                    // Our annotation is only valid on a field so we can asume that our element
                    // represents a field, but just to show how we check:
                    if (annotatedElement.getKind() == ElementKind.FIELD) {
                        VariableElement field = (VariableElement)annotatedElement;
                        // Out annotation only makes sense on a constant value.
                        if (checkModifier(field.getModifiers(), Modifier.STATIC) && checkModifier(field.getModifiers(), Modifier.FINAL)) {
                            // Now we know it is a constant.
                            String path = field.getConstantValue().toString();
                            boolean found = checkInClasspath(path);
                            if (!found) {
                                found = checkInMavenResourcePath(path);
                            }
                            if (!found) {
                                // Since we use Kind.ERROR here this will cause a compilation failure with our message.
                                super.processingEnv.getMessager().printMessage(Kind.ERROR, "The '" + path + "' path is not available!", field);
                            }
                        }
                        else {
                            super.processingEnv.getMessager().printMessage(Kind.ERROR, "@ResourcePath is only allowed on constants!", field);
                        }
                    }
                }
            }
        } 

        return true; // We always process our annotation.
    }

    private boolean checkModifier(Set<Modifier> modifiers, Modifier check) {
        boolean found = false;

        for (Modifier modifier : modifiers) {
            if (check == modifier) {
                found = true;
                break;
            }
        }

        return found;
    }

    private boolean checkInClasspath(String path) {
        if (ClassLoader.getSystemResource(path) != null) {
            return true;
        }
        return false;
    }

    private boolean checkInMavenResourcePath(String path) {
        // If it is a maven comile that calls us then the maven project root will be the current directory.
        File resource = new File("src/main/resources");
        resource = new File(resource, path);
        return resource.exists();
    }
}
