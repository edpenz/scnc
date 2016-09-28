package nz.ac.squash.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class SwingUtils {
    public static void closeOnEscape(final JDialog parent) {
        parent.getRootPane().getActionMap()
                .put("close_on_escape", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        parent.dispose();
                    }
                });
        parent.getRootPane()
                .getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        "close_on_escape");
    }
}
