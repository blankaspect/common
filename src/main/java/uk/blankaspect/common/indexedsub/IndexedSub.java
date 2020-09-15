/*====================================================================*\

IndexedSub.java

Class: indexed-substitution utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.indexedsub;

//----------------------------------------------------------------------


// CLASS: INDEXED-SUBSTITUTION UTILITY METHODS


/**
 * This class provides methods that substitute specified replacement sequences for occurrences of placeholders in a
 * specified string.  A placeholder has the form "%<i>n</i>", where <i>n</i> is a decimal-digit character in the range
 * '1'..'9'.
 */

public class IndexedSub
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The prefix of a placeholder in an input string. */
	public static final	char	PLACEHOLDER_PREFIX_CHAR	= '%';

	/** The character that represents the minimum index of a substitution. */
	public static final	char	MIN_SUBSTITUTION_INDEX_CHAR	= '1';

	/** The character that represents the maximum index of a substitution. */
	public static final	char	MAX_SUBSTITUTION_INDEX_CHAR	= '9';

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private IndexedSub()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Substitutes the specified replacement sequences for occurrences of placeholders in the specified string, and
	 * returns the resulting string.  A placeholder has the form "%<i>n</i>", where <i>n</i> is a decimal-digit
	 * character in the range '1'..'9'.  Each placeholder is replaced by the specified sequence whose zero-based index
	 * is <i>n</i>-1; for example, the placeholder "%3" will be replaced by the sequence at index 2.  A placeholder that
	 * does not have a corresponding replacement sequence is replaced by an empty string.
	 * <p>
	 * The special role of "%" may be escaped by prefixing another "%" to it (ie, "%%").
	 * </p>
	 *
	 * @param  str
	 *           the string on which substitutions will be performed.
	 * @param  replacements
	 *           the sequences that will replace placeholders in <i>str</i>.
	 * @return a transformation of the input string in which each placeholder is replaced by the element of
	 *         <i>replacements</i> at the corresponding index.
	 */

	public static String sub(String          str,
							 CharSequence... replacements)
	{
		// If there are no replacement sequences, return the input string
		if (replacements.length == 0)
			return str;

		// Allocate a buffer for the output string
		StringBuilder buffer = new StringBuilder(str.length() + 32);

		// Perform substitutions on the input string
		int index = 0;
		while (index < str.length())
		{
			// Set the start index to the end of the last placeholder
			int startIndex = index;

			// Get the index of the next placeholder prefix
			index = str.indexOf(PLACEHOLDER_PREFIX_CHAR, index);

			// If there are no more placeholder prefixes, set the index to the end of the input string
			if (index < 0)
				index = str.length();

			// Get the substring of the input string from the end of the last placeholder to the current placeholder
			// prefix, and append it to the output buffer
			if (index > startIndex)
				buffer.append(str.substring(startIndex, index));

			// Increment the index past the current placeholder prefix
			++index;

			// If the end of the input string has not been reached, process the character after the placeholder prefix
			if (index < str.length())
			{
				// Get the next character from the input string
				char ch = str.charAt(index);

				// If the placeholder prefix is followed by a substitution index, perform a substitution ...
				if ((ch >= MIN_SUBSTITUTION_INDEX_CHAR) && (ch <= MAX_SUBSTITUTION_INDEX_CHAR))
				{
					// Parse the substitution index
					int subIndex = ch - MIN_SUBSTITUTION_INDEX_CHAR;

					// If there is a replacement sequence for the substitution index, append it to the output buffer
					if (subIndex < replacements.length)
					{
						CharSequence replacement = replacements[subIndex];
						if (replacement != null)
							buffer.append(replacement);
					}

					// Increment the index past the substitution index
					++index;
				}

				// ... otherwise, append a placeholder prefix to the output buffer
				else
				{
					// Append a placeholder prefix to the output buffer
					buffer.append(PLACEHOLDER_PREFIX_CHAR);

					// If the placeholder prefix is followed by another one, skip it
					if (ch == PLACEHOLDER_PREFIX_CHAR)
						++index;
				}
			}

			// If the last character in the input string is a placeholder prefix, append it to the output buffer
			else if (index == str.length())
				buffer.append(PLACEHOLDER_PREFIX_CHAR);
		}

		// Return the output string
		return buffer.toString();
	}

	//------------------------------------------------------------------

	/**
	 * Substitutes the decimal string representations of the specified integer values for occurrences of placeholders in
	 * the specified string, and returns the resulting string.  A placeholder has the form "%<i>n</i>", where <i>n</i>
	 * is a decimal-digit character in the range '1'..'9'.  Each placeholder is replaced by the string representation of
	 * the specified value whose zero-based index is <i>n</i>-1; for example, the placeholder "%3" will be replaced by
	 * the string representation of the value at index 2.  A placeholder that does not have a corresponding replacement
	 * value is replaced by an empty string.
	 * <p>
	 * The special role of "%" may be escaped by prefixing another "%" to it (ie, "%%").
	 * </p>
	 *
	 * @param  str
	 *           the string on which substitutions will be performed.
	 * @param  replacementValues
	 *           the values whose decimal string representations will replace placeholders in <i>str</i>.
	 * @return a transformation of the input string in which each placeholder is replaced by the the decimal string
	 *         representation of the element of <i>replacementValues</i> at the corresponding index.
	 */

	public static String sub(String str,
							 int... replacementValues)
	{
		// Convert replacement values to strings
		String[] replacements = new String[replacementValues.length];
		for (int i = 0; i < replacementValues.length; i++)
			replacements[i] = Integer.toString(replacementValues[i]);

		// Perform substitutions
		return sub(str, replacements);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
