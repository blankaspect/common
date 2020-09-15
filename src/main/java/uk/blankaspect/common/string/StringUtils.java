/*====================================================================*\

StringUtils.java

Class: string-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.string;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.stream.Collectors;

//----------------------------------------------------------------------


// CLASS: STRING-RELATED UTILITY METHODS


public class StringUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	char	ESCAPE_PREFIX_CHAR	= '\\';

	public enum SplitMode
	{
		/**
		 * The separator is discarded after splitting.
		 */
		NONE,

		/**
		 * The separator is the last character of the prefix after splitting.
		 */
		PREFIX,

		/**
		 * The separator is the first character of the suffix after splitting.
		 */
		SUFFIX
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private StringUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean isNullOrEmpty(String str)
	{
		return (str == null) || str.isEmpty();
	}

	//------------------------------------------------------------------

	public static boolean equal(String str1,
								String str2)
	{
		return (str1 == null) ? (str2 == null) : str1.equals(str2);
	}

	//------------------------------------------------------------------

	public static int getIndex(String[] strs,
							   String   str)
	{
		for (int i = 0; i < strs.length; i++)
		{
			if (strs[i].equals(str))
				return i;
		}
		return -1;
	}

	//------------------------------------------------------------------

	public static char[] createCharArray(char ch,
										 int  length)
	{
		char[] array = new char[length];
		Arrays.fill(array, ch);
		return array;
	}

	//------------------------------------------------------------------

	public static String createCharString(char ch,
										  int  length)
	{
		return new String(createCharArray(ch, length));
	}

	//------------------------------------------------------------------

	public static int getMaxLength(Iterable<? extends CharSequence> seqs)
	{
		int maxLength = 0;
		for (CharSequence seq : seqs)
		{
			int length = seq.length();
			if (maxLength < length)
				maxLength = length;
		}
		return maxLength;
	}

	//------------------------------------------------------------------

	public static int getMaxLength(String... strs)
	{
		return getMaxLength(Arrays.asList(strs));
	}

	//------------------------------------------------------------------

	public static String stripBefore(CharSequence seq)
	{
		for (int i = 0; i < seq.length(); i++)
		{
			char ch = seq.charAt(i);
			if ((ch != '\t') && (ch != ' '))
				return seq.subSequence(i, seq.length()).toString();
		}
		return "";
	}

	//------------------------------------------------------------------

	public static String stripAfter(CharSequence seq)
	{
		for (int i = seq.length() - 1; i >= 0; i--)
		{
			char ch = seq.charAt(i);
			if ((ch != '\t') && (ch != ' '))
				return seq.subSequence(0, i + 1).toString();
		}
		return "";
	}

	//------------------------------------------------------------------

	public static String padBefore(CharSequence seq,
								   int          length)
	{
		return padBefore(seq, length, ' ');
	}

	//------------------------------------------------------------------

	public static String padBefore(CharSequence seq,
								   int          length,
								   char         ch)
	{
		int padLength = length - seq.length();
		return (padLength > 0) ? createCharString(ch, padLength) + seq.toString() : seq.toString();
	}

	//------------------------------------------------------------------

	public static String padAfter(CharSequence seq,
								  int          length)
	{
		return padAfter(seq, length, ' ');
	}

	//------------------------------------------------------------------

	public static String padAfter(CharSequence seq,
								  int          length,
								  char         ch)
	{
		int padLength = length - seq.length();
		return (padLength > 0) ? seq.toString() + createCharString(ch, padLength) : seq.toString();
	}

	//------------------------------------------------------------------

	public static List<String> split(String str,
									 char   separator)
	{
		return split(str, separator, false);
	}

	//------------------------------------------------------------------

	public static List<String> split(String  str,
									 char    separator,
									 boolean discardFinalEmptyElement)
	{
		List<String> strs = new ArrayList<>();
		int startIndex = 0;
		int endIndex = str.length();
		int index = 0;
		while (index < endIndex)
		{
			index = str.indexOf(separator, index);
			if (index < 0)
				index = endIndex;
			strs.add(str.substring(startIndex, index));
			startIndex = ++index;
		}

		if ((startIndex == endIndex) && !discardFinalEmptyElement)
			strs.add("");
		return strs;
	}

	//------------------------------------------------------------------

	public static String[] splitAt(String    str,
								   int       index,
								   SplitMode splitMode)
	{
		return (index < 0) ? new String[] { str, (splitMode == SplitMode.NONE) ? null : "" }
						   : new String[] { str.substring(0, (splitMode == SplitMode.PREFIX) ? index + 1 : index),
											str.substring((splitMode == SplitMode.SUFFIX) ? index : index + 1) };
	}

	//------------------------------------------------------------------

	public static String[] splitAtFirst(String str,
										char   ch)
	{
		return splitAt(str, str.indexOf(ch), SplitMode.NONE);
	}

	//------------------------------------------------------------------

	public static String[] splitAtFirst(String    str,
										char      ch,
										SplitMode splitMode)
	{
		return splitAt(str, str.indexOf(ch), splitMode);
	}

	//------------------------------------------------------------------

	public static String[] splitAtLast(String str,
									   char   ch)
	{
		return splitAt(str, str.lastIndexOf(ch), SplitMode.NONE);
	}

	//------------------------------------------------------------------

	public static String[] splitAtLast(String    str,
									   char      ch,
									   SplitMode splitMode)
	{
		return splitAt(str, str.lastIndexOf(ch), splitMode);
	}

	//------------------------------------------------------------------

	public static String getPrefixFirst(String str,
										char   ch)
	{
		int index = str.indexOf(ch);
		return (index < 0) ? str : str.substring(0, index);
	}

	//------------------------------------------------------------------

	public static String getPrefixLast(String str,
									   char   ch)
	{
		int index = str.lastIndexOf(ch);
		return (index < 0) ? str : str.substring(0, index);
	}

	//------------------------------------------------------------------

	public static String getSuffixFirst(String str,
										char   ch)
	{
		int index = str.indexOf(ch);
		return (index < 0) ? str : str.substring(index);
	}

	//------------------------------------------------------------------

	public static String getSuffixAfterFirst(String str,
											 char   ch)
	{
		int index = str.indexOf(ch);
		return (index < 0) ? str : str.substring(index + 1);
	}

	//------------------------------------------------------------------

	public static String getSuffixLast(String str,
									   char   ch)
	{
		int index = str.lastIndexOf(ch);
		return (index < 0) ? str : str.substring(index);
	}

	//------------------------------------------------------------------

	public static String getSuffixAfterLast(String str,
											char   ch)
	{
		int index = str.lastIndexOf(ch);
		return (index < 0) ? str : str.substring(index + 1);
	}

	//------------------------------------------------------------------

	public static String removePrefix(String str,
									  String prefix)
	{
		return (str.isEmpty() || prefix.isEmpty() || !str.startsWith(prefix)) ? str : str.substring(prefix.length());
	}

	//------------------------------------------------------------------

	public static String removeSuffix(String str,
									  String suffix)
	{
		return (str.isEmpty() || suffix.isEmpty() || !str.endsWith(suffix))
																? str
																: str.substring(0, str.length() - suffix.length());
	}

	//------------------------------------------------------------------

	public static String join(char      separator,
							  String... strs)
	{
		return join(Character.toString(separator), false, Arrays.asList(strs));
	}

	//------------------------------------------------------------------

	public static String join(char                         separator,
							  List<? extends CharSequence> seqs)
	{
		return join(Character.toString(separator), false, seqs);
	}

	//------------------------------------------------------------------

	public static String join(CharSequence separator,
							  String...    strs)
	{
		return join(separator, false, Arrays.asList(strs));
	}

	//------------------------------------------------------------------

	public static String join(CharSequence                 separator,
							  List<? extends CharSequence> seqs)
	{
		return join(separator, false, seqs);
	}

	//------------------------------------------------------------------

	public static String join(char      separator,
							  boolean   trailingSeparator,
							  String... strs)
	{
		return join(Character.toString(separator), trailingSeparator, Arrays.asList(strs));
	}

	//------------------------------------------------------------------

	public static String join(char                         separator,
							  boolean                      trailingSeparator,
							  List<? extends CharSequence> seqs)
	{
		return join(Character.toString(separator), trailingSeparator, seqs);
	}

	//------------------------------------------------------------------

	public static String join(CharSequence separator,
							  boolean      trailingSeparator,
							  String...    strs)
	{
		return join(separator, trailingSeparator, Arrays.asList(strs));
	}

	//------------------------------------------------------------------

	public static String join(CharSequence                 separator,
							  boolean                      trailingSeparator,
							  List<? extends CharSequence> seqs)
	{
		// Calculate length of buffer
		int length = (separator == null) ? 0 : separator.length() * seqs.size();
		for (CharSequence seq : seqs)
			length += seq.length();

		// Concatenate character sequences
		StringBuilder buffer = new StringBuilder(length);
		for (int i = 0; i < seqs.size(); i++)
		{
			if ((separator != null) && (i > 0))
				buffer.append(separator);
			buffer.append(seqs.get(i));
		}
		if ((separator != null) && trailingSeparator)
			buffer.append(separator);
		return buffer.toString();
	}

	//------------------------------------------------------------------

	/**
	 * Splits the specified text at line separators and returns the resulting list of lines.  The line separators that
	 * are recognised are CR+LF (U+000D, U+000A) and LF (U+000A).  The line separators are not included in the output
	 * list.
	 *
	 * @param  text
	 *           the text that will be split into lines at line separators.
	 * @return a list of the strings that result from splitting <i>text</i> at line separators.
	 */

	public static List<String> extractLines(CharSequence text)
	{
		// Initialise list of lines
		List<String> lines = new ArrayList<>();

		// Extract lines from input sequence
		int index = 0;
		int startIndex = 0;
		char prevCh = '\0';
		while (index < text.length())
		{
			char ch = text.charAt(index);
			if (ch == '\n')
			{
				lines.add(text.subSequence(startIndex, (prevCh == '\r') ? index - 1 : index).toString());
				startIndex = index + 1;
			}
			prevCh = ch;
			++index;
		}
		lines.add(text.subSequence(startIndex, index).toString());

		// Return list of lines
		return lines;
	}

	//------------------------------------------------------------------

	public static String wrap(CharSequence seq,
							  int          maxLineLength)
	{
		return wrapLines(seq, maxLineLength).stream().collect(Collectors.joining("\n"));
	}

	//------------------------------------------------------------------

	public static List<String> wrapLines(CharSequence seq,
										 int          maxLineLength)
	{
		// Initialise list of lines
		List<String> lines = new ArrayList<>();

		// Break input sequence into lines
		int index = 0;
		while (index < seq.length())
		{
			// Initialise loop variables
			boolean wordBreak = false;
			int startIndex = index;
			int endIndex = startIndex + maxLineLength;
			int breakIndex = startIndex;

			// Find next line break
			for (int i = startIndex; (i <= endIndex) || (breakIndex == startIndex); i++)
			{
				// If end of input, mark line break; stop
				if (i == seq.length())
				{
					if (!wordBreak)
						breakIndex = i;
					break;
				}

				// If character is space ...
				if (seq.charAt(i) == ' ')
				{
					// If not already in a word break, mark start of break
					if (!wordBreak)
					{
						wordBreak = true;
						breakIndex = i;
					}
				}

				// ... otherwise, clear 'word break' flag
				else
					wordBreak = false;
			}

			// Add line to list
			if (breakIndex - startIndex > 0)
				lines.add(seq.subSequence(startIndex, breakIndex).toString());

			// Advance to next non-space after line break
			for (index = breakIndex; index < seq.length(); index++)
			{
				if (seq.charAt(index) != ' ')
					break;
			}
		}

		// Return list of lines
		return lines;
	}

	//------------------------------------------------------------------

	public static boolean isAllLowerCase(CharSequence seq)
	{
		for (int i = 0; i < seq.length(); i++)
		{
			if (!Character.isLowerCase(seq.charAt(i)))
				return false;
		}
		return true;
	}

	//------------------------------------------------------------------

	public static boolean isAllUpperCase(CharSequence seq)
	{
		for (int i = 0; i < seq.length(); i++)
		{
			if (!Character.isUpperCase(seq.charAt(i)))
				return false;
		}
		return true;
	}

	//------------------------------------------------------------------

	public static String firstCharToLowerCase(String str)
	{
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}

	//------------------------------------------------------------------

	public static String firstCharsToLowerCase(String str)
	{
		StringBuilder buffer = new StringBuilder(str);
		boolean start = true;
		for (int i = 0; i < buffer.length(); i++)
		{
			char ch = buffer.charAt(i);
			if (start)
				buffer.setCharAt(i, Character.toLowerCase(ch));
			start = (ch == ' ') || (ch == '-');
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

	public static String firstCharToUpperCase(String str)
	{
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	//------------------------------------------------------------------

	public static String firstCharsToUpperCase(String str)
	{
		StringBuilder buffer = new StringBuilder(str);
		boolean start = true;
		for (int i = 0; i < buffer.length(); i++)
		{
			char ch = buffer.charAt(i);
			if (start)
				buffer.setCharAt(i, Character.toUpperCase(ch));
			start = (ch == ' ') || (ch == '-');
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

	/**
	 * Converts the specified text to camel case and returns the resulting string.
	 * <p>
	 * The conversion is described below.  In the description,
	 * <ul>
	 *   <li>a <i>digit</i> is a character for which {@link Character#isDigit(char)} returns {@code true},</li>
	 *   <li>a <i>letter</i> is a character for which {@link Character#isLetter(char)} returns {@code true},</li>
	 *   <li>a <i>non-alphanumeric character</i> is a character that is neither a <i>digit</i> nor a <i>letter</i>.</li>
	 * </ul>
	 * <ol>
	 *   <li>Each letter that immediately follows a digit or a non-alphanumeric character is converted to upper
	 *       case.</li>
	 *   <li>All other letters are converted to lower case.</li>
	 *   <li>All non-alphanumeric characters are removed.</li>
	 * </ol>
	 *
	 * @param  text
	 *           the text that will be converted.
	 * @return the camel-case string that results from the conversion of <i>text</i>.
	 */

	public static String toCamelCase(CharSequence text)
	{
		// Initialise buffer for output string
		StringBuilder buffer = new StringBuilder(text.length());

		// Convert input sequence to camel case
		boolean lowerCase = true;
		for (int i = 0; i < text.length(); i++)
		{
			char ch = text.charAt(i);
			if (Character.isLetter(ch))
			{
				buffer.append(lowerCase ? Character.toLowerCase(ch) : Character.toUpperCase(ch));
				lowerCase = true;
			}
			else
			{
				if (Character.isDigit(ch))
					buffer.append(ch);
				lowerCase = (buffer.length() == 0);
			}
		}

		// Return output string
		return buffer.toString();
	}

	//------------------------------------------------------------------

	public static String applyPrefix(String str,
									 String prefix)
	{
		if (isNullOrEmpty(prefix))
			return str;
		return prefix + (prefix.endsWith("_") ? str : firstCharToUpperCase(str));
	}

	//------------------------------------------------------------------

	public static String escape(CharSequence seq,
								String       metachars)
	{
		StringBuilder buffer = new StringBuilder(2 * seq.length());
		for (int i = 0; i < seq.length(); i++)
		{
			char ch = seq.charAt(i);
			if (metachars.indexOf(ch) >= 0)
				buffer.append(ESCAPE_PREFIX_CHAR);
			buffer.append(ch);
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
