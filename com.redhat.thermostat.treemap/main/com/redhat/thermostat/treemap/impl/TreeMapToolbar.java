package com.redhat.thermostat.treemap.impl;

import java.awt.BorderLayout;
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

@SuppressWarnings("serial")
public class TreeMapToolbar extends JComponent {
    
    private JPanel contentPane;
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
