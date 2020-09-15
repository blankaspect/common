/*====================================================================*\

ExceptionUtils.java

Class: exception-related utilities.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.exception;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

import uk.blankaspect.common.filesystem.PathnameUtils;

//----------------------------------------------------------------------


// CLASS: EXCEPTION-RELATED UTILITIES


public class ExceptionUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	ELLIPSIS_STR	= "...";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ExceptionUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	boolean	unixStyle;

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static List<String> getCauseStrings(Throwable cause)
	{
		// Initialise list of cause strings
		List<String> causes = new ArrayList<>();

		// Add cause strings to list
		while (cause != null)
		{
			// Get detail message of cause
			String str = cause.getMessage();

			// If there is no detail message, use string representation of cause
			if (str == null)
				str = cause.toString();

			// Add string to list
			causes.add(str);

			// Get next exception in chain of causes
			cause = cause.getCause();
		}

		// Return list of cause strings
		return causes;
	}

	//------------------------------------------------------------------

	public static String getCompositeCauseString(Throwable cause,
												 String    prefix)
	{
		List<String> causeStrs = getCauseStrings(cause);
		return causeStrs.isEmpty() ? "" : causeStrs.stream().collect(Collectors.joining("\n" + prefix, prefix, ""));
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a string representation of the specified instance of {@link Throwable}.
	 *
	 * @param  throwable
	 *           the {@code Throwable} for which a string representation will be created.
	 * @return a string representation of <i>throwable</i>.
	 */

	public static String throwableToString(Throwable throwable)
	{
		// Initialise buffer
		StringBuilder buffer = new StringBuilder(256);

		// Append detail message
		String message = throwable.getMessage();
		if (message != null)
			buffer.append(message);

		// Append string representation of chain of causes
		Throwable cause = throwable.getCause();
		if (cause != null)
		{
			if (buffer.length() > 0)
				buffer.append('\n');
			buffer.append(getCompositeCauseString(cause, "- "));
		}

		// Return string
		return buffer.toString();
	}

	//------------------------------------------------------------------

	public static boolean isUnixStyle()
	{
		return unixStyle;
	}

	//------------------------------------------------------------------

	public static void setUnixStyle(boolean unixStyle)
	{
		ExceptionUtils.unixStyle = unixStyle;
	}

	//------------------------------------------------------------------

	public static String getPathname(File file)
	{
		String pathname = null;
		try
		{
			pathname = file.getCanonicalPath();
		}
		catch (Exception e)
		{
			writeTopOfStack(e);
			pathname = file.getAbsolutePath();
		}
		return pathname;
	}

	//------------------------------------------------------------------

	public static String getLimitedPathname(File file,
											int  maxLength)
	{
		String pathname = getPathname(file);
		if (unixStyle && (pathname != null))
			pathname = PathnameUtils.toUnixStyle(pathname, true);

		return getLimitedPathname(pathname, maxLength);
	}

	//------------------------------------------------------------------

	public static String getLimitedPathname(String pathname,
											int    maxLength)
	{
		// Test for null pathname
		if (pathname == null)
			return null;

		// Split the pathname into its components
		char separatorChar = unixStyle ? '/' : File.separatorChar;
		List<String> strs = new ArrayList<>();
		int index = 0;
		while (index < pathname.length())
		{
			int startIndex = index;
			index = pathname.indexOf(separatorChar, index);
			if (index < 0)
				index = pathname.length();
			if (index > startIndex)
				strs.add(pathname.substring(startIndex, index));
			++index;
		}
		if (strs.isEmpty())
			return pathname;

		// Get the maximum number of components
		StringBuilder buffer = new StringBuilder(ELLIPSIS_STR);
		int numComponents = 0;
		for (int i = strs.size() - 1; i >= 0; i--)
		{
			buffer.append(separatorChar);
			buffer.append(strs.get(i));
			if (buffer.length() > maxLength)
				break;
			++numComponents;
		}

		// If last component is too wide, remove leading characters until it fits
		if (numComponents == 0)
		{
			String str = strs.get(strs.size() - 1);
			return (ELLIPSIS_STR + str.substring(Math.max(0, str.length() - maxLength + ELLIPSIS_STR.length())));
		}

		// If the entire pathname fits, return it
		if (numComponents == strs.size())
			return pathname;

		// Construct a reduced pathname
		buffer = new StringBuilder(ELLIPSIS_STR);
		for (int i = strs.size() - numComponents; i < strs.size(); i++)
		{
			buffer.append(separatorChar);
			buffer.append(strs.get(i));
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

	public static void writeTopOfStack(Exception e)
	{
		// Write exception to standard error stream
		System.err.println(e);

		// Write top of stack to standard error stream
		StackTraceElement[] stackTraceElements = e.getStackTrace();
		if (stackTraceElements.length > 0)
			System.err.println(stackTraceElements[0]);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
