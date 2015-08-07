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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 *  This class implements the Squarified algorithm for TreeMaps. Using it, it is 
 *  possible to associate a rectangle to a {@link TreeMapNode} element and its 
 *  children.
 *  <p>
 *  @see TreeMapNode
 *  @see TreMapBuilder
 */
public class SquarifiedTreeMap {
    
    /**
     * List of node to represent as TreeMap.
     */
    private LinkedList<TreeMapNode> elements;
    
    /**
     * Represent the area in which draw nodes.
     */
    private Rectangle2D.Double container;
    
    private enum DIRECTION {
        LEFT_RIGHT,
        TOP_BOTTOM
    }

    /**
     * Indicates the drawing direction.
     */
    private DIRECTION drawingDir;

    /**
     * The rectangles area available for drawing.
     */
    private Rectangle2D.Double availableArea;

    /**
     * List of the calculated rectangles.
     */
    private List<TreeMapNode> squarifiedNodes;

    /**
     * List of the current rectangles under processing.
     */
    private List<TreeMapNode> currentRow;

    /**
     * Coordinates on which to draw.
     */
    private double lastX = 0;
    private double lastY = 0;


    /**
     * Constructor.
     * 
     * @param d the dimension of the total area in which draw elements.
     * @param list the list of elements to draw as TreeMap.
     * 
     * @throws a NullPointerException if one of the arguments is null.
     */
    public SquarifiedTreeMap(Rectangle2D.Double bounds, List<TreeMapNode> list) {
        this.elements = new LinkedList<>();
        elements.addAll(Objects.requireNonNull(list));
        this.container = Objects.requireNonNull(bounds);
    }

    /**
     * Invoke this method to calculate the rectangles for the TreeMap.
     * 
     * @return a list of node having a rectangle built in percentage to the 
     * available area.
     */
    public List<TreeMapNode> squarify() {
        initializeArea();
        prepareData(elements);
        List<TreeMapNode> row = new ArrayList<>();
        double w = getPrincipalSide();	
        squarify(elements, row, w);
        return getSquarifiedNodes();
    }

    /**
     * Calculate recursively the rectangles to draw and their size.
     * 
     * @param nodes the list of elements to draw.
     * @param row the list of current rectangles to process.
     * @param w the side against which to calculate the rectangles.
     */
    private void squarify(LinkedList<TreeMapNode> nodes, List<TreeMapNode> row, double w) {
        if (nodes.isEmpty() && row.isEmpty()) {
            // work done
            return;
        }
        if (nodes.isEmpty()) {
            // no more element to process, just draw current row
            finalizeRow(row);
            return;
        }
        if (row.isEmpty()) {
            // add the first element to the row and iterate the process over it
            row.add(nodes.getFirst());
            nodes.removeFirst();
            squarify(nodes, row, w);
            return;
        }
        
        /*  Greedy step: calculate the best aspect ratio of actual row and the
         *  best aspect ratio given by adding another rectangle to the row.
         *  If the current row can not be improved then finalize it
         *  else add the next element, to improve the global aspect ratio
         */
        List<TreeMapNode> expandedRow = new ArrayList<TreeMapNode>(row);
        expandedRow.add(nodes.getFirst());
        double actualAspectRatio = bestAspectRatio(row, w);
        double expandedAspectRatio = bestAspectRatio(expandedRow, w);

        if (!willImprove(actualAspectRatio, expandedAspectRatio)) {
            finalizeRow(row);
            squarify(nodes, new ArrayList<TreeMapNode>(), getPrincipalSide());
        } else {
            nodes.removeFirst();
            squarify(nodes, expandedRow, w);
        }
    }

    /**
     * Return the rectangles list.
     * @return a list of rectangles.
     */
    public List<TreeMapNode> getSquarifiedNodes() {
        return squarifiedNodes;
    }
    
    /**
     * Initialize the available area used to create the tree map
     */
    private void initializeArea() {
        availableArea = new Rectangle2D.Double(container.getX(), container.getY(), 
                container.getWidth(), container.getHeight());
        lastX = 0;
        lastY = 0;
        squarifiedNodes = new ArrayList<>();
        currentRow = new ArrayList<>();
        updateDirection();
    }
    
