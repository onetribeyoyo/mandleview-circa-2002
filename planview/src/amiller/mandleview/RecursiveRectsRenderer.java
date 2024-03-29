/*
 *  Copyright (c) 1998-2002 Andrew R. Miller.  All rights reserved.
 *
 *  $Header: $
 */
package amiller.mandleview;

import amiller.colormap.ColorMap;

import amiller.planview.Planview;

import amiller.surface.Surface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import java.awt.geom.Point2D;


/**
 *  ???
 *
 *  @author		Andrew R. Miller
 *  @author		Code reviewed by NOBODY on NEVER
 *  @version    $Revision: $  $Modtime: $
 */
public class RecursiveRectsRenderer
    extends MandlebrotRenderer
{
    //
    // Static data.
    //

    /** The source revision. */
    public static final String REVISION = "$Revision: $";


    //
    // Static methods.
    //


    //
    // Instance data.
    //

    private HLinesRenderer hRenderer;
    private VLinesRenderer vRenderer;


    //
    // Constructors.
    //

    public RecursiveRectsRenderer(MandlebrotSet mset) {
        super(mset);

        this.hRenderer = new HLinesRenderer(mset);
        this.vRenderer = new VLinesRenderer(mset);
    }


    //
    // Instance methods.
    //

    public void setColorMap(ColorMap cmap) {
        super.setColorMap(cmap);

        if (this.hRenderer != null) {
            this.hRenderer.setColorMap(cmap);
        }
        if (this.vRenderer != null) {
            this.vRenderer.setColorMap(cmap);
        }
    }


    /**
     *  RenderingAlgorithm instance that paints the mandlebrot
     *  set by recursivly subdividing into smaller rectangles.
     */
    public final void render(Surface surf, Planview view, Graphics g, Point p, Dimension d)
    {
        long startTime = System.currentTimeMillis();


        // Determine the pixel value of the Y axis.
        Point2D.Double modelOrigin = new Point2D.Double(0.0, 0.0);
        Point2D        viewOrigin = view.getViewPoint(modelOrigin);
        int yAxis = (int) viewOrigin.getY();

        if ((yAxis < p.y) || ((p.y + d.height) <= yAxis)) {
            // The axis does not bisect the region we are painting.
            renderRect(view, g, p.x, p.y, d.width, d.height);
        }
        else {
            // The axis does bisect the region we are painting.
            renderRect(view, g, p.x,       p.y, d.width - 1,             yAxis - p.y);
            renderRect(view, g, p.x, yAxis + 1, d.width - 1, d.height - (yAxis - p.y) - 2);
        }

        long endTime = System.currentTimeMillis();

        System.out.println("RecursiveRects render(...) in " + (endTime - startTime)/1000.0 + " seconds.");
    }

    /** The size (width and height) of the smallest rectangle that we should subdivide. */
    private static final int smallestRect = 7;

    /**
     *  Utility method that paints a rectangular region of the
     *  mandlebrot set using the rectangular border tracing / edge
     *  checking method.  The region is defined by point (px, py) and
     *  dimensions (w, h).  It is assumed that the point region is not
     *  bisected by the Y axis.
     */
    public void renderRect(Planview view, Graphics g, int viewX, int viewY, int viewW, int viewH)
    {
        //System.out.println("renderRect(..., x=" + viewX + ", y=" + viewY + ", w=" + viewW + ", h=" + viewH + ")");

        // Don't bother trying to optimze tiny rectangles.
        if ((viewW < smallestRect) || (viewH < smallestRect)) {
            if (viewH < viewW) {
                //System.out.println("renderHLine(..., x=" + viewX + ", y=" + viewY + ", w=" + (viewX + 1) + ")");
                hRenderer.render(mset, view, g, new Point(viewX, viewY), new Dimension(viewW + 1, viewH));
            }
            else {
                //System.out.println("renderVLine(..., x=" + viewX + ", y=" + viewY + ", h=" + (viewY + 1) + ")");
                vRenderer.render(mset, view, g, new Point(viewX, viewY), new Dimension(viewW, viewH + 1));
            }

        } else {
            // for larger rectangles...
            // First, render the rect's border.

            Color left     = renderVerticalLine(view, g, viewX,         viewY + 1,     viewH - 1);
            Color right    = renderVerticalLine(view, g, viewX + viewW, viewY + 1,     viewH - 1);
            Color top    = renderHorizontalLine(view, g, viewX,         viewY,         viewW + 1);
            Color bottom = renderHorizontalLine(view, g, viewX,         viewY + viewH, viewW + 1);

            // check for the special case for border tracing / edge checking...
            if ((top != null) && (top == bottom) && (bottom == left) && (left == right)) {
                // all edges are the same colour? therefore the interior is that colour too
                g.setColor(top);
                //g.fillRect(viewX + 1, viewY + 1, viewW - 1, viewH - 1);
                g.fillRect(viewX, viewY, viewW, viewH);
            }
            else {
                // Divide the rectangle, and recurse.

                int halfWidth  = viewW / 2;
                int halfHeight = viewH / 2;

                // Top left.
                renderRect(view, g, viewX + 1, viewY + 1, halfWidth, halfHeight);

                // Top right.
                renderRect(view, g, viewX + halfWidth + 2, viewY + 1, viewW - halfWidth - 3, halfHeight);

                // Bottom left.
                renderRect(view, g, viewX + 1, viewY + halfHeight + 2, halfWidth, viewH - halfHeight - 3);

                // Bottom right.
                renderRect(view, g,
                           viewX + halfWidth + 2, viewY + halfHeight + 2,
                           viewW - halfWidth - 3, viewH - halfHeight - 3);
            }
        }
    }

}
