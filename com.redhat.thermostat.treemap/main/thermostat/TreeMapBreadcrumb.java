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
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * This object creates a breadcrumb navigation bar used to trace 
 * {@link TreeMapComponent} objects' state.
 */
public class TreeMapBreadcrumb extends JComponent implements TreeMapObserver {

    private static final long serialVersionUID = 1L;
    
    /**
     * Font used by bradcrumb items.
     */
    private Font FONT = new Font(Font.SERIF,  Font.PLAIN, 10);

    /**
     * Stack containing all items part of the breadcrumb.
     */
    private Stack<BreadcrumbItem> items;

    /**
     * The TreeMap object to interact with.
     */
    private TreeMapComponent treemap;

    /**
     * Constructor. Creates a breadcumbs navigation bar with the starting 
     * element and register itself as observer to the given treemap.
     * 
     * @param start the treemap's root.
     */
    public TreeMapBreadcrumb(TreeMapComponent treemap, TreeMapNode start) {
        super();  
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.items = new Stack<>();
        this.treemap = Objects.requireNonNull(treemap);
        this.treemap.register(this);
        buildBreadcrumb(start);
    }


    /**
     * Builds the breadcrumb using the nodes'ancestors.
     * @param node the tree's branch to represent ad breadcrumb bar.
     */
    public void buildBreadcrumb(TreeMapNode node) {
        LinkedList<TreeMapNode> nodes = node.getAncestors();

        while (!nodes.isEmpty()) {
            BreadcrumbItem item = new BreadcrumbItem(nodes.removeLast());
            items.push(item);
            add(item);
        }
        // the first element has no tail
        items.get(0).setAsFirst();

        //the last element has no head
        items.peek().setAsLast();
    }


    @Override
    public void notifySelection(TreeMapNode node) {
        // do nothing
    }

    @Override
    public void notifyZoomIn(TreeMapNode node) {
        items.clear();
        removeAll();
        buildBreadcrumb(node);
    }

    @Override
    public void notifyZoomOut() {
        this.remove(items.pop());
        items.peek().setAsLast();
        items.peek().repaint();
    }

    @Override
    public void notifyZoomFull() {
        items.clear();
        this.removeAll();
        BreadcrumbItem item = new BreadcrumbItem(treemap.getTreeMapRoot());
        item.setAsFirst();
        item.setAsLast();
        items.push(item);
        this.add(item);
    }



    /**
     *  This class allows to create a single item in a breadcrumb object.
     *  This component has 3 {@link JLabel}s which contain the images needed
     *  to draw an arrow.
     *        _____
     *    >  |_____|  >
     *    |     |     |
     *  tail   body  head
     *  
     */
    class BreadcrumbItem extends JComponent {
        
        private static final long serialVersionUID = 1L;

        private final String ROOT_TEXT = "root";

        private JLabel tail;
        private JLabel body;
        private JLabel head;

        /**
         * The node this items represents.
         */
        private TreeMapNode node;

        /**
         * The constructor creates a complete item, including both tail and head
         * @param node
         */
        public BreadcrumbItem(final TreeMapNode node) {
            super();
            this.node = node;
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            initComponent();

            /**
             * Simulate the click effect increasing and reducing the font size
             */
            this.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent arg0) {
                    increaseFont(body, 2);
                }

                @Override
                public void mousePressed(MouseEvent arg0) {
                    increaseFont(body, -2);
                }

                @Override
                public void mouseClicked(MouseEvent arg0) {
                    treemap.zoomIn(node);
                }
            });
        }


        /**
         * Increases the given component's font size.
         * @param comp the component which edit the font size to.
         * @param increment value of the increment. Negative values reduce the 
         * font size.
         */
        private void increaseFont(JComponent comp, int increment) {
            Font f = comp.getFont();
            int newSize = f.getSize() + increment;
            f = new Font(f.getName(), f.getStyle(), newSize);
            comp.setFont(f);
            comp.repaint();
        }

        private void initComponent() {
            initTail();
            initBody();
            initHead();
        }


        private void initTail() {
            tail = new JLabel();
            tail.setIcon(getTailImage());
            this.add(tail);
        }

        private void initHead() {
            head = new JLabel();
            head.setIcon(getHeadImage());
            this.add(head);
        }

        private void initBody() {
            body = new JLabel();
            body.setFont(FONT);
            body.setHorizontalTextPosition(JLabel.CENTER);
            body.setText(node.getLabel());
            adaptIcon(body, getBodyImage());
            this.add(body);
        }


        public TreeMapNode getNode() {
            return this.node;
        }

        /**
         * Remove the tail of his breadcrumb item.
         */
        public void setAsFirst() {
            this.remove(tail);
            this.tail = null;
            this.body.setText(ROOT_TEXT);
            adaptIcon(body, getBodyImage());
        }

        /**
         * Remove the head of his breadcrumb item.
         */
        public void setAsLast() {
            this.remove(head);
            this.head = null;
        }

        /**
         * Sets the text of this item, which is placed in the body.
         * @param text
         */
        public void setText(String text) {
            body.setText(text);
        }

        public int getHeight() {
            return body.getPreferredSize().height;
        }
    }
    
    /**
     * Calculates the labels' text size in order to scale the given image
     * and to apply to it.
     */
    private void adaptIcon(JLabel label, ImageIcon icon) {
        Rectangle fontArea;
        try {
            fontArea = label.getFont().getStringBounds(label.getText(), 
                    new FontRenderContext(label.getFont().getTransform(),
                            false, false)).getBounds();

        } catch (NullPointerException npe) {
            fontArea = label.getBounds();
        }
        
        Image img = icon.getImage();
        Image newimg = img.getScaledInstance(fontArea.getBounds().width + 10, 
                img.getHeight(null),  java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(newimg);
        label.setIcon(icon);
    }

    // FIXME
    private ImageIcon getHeadImage() {
        return new ImageIcon("resources/head.png");
    }

    private ImageIcon getBodyImage() {
        return new ImageIcon("resources/body.png");
    }

    private ImageIcon getTailImage() {
        return new ImageIcon("resources/tail.png");
    }
}
