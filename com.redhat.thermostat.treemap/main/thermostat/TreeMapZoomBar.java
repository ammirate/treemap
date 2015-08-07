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
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * This class provides a component containing zoom in/out/full buttons which can
 * control a {@link TreeMapComponent} object.
 * 
 * 
 * Thermostat original icons are taken from http://fortawesome.github.io/Font-Awesome/cheatsheet/
 * and are:
 *   - zoom in:   uf065
 *   - zoom out:  uf066
 *   - zoom full: uf03b
 */
public class TreeMapZoomBar extends JComponent implements TreeMapObserver {

    private static final long serialVersionUID = 1L;


    private JLabel zoomOut;
    private JLabel zoomFull;
    private JLabel zoomIn;
    
    private Color defaultColor = Color.BLACK;
    private Color enterColor = Color.BLUE;

    /**
     * The treemap object to interact with.
     */
    private TreeMapComponent treemap;

    /**
     * If an item is selected in the treemap, it is stored in order to zoom on 
     * it if the ZoomIn button is pressed.
     */
    private TreeMapNode selectedItem;
    
    /**
     * Constructor. It creates the zoom buttons and registers this object as 
     * treemap observer.
     */
    public TreeMapZoomBar(TreeMapComponent treemap) {
        super();
        this.treemap = Objects.requireNonNull(treemap);
        initComponent();
        treemap.register(this);
    }


    private void initComponent() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        createZoomInButton();
        createZoomFullButton();
        createZoomOutButton();

        /*
         * At the beginning no actions can be performed:
         * Cannot zoom in because no item is selected;
         * cannot zoom out because zoom in hasn't been performed;
         * the same for zoom full.
         */
        zoomIn.setEnabled(false);
        zoomFull.setEnabled(false);
        zoomOut.setEnabled(false);
    }



    private void createZoomInButton() {
        zoomIn = new JLabel();
        final Icon baseIcon = new ImageIcon("resources/zoomin.png");
        final Icon hoverIcon = new ImageIcon("resources/zoomin_.png");
        
        zoomIn.setIcon(baseIcon);
        zoomIn.setToolTipText("Click to zoom-in the selected item. Try also double click.");
        
        zoomIn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                zoomIn.setIcon(baseIcon);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                zoomIn.setIcon(hoverIcon);
            }

            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (selectedItem != null) {
                    treemap.zoomIn(selectedItem);
                }
            }
        });

        this.add(zoomIn);
    }

    private void createZoomOutButton() {
        zoomOut = new JLabel();
        
        final Icon baseIcon = new ImageIcon("resources/zoomout.png");
        final Icon hoverIcon = new ImageIcon("resources/zoomout_.png");
        
        zoomOut.setIcon(baseIcon);
        zoomIn.setToolTipText("Click to zoom out. Try also with a right click.");
        zoomOut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                zoomOut.setIcon(baseIcon);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                zoomOut.setIcon(hoverIcon);
            }

            @Override
            public void mouseClicked(MouseEvent arg0) {
                treemap.zoomOut();
            }
        });

        this.add(zoomOut);
    }

    private void createZoomFullButton() {
        zoomFull = new JLabel();

        final Icon baseIcon = new ImageIcon("resources/zoomfull.png");
        final Icon hoverIcon = new ImageIcon("resources/zoomfull_.png");
        
        zoomFull.setIcon(baseIcon);
        zoomIn.setToolTipText("Click to reset the zoom level. Try also with a mouse wheel click.");
        zoomFull.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                zoomFull.setIcon(baseIcon);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                zoomFull.setIcon(hoverIcon);
            }

            @Override
            public void mouseClicked(MouseEvent arg0) {
                treemap.zoomFull();
            }
        });

        this.add(zoomFull);
    }

    /**
     * Changes the buttons state in according to the treemap view.
     */
    private void changeState() {
        selectedItem = null;
        zoomIn.setEnabled(false);

        if (!isRootShown()) {
            zoomFull.setEnabled(true);
            zoomOut.setEnabled(true);
        } else {
            zoomFull.setEnabled(false);
            zoomOut.setEnabled(false);
        }
    }

    @Override
    public void notifySelection(TreeMapNode node) {
        // if an item different from the root has been selected
        // then it is possible to zoom in.
        if (node != treemap.getTreeMapRoot()) {
            zoomIn.setEnabled(true);
            selectedItem = node;
        } else {
            zoomIn.setEnabled(false);
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
        // no actions can be performed
        zoomFull.setEnabled(false);
        zoomOut.setEnabled(false);
        zoomIn.setEnabled(false);
    }


    private boolean isRootShown() {
        return treemap.getTreeMapRoot() == treemap.getZoomCallsStack().firstElement();
    }

}
