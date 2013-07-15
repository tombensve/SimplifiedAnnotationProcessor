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
package se.natusoft.annotation.processor.simplified;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import se.natusoft.annotation.processor.simplified.codegen.GenerationSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import se.natusoft.annotation.processor.simplified.annotations.AllProcessed;
import se.natusoft.annotation.processor.simplified.annotations.GenerateSource;
import se.natusoft.annotation.processor.simplified.annotations.NewRound;
import se.natusoft.annotation.processor.simplified.annotations.Process;
import se.natusoft.annotation.processor.simplified.annotations.ProcessedAnnotations;
import se.natusoft.annotation.processor.simplified.model.Type;

/**
 * This extends the AbstractProcessor and provides a simple API for subclasses for processing annotations.
 * <p>
 * Subclasses of this should look like this:
 * <pre>
 *
 *  import javax.annotation.processing.SupportedSourceVersion;
 *  import javax.lang.model.SourceVersion;
 *  import javax.lang.model.element.Element;
 *  import javax.lang.model.element.ElementKind;
 *  import javax.lang.model.element.TypeElement;
 *  import se.natusoft.annotation.processor.simplified.SimplifiedAnnotationProcessor;
 *  import se.natusoft.annotation.processor.simplified.GenerationSupport;
 *  import se.natusoft.annotation.processor.simplified.annotations.Process;
 *  import se.natusoft.annotation.processor.simplified.annotations.AllProcessed;
 *  import se.natusoft.annotation.processor.simplified.annotations.GenerateSource;
 *  import se.natusoft.annotation.processor.simplified.annotations.NewRound;
 *  import se.natusoft.annotation.processor.simplified.annotations.ProcessedAnnotations;
 *
 * &nbsp;@ProcessedAnnotations({MyAnnotation.class})
 * &nbsp;@SupportedSourceVersion(SourceVersion.RELEASE_6)
 *  public class MyAnnotationProcessor extends SimplifiedAnnotationProcessor {
 *
 *      // Gets called for each new processing round for clearing any already processed data.
 *      // Please note that only the first annotated method found will be called! Only one
 *      // methods should be annotated with @NewRound.
 *     &nbsp;@NewRound 
 *      public void newRound() { 
 *         
 *      }
 *
 *      // Gets called for processing the specified annotation per round.
 *      // annotation - This represents the actual annotation being processed (this parameter can be skipped!).
 *      // annotatedElements - All language elements annotated with the annotation.
 *     &nbsp;@Process(MyAnnotation.class)
 *      public processMyAnn(TypeElement annotation, Set&lt;? extends Element&gt; annotatedElements) {
 *
 *      }
 *
 *      // Called after all processing has been done for the round. Will only be called if anything has been processed.
 *      // All methods annotated with @Generate will be called in order. This is a convenience for when you want to
 *      // generate more than one file and wants to do it from separate methods. You can of course let your annotated
 *      // method call other methods to do the job. As said, multiple @GenerateSource annotated methods is only a convenicence.
 *     &nbsp;@GenerateSource 
 *      public void generate(GenerationSupport generationSupport) {
 *
 *      }
 *
 *      // Called after all rounds are done. This annotation can also as a convenience be specified on several methods.
 *      // In that case the methods will be called in order. In addition to possibly doing cleanup you might want to generare
 *      // resources that require input from all rounds of processing here. In that case you could have a generateMyResource()
 *      // method annotated with @AllProcessed and a cleanup() or done() or whatever method also annotated with @AllProcessed.
 *     &nbsp;@AllProcessed 
 *      public void done() {
 *
 *      }
 *  }
 * </pre>
 * All but @Process are optional! An @Process must be available for each annotation specified with @ProcessedAnnotations or @SupportedAnnotationTypes.
 * <p>
 * Please note that the @GenerateSource method can either take no arguments or an GenerationSupport instance. If it takes no arguments
 * the GenerationSupport object can be gotten with getGenerationSupport().
 * <p>
 * Use the failCompile(...) method of this base class to cause a compilation failure. failCompile() and all the print*() methods simply passes
 * information to the compiler which will deal with them later.
 * <p>
 * The se.natusoft.annotation.processor.simplified.model contains the following classes:
 * <pre>
 *   Annotation     - Wraps an AnnotationMirror
 *   BaseElement    - Baseclass for all other in this package.
 *   Executable     - Wraps ExecutableElement (constructors and methods)
 *   Variable       - Wraps VariableElement (field, parameter, etc)
 *   MemberVariable - Supclass of Variable and provides getter for the Type the member is part of.
 *   Type           - Wraps TypeElement (Class, Interace, Enum)
 * </pre>
 * These wrappers of javax.lang.model.element.* models makes it a little bit easier extracting information.
 */
