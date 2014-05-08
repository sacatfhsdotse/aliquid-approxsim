package StratmasClient.filter;

import java.awt.Color;
import java.awt.image.RGBImageFilter;
import java.awt.image.ImageFilter;

/**
 * ImageFilter used to convert the color of an order symbol into the color of the orders resource symbol.
 */
public class OrderColorFilter extends RGBImageFilter {
    /**
     * The color of the order.
     */
    Color color;

    /**
     * Creates new filter.
     */
    protected OrderColorFilter(Color color) {
        this.color = color;
        canFilterIndexColorModel = true;
    }

    /**
     * Returns new OrderColorFilter.
     */
    public static ImageFilter getFilter(Color color) {
        return new OrderColorFilter(color);
    }

    /**
     * Converts each pixel of the image into the color of the order.
     */
    public int filterRGB(int x, int y, int rgb) {
        return color.getRGB();
    }
}
