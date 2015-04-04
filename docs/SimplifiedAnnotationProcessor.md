# SimpleAnnotationProcessor

An abstract annotation processor base class that simplifies the annotation processing, but also limits it slightly. It is however good enough for most cases and makes things a bit easier and clearer.

## Usage

    import javax.annotation.processing.SupportedSourceVersion;
    import javax.lang.model.SourceVersion;
    import javax.lang.model.element.Element;
    import javax.lang.model.element.ElementKind;
    import javax.lang.model.element.TypeElement;
    import se.natusoft.annotation.processor.simplified.SimplifiedAnnotationProcessor;
    import se.natusoft.annotation.processor.simplified.GenerationSupport;
    import se.natusoft.annotation.processor.simplified.annotations.Process;
    import se.natusoft.annotation.processor.simplified.annotations.AllProcessed;
    import se.natusoft.annotation.processor.simplified.annotations.GenerateSource;
    import se.natusoft.annotation.processor.simplified.annotations.NewRound;
    import se.natusoft.annotation.processor.simplified.annotations.ProcessedAnnotations;

    @AutoDiscovery
    @ProcessedAnnotations({MyAnnotation.class})
    @SupportedSourceVersion(SourceVersion.RELEASE_6)
    public class MyAnnotationProcessor extends SimplifiedAnnotationProcessor {
  
        // Gets called for each new processing round for clearing any already processed data.
        // Please note that only the first annotated method found will be called! Only one
        // methods should be annotated with @NewRound.
        @NewRound 
        public void newRound() { 
           
        }
  
        // Gets called for processing the specified annotation per round.
        // annotation - This represents the actual annotation being processed (This parameter can be skipped!)
        // annotatedElements - All language elements annotated with the annotation.
        @Process(MyAnnotation.class)
        public processMyAnn(TypeElement annotation, Set<? extends Element> annotatedElements) {
  
        }
  
        // Called after all processing has been done for the round. Will only be called if anything has been processed.
        // All methods annotated with @Generate will be called in order. This is a convenience for when you want to
        // generate more than one file and wants to do it from separate methods. You can of course let your annotated
        // method call other methods to do the job. As said, multiple @GenerateSource annotated methods is only a convenicence.
        @GenerateSource 
        public void generate(GenerationSupport generationSupport) {
  
        }
  
        // Called after all rounds are done. This annotation can also as a convenience be specified on several methods.
        // In that case the methods will be called in order. In addition to possibly doing cleanup you might want to generare
        // resources that require input from all rounds of processing here. In that case you could have a generateMyResource()
        // method annotated with @AllProcessed and a cleanup() or done() or whatever method also annotated with @AllProcessed.
        @AllProcessed 
        public void done() {
  
        }
    }

All but _@Process_ are optional! An _@Process_ must be available for each annotation specified with _@ProcessedAnnotations_ or _@SupportedAnnotationTypes_.

Please note that the _@GenerateSource_ method can either take no arguments or an GenerationSupport instance. If it takes no arguments the GenerationSupport object can be gotten with `getGenerationSupport()`.

Use the `failCompile(...)` method of this base class to cause a compilation failure. `failCompile()` and all the `print*()` methods simply passes information to the compiler which will deal with them later.

The se.natusoft.annotation.processor.simplified.model contains the following classes:
   
     SAPAnnotation     - Wraps an AnnotationMirror
     SAPBaseElement    - Baseclass for all other in this package.
     SAPExecutable     - Wraps ExecutableElement (constructors and methods)
     SAPVariable       - Wraps VariableElement (field, parameter, etc)
     SAPMemberVariable - Supclass of Variable and provides getter for the Type the member is part of.
     SAPType           - Wraps TypeElement (Class, Interace, Enum)

These wrappers of javax.lang.model.element.* models makes it a little bit easier extracting information.

## Processed utility annotations

**@AutoDiscovery** - Use this annotation on an annotation processor to automatically update META-INF/services/javax.annotation.Processor with the processor. The file will be created if it does not exist.

**@MavenResource(resourceClassPath)** - This annotation is for fields and points out a classpath resource, and have a processor that compile-time verifies that the pointed to resource does exist. This will look under src/main/resources for the resource, and will only work if compiled by maven since it expects the current directory to be the compiled project root.

**@ResourcePath** - This annotation should be used on String constants that points out a classpath resource, and have a processor that compile-time verifies that the resource does exist. This will first check in the compile classpath, and if not found then check for a file under src/main/resources which will work if built by maven. The last is probably redundant since I think maven includes the resources on the classpath during compile. Note that this takes the resource from the String constant. This annotation is probably more useful than @MavenResource.

## Maven usage

    <dependencies>
        ...
        <dependency>
            <groupId>se.natusoft.annotation</groupId>
            <artifactId>simplified-annotation-processor</artifactId>
            <version>1.0</version>
        </dependendcy>
        ...
    </dependencies>

    <repositories>
        <repository>
            <id>maven-natusoft-se</id>
            <name>Natusofts Bintray maven repository</name>
            <url>https://dl.bintray.com/tommy/maven/</url>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>maven-natusoft-se</id>
            <name>Natusofts Bintray maven repository.</name>
            <url>https://dl.bintray.com/tommy/maven/</url>
        </pluginRepository>
    </pluginRepositories>