public abstract class SimplifiedAnnotationProcessor<ProcessingContext> extends AbstractProcessor {
    //
    // Private Members
    //

    /** A local copy of the messager. */
    private Messager messager = null;

    /** This gets passed to @Generator method if it takes it, otherwise it can be fetched with a getter. */
    private GenerationSupport generationSupport = null;

    /** A local copy of the element utils. */
    private Elements elementUtils = null;

    /** A local copy of the type utils. */
    private Types typeUtils = null;

    /** If true some verbose information about processing is displayed. */
    private boolean verbose = false;
    
    //
    // Constructors
    //

    /**
     * Creates a new SimplifiedAnnotationProcessor.
     */
    protected SimplifiedAnnotationProcessor() {
        super();
    }
    
    /**
     * Creates a new SimplifiedAnnotationProcessor.
     *
     * @param verbose If true verbose information about processing is output.
     */
    protected SimplifiedAnnotationProcessor(boolean verbose) {
        super();
        this.verbose = verbose;
    }

    //
    // Methods
    //

    /**
     * This version of this getter looks for a ProcessedAnnotations annotation that works
     * exactly like the standard SupportedAnnotationTypes annotation, but taking and array
     * of annotation classes instead of string specifications for the annotations to process.
     * <p>
     * This ofcourse requires that the processed annotations are available in the classpath
     * during the build of the annotation processor, which SupportedAnnotationTypes does not.
     * String specifications however can easily be misspelled and thus fail. Specifying classes
     * gets compiler support in determining the correctness.
     * <p>
     * This method only deliver its result if it finds the ProcessedAnnotations on the processor
     * class. If not it passes the call on to the super class. This makes the use of ProcessedAnnotations
     * optional. If not used the base class will still look for SupportedAnnotationTypes.
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotations = new HashSet<String>();
        ProcessedAnnotations processedAnnotations = getClass().getAnnotation(ProcessedAnnotations.class);
        if (processedAnnotations != null) {
            for (Class annotationClass : processedAnnotations.value()) {
                supportedAnnotations.add(annotationClass.getName());
            }
        }
        else {
            supportedAnnotations = super.getSupportedAnnotationTypes();
        }

        return supportedAnnotations;
    }

    /**
     * Returns the element utils. 
     */
    protected Elements getElementUtils() {
        return this.elementUtils;
    }

    /**
     * Returns the type utils.
     */
    protected Types getTypeUtils() {
        return this.typeUtils;
    }

    /**
     * This shoud be used to generate code. If your @GenerateSource annotated method
     * takes a GenerationSupport instance a call to this method is unnecesarry.
     * If not use this method to get hold of the GenerationSupport.
     */
    protected GenerationSupport getGenerationSupport() {
        return this.generationSupport;
    }

    /**
     * Prints the text to stdout if verbose is true.
     *
     * @param text The text to print.
     */
    private void verbose(String text) {
        if (this.verbose) {
            System.out.println(text);
        }
    }

    /**
     * Returns the stacktrace of the specified exception as a String.
     * @param e The exception to get stacktrace for.
     */
    private String toStackTrace(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        ps.close();
        return baos.toString();
    }

    /**
     * Fails compilation with a message. Please note that this does not throw
     * any exception! Calling this method will not break the execution flow!
     * All it does is to provide an error message and in doing so it also
     * tells the compiler that the compilation has failed. The compiler will
     * handle this later.
     *
     * @param message The error message to provide.
     * @param element The element that caused the compilation failure. Passing this
     *                will give you a source name with line and column number like
     *                any other compilation failure.
     * @param e An exception to include in message.
     */
    protected void failCompile(String message, Element element, Throwable e) {
        if (e != null) {
            this.messager.printMessage(Kind.ERROR, message + "\n" + toStackTrace(e), element);
        }
        else {
            this.messager.printMessage(Kind.ERROR, message, element);
        }
    }

    /**
     * Fails compilation with a message. Please note that this does not throw
     * any exception! Calling this method will not break the execution flow!
     * All it does is to provide an error message and in doing so it also
     * tells the compiler that the compilation has failed. The compiler will
     * handle this later.
     *
     * @param message The error message to provide.
     * @param element The element that caused the compilation failure. Passing this
     *                will give you a source name with line and column number like
     *                any other compilation failure.
     */
    protected void failCompile(String message, Element element) {
        failCompile(message, element, null);
    }

    /**
     * Fails compilation with a message. Please note that this does not throw
     * any exception! Calling this method will not break the execution flow!
     * All it does is to provide an error message and in doing so it also
     * tells the compiler that the compilation has failed. The compiler will
     * handle this later.
     *
     * @param message The error message to provide.
     * @param e An exception to include in message.
     */
    protected void failCompile(String message, Throwable e) {
        if (e != null) {
            this.messager.printMessage(Kind.ERROR, message + "\n" + toStackTrace(e));
        }
        else {
            this.messager.printMessage(Kind.ERROR, message);
        }
    }

