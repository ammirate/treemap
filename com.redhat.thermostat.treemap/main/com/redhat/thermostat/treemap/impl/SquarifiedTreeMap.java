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

package com.redhat.thermostat.treemap.impl;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.redhat.thermostat.treemap.model.TreeMapNode;

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
     * Object used to process rectangles.
     */
    TreeMapBuilder builder;
    
    /**
     * List of node to represent as TreeMap.
     */
    List<TreeMapNode> elements;
    
    /**
     * Represent the area in which draw nodes.
     */
    Rectangle2D.Double container;

    /**
     * Constructor.
     * 
     * @param d the dimension of the total area in which draw elements.
     * @param list the list of elements to draw as TreeMap.
     */
    public SquarifiedTreeMap(Rectangle2D.Double bounds, List<TreeMapNode> list) {
        if (bounds == null || list == null) {
            throw new TreeMapException(getClass() + " does not allow null parameters");
        }
        this.elements = list;
        this.container = bounds;
    }

    /**
     * Invoke this method to calculate the rectangles for the TreeMap.
     * 
     * @return a list of node having a rectangle built in percentage to the 
     * available area.
     */
    public List<TreeMapNode> squarefy() {
        builder = new TreeMapBuilder(container);
        builder.prepareData(elements);
        List<TreeMapNode> row = new ArrayList<>();
        double w = builder.getPrincipalSide();	
        squarefy(elements, row, w);
        return builder.getSquarifiedNodes();
    }

    /**
     * Calculate recursively the rectangles to draw and their size.
     * 
     * @param nodes the list of elements to draw.
     * @param row the list of current rectangles to process.
     * @param w the side against which to calculate the rectangles.
     */
    private void squarefy(List<TreeMapNode> nodes, List<TreeMapNode> row, double w) {
        if(nodes.isEmpty() && row.isEmpty()) {
            // work done
            return;
        }
        if(nodes.isEmpty()) {
            // no more element to process, just draw current row
            builder.finalizeRow(row);
            return;
        }
        if(row.isEmpty() && !nodes.isEmpty()) {
            // add the first element to the row and iterate the process over it
            row.add(head(nodes));
            pop(nodes);
            squarefy(nodes, row, w);
            return;
        }
        
        /*  Greedy step: calculate the best aspect ratio of actual row and the
         *  best aspect ratio given by adding another rectangle to the row.
         *  If the current row can not be improved then finalize it
         *  else add the next element, to improve the global aspect ratio
         */
        List<TreeMapNode> expandedRow = new ArrayList<TreeMapNode>(row);
        expandedRow.add(head(nodes));
        double actualAspectRatio = builder.bestAspectRatio(row, w);
        double expandedAspectRatio = builder.bestAspectRatio(expandedRow, w);

        if(!builder.willImprove(actualAspectRatio, expandedAspectRatio)) {
            builder.finalizeRow(row);
            squarefy(nodes, new ArrayList<TreeMapNode>(), builder.getPrincipalSide());
        } else {
            squarefy(tail(nodes), expandedRow, w);
        }
    }
    
    /**
     * Return the first element of the list.
     * @param list the list from which take the first element.
     * @return the first element of the list.
     */
    private TreeMapNode head(List<TreeMapNode> list) {
        return list.get(0);
    }

    /**
     * Return a copy of the actual list without its first element.
     * @param list the list from which remove the first element.
     * @return a copy of the given list without its first element.
     */
    private List<TreeMapNode> tail(List<TreeMapNode> list) {
        List<TreeMapNode> tail = new ArrayList<>(list);
        tail.remove(0);
        return tail;
    }

    /**
     * Remove the first element from the list.
     * @param list the list from which remove the first element.
     */
    private void pop(List<TreeMapNode> list) {
        list.remove(0);
    }

    /**
     * Return the rectangles list.
     * @return a list of rectangles.
     */
    public List<TreeMapNode> getSquarifiedNodes() {
        return builder.getSquarifiedNodes();
    }
}
