/*====================================================================*\

ByteDataList.java

Class: byte-data list.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.dataio;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

//----------------------------------------------------------------------


// CLASS: BYTE-DATA LIST


public class ByteDataList
	implements IByteDataSource
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<ByteData>	dataBlocks;
	private	int				outIndex;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ByteDataList()
	{
		// Initialise instance variables
		dataBlocks = new ArrayList<>();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IByteDataSource interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void reset()
	{
		outIndex = 0;
	}

	//------------------------------------------------------------------

	@Override
	public long getLength()
	{
		long length = 0;
		for (ByteData dataBlock : dataBlocks)
			length += dataBlock.getLength();
		return length;
	}

	//------------------------------------------------------------------

	@Override
	public ByteData getData()
	{
		return ((outIndex < dataBlocks.size()) ? dataBlocks.get(outIndex++) : null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getNumBlocks()
	{
		return dataBlocks.size();
	}

	//------------------------------------------------------------------

	public ByteData getBlock(int index)
	{
		return dataBlocks.get(index);
	}

	//------------------------------------------------------------------

	public void add(byte[] data)
	{
		dataBlocks.add(new ByteData(data));
	}

	//------------------------------------------------------------------

	public void add(byte[] data,
					int    offset,
					int    length)
	{
		dataBlocks.add(new ByteData(data, offset, length));
	}

	//------------------------------------------------------------------

	public int getData(byte[] buffer)
	{
		return getData(buffer, 0, buffer.length);
	}

	//------------------------------------------------------------------

	public int getData(byte[] buffer,
					   int    offset,
					   int    length)
	{
		int startOffset = offset;
		int endOffset = offset + length;
		for (ByteData dataBlock : dataBlocks)
		{
			if (offset >= endOffset)
				break;
			int blockLength = Math.min(dataBlock.getLength(), endOffset - offset);
			System.arraycopy(dataBlock.getData(), dataBlock.getOffset(), buffer, offset, blockLength);
			offset += blockLength;
		}
		return offset - startOffset;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
