package frege.rt;
// $Author$
// $Date$
// $Rev$
// $Id$
/**
 * <p> Frege functions with arity 3. </p>
 *
 * <p> See {@link Fun1} for a general discussion of function values. </p>
 *
 */
public abstract class Fun3<T1,T2,T3,T4> extends Fun<T1, Fun<T2, Fun<T3, T4>>> {
   /**
     * <p>Apply this function to an argument.</p>
     *
     * <p> This method creates an instance of {@link Fun2} that collects the
     * remaining arguments and, when evaluated, invokes the {@link Fun3#r} method of this
     * function.</p>
     *
     * @return an instance of type <tt>Fun2&lt;T2,T3,T4&gt;</tt> that waits for the
     * remaining arguments to be supplied and calls back with all arguments.
     */
    final public Fun2<T2,T3,T4> a(final Lazy<T1> arg1) {
        return new Fun2<T2,T3,T4> () {
            final public Lazy<T4> r(final Lazy<T3> arg3,final Lazy<T2> arg2) {
                return Fun3.this.r(arg3,arg2,arg1);
            }
        };
    }
    /**
     * <p>Apply this function to all its arguments at once.</p>
     *
     * <p> This method creates an instance of {@link Unknown} that,
     * when evaluated, invokes the {@link Fun3#r} method of this
     * function.</p>
     *
     * Use of this method is preferrable if all arguments are known compared
     * to repeated invokation of the single argument form since intermediate
     * closure creation is saved.
     *
     * @return an instance of type <tt>Unknown&lt;T4&gt;</tt>
     */
    final public Unknown<T4> a(final Lazy<T1> arg1,final Lazy<T2> arg2,final Lazy<T3> arg3) {
        return new Unknown<T4> () {
            final public Lazy<T4> _v() { return Fun3.this.r(arg3,arg2,arg1); }
        };
    }
    /*
     * <p> Always <tt>0</tt> for function values. </p>
     * @return 0
     */
    // final public int     _c() { return 0; }          // interface Value
    /*
     * <p> Return this function object. </p>
     * @return <tt>this</tt>
     */
    // final public Fun3<T1,T2,T3,T4> _e() { return this; }       // interface Lazy
    /*
     * <p> Return this function object. </p>
     * @return <tt>this</tt>
     */
    // final public Fun3<T1,T2,T3,T4> _v() { return this; }       // interface Lazy
    /*
     * <p> Always <tt>false</tt> for function values. </p>
     * @return <tt>false</tt>
     */
    // final public boolean _u() { return false; }      // interface Lazy
    /**
     * <p> Run the function. </p>
     *
     * <p> The run method will be called by the {@link Fun2#r} method
     * of the function value resulting from <tt>this.a(...)</tt>.
     * It actually performs computation and
     * returns a result or another lazy value that will evaluate to the result.<br>
     * This method must be implemented by all subclasses.</p>
     *
     * <p>
     * Note that the arguments must be passed in reverse order. The reason is that
     * in this way the byte code for any intermediate closure will only have to
     * push its argument and invoke the next higher closure's <tt>r</tt> method.
     * A reordering of the arguments on the stack will not be needed. This could save
     * a substantial amounts of memory writes (I hope).
     * </p>
     *
     *
     * @return boxed and possibly lazy result
     */
    abstract public Lazy<T4> r(final Lazy<T3> arg3,final Lazy<T2> arg2, Lazy<T1> arg1);
    /**
     * <p> Coerce the function to another type. </p>
     * <p> I see no other way to get around the limitations of the java type system, sorry.</p>
     * <p> This will be used in the case of constructor classes.</p>
     */
    @SuppressWarnings("unchecked")
    public final <X1,X2,X3,X4> Fun3<X1,X2,X3,X4> coerce() { return (Fun3<X1,X2,X3,X4>) this; }

}