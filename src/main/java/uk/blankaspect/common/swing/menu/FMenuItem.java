/*====================================================================*\

FMenuItem.java

Menu item class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.swing.menu;

//----------------------------------------------------------------------


// IMPORTS


import javax.swing.Action;
import javax.swing.JMenuItem;

import uk.blankaspect.common.swing.font.FontKey;
import uk.blankaspect.common.swing.font.FontUtils;

//----------------------------------------------------------------------


// MENU ITEM CLASS


public class FMenuItem
	extends JMenuItem
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FMenuItem(Action action)
	{
		super(action);
		FontUtils.setAppFont(FontKey.MAIN, this);
	}

	//------------------------------------------------------------------

	public FMenuItem(Action  action,
					 boolean enabled)
	{
		this(action);
		setEnabled(enabled);
	}

	//------------------------------------------------------------------

	public FMenuItem(Action action,
					 int    mnemonic)
	{
		this(action);
		setMnemonic(mnemonic);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
