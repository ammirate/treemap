package com.redhat.thermostat.treemap.impl;

import com.redhat.thermostat.treemap.model.TreeMapNode;

/**
 * This interface is used as part of the Observer Design Pattern developed
 * for objects who want to be notified about TreeMap's events.
 */
public interface TreeMapObserver {
    
    /**
     * This method inform the Observer object that the object passed as 
     * argument has been selected.
     * 
     * @param selectedComp the selected component to communicate to 
     * this Observer object.
     */
    public void notifySelection(TreeMapNode node);
    
    /**
     * This method informs objects that a zoom in event has been performed on
     * the given node.
     * @param node the zoomed node.
     */
    public void notifyZoomIn(TreeMapNode node);
    
    /**
     * This method informs objects that a zoom out event has been performed.
     */
    public void notifyZoomOut();
    
    /**
     * This method informs objects that the zoom level has been resetted.
     */
    public void notifyZoomFull();
}
