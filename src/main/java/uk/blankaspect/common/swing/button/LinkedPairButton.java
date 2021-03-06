/*====================================================================*\

LinkedPairButton.java

Linekd pair button class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.swing.button;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JToggleButton;

import uk.blankaspect.common.swing.misc.GuiConstants;

//----------------------------------------------------------------------


// LINKED PAIR BUTTON CLASS


public class LinkedPairButton
	extends JToggleButton
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	VERTICAL_MARGIN		= 6;
	private static final	int	HORIZONTAL_MARGIN	= 5;

	private static final	double	ICON_DISC_DIAMETER	= 7.0;
	private static final	double	ICON_DISC_GAP		= 4.0;
	private static final	double	ICON_RECT_WIDTH		= ICON_DISC_GAP + 1.0;
	private static final	double	ICON_RECT_HEIGHT	= 3.0;

	private static final	Color	BACKGROUND_COLOUR		= new Color(228, 240, 228);
	private static final	Color	ICON_DISC_COLOUR		= new Color(80, 96, 80);
	private static final	Color	ICON_RECT_COLOUR		= new Color(0, 144, 0);
	private static final	Color	BORDER_COLOUR			= new Color(160, 192, 160);
	private static final	Color	DISABLED_COLOUR			= new Color(176, 176, 176);
	private static final	Color	FOCUSED_BORDER_COLOUR	= Color.BLACK;

	private static final	String	LINK_STR	= "Link ";
	private static final	String	UNLINK_STR	= "Unlink ";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public LinkedPairButton()
	{
		this(null);
	}

	//------------------------------------------------------------------

	public LinkedPairButton(String tooltipText)
	{
		// Set attributes
		setBorder(null);
		setPreferredSize(new Dimension(2 * HORIZONTAL_MARGIN + (int)(2.0 * ICON_DISC_DIAMETER + ICON_DISC_GAP),
									   2 * VERTICAL_MARGIN + (int)ICON_DISC_DIAMETER));
		setToolTipText(tooltipText);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String getToolTipText()
	{
		String text = super.getToolTipText();
		return ((text == null) ? null : (isSelected() ? UNLINK_STR : LINK_STR) + text);
	}

	//------------------------------------------------------------------

	@Override
	protected void paintComponent(Graphics gr)
	{
		// Create copy of graphics context
		gr = gr.create();
		Graphics2D gr2d = (Graphics2D)gr;

		// Get dimensions
		int width = getWidth();
		int height = getHeight();

		// Fill interior
		gr.setColor(isEnabled() ? BACKGROUND_COLOUR : getBackground());
		gr.fillRect(0, 0, width, height);

		// Set antialiasing
		gr2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw icon
		if (isSelected())
		{
			gr.setColor(isEnabled() ? ICON_RECT_COLOUR : DISABLED_COLOUR);
			gr2d.fill(new Rectangle2D.Double(0.5 * ((double)width - ICON_RECT_WIDTH),
											 0.5 * ((double)height - ICON_RECT_HEIGHT), ICON_RECT_WIDTH,
											 ICON_RECT_HEIGHT));
		}
		gr.setColor(isEnabled() ? ICON_DISC_COLOUR : DISABLED_COLOUR);
		gr2d.fill(new Ellipse2D.Double((double)HORIZONTAL_MARGIN, (double)VERTICAL_MARGIN, ICON_DISC_DIAMETER,
									   ICON_DISC_DIAMETER));
		gr2d.fill(new Ellipse2D.Double((double)(width - HORIZONTAL_MARGIN) - ICON_DISC_DIAMETER,
									   (double)(height - VERTICAL_MARGIN) - ICON_DISC_DIAMETER,
									   ICON_DISC_DIAMETER, ICON_DISC_DIAMETER));

		// Draw border
		gr.setColor(isEnabled() ? BORDER_COLOUR : DISABLED_COLOUR);
		gr.drawRect(0, 0, width - 1, height - 1);
		if (isFocusOwner())
		{
			gr2d.setStroke(GuiConstants.BASIC_DASH);
			gr.setColor(FOCUSED_BORDER_COLOUR);
			gr.drawRect(1, 1, width - 3, height - 3);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
