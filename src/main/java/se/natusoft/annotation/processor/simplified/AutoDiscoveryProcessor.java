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
package se.natusoft.annotation.processor.simplified;

import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import se.natusoft.annotation.processor.simplified.codegen.GenerationSupport.ResourceReference;
import se.natusoft.annotation.processor.simplified.annotations.AutoDiscovery;
import se.natusoft.annotation.processor.simplified.annotations.ProcessedAnnotations;
import se.natusoft.annotation.processor.simplified.annotations.Process;
import se.natusoft.annotation.processor.simplified.codegen.GenerationSupport;

/**
 * This automatically updates the META-INF/services/javax.annotation.processing.Processor with the fully qualified processor name.
 */
@ProcessedAnnotations({AutoDiscovery.class})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AutoDiscoveryProcessor extends SimplifiedAnnotationProcessor {
    //
    // Constants
    //

    private static final String PROCESSOR_DISCOVERY_FILE = "javax.annotation.processing.Processor";
    private static final String RELATIVE_PROCESSOR_DISCOVERY_PATH = "META-INF/services";

    //
    // Processing Methods
    //

    @Process(AutoDiscovery.class)
    public void processAD(Set<? extends Element> annotatedElements) {
        System.out.println("@AutoDiscovery: Updating " + RELATIVE_PROCESSOR_DISCOVERY_PATH + "/" + PROCESSOR_DISCOVERY_FILE +
                " with the following processors:");
        for (Element annotatedElement : annotatedElements) {
            TypeElement type = (TypeElement)annotatedElement; // @AutoDiscovery can only be applied to types!
            String processor = type.getQualifiedName().toString();
            System.out.println("    " + processor);
            updateJavaxAnnotationProcessinProcessors(processor);
        }
    }

    private void updateJavaxAnnotationProcessinProcessors(String qualifiedProcessorName) {
        GenerationSupport genSupport = getGenerationSupport();
        ResourceReference resourceRef = genSupport.getBestEffortResourceReference(
                RELATIVE_PROCESSOR_DISCOVERY_PATH + "/" + PROCESSOR_DISCOVERY_FILE,
                new String[] {"src/main/processors"}
        );

        String processors = null;
        try {
            processors = resourceRef.readResourceAsString();
        }
        catch (IOException ioe) {
            processors = "";
        }

        if (!processors.contains(qualifiedProcessorName)) {
            processors = processors + qualifiedProcessorName + "\n";
            try {
                resourceRef.writeResourceFromString(processors);
            }
            catch (IOException ioe) {
                failCompile("Failed to update " + RELATIVE_PROCESSOR_DISCOVERY_PATH + "/" + PROCESSOR_DISCOVERY_FILE + "! [" + ioe.getMessage() + "]");
            }
        }
    }

}
