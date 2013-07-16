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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * This is a PrintStream subclass that provides functionallity to simplify code generation. It is mainly
 * intended to be subclassed with output streams for specific languages. Example: JavaSourceOutputStream.
 * <p>
 * This class supports the following features:
 * <h2>Indentation</h2>
 * It keeps an indentation level supported by the following methods:
 * <ul>
 * <li><a href="#incrementIndent()">incrementIndent()</a></li>
 * <li><a href="#decrementIndent()">decrementIndent()</a></li>
 * <li><a href="#indent()">indent()</a> - writes the current indentation level.</li>
 * <li>contentln(String text) - Calls indent(), prints the text, and ends with a newline.</li>
 * </ul>
 *
 * <h2>Support for lists of items</h2>
 *
 * <h3>Comma</h3>
 * When code generating comma separated lists are quite common. The following 2 methods support
 * comma handling:
 * <ul>
 * <li><a href="#newComma()">newComma()</a></li>
 * <li><a href="#printComma()">printComma()</a></li>
 * </ul>
 * The newComma() method causes the next call to printComma() print and emtpy string (""). Subsequent
 * calls to printComma() will print ", ". Thus printComma() should be used before each comma separated
 * item.
 *
 * <h3>FirstOnly</h3>
 * Sometimes a text needs to be printed only on the first call to a method to for example print a keyword
 * like "throws" or "implements". This can be accomplished with "FirstOnly" though there is a more advanced
 * way of handling this case (see further down). Anyhow this simpler feature is supported by the following
 * methods:
 * <ul>
 * <li><a href="#newFirstOnly(java.lang.String)">newFirstOnly(String text)</a></li>
 * <li><a href="#printFirstOnly()">printFirstOnly()</a></li>
 * </ul>
 * newFirstOnly("...") sets a text. The next call to printFirstOnly() will print that text. Subsequent
 * calls to printFirstOnly() will print "". The newFirstOnly() call must be done in a method that possibly
 * will be followed by a method that needs the text.
 *
 * <h3>More advanced "on first call" handling</h3>
 * The more advanced "on first call" handling is supported by the following methods:
 * <ul>
 * <li><a href="#initializeOnFirst()">initializeOnFirst()</a></li>
 * <li><a href="#onFirst(java.lang.Runnable)">onFirst(Runnable r)</a></li>
 * </ul>
 * initializeOnFirst() should be called in a method that can be followed by methods handling list entries.
 * The list entry handling method then calls onFirst() and provides an inline implementation of Runnable
 * that will be executed on the first call to the method, but not on subsequent calls. Here you have more
 * flexibility since your runnable can execute any Java code. It can print a keyword. It can initialize
 * a comma by calling newComma().
 *
 * <h2>Delayed printing</h2>
 * This is a very special, but extremely useful feature supported by these methods:
 * <ul>
 * <li><a href="#delayedPrint(java.lang.String)">delayedPrint(String text)</a></li>
 * <li><a href="#delayedPrintln(java.lang.String)">delayedPrintln(String text)</a></li>
 * </ul>
 * The text passed to these method will be put in a queue, one entry per call. Any call to other print*(*), format(*)
 * methods will first flush the queue before printing its text. use rawPrint(*) methods to print without flushing. 
 * To make this comprehensible, lets take an example:
 * <p>
 * Take generating a method with zero, one, or many arguments after a '(' and that must end with ") {" (ignoring
 * exception list for now). You can make a beginMethod(...) method that writes all upp to and including '('. This
 * method can end by doing delayedPrint(") {"); Then you have a methodArgument(...) method that supplies one
 * method argument. That method uses rawPrint() for the argument text. It also uses the comma support mentioned
 * above. This method can now be called any number of times including none. When the next print*() call is made
 * the delayed print will be flushed and the method definition will be ended with ") {".
 * <p>
 * Since each call to delayedPrint() is a separate entry in the delayed print queue there are 2 more methods
 * that affect the queue:
 * <ul>
 * <li><a href="#flushFirstDelayedPrint()">flushFirstDelayedPrint()</a></li>
 * <li><a href="#flushAndRemoveFirstDelayedPrint()">flushAndRemoveFirstDelayedPrint()</a></li>
 * </ul>
 * The first method flushes the first entry in the queue without removing the entry, but rather replacing it
 * with "". Subsequent calls to the same method will output nothing, and not affect the queue. The second method
 * both flushes and removes the first entry, making the second entry the new first entry.
 * <p>
 * So what is this useful for ? Well, it allows a method to do multiple delayed prints. If we take the above
 * method definition generation methods as example again, and this time also handle an exception list. In this
 * case doing
 * <pre>
 *     delayedPrint(") {");
 * </pre>
 * will not work very well since potential exceptions will need to come between the ')' and the '{'. So in our
 * beginMethod(...) method we end by doing
 * <pre>
 *     delayedPrint(")");
 *     delayedPrint(" {");
 * </pre>
 * instead. Now we also add a methodException(...) method that uses onFirst() to call flushAndRemoveFirstDelayedPrint(), and
 * then outputs the exception using the above mentioned comma support and rawPrint(). The first call will then end the
 * argument list by flushing ")". Subsequent calls will just output the exception. The next method call that calls a flushing
 * print method will flush the rest and end the metod definition with " {". If methodException(...) is never called, both the
 * ")" and the " {" will be flushed togheter at the next flushing print method call.
 */
