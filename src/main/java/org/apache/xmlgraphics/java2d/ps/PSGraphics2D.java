/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: PSGraphics2D.java 1345683 2012-06-03 14:50:33Z gadams $ */

package org.apache.xmlgraphics.java2d.ps;

//Java
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.xmlgraphics.java2d.AbstractGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.StrokingTextHandler;
import org.apache.xmlgraphics.java2d.TextHandler;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSImageUtils;

/**
 * This is a concrete implementation of <tt>AbstractGraphics2D</tt> (and
 * therefore of <tt>Graphics2D</tt>) which is able to generate PostScript code.
 *
 * @version $Id: PSGraphics2D.java 1345683 2012-06-03 14:50:33Z gadams $
 * @see org.apache.xmlgraphics.java2d.AbstractGraphics2D
 *
 *      Originally authored by Keiron Liddle.
 */
@Slf4j
public class PSGraphics2D extends AbstractGraphics2D {

    private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

    private static final boolean DEBUG = false;

    /**
     * The G2D instance that represents the root instance (used in context with
     * create()/dispose()). Null if this instance is the root instance.
     */
    protected PSGraphics2D rootG2D;

    /** the PostScript generator being created */
    protected PSGenerator gen;

    /** Disable or enable clipping */
    protected boolean clippingDisabled = false;

    /** Fallback text handler */

    protected TextHandler fallbackTextHandler = new StrokingTextHandler();

    /** Custom text handler */
    protected TextHandler customTextHandler;

    /**
     * the current colour for use in svg
     */
    protected Color currentColour = new Color(0, 0, 0);

    /**
     * Create a new Graphics2D that generates PostScript code.
     * 
     * @param textAsShapes
     *            True if text should be rendered as graphics
     * @see org.apache.xmlgraphics.java2d.AbstractGraphics2D#AbstractGraphics2D(boolean)
     */
    public PSGraphics2D(final boolean textAsShapes) {
        super(textAsShapes);
    }

    /**
     * Create a new Graphics2D that generates PostScript code.
     * 
     * @param textAsShapes
     *            True if text should be rendered as graphics
     * @param gen
     *            PostScript generator to use for output
     * @see org.apache.xmlgraphics.java2d.AbstractGraphics2D#AbstractGraphics2D(boolean)
     */
    public PSGraphics2D(final boolean textAsShapes, final PSGenerator gen) {
        this(textAsShapes);
        setPSGenerator(gen);
    }

    /**
     * Constructor for creating copies
     * 
     * @param g
     *            parent PostScript Graphics2D
     */
    public PSGraphics2D(final PSGraphics2D g) {
        super(g);

        this.rootG2D = g.rootG2D != null ? g.rootG2D : g;
        setPSGenerator(g.gen);
        this.clippingDisabled = g.clippingDisabled;
        // this.fallbackTextHandler is not copied
        // TODO The customTextHandler should probably not be passed over just
        // like that
        // fallbackTextHandler, for example, has to be recreated to point to the
        // sub-Graphics2D
        // to get the text positioning right. This might require changes in the
        // TextHandler interface
        this.customTextHandler = g.customTextHandler;
        this.currentColour = g.currentColour;
    }

    /**
     * Sets the PostScript generator
     * 
     * @param gen
     *            the PostScript generator
     */
    public void setPSGenerator(final PSGenerator gen) {
        this.gen = gen;
    }

    /** @return the PostScript generator used by this instance. */
    public PSGenerator getPSGenerator() {
        return this.gen;
    }

    /**
     * Sets the GraphicContext
     * 
     * @param c
     *            GraphicContext to use
     */
    public void setGraphicContext(final GraphicContext c) {
        this.gc = c;
        // setPrivateHints();
    }

    /** @return the fallback TextHandler implementation */
    public TextHandler getFallbackTextHandler() {
        return this.fallbackTextHandler;
    }

    /** @return the custom TextHandler implementation */
    public TextHandler getCustomTextHandler() {
        return this.customTextHandler;
    }

