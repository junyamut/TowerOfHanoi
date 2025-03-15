package xyz.joseyamut.gfx;

import javax.swing.*;
import java.awt.*;

public class Backstage extends JComponent {

    Image canvas;
    Graphics g;

    public void createCanvas() {
        canvas = null;
        try {
            canvas = createImage(getSize().width, getSize().height);
            Graphics2D g2d = (Graphics2D) canvas.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g = g2d;
        } catch (OutOfMemoryError e) {
            canvas = null;
            g = null;
        }
    }

    public void graphics(Graphics g) {
        g.clearRect(0, 0, getSize().width, getSize().height);
        g.setColor(Color.BLACK); // base color
        g.fillRect(0, 0, getSize().width, getSize().height);
    }

    public synchronized void paint(Graphics g) {
        createCanvas();
        if (canvas != null) {
            graphics(this.g);
        }

        if (canvas == null) {
            g.clearRect(0, 0, getSize().width, getSize().height);
            g.fillRect(0, 0, getSize().width, getSize().height);
            return;
        }

        g.drawImage(canvas, 0, 0, this);
    }

}
