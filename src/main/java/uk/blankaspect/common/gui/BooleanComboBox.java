/*====================================================================*\

BooleanComboBox.java

Boolean combo box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.gui;

//----------------------------------------------------------------------


// IMPORTS


import javax.swing.JComboBox;

import uk.blankaspect.common.misc.NoYes;

//----------------------------------------------------------------------


// BOOLEAN COMBO BOX CLASS


public class BooleanComboBox
	extends JComboBox<NoYes>
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public BooleanComboBox()
	{
		super(NoYes.values());
		GuiUtils.setAppFont(Constants.FontKey.COMBO_BOX, this);
		setRenderer(new ComboBoxRenderer<>(this));
	}

	//------------------------------------------------------------------

	public BooleanComboBox(boolean value)
	{
		this();
		setSelectedValue(value);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean getSelectedValue()
	{
		return ((NoYes)getSelectedItem()).toBoolean();
	}

	//------------------------------------------------------------------

	public void setSelectedValue(boolean value)
	{
		setSelectedItem(NoYes.forBoolean(value));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
