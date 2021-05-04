# SimplifiedAnnotationProcessor

Copyright © 2013 Natusoft AB

__Version:__ 1.3

__Author:__ Tommy Svensson (tommy@natusoft.se)

----

_Base class and support classes for simplifying annotation processing._

[Docs](https://github.com/tombensve/SimplifiedAnnotationProcessor/blob/master/docs/SimplifiedAnnotationProcessor.md)

[Javadoc](http://apidoc.natusoft.se/SimplifiedAnnotationProcessor/)

[Licenses](https://github.com/tombensve/SimplifiedAnnotationProcessor/blob/master/licenses.md)

# Binaries

Since bintray has shut down binaries are now available at https://download.natusoft.se/maven.

You need to add the following to your pom:
```xml
    <repositories>
        <repository>
            <id>ns-repo</id>
            <name>ns-artifact-repository</name>
            <url>https://download.natusoft.se/maven</url>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>ns-plugin-repo</id>
            <name>ns-plugin-repository</name>
            <url>https://download.natusoft.se/maven</url>
            <releases>
                <enabled>true</enabled>
            </releases>
        </pluginRepository>
    </pluginRepositories>
```

I'm choosing this solution for now since getting things into maven central is still painful even tough it has gotten a little bit better than before. 
The stuff at my GitHub account are things I work on in my spare time. I don't want that time wasted by an overly complicated way of sharing binaries. Bintray really made everyhing as simple as it should be and I mourn its passing! 

----

## History

## Version 1.3

* Just bumped 3rd party versions. 

* Now requires a minimum of JDK 1.8!

## Version 1.2

Brought up to date with other dependencies. No functional difference.

### Version 1.1

* SAPType.getPackage() now returns the package of the top "outer" class for annotated inner classes. Before the outer class name became part of the package, which of course was not correct and caused compilation error when used in code generators by processors using SimplifiedAnnotationProcessor.

