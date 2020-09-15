/*====================================================================*\

PathnameUtils.java

Class: pathname-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.filesystem;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.property.PropertyString;

//----------------------------------------------------------------------


// CLASS: PATHNAME-RELATED UTILITY METHODS


/**
 * This class contains utility methods that relate to pathnames.
 */

public class PathnameUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	String	USER_HOME_PREFIX	= "~";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private PathnameUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static String parsePathname(String str)
	{
		if (str.startsWith(USER_HOME_PREFIX))
		{
			int prefixLength = USER_HOME_PREFIX.length();
			if ((str.length() == prefixLength) || (str.charAt(prefixLength) == File.separatorChar)
				|| (str.charAt(prefixLength) == '/'))
			try
			{
				String pathname = SystemUtils.getUserHomePathname();
				if (pathname != null)
					str = pathname + str.substring(prefixLength);
			}
			catch (SecurityException e)
			{
				// ignore
			}
		}
		return PropertyString.parse(str);
	}

	//------------------------------------------------------------------

	public static String toUnixStyle(String  pathname,
									 boolean abbreviateUserHome)
	{
		// If pathname starts with user's home directory, replace it with '~'
		if (abbreviateUserHome)
		{
			String userHome = SystemUtils.getUserHomePathname();
			if ((userHome != null) && pathname.startsWith(userHome))
				pathname = PathnameUtils.USER_HOME_PREFIX + pathname.substring(userHome.length());
		}

		// Replace non-Unix file separators with Unix separators
		return pathname.replace(File.separatorChar, '/');
	}

	//------------------------------------------------------------------

	public static String toLocalStyle(String pathname)
	{
		if (File.separatorChar != '/')
		{
			// Replace Unix file separators with local separators
			pathname = pathname.replace('/', File.separatorChar);

			// If pathname starts with '~', replace it with user's home directory
			if (pathname.startsWith(USER_HOME_PREFIX))
			{
				String userHome = SystemUtils.getUserHomePathname();
				if (userHome != null)
					pathname = userHome + pathname.substring(USER_HOME_PREFIX.length());
			}
		}

		return pathname;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