    /**
     * Sets a custom TextHandler implementation that is responsible for painting
     * text. The default TextHandler paints all text as shapes. A custom
     * implementation can implement text painting using text painting operators.
     * 
     * @param handler
     *            the custom TextHandler implementation
     */
    public void setCustomTextHandler(final TextHandler handler) {
        this.customTextHandler = handler;
    }

    /*
     * TODO Add me back at the right place!!! private void setPrivateHints() {
     * setRenderingHint(RenderingHintsKeyExt.KEY_AVOID_TILE_PAINTING,
     * RenderingHintsKeyExt.VALUE_AVOID_TILE_PAINTING_ON); }
     */

    /**
     * Disable clipping on each draw command.
     *
     * @param b
     *            set to true to disable all clipping.
     */
    public void disableClipping(final boolean b) {
        this.clippingDisabled = b;
    }

    /**
     * Creates a new <code>Graphics</code> object that is a copy of this
     * <code>Graphics</code> object.
     * 
     * @return a new graphics context that is a copy of this graphics context.
     */
    @Override
    public Graphics create() {
        preparePainting();
        return new PSGraphics2D(this);
    }

    /**
     * This method is used by AbstractPSDocumentGraphics2D to prepare a new page
     * if necessary.
     */
    public void preparePainting() {
        // nop, used by AbstractPSDocumentGraphics2D
        if (this.rootG2D != null) {
            this.rootG2D.preparePainting();
        }
    }

    /**
     * Draws as much of the specified image as is currently available. The image
     * is drawn with its top-left corner at (<i>x</i>,&nbsp;<i>y</i>) in this
     * graphics context's coordinate space. Transparent pixels in the image do
     * not affect whatever pixels are already there.
     * <p>
     * This method returns immediately in all cases, even if the complete image
     * has not yet been loaded, and it has not been dithered and converted for
     * the current output device.
     * <p>
     * If the image has not yet been completely loaded, then
     * <code>drawImage</code> returns <code>false</code>. As more of the image
     * becomes available, the process that draws the image notifies the
     * specified image observer.
     * 
     * @param img
     *            the specified image to be drawn.
     * @param x
     *            the <i>x</i> coordinate.
     * @param y
     *            the <i>y</i> coordinate.
     * @param observer
     *            object to be notified as more of the image is converted.
     * @return True if the image has been fully drawn/loaded
     * @see java.awt.Image
     * @see java.awt.image.ImageObserver
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int,
     *      int, int, int)
     */
    @Override
    public boolean drawImage(final Image img, final int x, final int y,
            final ImageObserver observer) {
        preparePainting();
        if (DEBUG) {
            log.info("drawImage: " + x + ", " + y + " "
                    + img.getClass().getName());
        }

        final int width = img.getWidth(observer);
        final int height = img.getHeight(observer);
        if (width == -1 || height == -1) {
            return false;
        }

        final Dimension size = new Dimension(width, height);
        final BufferedImage buf = buildBufferedImage(size);

        final java.awt.Graphics2D g = buf.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.setBackground(new Color(1, 1, 1, 0));
        g.setPaint(new Color(1, 1, 1, 0));
        g.fillRect(0, 0, width, height);
        g.clip(new Rectangle(0, 0, buf.getWidth(), buf.getHeight()));

        if (!g.drawImage(img, 0, 0, observer)) {
            return false;
        }
        g.dispose();

        try {
            final AffineTransform at = getTransform();
            this.gen.saveGraphicsState();
            this.gen.concatMatrix(at);
            final Shape imclip = getClip();
            writeClip(imclip);
            PSImageUtils.renderBitmapImage(buf, x, y, width, height, this.gen);
            this.gen.restoreGraphicsState();
        } catch (final IOException ioe) {
            log.error("IOException", ioe);
        }

        return true;
    }

