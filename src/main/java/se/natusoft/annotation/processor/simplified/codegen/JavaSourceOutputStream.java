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
 *
 */
package se.natusoft.annotation.processor.simplified.codegen;

import java.io.OutputStream;
import java.util.StringTokenizer;

/**
 * This output stream class simplifies writing Java source code. This is best shown with an example:
 * <pre>
 *     JavaSourceOutputStream jos = new JavaSourceOutputStream(new FileOutputStream("src/main/java/jsos/example/MyClass"));
 *
 *     jos.comment(
 *             "NAME",
 *             "    TestOfJavaSourceOutputStream",
 *             "",
 *             "CREATED",
 *             "    2011-02-20"
 *     );
 *     jos.packageLine("jsos.example");
 *     jos.emptyLine();
 *     jos.importLine("java.text.*");
 *     jos.importLine("java.util.*");
 *     jos.emptyLine();
 *
 *     jos.javadocComment("Test class");
 *     jos.begClass("public", "", "MyClass");
 *     jos.extendsClass("MyOtherClass");
 *     jos.implementsInterface("MyInterface");
 *     jos.implementsInterface("MyOtherInterface");
 *     {
 *         jos.emptyLine();
 *
 *         jos.privateField("String", "name", "The name of this object.");
 *
 *         jos.emptyLine();
 *
 *         jos.javadocComment("Creates a new MyClass");
 *         jos.beginConstructorMethod("MyClass");
 *         jos.methodArg("String", "name");
 *         jos.methodException("IOException");
 *         jos.methodException("RuntimeException");
 *         {
 *             jos.contentln("this.name = name;");
 *         }
 *         jos.endMethod();
 *
 *         jos.emptyLine();
 *
 *         jos.javadocComment("Returns the name.");
 *         jos.begMethod("public", "", "String", "getName");
 *         {
 *             jos.contentln("return this.name");
 *         }
 *         jos.endMethod();
 *
 *         jos.emptyLine();
 *
 *         jos.begMethod("public", "", "doSomething");
 *         {
 *             jos.begIf("new Date().getTime() == 1234");
 *             {
 *                 jos.contentln("System.out.println(\"This will never happen!\")");
 *             }
 *             jos.endIf();
 *
 *             jos.emptyLine();
 *
 *             jos.begWhile();
 *             jos.whileCriteria("a > b");
 *             jos.whileCriteria(" && ");
 *             jos.whileCriteria("b > a");
 *             {
 *                 jos.contentln("System.out.println(\"This will never happen!\")");
 *             }
 *             jos.endWhile();
 *
 *             jos.emptyLine();
 *
 *             jos.begFor("int i", "i < 100", "i++");
 *             {
 *                 jos.contentln("System.out.println(\"\" + i);");
 *             }
 *             jos.endFor();
 *
 *             jos.emptyLine();
 *
 *             jos.begForShort("String propName", "System.getProperties().propertyNames()");
 *             {
 *                 jos.contentln("System.out.println(propName + \" = \" + System.getProperty(propName));");
 *             }
 *             jos.endFor();
 *
 *             jos.emptyLine();
 *
 *             jos.contentln("System.out.println(\"This will happen!\");");
 *         }
 *         jos.endMethod();
 *     }
 *     jos.endClass();
 *
 *     if (!jos.checkError()) {
 *         System.out.println("Class written OK!");
 *     }
 *     jos.flush();
 *     jos.close();
 *
 * </pre>
 * This results in (note that I had to put a space between * and / in end block comments to be able to put this in javadoc!):
 * <pre>
 *     /*
 *      * NAME
 *      *     TestOfJavaSourceOutputStream
 *      *
 *      * CREATED
 *      *     2011-02-20
 *      * /
 *     package jsos.example;
 *
 *     import java.text.*;
 *     import java.util.*;
 *
 *     /**
 *      * Test class
 *      * /
 *     public MyClass extends MyOtherClass implements MyInterface, MyOtherInterface {
 *
 *         /** The name of this object. * /
 *         private String name;
 *
 *         /**
 *          * Creates a new MyClass
 *          * /
 *         MyClass(String name) throws IOException, RuntimeException {
 *             this.name = name;
 *         }
 *
 *         /**
 *          * Returns the name.
 *          * /
 *         public String getName() {
 *             return this.name
 *         }
 *
 *         public doSomething() {
 *             if (new Date().getTime() == 1234) {
 *                 System.out.println("This will never happen!")
 *             }
 *
 *             while (a > b && b > a) {
 *                 System.out.println("This will never happen!")
 *             }
 *
 *             for (int i; i < 100 ; i++) {
 *                 System.out.println("" + i);
 *             }
 *
 *             for (String propName : System.getProperties().propertyNames()) {
 *                 System.out.println(propName + " = " + System.getProperty(propName));
 *             }
 *
 *             System.out.println("This will happen!");
 *         }
 *     }
 * </pre>
 */
