/*
    Copyright © 2011, Ingo Wechsung
    All rights reserved.
    
    Redistribution and use in source and binary forms, with or
    without modification, are permitted provided that the following
    conditions are met:
    
        Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
    
        Redistributions in binary form must reproduce the above
        copyright notice, this list of conditions and the following
        disclaimer in the documentation and/or other materials provided
        with the distribution. Neither the name of the copyright holder
        nor the names of its contributors may be used to endorse or
        promote products derived from this software without specific
        prior written permission. 
        
    THIS SOFTWARE IS PROVIDED BY THE
    COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
    SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
    LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
    USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
    AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
    LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
    IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
    THE POSSIBILITY OF SUCH DAMAGE.
 
 */

package frege.runtime;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;


/**
 * Miscellanous functions and methods needed in frege.
 * 
 * @author ingo
 *
 */
public class Runtime {

	/**
	 * Implementation for 
	 * <code>pure native constructor frege.runtime.Runtime.constructor :: a -> Int</code>
	 */
	final public static int constructor(Value v) {		// if it is statically known that v is a Value 
		return v._constructor(); 
	}
	// final public static int constructor(Integer v) { return v; }
	final public static int constructor(Object v) { 	// if v is completely unknown, it could still be a Value
		if (v instanceof Value) return ((Value)v)._constructor();
		if (v instanceof Integer) return ((Integer)v).intValue();
		return 0;
	}
	
	/**
	 * Implementation for frege.prelude.PreludeBase.getClass
	 */
	final public static Class<?> getClass(Object o) {
		return o.getClass();
	}
	
	/**
	 * Provide UTF-8 encoded standard printer for stdout with automatic line flushing
	 * <p>Must be thread local so that it works in the online REPL, for instance. </p>
	 */
	public static ThreadLocal<PrintWriter> stdout = new ThreadLocal<PrintWriter>() {
		@Override protected PrintWriter initialValue() {
			return new PrintWriter(
					new OutputStreamWriter(System.out, StandardCharsets.UTF_8),
					true);
		}
	};
	
	/**
	 * Provide UTF-8 encoded standard printer for stderr with autoflush
	 *  <p>Must be thread local so that it works in the online REPL, for instance. </p>
	 */
	public static ThreadLocal<PrintWriter> stderr = new ThreadLocal<PrintWriter>() {
		@Override protected PrintWriter initialValue() {
			return new PrintWriter(
					new OutputStreamWriter(System.err, StandardCharsets.UTF_8),
					true);
		}
	};
	
	
	/**
	 * Provide UTF-8 decoded standard inupt Reader
	 */
	public static ThreadLocal<BufferedReader> stdin = new ThreadLocal<BufferedReader>() {
		@Override protected BufferedReader initialValue() {
			return new BufferedReader(
					new InputStreamReader(System.in, StandardCharsets.UTF_8));
		}
	};
	
    /**
     * <p> Utility method used by <code>String.show</code> to quote a string. </p>
     */
    final public static java.lang.String quoteStr(java.lang.String a) {
		java.lang.StringBuilder sr = new java.lang.StringBuilder(2+((5*a.length()) >> 2));
		sr.append('"');
		int i = 0;
		char c;
		while (i<a.length()) {
			c = a.charAt(i++);
			if (c<' ' || c == '\177') {
				sr.append('\\');
				sr.append(java.lang.String.format("%o", (int) c));
			}
			else if (c == '\\' || c == '"') {
				sr.append('\\');
				sr.append(c);
			}
			else sr.append(c);
		}
		sr.append('"');
		return sr.toString();
	}

    /**
     * <p> Utility method used by <code>Char.show</code> to quote a character. </p>
     */
	final public static java.lang.String quoteChr(char c) {
        java.lang.StringBuilder sr = new java.lang.StringBuilder(8);
        sr.append("'");
        if (c<' ' || c == '\177') {
            sr.append('\\');
            sr.append(java.lang.String.format("%o", (int) c));
        }
        else if (c == '\\' || c == '\'') {
            sr.append('\\');
            sr.append(c);
        }
        else sr.append(c);
        sr.append("'");
        return sr.toString();
    }

	/*
     * <p> This is used to copy a mutable value. </p>
     * <p> The returned value is of same type, but is considered immutable.
     * Suitable for instances of classes that do implement {@link Cloneable}.</p> 
     * <p> Remember that only a deep copy makes a really safe copy .</p> 
     * @param o the value to copy
     * @return a copy obtained by cloning of the argument value, followed by deserialization
     */
    // public final static<T extends Cloneable> T clone(final T o) { return o.clone(); }

    /**
     * <p> This is used to copy a mutable value. </p>
     * <p> The returned value is of same type, but is considered immutable.
     * Suitable for instances of classes that do not implement {@link Cloneable}, 
     * but do implement {@link Serializable}.</p> 
     * @param o the value to copy
     * @return a copy obtained by serialization of the argument value, followed by deserialization
     */
    public final static<T extends Serializable> T copySerializable(final T o) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStream(bais);
            @SuppressWarnings("unchecked")
            final T s = (T) ois.readObject();
            ois.close();
            return s;
        } catch (Exception exc) {
            new Undefined("error in (de)serialization", exc).die();
            return null;
        }
    }

    /**
     * <p> Cheat with phantom types. </p>
     * <p> This method is announced with type String a -&gt; Int -&gt; a
     * but it always returns a char.</p>
     * <p> This is fine as long as nobody is able to actually create a
     * value with type, say String Int. <br>
     * This could be done only with another malicious native function, though.</p>
     */
    public final static Object itemAt(final String value, final int offset) {
        return value.charAt(offset);
    }
    /**
     * <p> The empty polymorphic value of type StringJ a </p>
     * <p> Referenced in frege.prelude.List.ListLike_StringJ
     */
   final public static String emptyString = "";
    
   /**
    *  <p> Run a IO action as main program. </p>
    *    *
    *  <p> Called from the java <tt>main</tt> method of a frege program.
    *  This converts the argument String array to a list and passes this to
    *  the compiled frege main function, if it is a function. 
    *  The result is an IO action of type
    *  <tt>IO a</tt> to which then <tt>IO.performUnsafe</tt> is applied.
    *  The resulting {@link Lambda} then actually executes the frege code
    *  when evaluated.</p>
    *
    */
	public static java.lang.Integer runMain(final Object arg) {
		java.lang.Integer xit = null;
		try {
			Object mainres = Delayed.delayed(arg).call();
			mainres = Delayed.<Object>forced(mainres);
			if (mainres instanceof java.lang.Integer) {
				xit = (java.lang.Integer)mainres;
			}
			else if (mainres instanceof java.lang.Boolean) {
				xit = (boolean)(java.lang.Boolean)mainres ? 0 : 1;
			}
		}
//		catch (Exception ex) {
//			throw new Error(ex); // ex.printStackTrace();
//		}
		finally {
			stderr.get().flush();
			stdout.get().flush();
		}
		return xit;
	}
	
	final public static void exit() {
//		stdout.close();
//		stderr.close();
	}
	
	/**
	 * set this if you need a different exit code
	 */
	private static volatile int exitCode = 0;
	final public static void setExitCode(int a) {
		synchronized (stdin) {
			exitCode = a > exitCode && a < 256 ? a : exitCode; 
		}
	}
}


