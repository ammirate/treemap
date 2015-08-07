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