public class JavaSourceOutputStream extends CodeGeneratorOutputStream {
    //
    // Private Members
    //

    //
    // Constructors
    //

    /**
     * Creates a new JavaSourceOutputStream.
     * 
     * @param os The OutputStream to write to.
     */
    public JavaSourceOutputStream(OutputStream os) {
        super(os);
    }

    //
    // Methods
    //

    /**
     * Prints a intComment to the stream.
     *
     * @param start The start of the intComment
     * @param forceBlock Forces a block comment.
     * @param commentLines each intComment line.
     */
    private void intComment(String start, boolean forceBlock, String... commentLines) {
        indent();
        if (commentLines.length == 1 && !forceBlock) {
            print(start);
            print(commentLines[0]);
            println(" */");
        }
        else {
            println(start);
            for (String comment : commentLines) {
                indent();
                print(" * ");
                println(comment);
            }
            indent();
            println(" */");
        }
    }

    /**
     * Prints a single line intComment.
     * 
     * @param intComment The intComment to print.
     */
    public void singeLineComment(String comment) {
        print("// ");
        println(comment);
    }

    /**
     * Prints a single line intComment.
     *
     * @param intComment The intComment to print.
     */
    public void singeLineCommentln(String comment) {
        indent();
        print("// ");
        println(comment);
    }

    /**
     * Prints a standard block intComment.
     *
     * @param forceBlock If true a block comment is forced even if there is only one line.
     * @param commentLines The lines of the intComment.
     */
    public void comment(boolean forceBlock, String... commentLines) {
        intComment("/* ", forceBlock, commentLines);
    }

    /**
     * Prints a standard block intComment.
     *
     * @param commentLines The lines of the intComment.
     */
    public void comment(String... commentLines) {
        intComment("/* ", true, commentLines);
    }

    /**
     * Prints a javadoc intComment block.
     *
     * @param forceBlock If true a block comment is forced even if there is only one line.
     * @param commentLines The lines of the intComment.
     */
    public void javadocComment(boolean forceBlock, String... commentLines) {
        intComment("/** ", forceBlock, commentLines);
    }

    /**
     * Prints a javadoc intComment block.
     *
     * @param commentLines The lines of the intComment.
     */
    public void javadocComment(String... commentLines) {
        intComment("/** ", true, commentLines);
    }

    /**
     * Prints the "package" defintion.
     * 
     * @param pkg The package the class belongs to.
     */
    public void packageLine(String pkg) {
        print("package ");
        print(pkg);
        println(";");
    }

    /**
     * Prints an "import" line.
     *
     * @param importSpec The import without the "import" keyword and the ending semicolon.
     */
    public void importLine(String importSpec) {
        print("import ");
        print(importSpec);
        println(";");
    }

    /**
     * Prints an annotation useage.
     *
     * @param annotation The annotation useage to print.
     * @param annParams The annotation parameters where each entry is in the format "name=value".
     */
    public void annotation(String annotation, String... annParams) {
        print("@");
        print(annotation);
        if (annParams.length > 0) {
            print("(");
            String comma = "";
            for (String param : annParams) {
                StringTokenizer tokenizer = new StringTokenizer(param, "=");
                String name = tokenizer.nextToken();
                String value = tokenizer.nextToken();
                print(comma);
                print(name);
                print("=");
                print(value);
                comma = ", ";
            }
            print(")");
        }
        println("");
    }

    /**
     * Prints an annotation useage.
     *
     * @param annotation The annotation useage to print.
     */
    public void annotation(String annotation) {
        annotation(annotation, new String[0]);
    }

    /**
     * Prints the start of a class definition.
     *
     * @param access The class access level.
     * @param modifiers The class modifiers.
     * @param name The name of the class.
     */
    public void begClass(String access, String modifiers, String name) {
        initializeOnFirst();
        indent();
        if (access.length() > 0) {
            print(access);
            print(" ");
        }
        if (modifiers.length() > 0) {
            print(modifiers);
            print(" ");
        }
        print("class ");

        print(name);

        delayedPrintln(" {");

        incrementIndent();
    }

    /**
     * Prints the ending of a class.
     */
    public void endClass() {
        decrementIndent();
        indent();
        println("}");
    }

