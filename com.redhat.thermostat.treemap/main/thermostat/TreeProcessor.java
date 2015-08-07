/*
 * Copyright 2012-2015 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package thermostat;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

public class TreeProcessor {

    /**
     * Padding between the main component and its sub component.
     */
    public static final int X_PADDING = 15;
    public static final int Y_PADDING = 20;

    /**
     * This method process recursively the tree nested in the node element
     * passed as argument in the constructor calculating the children TreeMap 
     * for each node also applying coloring.
     * @return the updated tree, where nodes have additional information like
     * {@link Rectangle2D>Float} instance and a color.
     */
    public static TreeMapNode processTreeMap(TreeMapNode tree, Rectangle2D.Double area) {
        Objects.requireNonNull(tree);
        Objects.requireNonNull(area);
        tree.setRectangle(area);
        if (tree.getColor() == null) {
            tree.setColor(tree.START_COLOR);
        }
        
        process(tree);
        return tree;
    }

    /**
     * This method is used to effectively process the whole tree structure. It
     * uses a {@link SquarifiedTreeMap} object to calculate a TreeMap for each
     * node who has children.
     * @param node the subtree's root to process
     */
    private static void process(TreeMapNode node) {                                                                                                                            
        
        SquarifiedTreeMap algorithm = new SquarifiedTreeMap(getSubArea(node.getRectangle()), node.getChildren());
        node.setChildren(algorithm.squarify());

        Color c = node.getNextColor();
        
        for (TreeMapNode child : node.getChildren()) {
            //children will have all the same color, which is the parent's next one
            if (child.getColor() == null) {
                child.setColor(c);
            }
            // if squarified rectangles have drawable sides then continue to 
            // process, else don't process the subtree having as root a 
            // non drawable rectangle.
            if (child.isDrawable()) {
                process(child);
            } 
        }
    }

    /**
     * Calculate space and coordinates in which children's rectangle will be 
     * drawn, from the main component.
     * @return the rectangle representing the new available area.
     */
    private static Rectangle2D.Double getSubArea(Rectangle2D.Double area) {
        Rectangle2D.Double subArea = new Rectangle2D.Double();
        subArea.setRect(area);

        subArea.width = Math.max(0, (subArea.width - 2 * X_PADDING));
        subArea.height = Math.max(0, (subArea.height - 1.5 * Y_PADDING));
        return subArea;
    }  
}