    /**
     * Fails compilation with a message. Please note that this does not throw
     * any exception! Calling this method will not break the execution flow!
     * All it does is to provide an error message and in doing so it also
     * tells the compiler that the compilation has failed. The compiler will
     * handle this later.
     *
     * @param message The error message to provide.
     */
    protected void failCompile(String message) {
        failCompile(message, (Exception)null);
    }

    /**
     * Provides a warning message to the compiler, which will handle it later.
     *
     * @param message The warning message to provide.
     * @param element The element that caused the warning. Passing this
     *                will give you a source name with line and column number like
     *                any other compiler message.
     */
    protected void printWarning(String message, Element element) {
        this.messager.printMessage(Kind.WARNING, message, element);
    }

    /**
     * Provides a warning message to the compiler, which will handle it later.
     *
     * @param message The warning message to provide.
     */
    protected void printWarning(String message) {
        this.messager.printMessage(Kind.WARNING, message);
    }

    /**
     * Provides an information/comment/note to the compiler, which will handle it later.
     *
     * @param message The note message to provide.
     * @param element The element that caused the note. Passing this
     *                will give you a source name with line and column number like
     *                any other compiler message.
     */
    protected void printNote(String message, Element element) {
        this.messager.printMessage(Kind.NOTE, message, element);
    }

    /**
     * Provides an information/comment/note to the compiler, which will handle it later.
     *
     * @param message The note message to provide.
     */
    protected void printNote(String message) {
        this.messager.printMessage(Kind.NOTE, message);
    }

    /**
     * Provides general information to the compiler, which will handle it later.
     *
     * @param message The message to provide.
     * @param element The element that caused this message. Passing this
     *                will give you a source name with line and column number like
     *                any other compiler message.
     */
    protected void printOther(String message, Element element) {
        this.messager.printMessage(Kind.OTHER, message, element);
    }

    /**
     * Provides general information to the compiler, which will handle it later.
     *
     * @param message The message to provide.
     */
    protected void printOther(String message) {
        this.messager.printMessage(Kind.OTHER, message);
    }

    /**
     * Copies parent stuff (that is not available until process() have been called)
     * locally, wrappingn some information in local support classes.
     */
    private void setupLocals() {
        this.messager = super.processingEnv.getMessager();
        this.generationSupport = new GenerationSupport(super.processingEnv.getFiler(), this.verbose);
        this.elementUtils = super.processingEnv.getElementUtils();
        this.typeUtils = super.processingEnv.getTypeUtils();
        Type.elementUtils = this.elementUtils;
    }

