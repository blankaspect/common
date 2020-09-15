/*====================================================================*\

LocationException.java

Class: location exception.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.exception2;

//----------------------------------------------------------------------


// CLASS: LOCATION EXCEPTION


/**
 * This class implements an exception that relates to a location.
 */

public class LocationException
	extends BaseException
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** Miscellaneous strings. */
	private static final	String	LOCATION_STR	= "Location: ";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of an exception with the specified detail message and location.
	 *
	 * @param message
	 *          the detail message of the exception.
	 * @param location
	 *          the location with which the exception will be associated, which may be {@code null}.
	 * @param replacements
	 *          the character sequences that will replace numbered placeholders in <i>message</i>.
	 */

	public LocationException(String          message,
							 String          location,
							 CharSequence... replacements)
	{
		// Call alternative constructor
		this(message, null, location, replacements);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an exception with the specified detail message, cause and location.
	 *
	 * @param message
	 *          the detail message of the exception.
	 * @param cause
	 *          the cause of the exception, which may be {@code null}.
	 * @param location
	 *          the location with which the exception will be associated, which may be {@code null}.
	 * @param replacements
	 *          the character sequences that will replace numbered placeholders in <i>message</i>.
	 */

	public LocationException(String          message,
							 Throwable       cause,
							 String          location,
							 CharSequence... replacements)
	{
		// Call superclass constructor
		super(createMessage(message, location, replacements), cause);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a composite message from the specified components, and returns the result.
	 *
	 * @param  message
	 *           the base message.
	 * @param  location
	 *           the location that will be prefixed to <i>message</i>, which may be {@code null}.
	 * @param  replacements
	 *           the character sequences that will replace numbered placeholders in <i>message</i>.
	 * @return the composite message that was created from <i>message</i>, <i>location</i> and <i>replacements</i>.
	 */

	public static String createMessage(String          message,
									   String          location,
									   CharSequence... replacements)
	{
		return ((location == null) ? "" : LOCATION_STR + location + "\n") + createMessage(message, replacements);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
