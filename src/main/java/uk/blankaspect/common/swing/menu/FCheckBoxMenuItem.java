/*====================================================================*\

FCheckBoxMenuItem.java

Check box menu item class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.swing.menu;

//----------------------------------------------------------------------


// IMPORTS


import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import uk.blankaspect.common.swing.font.FontKey;
import uk.blankaspect.common.swing.font.FontUtils;

//----------------------------------------------------------------------


// CHECK BOX MENU ITEM CLASS


public class FCheckBoxMenuItem
	extends JCheckBoxMenuItem
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FCheckBoxMenuItem(Action action)
	{
		super(action);
		FontUtils.setAppFont(FontKey.MAIN, this);
	}

	//------------------------------------------------------------------

	public FCheckBoxMenuItem(Action  action,
							 boolean selected)
	{
		this(action);
		setSelected(selected);
	}

	//------------------------------------------------------------------

	public FCheckBoxMenuItem(Action action,
							 int    mnemonic)
	{
		this(action);
		setMnemonic(mnemonic);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