    /**
     * Creates a buffered image.
     * 
     * @param size
     *            dimensions of the image to be created
     * @return the buffered image
     */
    public BufferedImage buildBufferedImage(final Dimension size) {
        return new BufferedImage(size.width, size.height,
                BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Draws as much of the specified image as has already been scaled to fit
     * inside the specified rectangle.
     * <p>
     * The image is drawn inside the specified rectangle of this graphics
     * context's coordinate space, and is scaled if necessary. Transparent
     * pixels do not affect whatever pixels are already there.
     * <p>
     * This method returns immediately in all cases, even if the entire image
     * has not yet been scaled, dithered, and converted for the current output
     * device. If the current output representation is not yet complete, then
     * <code>drawImage</code> returns <code>false</code>. As more of the image
     * becomes available, the process that draws the image notifies the image
     * observer by calling its <code>imageUpdate</code> method.
     * <p>
     * A scaled version of an image will not necessarily be available
     * immediately just because an unscaled version of the image has been
     * constructed for this output device. Each size of the image may be cached
     * separately and generated from the original data in a separate image
     * production sequence.
     * 
     * @param img
     *            the specified image to be drawn.
     * @param x
     *            the <i>x</i> coordinate.
     * @param y
     *            the <i>y</i> coordinate.
     * @param width
     *            the width of the rectangle.
     * @param height
     *            the height of the rectangle.
     * @param observer
     *            object to be notified as more of the image is converted.
     * @return True if the image has been fully loaded/drawn
     * @see java.awt.Image
     * @see java.awt.image.ImageObserver
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int,
     *      int, int, int)
     */
    @Override
    public boolean drawImage(final Image img, final int x, final int y,
            final int width, final int height, final ImageObserver observer) {
        preparePainting();
        log.error("NYI: drawImage");
        return true;
    }

    /**
     * Disposes of this graphics context and releases any system resources that
     * it is using. A <code>Graphics</code> object cannot be used after
     * <code>dispose</code>has been called.
     * <p>
     * When a Java program runs, a large number of <code>Graphics</code> objects
     * can be created within a short time frame. Although the finalization
     * process of the garbage collector also disposes of the same system
     * resources, it is preferable to manually free the associated resources by
     * calling this method rather than to rely on a finalization process which
     * may not run to completion for a long period of time.
     * <p>
     * Graphics objects which are provided as arguments to the
     * <code>paint</code> and <code>update</code> methods of components are
     * automatically released by the system when those methods return. For
     * efficiency, programmers should call <code>dispose</code> when finished
     * using a <code>Graphics</code> object only if it was created directly from
     * a component or another <code>Graphics</code> object.
     * 
     * @see java.awt.Graphics#finalize
     * @see java.awt.Component#paint
     * @see java.awt.Component#update
     * @see java.awt.Component#getGraphics
     * @see java.awt.Graphics#create
     */
    @Override
    public void dispose() {
        this.gen = null;
        this.fallbackTextHandler = null;
        this.customTextHandler = null;
        this.currentColour = null;
    }

    /**
     * Processes a Shape generating the necessary painting operations.
     * 
     * @param s
     *            Shape to process
     * @return the winding rule of the path defining the shape
     * @throws IOException
     *             In case of an I/O problem.
     */
    public int processShape(final Shape s) throws IOException {
        if (s instanceof Rectangle2D) {
            // Special optimization in case of Rectangle Shape
            final Rectangle2D r = (Rectangle2D) s;
            this.gen.defineRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
            return PathIterator.WIND_NON_ZERO;
        } else {
            final PathIterator iter = s.getPathIterator(IDENTITY_TRANSFORM);
            processPathIterator(iter);
            return iter.getWindingRule();
        }
    }

    /**
     * Processes a path iterator generating the necessary painting operations.
     * 
     * @param iter
     *            PathIterator to process
     * @throws IOException
     *             In case of an I/O problem.
     */
    public void processPathIterator(final PathIterator iter) throws IOException {
        final double[] vals = new double[6];
        while (!iter.isDone()) {
            final int type = iter.currentSegment(vals);
            switch (type) {
            case PathIterator.SEG_CUBICTO:
                this.gen.writeln(this.gen.formatDouble(vals[0]) + " "
                        + this.gen.formatDouble(vals[1]) + " "
                        + this.gen.formatDouble(vals[2]) + " "
                        + this.gen.formatDouble(vals[3]) + " "
                        + this.gen.formatDouble(vals[4]) + " "
                        + this.gen.formatDouble(vals[5]) + " "
                        + this.gen.mapCommand("curveto"));
                break;
            case PathIterator.SEG_LINETO:
                this.gen.writeln(this.gen.formatDouble(vals[0]) + " "
                        + this.gen.formatDouble(vals[1]) + " "
                        + this.gen.mapCommand("lineto"));
                break;
            case PathIterator.SEG_MOVETO:
                this.gen.writeln(this.gen.formatDouble(vals[0]) + " "
                        + this.gen.formatDouble(vals[1]) + " "
                        + this.gen.mapCommand("moveto"));
                break;
            case PathIterator.SEG_QUADTO:
                this.gen.writeln(this.gen.formatDouble(vals[0]) + " "
                        + this.gen.formatDouble(vals[1]) + " "
                        + this.gen.formatDouble(vals[2]) + " "
                        + this.gen.formatDouble(vals[3]) + " QT");
                break;
            case PathIterator.SEG_CLOSE:
                this.gen.writeln(this.gen.mapCommand("closepath"));
                break;
            default:
                break;
            }
            iter.next();
        }
    }

    /**
     * Strokes the outline of a <code>Shape</code> using the settings of the
     * current <code>Graphics2D</code> context. The rendering attributes applied
     * include the <code>Clip</code>, <code>Transform</code>, <code>Paint</code>
     * , <code>Composite</code> and <code>Stroke</code> attributes.
     * 
     * @param s
     *            the <code>Shape</code> to be rendered
     * @see #setStroke
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #transform
     * @see #setTransform
     * @see #clip
     * @see #setClip
     * @see #setComposite
     */
    @Override
    public void draw(final Shape s) {
        preparePainting();
        try {
            this.gen.saveGraphicsState();

            final AffineTransform trans = getTransform();
            final boolean newTransform = !trans.isIdentity();

            if (newTransform) {
                this.gen.concatMatrix(trans);
            }
            final Shape imclip = getClip();
            if (shouldBeClipped(imclip, s)) {
                writeClip(imclip);
            }
            establishColor(getColor());

            applyPaint(getPaint(), false);
            applyStroke(getStroke());

            this.gen.writeln(this.gen.mapCommand("newpath"));
            processShape(s);
            doDrawing(false, true, false);
            this.gen.restoreGraphicsState();
        } catch (final IOException ioe) {
            log.error("IOException", ioe);
        }
    }

    /**
     * Determines if a shape interacts with a clipping region.
     * 
     * @param clip
     *            Shape defining the clipping region
     * @param s
     *            Shape to be drawn
     * @return true if code for a clipping region needs to be generated.
     */
    public boolean shouldBeClipped(final Shape clip, final Shape s) {
        if (clip == null || s == null) {
            return false;
        }
        final Area as = new Area(s);
        final Area imclip = new Area(clip);
        imclip.intersect(as);
        return !imclip.equals(as);
    }

    /**
     * Establishes a clipping region
     * 
     * @param s
     *            Shape defining the clipping region
     */
    public void writeClip(final Shape s) {
        if (s == null) {
            return;
        }
        if (!this.clippingDisabled) {
            preparePainting();
            try {
                this.gen.writeln(this.gen.mapCommand("newpath"));
                processShape(s);
                // clip area
                this.gen.writeln(this.gen.mapCommand("clip"));
            } catch (final IOException ioe) {
                log.error("IOException", ioe);
            }
        }
    }

    /**
     * Applies a new Paint object.
     * 
     * @param paint
     *            Paint object to use
     * @param fill
     *            True if to be applied for filling
     */
    protected void applyPaint(final Paint paint, final boolean fill) {
        preparePainting();
        if (paint instanceof GradientPaint) {
            log.error("NYI: Gradient paint");
        } else if (paint instanceof TexturePaint) {
            if (fill) {
                try {
                    // create pattern with texture and use it for filling of a
                    // graphics object
                    final PSTilingPattern psTilingPattern = new PSTilingPattern(
                            "Pattern1", (TexturePaint) paint, 0, 0, 3, null);
                    this.gen.write(psTilingPattern.toString());
                    this.gen.writeln("/Pattern "
                            + this.gen.mapCommand("setcolorspace"));
                    this.gen.writeln(psTilingPattern.getName() + " "
                            + this.gen.mapCommand("setcolor"));
                } catch (final IOException ioe) {
                    log.error("IOException", ioe);
                }
            }
        }
    }

    /**
     * Applies a new Stroke object.
     * 
     * @param stroke
     *            Stroke object to use
     */
    protected void applyStroke(final Stroke stroke) {
        preparePainting();
        try {
            applyStroke(stroke, this.gen);
        } catch (final IOException ioe) {
            log.error("IOException", ioe);
        }
    }

    /**
     * Applies a new Stroke object.
     * 
     * @param stroke
     *            the Stroke instance
     * @param gen
     *            the PS generator
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void applyStroke(final Stroke stroke, final PSGenerator gen)
            throws IOException {
        if (stroke instanceof BasicStroke) {
            final BasicStroke basicStroke = (BasicStroke) stroke;

            final float[] da = basicStroke.getDashArray();
            if (da != null) {
                final StringBuilder sb = new StringBuilder("[");
                for (int count = 0; count < da.length; count++) {
                    sb.append(gen.formatDouble(da[count]));
                    if (count < da.length - 1) {
                        sb.append(" ");
                    }
                }
                sb.append("] ");
                final float offset = basicStroke.getDashPhase();
                sb.append(gen.formatDouble(offset));
                gen.useDash(sb.toString());
            } else {
                gen.useDash(null);
            }
            final int ec = basicStroke.getEndCap();
            switch (ec) {
            case BasicStroke.CAP_BUTT:
                gen.useLineCap(0);
                break;
            case BasicStroke.CAP_ROUND:
                gen.useLineCap(1);
                break;
            case BasicStroke.CAP_SQUARE:
                gen.useLineCap(2);
                break;
            default:
                log.error("Unsupported line cap: " + ec);
            }

            final int lj = basicStroke.getLineJoin();
            switch (lj) {
            case BasicStroke.JOIN_MITER:
                gen.useLineJoin(0);
                final float ml = basicStroke.getMiterLimit();
                gen.useMiterLimit(ml >= -1 ? ml : 1);
                break;
            case BasicStroke.JOIN_ROUND:
                gen.useLineJoin(1);
                break;
            case BasicStroke.JOIN_BEVEL:
                gen.useLineJoin(2);
                break;
            default:
                log.error("Unsupported line join: " + lj);
            }
            final float lw = basicStroke.getLineWidth();
            gen.useLineWidth(lw);
        } else {
            log.error("Stroke not supported: " + stroke.toString());
        }
    }

    /**
     * Renders a {@link RenderedImage}, applying a transform from image space
     * into user space before drawing. The transformation from user space into
     * device space is done with the current <code>Transform</code> in the
     * <code>Graphics2D</code>. The specified transformation is applied to the
     * image before the transform attribute in the <code>Graphics2D</code>
     * context is applied. The rendering attributes applied include the
     * <code>Clip</code>, <code>Transform</code>, and <code>Composite</code>
     * attributes. Note that no rendering is done if the specified transform is
     * noninvertible.
     * 
     * @param img
     *            the image to be rendered
     * @param xform
     *            the transformation from image space into user space
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    @Override
    public void drawRenderedImage(final RenderedImage img,
            final AffineTransform xform) {
        preparePainting();
        try {
            final AffineTransform at = getTransform();
            this.gen.saveGraphicsState();
            this.gen.concatMatrix(at);
            this.gen.concatMatrix(xform);
            final Shape imclip = getClip();
            writeClip(imclip);
            PSImageUtils.renderBitmapImage(img, 0, 0, img.getWidth(),
                    img.getHeight(), this.gen);
            this.gen.restoreGraphicsState();
        } catch (final IOException ioe) {
            log.error("IOException", ioe);
        }
    }

    /**
     * Renders a {@link RenderableImage}, applying a transform from image space
     * into user space before drawing. The transformation from user space into
     * device space is done with the current <code>Transform</code> in the
     * <code>Graphics2D</code>. The specified transformation is applied to the
     * image before the transform attribute in the <code>Graphics2D</code>
     * context is applied. The rendering attributes applied include the
     * <code>Clip</code>, <code>Transform</code>, and <code>Composite</code>
     * attributes. Note that no rendering is done if the specified transform is
     * noninvertible.
     * <p>
     * Rendering hints set on the <code>Graphics2D</code> object might be used
     * in rendering the <code>RenderableImage</code>. If explicit control is
     * required over specific hints recognized by a specific
     * <code>RenderableImage</code>, or if knowledge of which hints are used is
     * required, then a <code>RenderedImage</code> should be obtained directly
     * from the <code>RenderableImage</code> and rendered using
     * {@link #drawRenderedImage(RenderedImage, AffineTransform)
     * drawRenderedImage}.
     * 
     * @param img
     *            the image to be rendered
     * @param xform
     *            the transformation from image space into user space
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     * @see #drawRenderedImage
     */
    @Override
    public void drawRenderableImage(final RenderableImage img,
            final AffineTransform xform) {
        preparePainting();
        log.error("NYI: drawRenderableImage");
    }

    /**
     * Establishes the given color in the PostScript interpreter.
     * 
     * @param c
     *            the color to set
     * @throws IOException
     *             In case of an I/O problem
     */
    public void establishColor(final Color c) throws IOException {
        this.gen.useColor(c);
    }

    /**
     * Renders the text specified by the specified <code>String</code>, using
     * the current <code>Font</code> and <code>Paint</code> attributes in the
     * <code>Graphics2D</code> context. The baseline of the first character is
     * at position (<i>x</i>,&nbsp;<i>y</i>) in the User Space. The rendering
     * attributes applied include the <code>Clip</code>, <code>Transform</code>,
     * <code>Paint</code>, <code>Font</code> and <code>Composite</code>
     * attributes. For characters in script systems such as Hebrew and Arabic,
     * the glyphs can be rendered from right to left, in which case the
     * coordinate supplied is the location of the leftmost character on the
     * baseline.
     * 
     * @param s
     *            the <code>String</code> to be rendered
     * @param x
     *            the x-coordinate where the <code>String</code> should be
     *            rendered
     * @param y
     *            the y-coordinate where the <code>String</code> should be
     *            rendered
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see java.awt.Graphics#setFont
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    @Override
    public void drawString(final String s, final float x, final float y) {
        try {
            if (this.customTextHandler != null && !this.textAsShapes) {
                this.customTextHandler.drawString(this, s, x, y);
            } else {
                this.fallbackTextHandler.drawString(this, s, x, y);
            }
        } catch (final IOException ioe) {
            log.error("IOException", ioe);
        }
    }

    /**
     * Fills the interior of a <code>Shape</code> using the settings of the
     * <code>Graphics2D</code> context. The rendering attributes applied include
     * the <code>Clip</code>, <code>Transform</code>, <code>Paint</code>, and
     * <code>Composite</code>.
     * 
     * @param s
     *            the <code>Shape</code> to be filled
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    @Override
    public void fill(final Shape s) {
        preparePainting();
        try {
            this.gen.saveGraphicsState();

            final AffineTransform trans = getTransform();
            final boolean newTransform = !trans.isIdentity();

            if (newTransform) {
                this.gen.concatMatrix(trans);
            }
            final Shape imclip = getClip();
            if (shouldBeClipped(imclip, s)) {
                writeClip(imclip);
            }

            establishColor(getColor());

            applyPaint(getPaint(), true);

            this.gen.writeln(this.gen.mapCommand("newpath"));
            final int windingRule = processShape(s);
            doDrawing(true, false, windingRule == PathIterator.WIND_EVEN_ODD);
            this.gen.restoreGraphicsState();
        } catch (final IOException ioe) {
            log.error("IOException", ioe);
        }
    }

    /**
     * Commits a painting operation.
     * 
     * @param fill
     *            filling
     * @param stroke
     *            stroking
     * @param nonzero
     *            true if the non-zero winding rule should be used when filling
     * @exception IOException
     *                In case of an I/O problem
     */
    protected void doDrawing(final boolean fill, final boolean stroke,
            final boolean nonzero) throws IOException {
        preparePainting();
        if (fill) {
            if (stroke) {
                if (!nonzero) {
                    this.gen.writeln(this.gen.mapCommand("gsave") + " "
                            + this.gen.mapCommand("fill") + " "
                            + this.gen.mapCommand("grestore") + " "
                            + this.gen.mapCommand("stroke"));
                } else {
                    this.gen.writeln(this.gen.mapCommand("gsave") + " "
                            + this.gen.mapCommand("eofill") + " "
                            + this.gen.mapCommand("grestore") + " "
                            + this.gen.mapCommand("stroke"));
                }
            } else {
                if (!nonzero) {
                    this.gen.writeln(this.gen.mapCommand("fill"));
                } else {
                    this.gen.writeln(this.gen.mapCommand("eofill"));
                }
            }
        } else {
            // if(stroke)
            this.gen.writeln(this.gen.mapCommand("stroke"));
        }
    }

    /**
     * Returns the device configuration associated with this
     * <code>Graphics2D</code>.
     * 
     * @return the device configuration
     */
    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return new PSGraphicsConfiguration();
    }

