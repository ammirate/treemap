package com.redhat.thermostat.treemap.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;

import com.redhat.thermostat.treemap.model.TreeMapNode;

@SuppressWarnings("serial")
public class TreeMapZoomBar extends JComponent implements TreeMapObserver {
    
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton zoomFullButton;
    
    private TreeMapComponent treemap;
    
    private TreeMapNode selectedItem;
    
    public TreeMapZoomBar(TreeMapComponent treemap) {
        super();
        this.treemap = Objects.requireNonNull(treemap);
        initComponent();
        this.treemap.register(this);
    }
    
    
    private void initComponent() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        zoomInButton = createZoomInButton();
        zoomOutButton = createZoomOutButton();
        zoomFullButton = createZoomFullButton();
        
        this.add(zoomInButton);
        this.add(zoomFullButton);
        this.add(zoomOutButton);
        
        zoomInButton.setEnabled(false);
        zoomFullButton.setEnabled(false);
        zoomOutButton.setEnabled(false);
    }
    
    private JButton createZoomInButton() {
        JButton btn = new JButton();
        btn.setText("+");
        btn.setToolTipText("Zoom in the selected item.");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (selectedItem != null) {
                    treemap.zoomIn(selectedItem);
                }
            }
        });
        return btn;
    }
    
    private JButton createZoomOutButton() {
        JButton btn = new JButton();
        btn.setText("-");
        btn.setToolTipText("Zoom out to the above item.");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treemap.zoomOut();
            }
        });
        return btn;
    }
    
    private JButton createZoomFullButton() {
        JButton btn = new JButton();
        btn.setText("x");
        btn.setToolTipText("Restore the view.");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treemap.zoomFull();
            }
        });
        return btn;
    }
    
    
    private void changeState() {
        selectedItem = null;
        zoomInButton.setEnabled(false);
        
        if (!isRootShown()) {
            zoomFullButton.setEnabled(true);
            zoomOutButton.setEnabled(true);
        } else {
            zoomFullButton.setEnabled(false);
            zoomOutButton.setEnabled(false);
        }
    }

    @Override
    public void notifySelection(TreeMapNode node) {
        // if an item different from the root has been selected
        // then is possible to zoom in
        if (node != treemap.getTreeMapRoot()) {
            zoomInButton.setEnabled(true);
            selectedItem = node;
        } else {
            zoomInButton.setEnabled(false);
        }
    }

    @Override
    public void notifyZoomIn(TreeMapNode node) {
        changeState();
    }
   
    

    @Override
    public void notifyZoomOut() {
        changeState();
    }

    @Override
    public void notifyZoomFull() {
        zoomFullButton.setEnabled(false);
        zoomOutButton.setEnabled(false);
        zoomInButton.setEnabled(false);
    }
    
    
    private boolean isRootShown() {
        return treemap.getTreeMapRoot() == treemap.getZoomCallsStack().firstElement();
    }

}
