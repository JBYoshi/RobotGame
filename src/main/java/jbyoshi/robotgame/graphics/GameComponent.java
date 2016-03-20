/*
 * Copyright (C) 2016 JBYoshi.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jbyoshi.robotgame.graphics;

import jbyoshi.robotgame.api.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

final class GameComponent extends JComponent {
    private static final long serialVersionUID = 1L;
    private final GameDraw draw;
    private int lastMouseX, lastMouseY;
    private double drawX, drawY;
    private double zoom = 16.0;
    private BufferLayer[] layers;
    private final Object bufferLock = new Object();

    GameComponent(GameDraw draw) {
        this.draw = draw;

        setOpaque(true);

        final MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                drawX = wrapToGameSize(drawX + (e.getX() - lastMouseX) * getPixelSize());
                drawY = wrapToGameSize(drawY + (e.getY() - lastMouseY) * getPixelSize());
                doRepaint();
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                    double oldPixelSize = getPixelSize();
                    zoom = zoom - e.getUnitsToScroll() * 0.1;
                    revalidateZoom();
                    double newPixelSize = getPixelSize();
                    drawX = wrapToGameSize(drawX - e.getX() * oldPixelSize + e.getX() * newPixelSize);
                    drawY = wrapToGameSize(drawY - e.getY() * oldPixelSize + e.getY() * newPixelSize);
                }
            }
        };
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                double oldPixelSize = getPixelSize();
                revalidateZoom();
                double newPixelSize = getPixelSize();
                drawX = wrapToGameSize(drawX - getWidth() / 2 * oldPixelSize + getWidth() / 2 * newPixelSize);
                drawY = wrapToGameSize(drawY - getHeight() / 2 * oldPixelSize + getHeight() / 2 * newPixelSize);
                repaint();
            }
        });
    }

    private void revalidateZoom() {
        zoom = Math.max(zoom, Math.min(getWidth() / 2.0 / Game.WORLD_SIZE, getHeight() / 2.0 / Game.WORLD_SIZE));
    }

    private double wrapToGameSize(double value) {
        while (value < 0) value += Game.WORLD_SIZE;
        while (value >= Game.WORLD_SIZE) value -= Game.WORLD_SIZE;
        return value;
    }

    @Override
    public void paintComponent(Graphics basicGraphics) {
        Graphics2D g = (Graphics2D) basicGraphics;
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));

        synchronized (bufferLock) {
            if (layers == null) {
                // Too slow.
                layers = draw.updateBuffer();
            }
            for (BufferLayer layer : layers) {
                layer.paint(this, g);
            }
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(10);
                doRepaint();
                return null;
            }
        }.execute();
    }

    double getGameSize() {
        return Game.WORLD_SIZE * zoom;
    }

    double getPixelSize() {
        return 1.0 / zoom;
    }

    double getGridSpotSize() {
        return zoom;
    }

    void doRepaint() {
        synchronized (bufferLock) {
            layers = draw.updateBuffer();
        }
        repaint();
    }

    BufferLayer createLayer(Consumer<Graphics2D> drawCode) {
        double size = getGameSize();
        int imgSize = (int) Math.ceil(size + 2 * zoom);
        BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imgGraphics = img.createGraphics();
        imgGraphics.translate(zoom, zoom);
        drawCode.accept(imgGraphics);
        imgGraphics.dispose();
        return (comp, g) -> {
            for (double x1 = -size; x1 < getWidth(); x1 += size) {
                for (double y1 = -size; y1 < getHeight(); y1 += size) {
                    AffineTransform tx = new AffineTransform();
                    tx.translate(drawX * zoom + x1 - zoom, drawY * zoom + y1 - zoom);
                    g.drawRenderedImage(img, tx);
                }
            }
        };
    }

    interface BufferLayer {
        void paint(GameComponent comp, Graphics2D g);
    }
}
