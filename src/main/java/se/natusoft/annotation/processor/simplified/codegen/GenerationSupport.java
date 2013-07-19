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
package se.natusoft.annotation.processor.simplified.codegen;

import se.natusoft.annotation.processor.simplified.Verbose;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 * Provides support for source and resource generation. It wraps a Filer and provides much clearer names
 * and simpler APIs. It also adds support for generating resource files when using maven which are placed
 * in src/main/resources.
 */
public class GenerationSupport {

    //
    // Private Members
    //

    /** We wrap a Filer instance. */
    private Filer filer = null;

    private Verbose verbose = null;

    //
    // Constructors
    //

    public GenerationSupport(Filer filer, Verbose verbose) {
        this.filer = filer;
        this.verbose = verbose;
    }

    public GenerationSupport(Filer filer) {
        this.filer = filer;
    }

    //
    // Methods
    //

    private void verbose(String text) {
        this.verbose.verbose(text);
    }

    /**
     * Returns the filer.
     */
    public Filer getFiler() {
        return this.filer;
    }

    /**
     * Returns a JavaFileObject for writing Java source code to be compiled.
     *
     * @param qualifiedName The fully qualified name of the class to write.
     * @param element An optional Element to associate with this file.
     *
     * @throws IOException
     */
    public JavaFileObject getWritableJavaFileObjectForToBeCompiledSource(String qualifiedName, Element element) throws IOException {
        verbose("Producing Java source file: " + qualifiedName);
        return this.filer.createSourceFile(qualifiedName, element);
    }

    /**
     * Returns a JavaFileObject for writing Java source code to be compiled.
     *
     * @param qualifiedName The fully qualified name of the class to write.
     *
     * @throws IOException
     */
    public JavaFileObject getWritableJavaFileObjectForToBeCompiledSource(String qualifiedName) throws IOException {
        return getWritableJavaFileObjectForToBeCompiledSource(qualifiedName, null);
    }

    /**
     * Returns an OutputStream for writing Java source code to be compiled.
     *
     * @param qualifiedName The fully qualified name of the class to write.
     * @param element An optional Element to associate with this file.
     *
     * @throws IOException
     */
    public OutputStream getToBeCompiledSourceFileStream(String qualifiedName, Element element) throws IOException {
        JavaFileObject jfo = getWritableJavaFileObjectForToBeCompiledSource(qualifiedName, element);
        jfo.delete();
        return jfo.openOutputStream();
    }

    /**
     * Returns an OutputStream for writing Java source code to be compiled.
     *
     * @param qualifiedName The fully qualified name of the class to write.
     *
     * @throws IOException
     */
    public OutputStream getToBeCompiledSourceFileStream(String qualifiedName) throws IOException {
        JavaFileObject jfo = getWritableJavaFileObjectForToBeCompiledSource(qualifiedName);
        jfo.delete();
        return jfo.openOutputStream();
    }

    /**
     * Returns a JavaSourceOutputStream for writing Java Source code to be compiled.
     * 
     * @param qualifiedName The fully qualified name of the class to write.
     * @param element An optional Element to associate with this file.
     *
     * @throws IOException
     */
    public JavaSourceOutputStream getToBeCompiledJavaSourceOutputStream(String qualifiedName, Element element) throws IOException {
        return new JavaSourceOutputStream(getToBeCompiledSourceFileStream(qualifiedName, element));
    }

    /**
     * Returns a JavaSourceOutputStream for writing Java Source code to be compiled.
     *
     * @param qualifiedName The fully qualified name of the class to write.
     *
     * @throws IOException
     */
    public JavaSourceOutputStream getToBeCompiledJavaSourceOutputStream(String qualifiedName) throws IOException {
        return new JavaSourceOutputStream(getToBeCompiledSourceFileStream(qualifiedName));
    }

    /**
     * Returns a FileObject for writing a resource file to the source three.
     * 
     * @param pkg The package of the resource file
     * @param name The name of the resource file.
     * @param element An optional element to associate with the file.
     *
     * @throws IOException
     */
    public FileObject getWritableResourceFileObject(String pkg, String name, Element element) throws IOException {
        verbose("    Producing resource file: " + pkg + "." + name);
        return this.filer.createResource(StandardLocation.SOURCE_OUTPUT, pkg, name, element);
    }

    /**
     * Returns a FileObject for writing a resource file to the source three.
     *
     * @param qualifiedName A fully qualified name of the file to write.
     * @param element An optional element to associate with the file.
     *
     * @throws IOException
     */
    public FileObject getWritableResourceFileObject(String qualifiedName, Element element) throws IOException {
        String pkg, name;
        int ix = qualifiedName.lastIndexOf('.');
        if (ix > 0) {
            pkg = qualifiedName.substring(0, ix);
            name = qualifiedName.substring(ix + 1);
        }
        else {
            pkg = "";
            name = qualifiedName;
        }
        return getWritableResourceFileObject(pkg, name, element);
    }

