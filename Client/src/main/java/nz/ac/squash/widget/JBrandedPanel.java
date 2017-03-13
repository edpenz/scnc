package nz.ac.squash.widget;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public class JBrandedPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Color BASE_COLOR = Color.decode("#535353");
    private static final Image BASE_IMAGE;
    private static final Color FADE_COLOR = new Color(
            BASE_COLOR.getRed() / 255.0f,
            BASE_COLOR.getGreen() / 255.0f,
            BASE_COLOR.getBlue() / 255.0f,
            0.75f);

    static {
        Image loaded = null;
        try {
            loaded = ImageIO.read(ClassLoader.getSystemResource("images/racquet_white.png"));
        } catch (Exception e) {
            // This throws in UI designer.
        }
        BASE_IMAGE = loaded;
    }

    private Image mScaledImage = null;

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(BASE_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (BASE_IMAGE != null) {
            if (mScaledImage == null) {
                mScaledImage = BASE_IMAGE.getScaledInstance(getHeight(),
                        getHeight(), Image.SCALE_SMOOTH);
            }

            g.drawImage(mScaledImage, getWidth() - getHeight(), 0, getWidth(),
                    getHeight(), 0, 0, mScaledImage.getWidth(null),
                    mScaledImage.getHeight(null), null);
        }

        g.setColor(FADE_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