    /**
     * Prints the class this class extends. This should be called directly after begClass().
     *
     * @param extendsClass The extended class to print.
     */
    public void extendsClass(String extendsClass) {
        rawPrint(" extends ");
        rawPrint(extendsClass);
    }

    /**
     * Prints a an implemented interface. This should be called directly after begClass(), extendsClass(), or another implementsInterface().
     *
     * @param implementsInterface The implemented interface to print.
     */
    public void implementsInterface(String implementsInterface) {
        onFirst(new Runnable() {
            @Override
            public void run() {
                rawPrint(" implements ");
                newComma();
            }
        });
        printComma();
        rawPrint(implementsInterface);
    }

    /**
     * Prints the start of a method definition.
     *
     * @param access The access level.
     * @param modifiers The modifiers.
     * @param returnType The return type of the method.
     * @param name The method name.
     */
    public void begMethod(String access, String modifiers, String returnType, String name) {
        initializeOnFirst();
        indent();

        if (access.length() > 0) {
            print(access);
            print(" ");
        }
        if (modifiers.length() > 0) {
            print(modifiers);
            print(" ");
        }
        print(returnType);
        if (returnType.length() > 0) {
            print(" ");
        }

        print(name);
        print("(");

        delayedPrint(")");
        delayedPrintln(" {");

        incrementIndent();

        newComma();
    }

    /**
     * Begins a method that is a constructor.
     *
     * @param name Should be same name as class.
     */
    public void beginConstructorMethod(String name) {
        begMethod("", "", "", name);
    }

    /**
     * Prints a method argument. This should be called after begMethod() or another methodArg().
     *
     * @param type The argument type.
     * @param name The argument name.
     */
    public void methodArg(String type, String name) {
        printComma();
        rawPrint(type);
        rawPrint(" ");
        rawPrint(name);
    }

    /**
     * Prints a method exception. This should be called after begMethod() or methodArg(), and should
     * be called once for each thrown exception. 
     * <p>
     * The ending of the argument list (')') and the ending of the method definition ('{') is handled
     * automatically (begMethod() set this up!).
     *
     * @param exception
     */
    public void methodException(String exception) {
        onFirst(new Runnable() {
            @Override
           public void run() {
               flushAndRemoveFirstDelayedPrint();
               rawPrint(" throws ");
               newComma();
           }
        });
        printComma();
        rawPrint(exception);
    }

    public void endMethod() {
        decrementIndent();
        indent();
        println("}");
    }

    /**
     * Writes a field.
     *
     * @param access The access of the field.
     * @param type The type of the field.
     * @param name The name of the field.
     */
    public void field(String access, String type, String name) {
        indent();
        print(access);
        print(" ");
        print(type);
        print(" ");
        print(name);
        println(";");
    }

    /**
     * Writes a field.
     *
     * @param type The type of the field.
     * @param name The name of the field.
     * @param javadoc the javadoc for this field.
     */
    public void privateField(String type, String name, String javadoc) {
        if (javadoc != null) {
            javadocComment(false, javadoc);
        }
        field("private", type, name);
        emptyLine();
    }

    /**
     * Writes a field.
     *
     * @param type The type of the field.
     * @param name The name of the field.
     */
    public void privateField(String type, String name) {
        privateField(type, name, null);
    }

    // if statement support

    /**
     * Begins an if statement and increments the indent. This prints the whole if line: "if (criteria) {".
     *
     * @param criteria The complete if statement criteria.
     */
    public void begIf(String criteria) {
        indent();
        print("if (");
        print(criteria);
        println(") {");
        incrementIndent();
    }

    /**
     * Begins an if statement and increments the indent. This version noly prints the start of the if: "if (".
     * Follow this call with one or more ifCriteria(criteria) calls to complete the if criteria. The end part
     * ") {" is delayed printed.
     */
    public void begIf() {
        indent();
        print("if (");
        delayedPrintln(") {");
        incrementIndent();
    }

    /**
     * This method is basically for making the generating code slightly more readable. All it does is
     * "rawPrint(criteria)", which can be done directly as well as calling this method.
     *
     * @param criteria
     */
    public void ifCriteria(String criteria) {
        rawPrint(criteria);
    }

    /**
     * This decrements the indent and writes the ending "}" for the if block. One or more contentln(text) should
     * be done before this call.
     */
    public void endIf() {
        decrementIndent();
        indent();
        println("}");
    }

    // while statement support

    /**
     * Begins a while statement and increments the indent. This prints the whole wile line: "while (criteria) {".
     *
     * @param criteria The complete while statement criteria.
     */
    public void begWhile(String criteria) {
        indent();
        print("while (");
        print(criteria);
        println(") {");
        incrementIndent();
    }

