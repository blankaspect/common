/*====================================================================*\

Tokeniser.java

Class: tokeniser.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.misc;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

import uk.blankaspect.common.function.IFunction1;

//----------------------------------------------------------------------


// CLASS: TOKENISER


/**
 * This class provides a means of extracting subsequences from an input sequence of characters.  A tokeniser may extract
 * either <i>tokens</i> or <i>fields</i> according to the constructor that is called.  Tokens and fields differ in the
 * following respects:
 * <ul>
 *   <li>Adjacent <b>tokens</b> are separated by one or more characters that are tested with a function that is
 *       specified in the constructor, and the end of the input sequence is tested with another specified function.
 *       Typical use cases are a script or a command line.</li>
 *   <li>Adjacent <b>fields</b> are separated by a single character that is specified in the constructor, and the end of
 *       the input sequence is denoted by another specified character.  A typical use case is the extraction of fields
 *       from a file of comma-separated values, where the input sequence is a single line of text from the file.</li>
 * </ul>
 * A tokeniser can be used in a mode in which a subsequence may be <i>quoted</i> (enclosed in quotation marks (U+0022))
 * to enable it to contain separator characters.  The enclosing quotation marks are removed when the subsequence is
 * extracted.  Within a quoted subsequence, a quotation mark may be escaped by prefixing another quotation mark to it.
 */