    /**
     * Returns a FileObject for writing a resource file to the source three.
     *
     * @param pkg The package of the resource file
     * @param name The name of the resource file.
     *
     * @throws IOException
     */
    public FileObject getWritableResourceFileObject(String pkg, String name) throws IOException {
        return getWritableResourceFileObject(pkg, name, null);
    }

    /**
     * Returns a FileObject for writing a resource file to the source three.
     *
     * @param qualifiedName A fully qualified name of the file to write.
     *
     * @throws IOException
     */
    public FileObject getWritableResourceFileObject(String qualifiedName) throws IOException {
        String pkg, name;
        int ix = qualifiedName.lastIndexOf('.');
        if (ix > 0) {
            pkg = qualifiedName.substring(0, ix);
            name = qualifiedName.substring(ix + 1);
        }
        else {
            pkg = "";
            name = qualifiedName;
        }
        return getWritableResourceFileObject(pkg, name);
    }

    /**
     * Returns an OutputStream for writing a resource file to the source three.
     *
     * @param pkg The package of the resource file
     * @param name The name of the resource file.
     * @param element An optional element to associate with the file.
     *
     * @throws IOException
     */
    public OutputStream getWritableResourceFileStream(String pkg, String name, Element element) throws IOException {
        FileObject fo = getWritableResourceFileObject(pkg, name, element);
        fo.delete();
        return fo.openOutputStream();
    }

    /**
     * Returns an OutputStream for writing a resource file to the source three.
     *
     * @param qualifiedName A fully qualified name of the file to write.
     * @param element An optional element to associate with the file.
     *
     * @throws IOException
     */
    public OutputStream getWritableResourceFileStream(String qualifiedName, Element element) throws IOException {
        String pkg, name;
        int ix = qualifiedName.lastIndexOf('.');
        if (ix > 0) {
            pkg = qualifiedName.substring(0, ix);
            name = qualifiedName.substring(ix + 1);
        }
        else {
            pkg = "";
            name = qualifiedName;
        }
        return getWritableResourceFileStream(pkg, name, element);
    }

    /**
     * Returns an OutputStream for writing a resource file to the source three.
     *
     * @param pkg The package of the resource file
     * @param name The name of the resource file.
     *
     * @throws IOException
     */
    public OutputStream getWritableResourceFileStream(String pkg, String name) throws IOException {
        FileObject fo = getWritableResourceFileObject(pkg, name);
        fo.delete();
        return fo.openOutputStream();
    }

    /**
     * Returns an OutputStream for writing a resource file to the source three.
     *
     * @param qualifiedName A fully qualified name of the file to write.
     *
     * @throws IOException
     */
    public OutputStream getWritableResourceFileStream(String qualifiedName) throws IOException {
        String pkg, name;
        int ix = qualifiedName.lastIndexOf('.');
        if (ix > 0) {
            pkg = qualifiedName.substring(0, ix);
            name = qualifiedName.substring(ix + 1);
        }
        else {
            pkg = "";
            name = qualifiedName;
        }
        return getWritableResourceFileStream(pkg, name);
    }

    /**
     * Writes a resource file under the maven src/main/resources path.
     *
     * @param path The path for the resource file.
     *
     * @throws IOException
     */
    public OutputStream getWritableMavenResourceFileStream(String path) throws IOException {
        verbose("Producing maven resource file: " + path);
        int ix = path.lastIndexOf(File.separatorChar);
        String directory = null;
        String name = null;
        if (ix > 0) {
            directory = path.substring(0, ix);
            name = path.substring(ix + 1);
        }
        else {
            directory = "";
            name = path;
        }
        File rootPath = new File("src/main/resources");
        File resourceFile = new File(rootPath, directory);
        resourceFile.mkdirs();
        resourceFile = new File(resourceFile, name);
        System.out.println("Resource file: [" + resourceFile + "]");
        return new FileOutputStream(resourceFile);
    }

    /**
     * Writes a resource file under the current directory. If run from a maven build this
     * will be the maven project root. 
     *
     * @param path
     * @return
     * @throws IOException
     */
    public OutputStream getWritableCurrentDirRelativeResourceFileStream(String path) throws IOException {
        verbose("Producing current directory relative resource file: " + path);
        int ix = path.lastIndexOf(File.separatorChar);
        String directory = null;
        String name = null;
        if (ix > 0) {
            directory = path.substring(0, ix);
            name = path.substring(ix + 1);
        }
        else {
            directory = "";
            name = path;
        }
        directory = directory.trim();
        if (directory.startsWith(File.separator)) {
            directory = directory.substring(1);
        }
        File resourceFile = new File(directory);
        resourceFile.mkdirs();
        resourceFile = new File(resourceFile, name);
        System.out.println("Resource file: [" + resourceFile + "]");
        return new FileOutputStream(resourceFile);

    }

