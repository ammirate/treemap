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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * This class provides a tool bar containing a {@lnk TreeMapBreadcrumb} object 
 * and a {@link TreeMapZoomBar} instance to control the state of 
 * {@link TreeMapComponent} objects.
 */
@SuppressWarnings("serial")
public class TreeMapToolbar extends JComponent {
    
    /**
     * The panel in which objects are placed.
     */
    private JPanel contentPane;
    
    /**
     * The scroll pane used to hide breadcrumb's first items.
     */
    private JScrollPane scrollPane;
    
    
    public TreeMapToolbar(TreeMapComponent treemap) {
        super();
        initComponent(treemap);
    }


    private void initComponent(TreeMapComponent treemap) {
        this.setLayout(new BorderLayout());
        
        final JPanel breadcrumbPanel = new JPanel();
        breadcrumbPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        add(breadcrumbPanel, BorderLayout.CENTER);
        
        JPanel zoomPanel = new JPanel(new FlowLayout());
        // add some empty space between the breadcrumb bar and buttons
        zoomPanel.add(Box.createHorizontalStrut(20));
        add(zoomPanel, BorderLayout.EAST);

        TreeMapZoomBar zoomBar = new TreeMapZoomBar(treemap);
        zoomPanel.add(zoomBar);
        
        TreeMapBreadcrumb bc = new TreeMapBreadcrumb(treemap, treemap.getTreeMapRoot());
        
        contentPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPane.add(bc);

        scrollPane = new JScrollPane(contentPane);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        
        // allows to see always the last elements of the breadcrumb.
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });
        breadcrumbPanel.add(scrollPane);
        
        // when the component is resized the new dimension is used to arrange 
        // the scrollpane, in order to use all available space.
        breadcrumbPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                Dimension d = breadcrumbPanel.getSize();
                d.height = 20;
                scrollPane.setPreferredSize(d);
            }
        });
    }

}
