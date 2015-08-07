package com.redhat.thermostat.treemap.util;

import java.awt.Color;

/**
 * This class provides a simple iterator on an array of light colors.
 * It implements the Singleton Design Pattern so you need to call getInstance()
 * to use it.
 */
public class ColorManager {
    /**
     * Singleton instance of this class.
     */
    private static ColorManager instance;    

    /**
     * Colors available on which iterate
     */
    private Color[] colors = {
            Color.decode("#CDF9D4"), // green
            Color.decode("#B9D6FF"), // blue
            Color.decode("#FACED2"), // red
            Color.decode("#E5E5E5"), // grey
            Color.decode("#E4D1FC"), // purple
            Color.decode("#ABEBEE"), // aqua
            Color.decode("#FFE7C7"), // orange
            Color.decode("#FFFFFF")  // white
    };

    /**
     * Hold the array's index.
     */
    private static int index;

    /**
     * Private constructor. It resets the index.
     */
    private ColorManager() {
        reset();
    }   
    
    /**
     * Reinitializes the array's index.
     */
    public void reset() {
        index = 0;
    }
    
    /**
     * Return the next color on the array.
     * @return a {@link Color} object.
     */
    public Color getNextColor() {  
        return colors[(index++) % colors.length];
    }

    /**
     * Returns the following color in the colors' array.
     * @param c the starting color.
     * @return the color which came after the one given in input.
     */
    public Color getNextColor(Color c) {
        for (int i = 0; i < colors.length; i++) {
            if (c == colors[i]) {
                return colors[(i + 1) % colors.length];
            }
        }
        return getNextColor();
    }

    /**
     * This method allow to access to this class' constructor.
     * @return the unique instance of this class.
     */
    public static ColorManager getInstance() {
        if (instance == null) {
            instance = new ColorManager();
        }
        return instance;
    }
}
