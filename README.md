# SimplifiedAnnotationProcessor

Copyright © 2013 Natusoft AB

__Version:__ 1.2

__Author:__ Tommy Svensson (tommy@natusoft.se)

----

_Base class and support classes for simplifying annotation processing._

[Docs](https://github.com/tombensve/SimplifiedAnnotationProcessor/blob/master/docs/SimplifiedAnnotationProcessor.md)

[Javadoc](http://apidoc.natusoft.se/SimplifiedAnnotationProcessor/)

[Licenses](https://github.com/tombensve/SimplifiedAnnotationProcessor/blob/master/licenses.md)

[Download](http://download.natusoft.se/annotation/simplified-annotation-processor-1.0.jar)

----

## History

## Version 1.2

Brought up to date with other dependencies. Functionwise there is no difference.

### Version 1.1

* SAPType.getPackage() now returns the package of the top "outer" class for annotated inner classes. Before the outer class name became part of the package, which of course was not correct and caused compilation error when used in code generators by processors using SimplifiedAnnotationProcessor.

