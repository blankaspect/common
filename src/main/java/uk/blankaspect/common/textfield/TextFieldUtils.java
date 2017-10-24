/*====================================================================*\

TextFieldUtils.java

Text field utility methods class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.textfield;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.KeyboardFocusManager;

import java.beans.PropertyChangeListener;

import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

//----------------------------------------------------------------------


// TEXT FIELD UTILITY METHODS CLASS


public class TextFieldUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	PERMANENT_FOCUS_OWNER_PROPERTY_KEY	= "permanentFocusOwner";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private TextFieldUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void selectAllOnFocusGained()
	{
		PropertyChangeListener listener = event ->
		{
			Object value = event.getNewValue();
			if ((value instanceof JTextField) && !(value instanceof JPasswordField))
				SwingUtilities.invokeLater(() -> ((JTextField)value).selectAll());
		};
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
											.addPropertyChangeListener(PERMANENT_FOCUS_OWNER_PROPERTY_KEY, listener);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
