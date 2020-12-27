/*====================================================================*\

IFunction2.java

Interface: function with two parameters.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.function;

//----------------------------------------------------------------------


// INTERFACE: FUNCTION WITH TWO PARAMETERS


/**
 * This functional interface defines the method that must be implemented by a function with two parameters.
 *
 * @param <R>
 *          the type of the return value.
 * @param <T1>
 *          the type of the first parameter.
 * @param <T2>
 *          the type of the second parameter.
 */

@FunctionalInterface
public interface IFunction2<R, T1, T2>
{

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Invokes this function with the specified arguments.
	 *
	 * @param  arg1
	 *           the first argument.
	 * @param  arg2
	 *           the second argument.
	 * @return the result of applying this function to <i>arg1</i> and <i>arg2</i>.
	 */

	R invoke(T1 arg1,
			 T2 arg2);

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