    /**
     * Returns a ResourceReference to the specified relative resource path by using a best effort to find its location.
     * <p>
     * It will start by checking the tryFirstRootPaths and if none of those root paths were found "src/main/resources"
     * will be tried. If that were not found either then the Filer will be used with StandardLocation.SOURCE_OUTPUT as
     * root path.
     *
     * @param resourceRelPath
     */
    public ResourceReference getBestEffortResourceReference(String resourceRelPath, String[] tryFirstRootPaths) {
        verbose("Producing best effort resource file: " + resourceRelPath);
        ResourceReference rr = null;
        File resourceRoot = null;

        for (String path : tryFirstRootPaths) {
            resourceRoot = new File(path);
            if (resourceRoot.exists()) {
                resourceRoot = new File(resourceRoot, resourceRelPath);
                rr = new ResourceReference(resourceRoot);
                break;
            }
        }

        if (rr == null) {
            resourceRoot = new File("src/main/resources");
            if (!resourceRoot.exists()) {
                rr = new ResourceReference(this.filer, resourceRelPath);
            }
            else {
                resourceRoot = new File(resourceRoot, resourceRelPath);
                rr = new ResourceReference(resourceRoot);
            }
        }

        return rr;
    }

    /**
     * Returns a ResourceReference to the specified relative resource path by using a best effort to find its location.
     * <p>
     * It will start by checking "src/main/resources". If that were not found then the Filer will be used with
     * StandardLocation.SOURCE_OUTPUT as root path.
     *
     * @param resourcePath The path to the resource.
     */
    public ResourceReference getBestEffortResourceReference(String resourcePath) {
        return getBestEffortResourceReference(resourcePath, new String[0]);
    }

    //
    // Inner Classes
    //
    
    /**
     * This holds a reference to a resource file that can be both read and written.
     */
    public static class ResourceReference {
        //
        // Private Members
        //

        /** A FileObject provided by Filer. */
        private Filer filer = null;

        /** The relative package of the resource. */
        private String pkg = null;

        /** The name of the resource. */
        private String name = null;

        /** A File used for maven path specific resource. */
        private File file = null;

        //
        // Constructors
        //

        /**
         * Creates a new ResourceReference.
         *
         * @param filer A Filer instance.
         * @param relativePath The relative path to the resource.
         */
        public ResourceReference(Filer filer, String relativePath) {
            this.filer = filer;
            int ix = relativePath.lastIndexOf(File.separatorChar);
            this.pkg = relativePath.substring(0, ix).replace(File.separatorChar, '.');
            this.name = relativePath.substring(ix + 1);
        }

        /**
         * Creates a new ResourceReference.
         *
         * @param file A File for this reference.
         */
        public ResourceReference(File file) {
            this.file = file;
        }

        //
        // Methods
        //

        /**
         * Returns an input stream to the resource.
         *
         * @throws IOException
         */
        public InputStream getInputStream() throws IOException {
            InputStream inputStream = null;

            if (this.filer != null) {
                FileObject fileObject = this.filer.getResource(StandardLocation.SOURCE_OUTPUT, pkg, name);
                inputStream = fileObject.openInputStream();
            }
            else {
                inputStream = new FileInputStream(this.file);
            }

            return inputStream;
        }

        /**
         * Returns an output stream to the resource.
         *
         * @throws IOException
         */
        public OutputStream getOutputStream() throws IOException {
            OutputStream outputStream = null;

            if (this.filer != null) {
                FileObject fileObject = this.filer.createResource(StandardLocation.SOURCE_OUTPUT, pkg, name, new Element[0]);
                outputStream = fileObject.openOutputStream();
            }
            else {
                this.file.getParentFile().mkdirs();
                outputStream = new FileOutputStream(this.file);
            }

            return outputStream;
        }

        /**
         * Reads the complete resource file and returns it as one String.
         *
         * @throws IOException
         */
        public String readResourceAsString() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            InputStream is = getInputStream();
            try {
                int b;
                while ((b = is.read()) != -1) {
                    baos.write(b);
                }
            }
            finally {
                is.close();
            }

            return baos.toString();
        }

        /**
         * Writes the complete resource file from one String.
         *
         * @param content The string content to write.
         * 
         * @throws IOException
         */
        public void writeResourceFromString(String content) throws IOException {
            OutputStream os = getOutputStream();
            try {
                os.write(content.getBytes());
            }
            finally {
                os.close();
            }
        }
    }
}
