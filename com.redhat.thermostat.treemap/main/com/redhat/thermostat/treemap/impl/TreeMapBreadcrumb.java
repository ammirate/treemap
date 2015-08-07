package com.redhat.thermostat.treemap.impl;

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

import com.redhat.thermostat.treemap.model.TreeMapNode;

public class TreeMapBreadcrumb extends JComponent implements TreeMapObserver {

    private static final long serialVersionUID = 1L;

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
 

    
    public void buildBreadcrumb(TreeMapNode node) {
        LinkedList<TreeMapNode> nodes = node.getAncestors();

        while (!nodes.isEmpty()) {
            BreadcrumbItem item = new BreadcrumbItem(nodes.removeLast());
            items.push(item);
            add(item);
        }
        items.get(0).setAsFirst();
        items.peek().setAsLast();
    }


    @Override
    public void notifySelection(TreeMapNode node) {

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
        this.repaint();
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
    

    public int getBreadcrumbWidth() {
        int sum = 0;
        for (BreadcrumbItem i : items) {
            sum += i.getWidth();
        }
        return sum;
    }
 


    /**=========================================================================================
     * 
     */
    class BreadcrumbItem extends JComponent {
        private final String ROOT_TEXT = "ROOT";

        private static final long serialVersionUID = 1L;
        private JLabel tail;
        private JLabel body;
        private JLabel head;

        /**
         * The node this items represents.
         */
        private TreeMapNode node;

        float clickFactor = 1.5f;    

        /**
         * Constructor.
         * @param node
         * @param type
         */
        public BreadcrumbItem(final TreeMapNode node) {
            super();
            this.node = node;
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            renderComponent();

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



        private void increaseFont(JComponent comp, int increment) {
            Font f = comp.getFont();
            int newSize = f.getSize() + increment;
            f = new Font(f.getName(), f.getStyle(), newSize);
            comp.setFont(f);
            comp.repaint();
        }


        private void renderComponent() {
            initTail();
            initBody();
            initHead();
        }


        private void initTail() {
            tail = new JLabel();
            tail.setIcon(new ImageIcon("/home/acesaran/Desktop/breadcrumb/best/tail.png"));
            this.add(tail);
        }

        private void initHead() {
            head = new JLabel();
            head.setIcon(new ImageIcon("/home/acesaran/Desktop/breadcrumb/best/head.png"));
            this.add(head);
        }

        private void initBody() {
            body = new JLabel();
            body.setHorizontalTextPosition(JLabel.CENTER);
            body.setText(node.getLabel());

            adaptImage(body);
            this.add(body);
        }



        private void adaptImage(JLabel comp) {
            Rectangle fontArea = comp.getFont().getStringBounds(comp.getText(), 
                    new FontRenderContext(comp.getFont().getTransform(),
                            false, false)).getBounds();

            ImageIcon bodyIcon = new ImageIcon("/home/acesaran/Desktop/breadcrumb/best/body.png");
            Image img = bodyIcon.getImage();
            Image newimg = img.getScaledInstance(fontArea.getBounds().width + 10, img.getHeight(null),  java.awt.Image.SCALE_SMOOTH);
            bodyIcon = new ImageIcon(newimg);
            comp.setIcon(bodyIcon);
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
            adaptImage(body);
        }

        public void setAsLast() {
            this.remove(head);
            this.head = null;
        }

        public void setText(String text) {
            body.setText(text);
        }

        public int getWidth() {
            int sum = body.getPreferredSize().width;
            sum += head != null ? head.getPreferredSize().width : 0;
            sum += tail != null ? tail.getPreferredSize().width : 0;
            return sum;
        }

        public int getHeight() {
            return body.getPreferredSize().height;
        }
    }


}
