/*====================================================================*\

AppException.java

Class: application exception.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.exception;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.indexedsub.IndexedSub;

//----------------------------------------------------------------------


// CLASS: APPLICATION EXCEPTION


public class AppException
	extends Exception
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	DEFAULT_MAX_CAUSE_MESSAGE_LINE_LENGTH	= 160;

	private static final	String	NO_ERROR_STR	= "No error";

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// INTERFACE: EXCEPTION IDENTIFIER


	@FunctionalInterface
	public interface IId
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		String getMessage();

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: ANONYMOUS IDENTIFIER


	protected static class AnonymousId
		implements IId
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		protected AnonymousId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Id interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	int	maxCauseMessageLineLength	= DEFAULT_MAX_CAUSE_MESSAGE_LINE_LENGTH;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	IId				id;
	private	CharSequence[]	replacements;
	private	String			parentPrefix;
	private	String			parentSuffix;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public AppException()
	{
	}

	//------------------------------------------------------------------

	public AppException(String messageStr)
	{
		this(new AnonymousId(messageStr));
	}

	//------------------------------------------------------------------

	public AppException(String          messageStr,
						CharSequence... replacements)
	{
		this(new AnonymousId(messageStr), replacements);
	}

	//------------------------------------------------------------------

	public AppException(String    messageStr,
						Throwable cause)
	{
		this(new AnonymousId(messageStr), cause);
	}

	//------------------------------------------------------------------

	public AppException(String          messageStr,
						Throwable       cause,
						CharSequence... replacements)
	{
		this(new AnonymousId(messageStr), cause, replacements);
	}

	//------------------------------------------------------------------

	public AppException(IId id)
	{
		this(id, (Throwable)null);
	}

	//------------------------------------------------------------------

	public AppException(IId             id,
						CharSequence... replacements)
	{
		this(id);
		setReplacements(replacements);
	}

	//------------------------------------------------------------------

	public AppException(IId       id,
						Throwable cause)
	{
		super(getString(id), cause);
		this.id = id;
	}

	//------------------------------------------------------------------

	public AppException(IId             id,
						Throwable       cause,
						CharSequence... replacements)
	{
		this(id, cause);
		setReplacements(replacements);
	}

	//------------------------------------------------------------------

	public AppException(AppException exception)
	{
		this(exception, false);
	}

	//------------------------------------------------------------------

	public AppException(AppException exception,
						boolean      ignorePrefixAndSuffix)
	{
		this(exception.id, exception.getCause(), exception.replacements);
		parentPrefix = exception.parentPrefix;
		parentSuffix = exception.parentSuffix;
		if (!ignorePrefixAndSuffix)
		{
			String prefix = exception.getPrefix();
			if (prefix != null)
				parentPrefix = (parentPrefix == null) ? prefix : prefix + parentPrefix;

			String suffix = exception.getSuffix();
			if (suffix != null)
				parentSuffix = (parentSuffix == null) ? suffix : suffix + parentSuffix;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int getMaxCauseMessageLineLength()
	{
		return maxCauseMessageLineLength;
	}

	//------------------------------------------------------------------

	public static String getString(IId id)
	{
		return ((id == null) ? NO_ERROR_STR : id.getMessage());
	}

	//------------------------------------------------------------------

	public static void setMaxCauseMessageLineLength(int length)
	{
		maxCauseMessageLineLength = length;
	}

	//------------------------------------------------------------------

	protected static String createString(String         message,
										 String         prefix,
										 String         suffix,
										 CharSequence[] replacements,
										 Throwable      cause)
	{
		// Append the detail message with prefix, suffix and any substitutions
		StringBuilder buffer = new StringBuilder();
		if (message != null)
		{
			if (prefix != null)
				buffer.append(prefix);
			buffer.append((replacements == null) ? message : IndexedSub.sub(message, replacements));
			if (suffix != null)
				buffer.append(suffix);
		}

		// Wrap the text of the detail message of the cause and append the text to the detail message
		while (cause != null)
		{
			String str = cause.getMessage();
			if ((str == null) || (cause instanceof AppException))
				str = cause.toString();
			buffer.append("\n- ");
			int index = 0;
			while (index < str.length())
			{
				boolean space = false;
				int breakIndex = index;
				int endIndex = index + Math.max(1, maxCauseMessageLineLength);
				for (int i = index; (i <= endIndex) || (breakIndex == index); i++)
				{
					if (i == str.length())
					{
						if (!space)
							breakIndex = i;
						break;
					}
					if (str.charAt(i) == ' ')
					{
						if (!space)
						{
							space = true;
							breakIndex = i;
						}
					}
					else
						space = false;
				}
				if (breakIndex - index > 0)
					buffer.append(str.substring(index, breakIndex));
				buffer.append("\n  ");
				for (index = breakIndex; index < str.length(); index++)
				{
					if (str.charAt(index) != ' ')
						break;
				}
			}
			index = buffer.length();
			while (--index >= 0)
			{
				if (!Character.isWhitespace(buffer.charAt(index)))
					break;
			}
			buffer.setLength(++index);

			// Get next exception in chain of causes
			cause = cause.getCause();
		}

		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		// Get the combined prefix
		String prefix = getPrefix();
		if (parentPrefix != null)
			prefix = (prefix == null) ? parentPrefix : prefix + parentPrefix;

		// Get the combined suffix
		String suffix = getSuffix();
		if (parentSuffix != null)
			suffix = (suffix == null) ? parentSuffix : suffix + parentSuffix;

		// Create the string from its components
		return createString(getMessage(), prefix, suffix, replacements, getCause());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public IId getId()
	{
		return id;
	}

	//------------------------------------------------------------------

	public CharSequence getReplacement(int index)
	{
		return replacements[index];
	}

	//------------------------------------------------------------------

	public CharSequence[] getReplacements()
	{
		return replacements;
	}

	//------------------------------------------------------------------

	public void clearReplacements()
	{
		replacements = null;
	}

	//------------------------------------------------------------------

	public void setReplacement(int    index,
							   String str)
	{
		replacements[index] = str;
	}

	//------------------------------------------------------------------

	public void setReplacement(int value)
	{
		setReplacements(Integer.toString(value));
	}

	//------------------------------------------------------------------

	public void setReplacements(CharSequence... strs)
	{
		replacements = strs;
	}

	//------------------------------------------------------------------

	protected String getPrefix()
	{
		return null;
	}

	//------------------------------------------------------------------

	protected String getSuffix()
	{
		return null;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