    /**
     * Recalculate the drawing direction.
     */
    private void updateDirection() {
        drawingDir = availableArea.getWidth() > availableArea.getHeight() ? 
                DIRECTION.TOP_BOTTOM : DIRECTION.LEFT_RIGHT;
    }


    /**
     * Invert the drawing direction.
     */
    private void invertDirection() {
        drawingDir = drawingDir == DIRECTION.LEFT_RIGHT ? 
                DIRECTION.TOP_BOTTOM : DIRECTION.LEFT_RIGHT;
    }
    
    /**
     * Keep the current list of nodes which produced the best aspect ratio
     * in the available area, draw their respective rectangles and reinitialize 
     * the current row to draw.
     * <p>
     * @param nodes the list of numbers which represent the rectangles' area.
     * @return the number of Rectangles created.
     */
    private void finalizeRow(List<TreeMapNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        // get the total weight of nodes in order to calculate their percentages
        double sum = getSum(nodes);
        // greedy optimization step: get the best aspect ratio for nodes drawn 
        // on the longer and on the smaller side, to evaluate the best.
        double actualAR = bestAspectRatio(nodes, getPrincipalSide());
        double alternativeAR = bestAspectRatio(nodes, getSecondarySide());
      
        if (willImprove(actualAR, alternativeAR)) {
            invertDirection();
        }

        for (TreeMapNode node: nodes) {
            // assign a rectangle calculated as percentage of the total weight
            Rectangle2D.Double r = createRectangle(sum, node.getWeight());
            node.setRectangle(r);
            
            // recalculate coordinates to draw next rectangle
            updateXY(r);

            // add the node to the current list of rectangles in processing
            currentRow.add(node);
        }
        // recalculate the area in which new rectangles will be drawn and 
        // reinitialize the current list of node to represent.
        reduceAvailableArea();
        newRow();
    }
    

    /**
     * Create a rectangle having area = @param area in percentage of @param sum. 
     * <p>
     * For example: assume @param area = 4 and @param sum = 12 and the 
     * drawing direction is top to bottom. <br>
     * <p>
     *   __ __ __ __
     *  |     |     | 
     *  |__ __|     |  
     *  |__ __ __ __|
     * 
     * <br>the internal rectangle will be calculated as follow:<br>
     *  {@code height = (4 / 9) * 3} <--note that the principal side for actual  
     *  drawing direction is 3.
     *  <br>Now it is possible to calculate the width:<br>
     *  {@code width = 4 / 1.3} <-- note this is the height value
     *  
     * <p>
     * @param sum the total size of all rectangles in the actual row.
     * @param area this Rectangle's area.
     * @return the Rectangle which correctly fill the available area.
     */
    private Rectangle2D.Double createRectangle(Double sum, Double area) {
        double side = getPrincipalSide();
        double w = 0;
        double h = 0;
        
        //don't want division by 0
        if (validate(area) == 0 || validate(sum) == 0 || validate(side) == 0) {
            return new Rectangle2D.Double(lastX, lastY, 0, 0);
        }
        
        // calculate the rectangle's principal side relatively to the container 
        // rectangle's principal side.
        if (drawingDir == DIRECTION.TOP_BOTTOM) {
            h = (area / sum) * side;
            w = area / h;
        } else {
            w = (area / sum) * side;
            h = area / w;
        }        
        return new Rectangle2D.Double(lastX, lastY, w, h);
    }
    
    /**
     * Check if a double value is defined as Not a Number and sets it to 0.
     * @param d the value to check.
     * @return the checked value: 0 if the given number is NaN, else the number
     * itself.
     */
    private double validate(double d) {
        if (d == Double.NaN) {
            d = 0;
        }
        return d;
    }

    /**
     * Check in which direction the rectangles have to be drawn.
     * @return the side on which rectangles will be created.
     */
    private double getPrincipalSide() {
        return drawingDir == DIRECTION.LEFT_RIGHT ? 
                availableArea.getWidth() : availableArea.getHeight();
    }

    /**
     * 
     * @return the secondary available area's side.
     */
    private double getSecondarySide() {
        return drawingDir == DIRECTION.LEFT_RIGHT ? 
                availableArea.getHeight() : availableArea.getWidth();
    }