    /**
     * Used to create proper font metrics
     */
    private Graphics2D fmg;

    {
        final BufferedImage bi = new BufferedImage(1, 1,
                BufferedImage.TYPE_INT_ARGB);

        this.fmg = bi.createGraphics();
    }

    /**
     * Gets the font metrics for the specified font.
     * 
     * @return the font metrics for the specified font.
     * @param f
     *            the specified font
     * @see java.awt.Graphics#getFont
     * @see java.awt.FontMetrics
     * @see java.awt.Graphics#getFontMetrics()
     */
    @Override
    public java.awt.FontMetrics getFontMetrics(final java.awt.Font f) {
        return this.fmg.getFontMetrics(f);
    }

    /**
     * Sets the paint mode of this graphics context to alternate between this
     * graphics context's current color and the new specified color. This
     * specifies that logical pixel operations are performed in the XOR mode,
     * which alternates pixels between the current color and a specified XOR
     * color.
     * <p>
     * When drawing operations are performed, pixels which are the current color
     * are changed to the specified color, and vice versa.
     * <p>
     * Pixels that are of colors other than those two colors are changed in an
     * unpredictable but reversible manner; if the same figure is drawn twice,
     * then all pixels are restored to their original values.
     * 
     * @param c1
     *            the XOR alternation color
     */
    @Override
    public void setXORMode(final Color c1) {
        log.error("NYI: setXORMode");
    }

