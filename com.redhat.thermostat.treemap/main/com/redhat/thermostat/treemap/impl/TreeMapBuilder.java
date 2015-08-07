package com.redhat.thermostat.treemap.impl;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.redhat.thermostat.treemap.model.TreeMapNode;

/**
 * This class provides a set of methods to build a not nested TreeMap. It is 
 * based on {@link TreeMapNode} objects, in order to link each node to its  
 * rectangle inside the final TreeMap structure. 
 * 
 * @see SquarifiedTreeMap
 * @see TreeMapNode
 */
public class TreeMapBuilder {

    private static final int LEFT_RIGHT = 0;
    private static final int TOP_BOTTOM = 1;

    /**
     * Indicates the drawing direction.
     */
    private int direction;

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
     * @param the area dimension in which rectangles will be drawn.
     */
    public TreeMapBuilder(Rectangle2D rect) {
        if (rect == null) {
            throw new TreeMapException(getClass() + " constructor does not allow a null parameter");
        }
        availableArea = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
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
        direction = availableArea.getWidth() > availableArea.getHeight() ? TOP_BOTTOM : LEFT_RIGHT;
    }


    /**
     * Invert the drawing direction.
     */
    private void invertDirection() {
        direction = direction == LEFT_RIGHT ? TOP_BOTTOM : LEFT_RIGHT;
    }

    /**
     * Add the given node to the current list of element to draw as a rectangle.
     * @param node the node to represent.
     */
    public void addToCurrentRow(TreeMapNode node) {
        currentRow.add(node);
    }

    /**
     * Keep the current list of nodes which produced the best aspect ratio
     * in the available area, draw their respective rectangles and reinitialize 
     * the current row to draw.
     * <p>
     * @param nodes the list of numbers which represent the rectangles' area.
     * @return the number of Rectangles created.
     */
    public void finalizeRow(List<TreeMapNode> nodes) {
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
        if (direction == TOP_BOTTOM) {
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
    public double getPrincipalSide() {
        return direction == LEFT_RIGHT ? availableArea.getWidth() : availableArea.getHeight();
    }

    /**
     * 
     * @return the secondary available area's side.
     */
    private double getSecondarySide() {
        return direction == LEFT_RIGHT ? availableArea.getHeight() : availableArea.getWidth();
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
        if (direction == LEFT_RIGHT) {
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
        if (direction == LEFT_RIGHT) {
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
     * Store the current row in the parent node
     */
    private void storeRowInParent() {
        TreeMapNode parent = currentRow.get(0).getParent();
        if(parent == null) {
            return;
        }
        List<Integer> row = new ArrayList<>();
        for(TreeMapNode child : currentRow) {
            row.add(child.getId());
        }
    }
    
    
    /**
     * Close the current row and initialize a new one.
     */
    private void newRow() {
        storeRowInParent();
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
    public double bestAspectRatio(List<TreeMapNode> row, double side) {
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
     * Return the list of rectangles.
     * @return The list of rectangles rows.
     */
    public List<TreeMapNode> getSquarifiedNodes() {
        return this.squarifiedNodes;
    }
    
    /**
     * Prepare the elements in the list, sorting them and transforming them
     * proportionally the given dimension.
     * @param dim the dimension in which rectangles will be drawn.
     * @param elements the list of elements to draw.
     * @return the list sorted and proportioned to the dimension.
     */
    public void  prepareData( List<TreeMapNode> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }
        TreeMapNode.quickSort(elements, 0, elements.size() - 1);
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
    public boolean willImprove(double actualAR, double expandedAR) {
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