public class CodeGeneratorOutputStream extends PrintStream {

    //
    // Private Members
    //

    /** A comma handler for comma separated lists. */
    private Comma comma = null;

    /** Is initialized with a value that gets returned only once and after that empty string. */
    private FirstOnly firstOnly = null;

    /** Entries for delayed printing. */
    private List<String> delayedPrintQueue = null;

    /** The current indent. */
    private String indent = "";

    /** The last caller. */
    private String lastCaller = "";

    //
    // Constructors
    //

    /**
     * Creates a new CodeGeneratorOutputStream instance.
     *
     * @param os
     */
    public CodeGeneratorOutputStream(OutputStream os) {
        super(os, true);
    }

    //
    // Methods
    //

    /**
     * Increments the indentation level.
     */
    public void incrementIndent() {
        this.indent = this.indent + "    ";
    }

    /**
     * Decrements the indentation level.
     */
    public void decrementIndent() {
        this.indent = this.indent.substring(0, this.indent.length() - 4);
    }
    
    /**
     * Writes one indent.
     */
    public void indent() {
        flushAllDelayedPrints();
        super.print(this.indent);
    }

    /**
     * Initializes a new comma with an empty string as first value and ", " as subsequent values.
     */
    public void newComma() {
        this.comma = new Comma();
    }

    /**
     * Prints the current comma value.
     */
    public void printComma() {
        super.print(this.comma.toString());
    }

    /**
     * Initializes a new "first only" with the specified value. The value will be returned on
     * the first printFirstOnly() and subsequent calls will print an empty string.
     *
     * @param value The value to initialize with.
     */
    public void newFirstOnly(String value) {
        this.firstOnly = new FirstOnly(value);
    }

    /**
     * Print current first value.
     */
    public void printFirstOnly() {
        super.print(this.firstOnly.toString());
    }

    /**
     * Delays printing of text until next non raw print.
     * 
     * @param text The text to print.
     */
    public void delayedPrint(String text) {
        if (this.delayedPrintQueue == null) {
            this.delayedPrintQueue = new ArrayList<String>();
        }
        this.delayedPrintQueue.add(text);
    }

    /**
     * Delays printing of text until next non raw print.
     *
     * @param text The text to print.
     */
    public void delayedPrintln(String text) {
        delayedPrint(text + "\n");
    }

    /**
     * Flushes any delayed text.
     */
    private void flushAllDelayedPrints() {
        if (this.delayedPrintQueue != null) {
            for (String value : this.delayedPrintQueue) {
                super.print(value);
            }
            this.delayedPrintQueue = null;
        }
    }

    /**
     * Flushes the first delayed print, not removing it but changeing it to "" so that subsequent calls will return "".
     */
    public void flushFirstDelayedPrint() {
        if (this.delayedPrintQueue != null) {
            super.print(this.delayedPrintQueue.get(0));
            this.delayedPrintQueue.set(0, "");
        }
    }

    /**
     * Flushes the first delayed print and removes it so that next call to this or flushFirstDelayedPrint() will
     * return the next delayed print.
     */
    public void flushAndRemoveFirstDelayedPrint() {
        if (this.delayedPrintQueue != null) {
            super.print(this.delayedPrintQueue.remove(0));
            if (this.delayedPrintQueue.isEmpty()) {
                this.delayedPrintQueue = null;
            }
        }
    }

