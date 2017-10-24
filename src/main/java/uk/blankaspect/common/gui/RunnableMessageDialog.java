/*====================================================================*\

RunnableMessageDialog.java

Runnable message dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.gui;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

//----------------------------------------------------------------------


// RUNNABLE MESSAGE DIALOG BOX CLASS


public class RunnableMessageDialog
	extends JDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	VERTICAL_PADDING	= 12;
	private static final	int	HORIZONTAL_PADDING	= 24;

	private static final	Color	BACKGROUND_COLOUR	= new Color(248, 240, 192);
	private static final	Color	BORDER_COLOUR		= new Color(224, 128, 64);

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// RUNNABLE INTERFACE


	public interface IRunnable
		extends Runnable
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		String getMessage();

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected RunnableMessageDialog(Window    owner,
									IRunnable runnable)
	{
		// Call superclass constructor
		super(owner, Dialog.ModalityType.APPLICATION_MODAL);


		//----  Message label

		FLabel messageLabel = new FLabel(runnable.getMessage());
		messageLabel.setBackground(BACKGROUND_COLOUR);
		messageLabel.setOpaque(true);
		GuiUtils.setPaddedLineBorder(messageLabel, VERTICAL_PADDING, HORIZONTAL_PADDING, BORDER_COLOUR);


		//----  Window

		// Set content pane
		setContentPane(messageLabel);

		// Omit frame from dialog box
		setUndecorated(true);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Run Runnable when window is activated
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowActivated(WindowEvent event)
			{
				if (!running)
				{
					// Prevent from running again
					running = true;

					// Run Runnable
					runnable.run();

					// Close and destroy dialog
					setVisible(false);
					dispose();
				}
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog box
		setLocation(GuiUtils.getComponentLocation(this, owner));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showDialog(Component component,
								  IRunnable runnable)
	{
		new RunnableMessageDialog(GuiUtils.getWindow(component), runnable).setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	boolean	running;

}

//----------------------------------------------------------------------
