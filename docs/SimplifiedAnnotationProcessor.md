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
   
     Annotation     - Wraps an AnnotationMirror
     BaseElement    - Baseclass for all other in this package.
     Executable     - Wraps ExecutableElement (constructors and methods)
     Variable       - Wraps VariableElement (field, parameter, etc)
     MemberVariable - Supclass of Variable and provides getter for the Type the member is part of.
     Type           - Wraps TypeElement (Class, Interace, Enum)

These wrappers of javax.lang.model.element.* models makes it a little bit easier extracting information.

