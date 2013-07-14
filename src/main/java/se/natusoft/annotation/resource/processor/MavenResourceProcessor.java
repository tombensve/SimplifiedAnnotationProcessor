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
 */
package se.natusoft.annotation.resource.processor;

import java.io.File;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import se.natusoft.annotation.processor.simplified.annotations.AutoDiscovery;

/**
 * This processor validates that the specified classpath resource is available on the classpath.
 */
@AutoDiscovery
@SupportedAnnotationTypes("se.natusoft.annotation.resource.MavenResource")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class MavenResourceProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (!roundEnv.processingOver()) {
            // We only have one annotation, but this is still easier.
            for (TypeElement annotationType : annotations) {
                for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotationType)) {
                    // Loops through the annotations on the annotated element. At least one should be our annotation.
                    for (AnnotationMirror annotation : annotatedElement.getAnnotationMirrors()) {
                        // Check if the current annotation has our annotation type.
                        if (annotation.getAnnotationType().asElement().equals(annotationType)) {
                            // Find annotation value.
                            AnnotationValue value = null;

                            for (ExecutableElement executableElement :annotation.getElementValues().keySet()) {
                                if (executableElement.getSimpleName().contentEquals("value")) {
                                    value = annotation.getElementValues().get(executableElement);
                                    break;
                                }
                            }

                            // Lets validate the the pointed to path is available.
                            File resourceDir = new File("src/main/resources");
                            File resourceFile = new File(resourceDir, value.getValue().toString());
                            if (!resourceFile.exists()) {
                                // Kind.ERROR will cause a compilation failure!
                                super.processingEnv.getMessager().printMessage(Kind.ERROR, "The specified resource (" + value.getValue() + ") is not available!", annotatedElement, annotation, value);
                            }
                        }
                    }
                }
            }
        } 

        return true; // We always process our annotation.
    }
}
