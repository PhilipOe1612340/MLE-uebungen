package aufgabe05;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel {

    public Image img = null;
    public ImageObserver imageObserver = null;

    public void paint(Graphics g) {
        super.paint(g);

        g.drawImage(img, 0, 0, imageObserver);
    }
}