    /**
     * This is the real process() method called by AbstractProcessor. We handle this, and calls
     * appropriate subclass processing methods at appropriate times.
     *
     * @param annotations The annotations to process for this round.
     * @param roundEnv information about the round.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        setupLocals();

        boolean allAnnotationsHandled = true;

        if (!roundEnv.processingOver()) {
            verbose(getClass().getSimpleName() + " invoked:");

            // @NewRound
            Method newRoundMethod = findAnnotatedMethod(NewRound.class);
            if (newRoundMethod != null && newRoundMethod.getParameterTypes().length == 0) {
                try {
                    newRoundMethod.invoke(this, new Object[0]);
                }
                catch (Exception e) {
                    if (e.getCause() != null) {
                        Throwable cause = e.getCause();
                        failCompile("" + cause.getMessage(), cause);
                    }
                    failCompile("" + e.getMessage(), e);
                }
            }

            // @Process(x.class)
            int processed = 0;
            for (TypeElement annotationTypeElement : annotations) {
                ++processed;
                if (!process(annotationTypeElement, roundEnv)) {
                    allAnnotationsHandled = false;
                }
            }

            // @GenerateSource
            if (processed > 0) {
                List<Method> generateMethods = findAnnotatedMethods(GenerateSource.class);
                for (Method generateMethod : generateMethods) {
                    if (generateMethod != null) {
                        if (generateMethod.getParameterTypes().length == 1 && generateMethod.getParameterTypes()[0].equals(GenerationSupport.class)) {
                            try {
                                generateMethod.invoke(this, this.generationSupport);
                            }
                            catch (Exception e) {
                                if (e.getCause() != null) {
                                    Throwable cause = e.getCause();
                                    failCompile("" + cause.getMessage(), cause);
                                }
                                else {
                                    failCompile("" + e.getMessage(), e);
                                }
                            }
                        }
                        else if(generateMethod.getParameterTypes().length == 0) {
                            try {
                                generateMethod.invoke(this, new Object[0]);
                            }
                            catch (Exception e) {
                                if (e.getCause() != null) {
                                    Throwable cause = e.getCause();
                                    failCompile("" + cause.getMessage(), cause);
                                }
                                else {
                                    failCompile("" + e.getMessage(), e);
                                }
                            }
                        }
                        else {
                            failCompile("Found @Generate annotated method, but with wrong parameters! The @Generate method\n" +
                                    "should take 1 or 0 parameters: GenerationSupport or no parameter.");
                        }
                    }
                }
            }
        }
        else {
            verbose("    Done.\n");
            // @AllProcessed
            List<Method> allProcessedMethods = findAnnotatedMethods(AllProcessed.class);
            for (Method allProcessedMethod : allProcessedMethods) {
                if (allProcessedMethod != null && allProcessedMethod.getParameterTypes().length == 0) {
                    try {
                        allProcessedMethod.invoke(this, new Object[0]);
                    }
                    catch (Exception e) {
                        if (e.getCause() != null) {
                            Throwable cause = e.getCause();
                            failCompile("" + cause.getMessage(), cause);
                        }
                        else {
                            failCompile("" + e.getMessage(), e);
                        }
                    }
                }
            }
        }

        return allAnnotationsHandled;
    }
    
    /**
     * Support method to process a specific annotation type.
     *
     * @param annotationTypeElement The annotation type to process.
     * @param roundEnv information about the round.
     */
    protected boolean process(TypeElement annotationTypeElement, RoundEnvironment roundEnv) {
        boolean annotationHandled = false;

        Method processMethod = findProcessMethod(annotationTypeElement.getQualifiedName());
        if (processMethod != null) {
            if (processMethod.getParameterTypes().length == 1
                    && processMethod.getParameterTypes()[0].equals(Set.class)) {
                try {
                    Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(annotationTypeElement);
                    verbose("    @" + annotationTypeElement.getSimpleName() + " - Processing " + elementsAnnotatedWith.size() + " elements.");
                    Object ret = null;
                    if (processMethod.getParameterCount() == 1) {
                        ret = processMethod.invoke(this, elementsAnnotatedWith);
                    }
                    else if (processMethod.getParameterCount() == 2) {
                        ret = processMethod.invoke(this, annotationTypeElement, elementsAnnotatedWith);
                    }
                    else {
                        failCompile("@Process annotated method must take either (TypeElement, Set<? extends Element>) or " +
                            "(Set<? extends Element>)! [" + processMethod.toGenericString() + "]");
                    }
                    annotationHandled = true;
                }
                catch (Exception e) {
                    if (e.getCause() != null) {
                        Throwable cause = e.getCause();
                        failCompile("" + cause.getMessage(), annotationTypeElement, cause);
                    }
                    else {
                         failCompile("" + e.getMessage(), annotationTypeElement, e);
                     }
                }
            }
            else {
                failCompile("Found annotated processing method, but it does not take one TypeElement argument!", annotationTypeElement);
            }
        }
        else {
            failCompile("Found no processor for annotation '" + annotationTypeElement + "'!", annotationTypeElement);
        }


        return annotationHandled;
    }

    //
    // Private Support Methods
    //
    
    /**
     * Support method for finding the method to invoke for processing a specific annotation.
     * Null is returned if none found.
     *
     * @param qualifiedName The qualified name of the annotation to find processing method for.
     */
    private Method findProcessMethod(Name qualifiedName) {
        Method processMethod = null;

        for (Method pmethod : getClass().getDeclaredMethods()) {
            Process processAnn = pmethod.getAnnotation(Process.class);
            if (processAnn != null) {
                if (qualifiedName.contentEquals(processAnn.value().getName())) {
                    processMethod = pmethod;
                    break;
                }
            }
        }

        return processMethod;
    }

    /**
     * Finds the first method annotated with the specified annotation and return it
     * or null if not found.
     *
     * @param annotationClass The annotation to look for.
     */
    private Method findAnnotatedMethod(Class annotationClass) {
        List<Method> annotatedMethods = findAnnotatedMethods(annotationClass);
        return annotatedMethods.size() > 0 ? annotatedMethods.get(0) : null;
    }

    /**
     * Finds all methods annotated with the specified annotation. An emmpty list
     * will be returned if none found.
     *
     * @param annotationClass The annotation to look for.
     */
    private List<Method> findAnnotatedMethods(Class annotationClass) {
        List<Method> annotatedMethods = new ArrayList<Method>();

        for (Method amethod : getClass().getDeclaredMethods()) {
            Object annotation = amethod.getAnnotation(annotationClass);
            if (annotation != null) {
                annotatedMethods.add(amethod);

                break;
            }
        }

        return annotatedMethods;
    }
}
