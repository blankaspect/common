/*====================================================================*\

SinglePathnameFieldDialog.java

Single pathname field dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.gui;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.common.misc.KeyAction;

import uk.blankaspect.common.textfield.PathnameField;

//----------------------------------------------------------------------


// SINGLE PATHNAME FIELD DIALOG BOX CLASS


public class SinglePathnameFieldDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	DEFAULT_NUM_COLUMNS	= 72;

	// Commands
	private interface Command
	{
		String	CHOOSE_PATHNAME	= "choosePathname";
		String	ACCEPT			= "accept";
		String	CLOSE			= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// FIELD CLASS


	private static class Field
		extends PathnameField
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Field(String  pathname,
					  int     numColumns,
					  boolean unixStyle)
		{
			super(pathname, numColumns);
			GuiUtils.setAppFont(Constants.FontKey.TEXT_FIELD, this);
			GuiUtils.setTextComponentMargins(this);
			setUnixStyle(unixStyle);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected int getColumnWidth()
		{
			return GuiUtils.getCharWidth('0', getFontMetrics(getFont()));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected SinglePathnameFieldDialog(Window owner,
										String titleStr,
										String key,
										String labelStr,
										String pathname)
	{
		this(owner, titleStr, key, labelStr, pathname, 0, false);
	}

	//------------------------------------------------------------------

	protected SinglePathnameFieldDialog(Window  owner,
										String  titleStr,
										String  key,
										String  labelStr,
										String  pathname,
										int     numColumns,
										boolean unixStyle)
	{

		// Call superclass constructor
		super(owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		if (owner != null)
			setIconImages(owner.getIconImages());

		// Initialise instance fields
		this.key = key;


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label
		JLabel label = new FLabel(labelStr);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = Constants.COMPONENT_INSETS;
		gridBag.setConstraints(label, gbc);
		controlPanel.add(label);

		// Pathname panel
		field = new Field(pathname, (numColumns == 0) ? DEFAULT_NUM_COLUMNS : numColumns, unixStyle);
		PathnamePanel pathnamePanel = new PathnamePanel(field, Command.CHOOSE_PATHNAME, this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = Constants.COMPONENT_INSETS;
		gridBag.setConstraints(pathnamePanel, gbc);
		controlPanel.add(pathnamePanel);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

		// Button: OK
		JButton okButton = new FButton(Constants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(Constants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		mainPanel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, this);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				onClose();
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog box
		Point location = locations.get(key);
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Set default button
		getRootPane().setDefaultButton(okButton);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static String showDialog(Component parent,
									String    titleStr,
									String    key,
									String    labelStr,
									String    pathname)
	{
		SinglePathnameFieldDialog dialog = new SinglePathnameFieldDialog(GuiUtils.getWindow(parent),
																		 titleStr, key, labelStr,
																		 pathname);
		dialog.setVisible(true);
		return dialog.getPathname();
	}

	//------------------------------------------------------------------

	public static String showDialog(Component parent,
									String    titleStr,
									String    key,
									String    labelStr,
									String    pathname,
									int       numColumns,
									boolean   unixStyle)
	{
		SinglePathnameFieldDialog dialog = new SinglePathnameFieldDialog(GuiUtils.getWindow(parent),
																		 titleStr, key, labelStr,
																		 pathname, numColumns, unixStyle);
		dialog.setVisible(true);
		return dialog.getPathname();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.CHOOSE_PATHNAME))
			onChoosePathname();

		if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	protected PathnameField getField()
	{
		return field;
	}

	//------------------------------------------------------------------

	protected String getPathname()
	{
		return (accepted ? field.getText() : null);
	}

	//------------------------------------------------------------------

	protected File chooseFile()
	{
		return null;
	}

	//------------------------------------------------------------------

	protected boolean isTextValid()
	{
		return true;
	}

	//------------------------------------------------------------------

	private void onChoosePathname()
	{
		File file = chooseFile();
		if (file != null)
			field.setFile(file);
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		if (isTextValid())
		{
			accepted = true;
			onClose();
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		locations.put(key, getLocation());
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class fields
////////////////////////////////////////////////////////////////////////

	private static	Map<String, Point>	locations	= new Hashtable<>();

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	String			key;
	private	boolean			accepted;
	private	PathnameField	field;

}

//----------------------------------------------------------------------