    /**
     * Begins a while statement and increments the indent. This version noly prints the start of the while: "while (".
     * Follow this call with one or more whileCriteria(criteria) calls to complete the while criteria. The end part
     * ") {" is delayed printed.
     */
    public void begWhile() {
        indent();
        print("while (");
        delayedPrintln(") {");
        incrementIndent();
    }

    /**
     * This method is basically for making the generating code slightly more readable. All it does is
     * "rawPrint(criteria)", which can be done directly as well as calling this method.
     *
     * @param criteria
     */
    public void whileCriteria(String criteria) {
        rawPrint(criteria);
    }

    /**
     * This decrements the indent and writes the ending "}" for the while block. One or more contentln(text) should
     * be done before this call.
     */
    public void endWhile() {
        decrementIndent();
        indent();
        println("}");
    }

    // for statement support

    /**
     * Begins a for loop and increments indent. This prints a full 3 part for line: "for (init ; compare ; increment) {".
     *
     * @param init
     * @param compare
     * @param increment
     */
    public void begFor(String init, String compare, String increment) {
        indent();
        print("for (");
        print(init);
        print("; ");
        print(compare);
        print(" ; ");
        print(increment);
        println(") {");
        incrementIndent();
    }

    /**
     * Begins a for loop and increments indent. This prints a full shorth loop over collection or array variant: "for (itemDecl : items) {".
     *
     * @param itemDecl
     * @param items
     */
    public void begForShort(String itemDecl, String items) {
        indent();
        print("for (");
        print(itemDecl);
        print(" : ");
        print(items);
        println(") {");
        incrementIndent();
    }

    /**
     * This ends a for loop and decrements indent.
     */
    public void endFor() {
        decrementIndent();
        indent();
        println("}");
    }

    //
    // Test / Validation (not a unit test!).
    //

//    public static void main(String[] args) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        JavaSourceOutputStream jos = new JavaSourceOutputStream(baos);
//
//        jos.comment(
//                "NAME",
//                "    TestOfJavaSourceOutputStream",
//                "",
//                "CREATED",
//                "    2011-02-20"
//        );
//        jos.packageLine("jsos.example");
//        jos.emptyLine();
//        jos.importLine("java.text.*");
//        jos.importLine("java.util.*");
//        jos.emptyLine();
//
//        jos.javadocComment("Test class");
//        jos.begClass("public", "", "MyClass");
//        jos.extendsClass("MyOtherClass");
//        jos.implementsInterface("MyInterface");
//        jos.implementsInterface("MyOtherInterface");
//        {
//            jos.emptyLine();
//
//            jos.privateField("String", "name", "The name of this object.");
//
//            jos.emptyLine();
//
//            jos.javadocComment("Creates a new MyClass");
//            jos.beginConstructorMethod("MyClass");
//            jos.methodArg("String", "name");
//            jos.methodException("IOException");
//            jos.methodException("RuntimeException");
//            {
//                jos.contentln("this.name = name;");
//            }
//            jos.endMethod();
//
//            jos.emptyLine();
//
//            jos.javadocComment("Returns the name.");
//            jos.begMethod("public", "", "getName");
//            {
//                jos.contentln("return this.name");
//            }
//            jos.endMethod();
//
//            jos.emptyLine();
//
//            jos.begMethod("public", "", "doSomething");
//            {
//                jos.begIf("new Date().getTime() == 1234");
//                {
//                    jos.contentln("System.out.println(\"This will never happen!\")");
//                }
//                jos.endIf();
//
//                jos.emptyLine();
//
//                jos.begWhile();
//                jos.whileCriteria("a > b");
//                jos.whileCriteria(" && ");
//                jos.whileCriteria("b > a");
//                {
//                    jos.contentln("System.out.println(\"This will never happen!\")");
//                }
//                jos.endWhile();
//
//                jos.emptyLine();
//
//                jos.begFor("int i", "i < 100", "i++");
//                {
//                    jos.contentln("System.out.println(\"\" + i);");
//                }
//                jos.endFor();
//
//                jos.emptyLine();
//
//                jos.begForShort("String propName", "System.getProperties().propertyNames()");
//                {
//                    jos.contentln("System.out.println(propName + \" = \" + System.getProperty(propName));");
//                }
//                jos.endFor();
//
//                jos.emptyLine();
//
//                jos.contentln("System.out.println(\"This will happen!\");");
//            }
//            jos.endMethod();
//        }
//        jos.endClass();
//
//        if (!jos.checkError()) {
//            System.out.println("Class written OK!");
//        }
//        jos.flush();
//        jos.close();
//
//
//        System.out.println(baos.toString());
//    }
}
