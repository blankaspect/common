/*====================================================================*\

FTabbedPane.java

Tabbed pane class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.swing.tabbedpane;

//----------------------------------------------------------------------


// IMPORTS


import javax.swing.JTabbedPane;

import uk.blankaspect.common.swing.font.FontKey;
import uk.blankaspect.common.swing.font.FontUtils;

//----------------------------------------------------------------------


// TABBED PANE CLASS


public class FTabbedPane
	extends JTabbedPane
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FTabbedPane()
	{
		FontUtils.setAppFont(FontKey.MAIN, this);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