This have been submitted to Bintrays JCenter but have not yet been approved. 

## Example of a complete processor:

    import se.natusoft.annotation.beanannotationprocessor.annotations.Bean;
    import se.natusoft.annotation.processor.simplified.SimplifiedAnnotationProcessor;
    import se.natusoft.annotation.processor.simplified.annotations.*;
    import se.natusoft.annotation.processor.simplified.annotations.Process;
    import se.natusoft.annotation.processor.simplified.codegen.GenerationSupport;
    import se.natusoft.annotation.processor.simplified.codegen.JavaSourceOutputStream;
    import se.natusoft.annotation.processor.simplified.model.SAPAnnotation;
    import se.natusoft.annotation.processor.simplified.model.SAPType;

    import javax.annotation.processing.SupportedSourceVersion;
    import javax.lang.model.SourceVersion;
    import javax.lang.model.element.*;
    import java.io.IOException;
    import java.util.LinkedList;
    import java.util.List;
    import java.util.Set;

    /**
     * This generates the class that the annotated file extends.
     */
    @AutoDiscovery
    @ProcessedAnnotations({Bean.class})
    @SupportedSourceVersion(SourceVersion.RELEASE_7)
    public class BeanProcessor extends SimplifiedAnnotationProcessor {
        //
        // Private Members
        //

        private static final boolean verbose = true;

        private List<Element> toGenerate = null;


        //
        // Constructors
        //

        public BeanProcessor() {
            super(verbose);
        }

        //
        // Methods
        //

        @NewRound
        public void newRound() {
            this.toGenerate = new LinkedList<>();
        }

        @Process(Bean.class)
        public void processBean(Set<? extends Element> annotatedElements) {
            for (Element element : annotatedElements) {
                this.toGenerate.add(element);
            }
        }

        @GenerateSource
        public void generate(GenerationSupport generationSupport) {
            try {
                for (Element annotatedElement : this.toGenerate) {
                    SAPType type = new SAPType((TypeElement)annotatedElement);

                    String genQName = type.getPackage() + "." + type.getExtends();
                    JavaSourceOutputStream jos = generationSupport.getToBeCompiledJavaSourceOutputStream(genQName);
                    jos.packageLine(type.getPackage());
                    jos.generatedAnnotation(getClass().getName(), "");
                    jos.begClass("public", "", type.getExtends());
                    jos.begMethod("protected", "", "", type.getExtends());
                    jos.endMethod();
                    jos.emptyLine();
                    {
                        SAPAnnotation beanAnnotation = type.getAnnotationByClass(Bean.class);

                        AnnotationValue annVal = beanAnnotation.getAnnotationValueFor("value");
                        List<AnnotationMirror> props = (List<AnnotationMirror>)annVal.getValue();

                        List<String> nonNullProps = new LinkedList<>();

                        for (AnnotationMirror propMirror : props) {
                            SAPAnnotation propAnn = new SAPAnnotation(propMirror);
                            String propName = propAnn.getValueFor("name").toString();
                            String propType = propAnn.getValueFor("type").toString();
                            String propDef = propAnn.getValueFor("init").toString();
                            boolean required = propAnn.getValueFor("required").toBoolean();

                            if (required) {
                                nonNullProps.add(propName);
                            }

                            verbose("Generating property: " + propName);
                            String defValue = propDef.trim().length() > 0 ? propDef : null;
                            jos.field("private", propType, propName, defValue);

                            jos.begMethod("public", "", "void", setterName(propName));
                            {
                                jos.methodArg(propType, "value");
                                jos.println("        this." + propName + " = value;");
                            }
                            jos.endMethod();

                            jos.begMethod("public", "", propType, getterName(propName));
                            {
                                jos.println("        return this." + propName + ";");
                            }
                            jos.endMethod();
                            jos.emptyLine();
                        }

                        if (!nonNullProps.isEmpty()) {
                            jos.begMethod("public", "", "void", "validate");
                            jos.println("        StringBuilder sb = new StringBuilder();");
                            jos.println("        String space = \"\";");
                            for (String propName : nonNullProps) {
                                jos.println("        if (this." + propName + " == null) {");
                                jos.println("            sb.append(space);");
                                jos.println("            space = \" \";");
                                jos.println("            sb.append(\"'" + propName + "' cannot be null!\");");
                                jos.println("        }");
                            }
                            jos.println("        if (sb.length() > 0) throw new IllegalStateException(sb.toString());");
                            jos.endMethod();
                        }
                    }
                    jos.endClass();
                    jos.close();
                }
            }
            catch (IOException ioe) {
                failCompile("@Bean processor failed to generate bean!", ioe);
            }
        }

        private String setterName(String propName) {
            return "set" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
        }

        private String getterName(String propName) {
            return "get" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
        }

        @AllProcessed
        public void done() {
        }

    }