    /**
     * Copies an area of the component by a distance specified by
     * <code>dx</code> and <code>dy</code>. From the point specified by
     * <code>x</code> and <code>y</code>, this method copies downwards and to
     * the right. To copy an area of the component to the left or upwards,
     * specify a negative value for <code>dx</code> or <code>dy</code>. If a
     * portion of the source rectangle lies outside the bounds of the component,
     * or is obscured by another window or component, <code>copyArea</code> will
     * be unable to copy the associated pixels. The area that is omitted can be
     * refreshed by calling the component's <code>paint</code> method.
     * 
     * @param x
     *            the <i>x</i> coordinate of the source rectangle.
     * @param y
     *            the <i>y</i> coordinate of the source rectangle.
     * @param width
     *            the width of the source rectangle.
     * @param height
     *            the height of the source rectangle.
     * @param dx
     *            the horizontal distance to copy the pixels.
     * @param dy
     *            the vertical distance to copy the pixels.
     */
    @Override
    public void copyArea(final int x, final int y, final int width,
            final int height, final int dx, final int dy) {
        log.error("NYI: copyArea");
    }

    /*
     * --- for debugging public void transform(AffineTransform tx) {
     * log.info("transform(" + toArray(tx) + ")"); super.transform(zx); }
     * 
     * public void scale(double sx, double sy) { log.info("scale(" + sx + ", " +
     * sy + ")"); super.scale(sx, sy); }
     * 
     * public void translate(double tx, double ty) {
     * log.info("translate(double " + tx + ", " + ty + ")"); super.translate(tx,
     * ty); }
     * 
     * public void translate(int tx, int ty) { log.info("translate(int " + tx +
     * ", " + ty + ")"); super.translate(tx, ty); }
     */

}