    /**
     * Sum the elements in the list.
     * @param nodes the list which contains elements to sum.
     * @return the sum of the elements.
     */
    private double getSum(List<TreeMapNode> nodes) {
        int sum = 0;
        for (TreeMapNode n : nodes) {
            sum += n.getWeight();
        }
        return sum;
    }

    /**
     * Recalculate the origin to draw next rectangles.
     * @param r the rectangle from which recalculate the origin.
     */
    private void updateXY(Rectangle2D.Double r) {
        if (drawingDir == DIRECTION.LEFT_RIGHT) {
            //lastY doesn't change
            lastX += r.width; 
        } else {
            //lastX doesn't change
            lastY += r.height;
        }
    }

    /**
     * Initialize the origin at the rectangle's origin.
     * @param r the rectangle used as origin source.
     */
    private void initializeXY(Rectangle2D.Double r) {
        lastX = r.x;
        lastY = r.y;
    }

    /**
     * Reduce the size of the available rectangle. Use it after the current 
     * row's closure.
     */
    private void reduceAvailableArea() {
        if (drawingDir == DIRECTION.LEFT_RIGHT) {
            // all rectangles inside the row have the same height
            availableArea.height -= currentRow.get(0).getRectangle().height;
            availableArea.y = lastY + currentRow.get(0).getRectangle().height;
            availableArea.x = currentRow.get(0).getRectangle().x;
        } else {
            // all rectangles inside the row have the same width
            availableArea.width -= currentRow.get(0).getRectangle().width;
            availableArea.x = lastX + currentRow.get(0).getRectangle().width;
            availableArea.y = currentRow.get(0).getRectangle().y;
        }
        updateDirection();
        initializeXY(availableArea);
    }
    
    /**
     * Close the current row and initialize a new one.
     */
    private void newRow() {
        squarifiedNodes.addAll(currentRow);
        currentRow = new ArrayList<>();
    }

    /**
     * Calculate the aspect ratio for all the rectangles in the list and
     * return the max of them.
     * @param row the list of rectangles.
     * @param side the side against which to calculate the the aspect ratio.
     * @return the max aspect ratio calculated for the row.
     */
    private double bestAspectRatio(List<TreeMapNode> row, double side) {
        if (row == null || row.isEmpty()) {
            return Double.MAX_VALUE;
        }
        double sum = getSum(row);
        double max = 0;
        // calculate the aspect ratio against the main side, and also its inverse.
        // this is because aspect ratio of rectangle 6x4 can be calculated as 
        // 6/4 but also 4/6. Here the aspect ratio has been calculated as 
        // indicated in the Squarified algorithm.
        for (TreeMapNode node : row) {
            double m1 = (Math.pow(side, 2) * node.getWeight()) / Math.pow(sum, 2);
            double m2 = Math.pow(sum, 2) / (Math.pow(side, 2) * node.getWeight());
            double m = Math.max(m1, m2);

            if (m > max) {
                max = m;
            }
        }
        return max;
    }

    
    /**
     * Prepare the elements in the list, sorting them and transforming them
     * proportionally the given dimension.
     * @param dim the dimension in which rectangles will be drawn.
     * @param elements the list of elements to draw.
     * @return the list sorted and proportioned to the dimension.
     */
    private void  prepareData(List<TreeMapNode> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }
        TreeMapNode.sort(elements);
        double totArea = availableArea.width * availableArea.height;
        double sum = getSum(elements);
        
        // recalculate weights in percentage of their sum
        for (TreeMapNode node : elements) {
            int w = (int) Math.round((node.getWeight()/sum) * totArea);
            node.setWeight(w);
        }
    }

    /**
     * This method check which from the values in input, that represent 
     * rectangles' aspect ratio, produces more approximatively a square.
     * It checks if one of the aspect ratio values gives a value nearest to 1 
     * against the other, which means that width and height are similar.
     * @param actualAR the actual aspect ratio
     * @param expandedAR the aspect ratio to evaluate
     * @return false if the actual aspect ratio is better than the new one, 
     * else true.
     */
    private boolean willImprove(double actualAR, double expandedAR) {
        if (actualAR == 0) {
            return true;
        }
        if (expandedAR == 0) {
            return false;
        }
        // check which value is closer to 1, the square's aspect ratio
        double v1 = Math.abs(actualAR - 1);
        double v2 = Math.abs(expandedAR - 1);       
        return v1 > v2;
    }
}
