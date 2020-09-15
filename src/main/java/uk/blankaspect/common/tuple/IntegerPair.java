/*====================================================================*\

IntegerPair.java

Class: integer pair.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.tuple;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.exception.UnexpectedRuntimeException;

//----------------------------------------------------------------------


// CLASS: INTEGER PAIR


public class IntegerPair
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int	first;
	private	int	second;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public IntegerPair()
	{
	}

	//------------------------------------------------------------------

	public IntegerPair(int first,
					   int second)
	{
		this.first = first;
		this.second = second;
	}

	//------------------------------------------------------------------

	public IntegerPair(IntegerPair pair)
	{
		first = pair.first;
		second = pair.second;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @throws NumberFormatException
	 */

	public static IntegerPair parse(String str)
	{
		String[] strs = str.split(" *, *", -1);
		if (strs.length != 2)
			throw new NumberFormatException();
		return new IntegerPair(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public IntegerPair clone()
	{
		try
		{
			return (IntegerPair)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof IntegerPair)
		{
			IntegerPair pair = (IntegerPair)obj;
			return ((first == pair.first) && (second == pair.second));
		}
		return false;
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		int sum = first + second;
		return (sum * (sum + 1) / 2 + first);
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		return new String(Integer.toString(first) + ", " + Integer.toString(second));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the first element of this pair.
	 *
	 * @return the first element of this pair.
	 */

	public int getFirst()
	{
		return first;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the second element of this pair.
	 *
	 * @return the second element of this pair.
	 */

	public int getSecond()
	{
		return second;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