    /**
     * Writes a "content" line like method code or static initializer contents. This will be indented at current indentation
     * level and line terminated.
     *
     * @param line
     */
    public void contentln(String line) {
        indent();
        super.println(line);
    }

    /**
     * Writes an empty line (with indent!)
     */
    public void emptyLine() {
        indent();
        super.println("");
    }

    /**
     * Returns the caller.
     */
    private String getCaller() {
        return getCaller(3);
    }

    /** 
     * Returns the caller.
     */
    private String getCaller(int ix) {
        Exception e = new Exception();
        StackTraceElement[] st = e.getStackTrace();
        return st[ix].getClassName() + "." + st[ix].getMethodName();
    }

    /**
     * This saves the called subclass method and is for using onFirst(). Any method that can be followed by methods
     * called consequtively to provide a list of items should call this to initialize.
     * <p>
     * This allows for execution of certiain code on the first of a set of consequtive calls to the same subclass method.
     * Usually in this case the first call needs to output a keyword like "throws" or "implements" while consecutive calls
     * do not.
     */
    public void initializeOnFirst() {
        this.lastCaller = getCaller(2);
    }

    /**
     * This executes the Runnable if this is the first call to the method calling this in a series of consecutive calls
     * to the method. For this to work initializeOnFirst() needs to be called for other methods not using this method.
     *
     * @param r The Runnable to execute on first call.
     */
    public void onFirst(Runnable r) {
        String caller = getCaller();
        if (!caller.equals(this.lastCaller)) {
            r.run();
        }
        this.lastCaller = caller;
    }

    /**
     * Does the opposite of onFirst(). The runnable is executed on second to n:th consecutive call to the method.
     *
     * @param r The Runnable to execute on non first call.
     */
    public void onNonFirst(Runnable r) {
        String caller = getCaller();
        if (caller.equals(this.lastCaller)) {
            r.run();
        }
        this.lastCaller = caller;
    }

    // Special variable expanding string versions

    /**
     * Expands a String containing variable references using the supplied variable varContext.
     *
     * @param toExpand The string to expand variables for.
     * @param varContext The variables with their values.
     */
    private String expand(String toExpand, Properties varContext) {
        String result = toExpand;

        for (String key : varContext.stringPropertyNames()) {
            String value = varContext.getProperty(key);

            String replace = "${" + key + "}";
            result = replace(result, replace, value);

            String replaceGetter = "${get:" + key + "}";
            String getValue = "get" + value.substring(0, 1).toUpperCase() + value.substring(1) + "()";
            result = replace(result, replaceGetter, getValue);

            String replaceSetter = "${set:" + key + "}";
            String setValue = "set" + value.substring(0, 1).toUpperCase() + value.substring(1);
            result = replace(result, replaceSetter, setValue);
        }

        return result;
    }

    /**
     * I'm having strange problems with String.replace() so I tried doing my own instead.
     *
     * @param toReplaceIn
     * @param from
     * @param to
     */
    private String replace(String toReplaceIn, String from, String to) {
        String result = toReplaceIn;
        int ix = result.indexOf(from);
        while (ix >= 0) {
            result = (result.substring(0, ix) + to + result.substring(ix + from.length()));
            ix = result.indexOf(from);
        }

        return result;
    }

    /**
     * Prints the text with variable expandsion from the variable context.
     * <p>
     * SAPVariable references are in the form of ${name} or ${get:name} which
     * takes the name and converts it to a getter including (), or ${set:name}
     * which takes the name and converts it to a setter <b>not</b> including ().
     * 
     * @param text The text to print.
     * @param varContext The variable context to expand variables from.
     */
    public void print(Properties varContext, String text) {
        print(expand(text, varContext));
    }

    /**
     * Prints the text with variable expandsion from the variable context.
     * <p>
     * SAPVariable references are in the form of ${name} or ${get:name} which
     * takes the name and converts it to a getter including (), or ${set:name}
     * which takes the name and converts it to a setter <b>not</b> including ().
     *
     * @param varContext The variable context to expand variables from.
     * @param texts The texts to print.
     */
    public void println(Properties varContext, String... texts) {
        for (String text : texts) {
            println(expand(text, varContext));
        }
    }

