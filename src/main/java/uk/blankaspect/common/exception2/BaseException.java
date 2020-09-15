/*====================================================================*\

BaseException.java

Class: base exception.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.exception2;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.indexedsub.IndexedSub;

//----------------------------------------------------------------------


// CLASS: BASE EXCEPTION


/**
 * This class implements a base checked exception.
 */

public class BaseException
	extends Exception
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of an exception with no detail message.
	 */

	protected BaseException()
	{
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an exception with the specified detail message.
	 *
	 * @param message
	 *          the detail message of the exception.
	 * @param replacements
	 *          the character sequences that will replace numbered placeholders in <i>message</i>.
	 */

	public BaseException(String          message,
						 CharSequence... replacements)
	{
		// Call superclass constructor
		super(createMessage(message, replacements));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an exception with the specified detail message and cause.
	 *
	 * @param message
	 *          the detail message of the exception.
	 * @param cause
	 *          the underlying cause of the exception, which may be {@code null}.
	 * @param replacements
	 *          the character sequences that will replace numbered placeholders in <i>message</i>.
	 */

	public BaseException(String          message,
						 Throwable       cause,
						 CharSequence... replacements)
	{
		// Call superclass constructor
		super(createMessage(message, replacements), cause);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a composite message from the specified message and replacement sequences, and returns the result.
	 *
	 * @param  message
	 *           the base message.
	 * @param  replacements
	 *           the character sequences that will replace numbered placeholders in <i>message</i>.
	 * @return the composite message that was created from <i>message</i> and <i>replacements</i>.
	 */

	public static String createMessage(String          message,
									   CharSequence... replacements)
	{
		return IndexedSub.sub(message, replacements);
	}

	//------------------------------------------------------------------

	/**
	 * Tests the cause of the specified exception, and throws the causal exception if it is a {@link BaseException}.
	 *
	 * @param  exception
	 *           the exception of interest.
	 * @throws BaseException
	 *           if the cause of <i>exception</i> is a {@code BaseException}.
	 */

	public static void throwCause(Throwable exception)
		throws BaseException
	{
		throwCause(exception, false);
	}

	//------------------------------------------------------------------

	/**
	 * Tests the cause of the specified exception, or, optionally, searches the causal chain of the specified exception,
	 * and, if a {@link BaseException} is found, throws it.
	 *
	 * @param  exception
	 *           the exception of interest.
	 * @param  traverse
	 *           if {@code true}, the causal chain of <i>exception</i> will be searched for a {@code BaseException};
	 *           otherwise, only the immediate cause will be tested.
	 * @throws BaseException
	 *           <ul>
	 *             <li>if <i>traverse</i> is {@code false} and the cause of <i>exception</i> is a {@code BaseException},
	 *                 or</li>
	 *             <li>if <i>traverse</i> is {@code true} and a {@code BaseException} is found in the causal chain of
	 *                 <i>exception</i>.</li>
	 *           </ul>
	 */

	public static void throwCause(Throwable exception,
								  boolean   traverse)
		throws BaseException
	{
		Throwable cause = exception.getCause();
		while (cause != null)
		{
			if (cause instanceof BaseException)
				throw (BaseException)cause;
			if (!traverse)
				break;
			cause = cause.getCause();
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
