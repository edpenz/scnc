package nz.ac.squash.widget.generic;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class JOverlay extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private static final Color sOverlayColor = new Color(235, 235, 235, 128);
	//private static final Color sOverlayColor = new Color(0, 0, 0, 128);

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(sOverlayColor);
		g.fillRect(0, 0, getWidth(), getHeight());

	}

}