    /**
     * Does a contentln() with variable expandsion from the variable context.
     * <p>
     * SAPVariable references are in the form of ${name} or ${get:name} which
     * takes the name and converts it to a getter including (), or ${set:name}
     * which takes the name and converts it to a setter <b>not</b> including ().
     *
     * @param varContext The variable context to expand variables from.
     * @param texts The texts to contentln()
     */
    public void contentln(Properties varContext, String... texts) {
        for (String text : texts) {
            contentln(expand(text, varContext));
        }
    }

    // Raw, non flushing versions of the print(*) methods.

    public void rawPrint(boolean b) {
        super.print(Boolean.toString(b));
    }

    public void rawPrint(char c) {
        super.print(Character.toString(c));
    }

    public void rawPrint(int i) {
        super.print(Integer.toString(i));
    }

    public void rawPrint(long l) {
        super.print(Long.toString(l));
    }

    public void rawPrint(float f) {
        super.print(Float.toString(f));
    }

    public void rawPrint(double d) {
        super.print(Double.toString(d));
    }

    public void rawPrint(char[] s) {
        for (char c : s) {
            super.print(Character.toString(c));
        }
    }

    public void rawPrint(String s) {
        super.print(s);
    }

    public void rawPrint(Object obj) {
        super.print(obj.toString());
    }

    //
    // Base class overrides
    //

    @Override
    public void print(boolean b) {
        flushAllDelayedPrints();
        super.print(b);
    }

    @Override
    public void print(char c) {
        flushAllDelayedPrints();
        super.print(c);
    }

    @Override
    public void print(int i) {
        flushAllDelayedPrints();
        super.print(i);
    }

    @Override
    public void print(long l) {
        flushAllDelayedPrints();
        super.print(l);
    }

    @Override
    public void print(float f) {
        flushAllDelayedPrints();
        super.print(f);
    }

    @Override
    public void print(double d) {
        flushAllDelayedPrints();
        super.print(d);
    }

    @Override
    public void print(char[] s) {
        flushAllDelayedPrints();
        super.print(s);
    }

    @Override
    public void print(String s) {
        flushAllDelayedPrints();
        super.print(s);
    }

    @Override
    public void print(Object obj) {
        flushAllDelayedPrints();
        super.print(obj);
    }

    @Override
    public PrintStream printf(String format, Object[] args) {
        flushAllDelayedPrints();
        super.printf(format, args);
        return this;
    }

    @Override
    public PrintStream printf(Locale l, String format, Object[] args) {
        flushAllDelayedPrints();
        super.printf(l, format, args);
        return this;
    }

    @Override
    public PrintStream format(String format, Object[] args) {
        flushAllDelayedPrints();
        super.format(format, args);
        return this;
    }

    @Override
    public PrintStream format(Locale l, String format, Object[] args) {
        flushAllDelayedPrints();
        super.format(l, format, args);
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq) {
        flushAllDelayedPrints();
        super.append(csq);
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        flushAllDelayedPrints();
        super.append(csq, start, end);
        return this;
    }

    @Override
    public PrintStream append(char c) {
        flushAllDelayedPrints();
        super.append(c);
        return this;
    }


    //
    // Inner Support Classes
    //

    /**
     * Represents a comma separating a list of elements and is designed to be
     * used before each element rendering "" the first time and ", " consequent
     * times.
     */
    private static class Comma {
        //
        // Private Members
        //

        /** The comma value. */
        private String value ="";

        //
        // Constructors
        //

        /**
         * Creates a new Comma.
         */
        public Comma() {}

        //
        // Methods
        //

        /**
         * Renders the comma value.
         */
        @Override
        public String toString() {
            String comma = this.value;
            this.value = ", ";
            return comma;
        }
    }

    /**
     * Holds a value that gets rendered the first time only. The rest
     * of the time only "" is rendered.
     */
    private static class FirstOnly {
        //
        // Private Members
        //

        /** The value. */
        private String value = null;

        //
        // Constructors
        //

        /**
         * Creates a new FirstOnly.
         *
         * @param value The initial value.
         */
        public FirstOnly(String value) {
            this.value = value;
        }

        //
        // Methods
        //

        /**
         * Renders the first only value.
         */
        @Override
        public String toString() {
            String str = this.value;
            this.value = "";
            return str;
        }
    }
}
