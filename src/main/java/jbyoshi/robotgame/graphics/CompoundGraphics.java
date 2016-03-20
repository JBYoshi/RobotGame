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

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

final class CompoundGraphics extends ProxyGraphics {
    private Composite extraComposite = null;
    private final Graphics2D parent;

    CompoundGraphics(Graphics2D parent) {
        super((Graphics2D) parent.create());
        this.parent = parent;
        delegate.setComposite(new CompoundComposite());
    }

    @Override
    public void setComposite(Composite comp) {
        this.extraComposite = comp;
    }

    @Override
    protected ProxyGraphics createNew(Graphics2D newDelegate) {
        return new CompoundGraphics(newDelegate);
    }

    private final class CompoundComposite implements Composite {

        @Override
        public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
            if (extraComposite == null) {
                return parent.getComposite().createContext(srcColorModel, dstColorModel, hints);
            }
            CompositeContext base = parent.getComposite().createContext(dstColorModel, dstColorModel, hints);
            CompositeContext extra = extraComposite.createContext(srcColorModel, dstColorModel, hints);
            return new CompositeContext() {
                @Override
                public void dispose() {
                    try {
                        extra.dispose();
                    } finally {
                        base.dispose();
                    }
                }

                @Override
                public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
                    WritableRaster mid = dstIn.createCompatibleWritableRaster();
                    extra.compose(src, dstIn, mid);
                    base.compose(mid, dstIn, dstOut);
                }
            };
        }
    }
}