public class Tokeniser
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The character that, when extracting fields, is used instead of a character that denotes the end of the input
		sequence on the assumption that the input sequence will not contain this character. */
	public static final	char	INVALID_CHAR	= '\uFFFE';

	/**
	 * The states of the finite state machine that extracts <i>fields</i> from an input sequence.
	 */
	private enum FieldState
	{
		START_OF_FIELD,
		FIELD,
		QUOTATION,
		QUOTATION_PENDING,
		END_OF_FIELD,
		STOP
	}

	/**
	 * The states of the finite state machine that extracts <i>tokens</i> from an input sequence.
	 */
	private enum TokenState
	{
		START_OF_TOKEN,
		TOKEN,
		QUOTATION,
		QUOTATION_PENDING,
		END_OF_QUOTATION,
		END_OF_TOKEN,
		STOP
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: TOKEN


	/**
	 * This class encapsulates a subsequence of the input sequence of a {@link Tokeniser}.  A subsequence is either a
	 * token or a field.
	 */

	public static class Token
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The text of this token. */
		private	String	text;

		/** Flag: if {@code true}, this token was enclosed in quotation marks (U+0022) in the input sequence. */
		private	boolean	quoted;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an empty token.
		 */

		public Token()
		{
			// Call alternative constructor
			this("");
		}

		//--------------------------------------------------------------

		/**
		 * Creates a new instance of a token with the specified text.
		 *
		 * @param text
		 *          the text of the token.
		 */

		public Token(String text)
		{
			// Initialise instance variables
			this.text = text;
		}

		//--------------------------------------------------------------

		/**
		 * Creates a new instance of a token with the specified text.
		 *
		 * @param text
		 *          the text of the token.
		 * @param quoted
		 *          if {@code true}, the token was enclosed in quotation marks (U+0022) in the input sequence.
		 */

		public Token(String  text,
					 boolean quoted)
		{
			// Call alternative constructor
			this(text);

			// Initialise remaining instance variables
			this.quoted = quoted;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the raw text of this token.
		 */

		@Override
		public String toString()
		{
			return getRawText();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the text of this token.
		 *
		 * @return the text of this token.
		 */

		public String getText()
		{
			return text;
		}

		//--------------------------------------------------------------

		/**
		 * Returns the raw text of this token; that is, the text as it was found in the input sequence (ie, enclosed in
		 * quotation marks, if appropriate).
		 *
		 * @return the raw text of this token.
		 */

		public String getRawText()
		{
			return quoted ? "\"" + text.replace("\"", "\"\"") + "\"" : text;
		}

		//--------------------------------------------------------------

		/**
		 * Returns {@code true} if this token was enclosed in quotation marks (U+0022) in the input sequence.
		 *
		 * @return {@code true} if this token was enclosed in quotation marks (U+0022) in the input sequence.
		 */

		public boolean isQuoted()
		{
			return quoted;
		}

		//--------------------------------------------------------------

		/**
		 * Returns {@code true} if the text of this token matches the specified string.
		 *
		 * @param  str
		 *           the string against which the text of this token will be matched.
		 * @return {@code true} if the text of this token matches the specified string.
		 */

		public boolean matches(String str)
		{
			return text.equals(str);
		}

		//--------------------------------------------------------------

		/**
		 * Returns {@code true} if the raw text of this token matches the specified string.
		 *
		 * @param  str
		 *           the string against which the raw text of this token will be matched.
		 * @return {@code true} if the raw text of this token matches the specified string.
		 * @see    #getRawText()
		 */

		public boolean matchesRaw(String str)
		{
			return getRawText().equals(str);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: UNCLOSED QUOTATION EXCEPTION


	/**
	 * This class implements an exception that is thrown when a subsequence starts with a quotation mark (U+0022) but
	 * does not end with a corresponding quotation mark.
	 */

	public static class UnclosedQuotationException
		extends RuntimeException
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an <i>unclosed quotation</i> exception.
		 *
		 * @param index
		 *          the index of the subsequence at which the exception occurred.
		 */

		private UnclosedQuotationException(int index)
		{
			// Call superclass constructor
			super(Integer.toString(index));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The character that separates adjacent fields. */
	private	char							fieldSeparatorChar;

	/** The character that denotes the end of the input sequence. */
	private	char							inputEndChar;

	/** The test that is performed on a character to determine whether it separates adjacent tokens. */
	private	IFunction1<Boolean, Character>	tokenSeparatorTest;

	/** The test that is performed on a character to determine whether it denotes the end of the input sequence. */
	private	IFunction1<Boolean, Character>	inputEndTest;

	/** The input sequence. */
	private	CharSequence					sequence;

	/** The index of the current character in the input sequence. */
	private	int								sequenceIndex;

	/** The index of the previous token or field in the input sequence. */
	private	int								prevSubsequenceIndex;

	/** The index of the end of the input sequence. */
	private	int								sequenceEndIndex;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a tokeniser that extracts <i>fields</i> from an input sequence of characters.
	 *
	 * @param fieldSeparatorChar
	 *          the character that separates adjacent fields in the input sequence.
	 */

	public Tokeniser(char fieldSeparatorChar)
	{
		// Call alternative constructor
		this(fieldSeparatorChar, INVALID_CHAR);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a tokeniser that extracts <i>fields</i> from an input sequence of characters.
	 *
	 * @param fieldSeparatorChar
	 *          the character that separates adjacent fields in the input sequence.
	 * @param inputEndChar
	 *          the character that denotes the end of the input sequence.
	 */

	public Tokeniser(char fieldSeparatorChar,
					 char inputEndChar)
	{
		// Initialise instance variables
		sequence = "";
		this.fieldSeparatorChar = fieldSeparatorChar;
		this.inputEndChar = inputEndChar;
		sequenceEndIndex = -1;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a tokeniser that extracts <i>tokens</i> from an input sequence of characters.
	 *
	 * @param  tokenSeparatorTest
	 *           the test that will be performed on a character to determine whether it separates adjacent tokens.
	 * @throws IllegalArgumentException
	 *           if <i>tokenSeparatorTest</i> is {@code null}.
	 */

	public Tokeniser(IFunction1<Boolean, Character> tokenSeparatorTest)
	{
		// Call alternative constructor
		this(tokenSeparatorTest, null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a tokeniser that extracts <i>tokens</i> from an input sequence of characters.
	 *
	 * @param  tokenSeparatorTest
	 *           the test that will be performed on a character to determine whether it separates adjacent tokens.
	 * @param  inputEndTest
	 *           the test that will be performed on a character to determine whether it denotes the end of the input
	 *           sequence.
	 * @throws IllegalArgumentException
	 *           if <i>tokenSeparatorTest</i> is {@code null}.
	 */

	public Tokeniser(IFunction1<Boolean, Character> tokenSeparatorTest,
					 IFunction1<Boolean, Character> inputEndTest)
	{
		// Validate arguments
		if (tokenSeparatorTest == null)
			throw new IllegalArgumentException("Token-separator test is null");

		// Initialise instance variables
		sequence = "";
		this.tokenSeparatorTest = tokenSeparatorTest;
		this.inputEndTest = (inputEndTest == null) ? ch -> false : inputEndTest;
		sequenceEndIndex = -1;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the input sequence to the specified value.
	 *
	 * @param sequence
	 *          the value to which the input sequence will be set.
	 */

	public void setSequence(CharSequence sequence)
	{
		// Update instance variable
		this.sequence = sequence;

		// Reset to start of input sequence
		reset();
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the next subsequence from the input sequence and returns the result.  Quotation marks (U+0022) have no
	 * special role.
	 *
	 * @return the next subsequence from the input sequence.
	 */

	public Token next()
	{
		return next(false);
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the next subsequence from the input sequence and returns the result.
	 *
	 * @param  quotingEnabled
	 *           if {@code true} and a subsequence is enclosed in quotation marks (U+0022), the quotation marks will be
	 *           removed, and pairs of quotation marks within the subsequence will be replaced by a single character.
	 * @return the next subsequence from the input sequence.
	 * @throws UnclosedQuotationException
	 *           if the next token contains an unclosed quotation.
	 */

	public Token next(boolean quotingEnabled)
	{
		// Update 'previous sequence' index
		prevSubsequenceIndex = sequenceIndex;

		// Return next field or token
		return (tokenSeparatorTest == null) ? nextField(quotingEnabled) : nextToken(quotingEnabled);
	}

	//------------------------------------------------------------------

	/**
	 * Resets this tokeniser to the start of its input sequence.
	 */

	public void reset()
	{
		sequenceIndex = 0;
		prevSubsequenceIndex = 0;
		sequenceEndIndex = -1;
	}

	//------------------------------------------------------------------

	/**
	 * Reverses the effect of the last call to either {@link #next()} or {@link #next(boolean)}.  This method may be
	 * applied only once to any subsequence; that is, it cannot be used to move backwards by more than one subsequence.
	 */

	public void putBack()
	{
		sequenceIndex = prevSubsequenceIndex;
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the subsequences (tokens or fields) of the input sequence and returns a list of the subsequences.
	 * Quotation marks (U+0022) have no special role.
	 *
	 * @return a list of the subsequences (tokens or fields) of the input sequence.
	 */

	public List<Token> getTokens()
	{
		return getTokens(false);
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the subsequences (tokens or fields) of the input sequence and returns a list of the subsequences.
	 *
	 * @param  quotingEnabled
	 *           if {@code true} and a subsequence is enclosed in quotation marks (U+0022), the quotation marks will be
	 *           removed, and pairs of quotation marks within the subsequence will be replaced by a single character.
	 * @return a list of the subsequences (tokens or fields) of the input sequence.
	 */

	public List<Token> getTokens(boolean quotingEnabled)
	{
		reset();
		return getRemainingTokens(quotingEnabled);
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the subsequences (tokens or fields) of the input sequence, starting from the current position (ie, from
	 * the subsequence that would be returned by a call to {@link #next()} or {@link #next(boolean)}), and returns a
	 * list of the subsequences.  Quotation marks (U+0022) have no special role.
	 *
	 * @return a list of the subsequences (tokens or fields) of the input sequence, starting from the current position.
	 */

	public List<Token> getRemainingTokens()
	{
		return getRemainingTokens(false);
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the subsequences (tokens or fields) of the input sequence, starting from the current position (ie, from
	 * the subsequence that would be returned by a call to {@link #next()} or {@link #next(boolean)}), and returns a
	 * list of the subsequences.
	 *
	 * @param  quotingEnabled
	 *           if {@code true} and a subsequence is enclosed in quotation marks (U+0022), the quotation marks will be
	 *           removed, and pairs of quotation marks within the subsequence will be replaced by a single character.
	 * @return a list of the subsequences (tokens or fields) of the input sequence, starting from the current position.
	 */

	public List<Token> getRemainingTokens(boolean quotingEnabled)
	{
		// Initialise list of subsequences
		List<Token> tokens = new ArrayList<>();

		// Extract subsequences from input sequence
		while (true)
		{
			Token token = next(quotingEnabled);
			if (token == null)
				break;
			tokens.add(token);
		}

		// Return list of subsequences
		return tokens;
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the subsequences (tokens or fields) of the input sequence and returns a list of the text of the
	 * subsequences.  Quotation marks (U+0022) have no special role.
	 *
	 * @return a list of the text of the subsequences (tokens or fields) of the input sequence.
	 */

	public List<String> getTokenText()
	{
		return getTokenText(false);
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the subsequences (tokens or fields) of the input sequence and returns a list of the text of the
	 * subsequences.
	 *
	 * @param  quotingEnabled
	 *           if {@code true} and a subsequence is enclosed in quotation marks (U+0022), the quotation marks will be
	 *           removed, and pairs of quotation marks within the subsequence will be replaced by a single character.
	 * @return a list of the text of the subsequences (tokens or fields) of the input sequence.
	 */

	public List<String> getTokenText(boolean quotingEnabled)
	{
		reset();
		return getRemainingTokenText(quotingEnabled);
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the subsequences (tokens or fields) of the input sequence, starting from the current position (ie, from
	 * the subsequence that would be returned by a call to {@link #next()} or {@link #next(boolean)}), and returns a
	 * list of the text of the subsequences.  Quotation marks (U+0022) have no special role.
	 *
	 * @return a list of the text of the subsequences (tokens or fields) of the input sequence, starting from the
	 *         current position.
	 */

	public List<String> getRemainingTokenText()
	{
		return getRemainingTokenText(false);
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the subsequences (tokens or fields) of the input sequence, starting from the current position (ie, from
	 * the subsequence that would be returned by a call to {@link #next()} or {@link #next(boolean)}), and returns a
	 * list of the text of the subsequences.
	 *
	 * @param  quotingEnabled
	 *           if {@code true} and a subsequence is enclosed in quotation marks (U+0022), the quotation marks will be
	 *           removed, and pairs of quotation marks within the subsequence will be replaced by a single character.
	 * @return a list of the text of the subsequences (tokens or fields) of the input sequence, starting from the
	 *         current position.
	 */

	public List<String> getRemainingTokenText(boolean quotingEnabled)
	{
		// Initialise list of subsequences
		List<String> tokens = new ArrayList<>();

		// Extract subsequences from input sequence
		while (true)
		{
			Token token = next(quotingEnabled);
			if (token == null)
				break;
			tokens.add(token.text);
		}

		// Return list of subsequences
		return tokens;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the residual text that follows the end of the input sequence.
	 *
	 * @return the residual text that follows the end of the input sequence, or {@code null} if the end of the input
	 *         sequence has not been reached.
	 */

	public String getResidue()
	{
		return (sequenceEndIndex < 0) ? null : sequence.subSequence(sequenceEndIndex, sequence.length()).toString();
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the next field from the input sequence and returns it.
	 *
	 * @param  quotingEnabled
	 *           if {@code true} and a field is enclosed in quotation marks (U+0022), the quotation marks will be
	 *           removed, and pairs of quotation marks within the field will be replaced by a single character.
	 * @return the next field from the input sequence, or {@code null} if the end of the input sequence has been
	 *         reached.
	 * @throws UnclosedQuotationException
	 *           if <i>quotingEnabled</i> is {@code true} and the next field starts with a quotation mark (U+0022) but
	 *           does not end with a corresponding quotation mark.
	 */

	private Token nextField(boolean quotingEnabled)
	{
		// Initialise variables
		String field = null;
		boolean quoted = false;
		StringBuilder buffer = new StringBuilder(128);

		// Process input sequence with finite state machine
		FieldState state = FieldState.START_OF_FIELD;
		while (state != FieldState.STOP)
		{
			switch (state)
			{
				case START_OF_FIELD:
					if (sequenceIndex < sequence.length())
					{
						char ch = sequence.charAt(sequenceIndex);
						if (ch == inputEndChar)
						{
							sequenceEndIndex = sequenceIndex;
							state = FieldState.STOP;
						}
						else
						{
							++sequenceIndex;
							if (ch == fieldSeparatorChar)
								state = FieldState.END_OF_FIELD;
							else if (quotingEnabled && (ch == '"'))
							{
								quoted = true;
								state = FieldState.QUOTATION;
							}
							else
							{
								buffer.append(ch);
								state = FieldState.FIELD;
							}
						}
					}
					else
					{
						sequenceEndIndex = sequenceIndex;
						state = FieldState.STOP;
					}
					break;

				case FIELD:
					if (sequenceIndex < sequence.length())
					{
						char ch = sequence.charAt(sequenceIndex);
						if (ch == inputEndChar)
						{
							sequenceEndIndex = sequenceIndex;
							state = FieldState.END_OF_FIELD;
						}
						else
						{
							++sequenceIndex;
							if (ch == fieldSeparatorChar)
								state = FieldState.END_OF_FIELD;
							else
								buffer.append(ch);
						}
					}
					else
					{
						sequenceEndIndex = sequenceIndex;
						state = FieldState.END_OF_FIELD;
					}
					break;

				case QUOTATION:
					if (sequenceIndex < sequence.length())
					{
						char ch = sequence.charAt(sequenceIndex++);
						if (ch == '"')
							state = FieldState.QUOTATION_PENDING;
						else
							buffer.append(ch);
					}
					else
						throw new UnclosedQuotationException(sequenceIndex - 1);
					break;

				case QUOTATION_PENDING:
					if (sequenceIndex < sequence.length())
					{
						char ch = sequence.charAt(sequenceIndex);
						if (ch == inputEndChar)
						{
							sequenceEndIndex = sequenceIndex;
							state = FieldState.END_OF_FIELD;
						}
						else
						{
							++sequenceIndex;
							if (ch == fieldSeparatorChar)
								state = FieldState.END_OF_FIELD;
							else
							{
								if (ch != '"')
									throw new UnclosedQuotationException(sequenceIndex - 1);
								buffer.append(ch);
								state = FieldState.QUOTATION;
							}
						}
					}
					else
					{
						sequenceEndIndex = sequenceIndex;
						state = FieldState.END_OF_FIELD;
					}
					break;

				case END_OF_FIELD:
					field = buffer.toString();
					state = FieldState.STOP;
					break;

				case STOP:
					// do nothing
					break;
			}
		}

		// Return field
		return (field == null) ? null : new Token(field, quoted);
	}

	//------------------------------------------------------------------

	/**
	 * Extracts the next token from the input sequence and returns it.
	 *
	 * @param  quotingEnabled
	 *           if {@code true} and a token is enclosed in quotation marks (U+0022), the quotation marks will be
	 *           removed, and pairs of quotation marks within the token will be replaced by a single character.
	 * @return the next token from the input sequence, or {@code null} if the end of the input sequence has been
	 *         reached.
	 * @throws UnclosedQuotationException
	 *           if <i>quotingEnabled</i> is {@code true} and the next token starts with a quotation mark (U+0022) but
	 *           does not end with a corresponding quotation mark.
	 */

	private Token nextToken(boolean quotingEnabled)
	{
		// Initialise variables
		String token = null;
		boolean quoted = false;
		StringBuilder buffer = new StringBuilder(128);

		// Process input sequence with finite state machine
		TokenState state = TokenState.START_OF_TOKEN;
		while (state != TokenState.STOP)
		{
			switch (state)
			{
				case START_OF_TOKEN:
					if (sequenceIndex < sequence.length())
					{
						char ch = sequence.charAt(sequenceIndex);
						if (!inputEndTest.invoke(ch))
						{
							++sequenceIndex;
							if (!tokenSeparatorTest.invoke(ch))
							{
								if (quotingEnabled && (ch == '"'))
								{
									quoted = true;
									state = TokenState.QUOTATION;
								}
								else
								{
									buffer.append(ch);
									state = TokenState.TOKEN;
								}
							}
						}
						else
						{
							sequenceEndIndex = sequenceIndex;
							state = TokenState.STOP;
						}
					}
					else
					{
						sequenceEndIndex = sequenceIndex;
						state = TokenState.STOP;
					}
					break;

				case TOKEN:
					if (sequenceIndex < sequence.length())
					{
						char ch = sequence.charAt(sequenceIndex);
						if (!inputEndTest.invoke(ch))
						{
							++sequenceIndex;
							if (!tokenSeparatorTest.invoke(ch))
								buffer.append(ch);
							else
								state = TokenState.END_OF_TOKEN;
						}
						else
						{
							sequenceEndIndex = sequenceIndex;
							state = TokenState.END_OF_TOKEN;
						}
					}
					else
					{
						sequenceEndIndex = sequenceIndex;
						state = TokenState.END_OF_TOKEN;
					}
					break;

				case QUOTATION:
					if (sequenceIndex < sequence.length())
					{
						char ch = sequence.charAt(sequenceIndex++);
						if (ch == '"')
							state = TokenState.QUOTATION_PENDING;
						else
							buffer.append(ch);
					}
					else
						throw new UnclosedQuotationException(sequenceIndex - 1);
					break;

				case QUOTATION_PENDING:
					if (sequenceIndex < sequence.length())
					{
						char ch = sequence.charAt(sequenceIndex);
						if (!inputEndTest.invoke(ch))
						{
							++sequenceIndex;
							if (!tokenSeparatorTest.invoke(ch))
							{
								if (ch != '"')
									throw new UnclosedQuotationException(sequenceIndex - 1);
								buffer.append(ch);
								state = TokenState.QUOTATION;
							}
							else
								state = TokenState.END_OF_QUOTATION;
						}
						else
							state = TokenState.END_OF_QUOTATION;
					}
					else
					{
						sequenceEndIndex = sequenceIndex;
						state = TokenState.END_OF_TOKEN;
					}
					break;

				case END_OF_QUOTATION:
					token = buffer.toString();
					state = TokenState.STOP;
					break;

				case END_OF_TOKEN:
					if (buffer.length() > 0)
						token = buffer.toString();
					state = TokenState.STOP;
					break;

				case STOP:
					// do nothing
					break;
			}
		}

		// Return token
		return (token == null) ? null : new Token(token, quoted);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
