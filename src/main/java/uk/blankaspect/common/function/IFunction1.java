/*====================================================================*\

IFunction1.java

Interface: function with one parameter.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.function;

//----------------------------------------------------------------------


// INTERFACE: FUNCTION WITH ONE PARAMETER


/**
 * This functional interface defines the method that must be implemented by a function with one parameter.
 *
 * @param <R>
 *          the type of the return value.
 * @param <T>
 *          the type of the parameter.
 */

@FunctionalInterface
public interface IFunction1<R, T>
{

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Invokes this function with the specified argument.
	 *
	 * @param  arg
	 *           the argument.
	 * @return the result of applying this function to <i>arg</i>.
	 */

	R invoke(T arg);

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
