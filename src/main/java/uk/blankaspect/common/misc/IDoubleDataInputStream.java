/*====================================================================*\

IDoubleDataInputStream.java

Double data input stream interface.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.misc;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.exception.AppException;

//----------------------------------------------------------------------


// DOUBLE DATA INPUT STREAM INTERFACE


public interface IDoubleDataInputStream
	extends IDataInput
{

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	int read(double[] buffer,
			 int      offset,
			 int      length)
		throws AppException;

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
