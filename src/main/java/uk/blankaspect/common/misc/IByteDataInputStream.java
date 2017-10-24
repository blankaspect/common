/*====================================================================*\

IByteDataInputStream.java

Byte data input stream interface.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.misc;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.exception.AppException;

//----------------------------------------------------------------------


// BYTE DATA INPUT STREAM INTERFACE


public interface IByteDataInputStream
	extends IDataInput
{

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	int read(byte[] buffer,
			 int    offset,
			 int    length)
		throws AppException;

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
