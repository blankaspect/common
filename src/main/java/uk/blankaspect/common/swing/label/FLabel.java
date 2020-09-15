/*====================================================================*\

FLabel.java

Label class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.swing.label;

//----------------------------------------------------------------------


// IMPORTS


import javax.swing.JLabel;

import uk.blankaspect.common.swing.font.FontKey;
import uk.blankaspect.common.swing.font.FontUtils;

//----------------------------------------------------------------------


// LABEL CLASS


public class FLabel
	extends JLabel
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FLabel(String text)
	{
		super(text);
		FontUtils.setAppFont(FontKey.MAIN, this);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
