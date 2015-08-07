package com.redhat.thermostat.treemap.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import com.redhat.thermostat.treemap.model.TreeMapNode;

/**
 * This class allows you to represent a hierarchical data structure as a TreeMap.
 * It extends {@link JComponent} so it can be used like usual Swing objects.
 * 
 * <p>It is also possible to register objects, implementing the Notifiable 
 * interface, in order to receive notifications about the main events of this 
 * component. 
 *
 */
public class TreeMapComponent extends JComponent{

    private static final long serialVersionUID = 1L;

    /**
     * TreeMap's graphic root.
     */
    Comp mainComp;

    /**
     * Label Object to clone for faster initialization.
     */
    private Label cachedLabel;

    /**
     * The tree to render as TreeMap.
     */
    TreeMapNode tree;

    /**
     * Horizontal and vertical padding for nested component.
     */
    private final int X_PADDING = TreeProcessor.X_PADDING;
    private final int Y_PADDING = TreeProcessor.Y_PADDING;

    /**
     * Min size for rectangles' sides. rectangles having one or both sides less
     * than MIN_SIDE pixels will be not drawn.
     */
    private final int MIN_SIDE = 2;

    /**
     * Default value for a TreeMap component.
     */
    private static final String TITLE = "";

    /**
     * TreeMap UI Constraint.
     */
    public static final int SIMPLE = 0;
    public static final int FLAT = 1;
    public static final int ETCHED_LOWERED = 2;
    public static final int ETCHED_RAISED = 3;

    /**
     * Stores the chosen UI mode.
     */
    private int borderStyle = ETCHED_LOWERED;

    /**
     * The components' border
     */
    private Border defaultBorder;

    /**
     * Font and size for this component's label.
     */
    private int FONT_SIZE = 10;
    private Font FONT = new Font(Font.SERIF, Font.PLAIN, FONT_SIZE);

    /**
     * Variable in which store last resize dimension.
     */
    private Dimension lastDim;

    /**
     * Variable in which store last resize event call time.
     */
    private static long lastCall = 0;

    /**
     * Wait time in millisec to resize the TreeMap.
     */
    private final int MIN_DRAGGING_TIME = 60;


    /**
     * List of objects observing this.
     */
    private List<TreeMapObserver> observers;

    /**
     * Stack containing the zoom calls on the TreeMap.
     */
    private Stack<TreeMapNode> zoomStack;

    /**
     * This object stores the last clicked rectangle in the TreeMap, in order to 
     * repaint it when another rectangle will be selected.
     */
    private static Comp lastClicked;

    /**
     * Constructor. It draw a TreeMap of the given tree in according to the 
     * {@Dimension} object in input.
     * 
     * @param tree the tree to represent as TreeMap.
     * @param d the dimension the TreeMap will fulfill.
     */
    public TreeMapComponent(TreeMapNode tree, Dimension d) {
        super();
        if (tree == null || d == null) {
            throw new TreeMapException(getClass() + " does not allow null parameters");
        }
        this.tree = tree;
        lastDim = getSize();
        this.observers = new ArrayList<>();
        this.zoomStack = new Stack<>();
        this.zoomStack.push(this.tree);

        // assign a rectangle to the tree's root in order to process the tree.
        Rectangle2D.Double area = new Rectangle2D.Double(0, 0, d.width, d.height);

        // calculate rectangles of tree's subtrees
        TreeProcessor processor = new TreeProcessor(tree, area);
        processor.processTreeMap();

        drawTreeMap(tree); 

        addResizeListener(this);        
        repaint();
    }

    /**
     * This method returns the root of the tree showed ad TreeMap.
     * @return the TreeMap's root node.
     */
    public TreeMapNode getTreeMapRoot() {
        return this.tree;
    }

    /**
     * This method is responsible for the TreeMap drawing process.
     * @param tree the tree to represent as TreeMap.
     */
    private void drawTreeMap(TreeMapNode tree) {
        // draw root
        drawMainComp(tree);
        setBorderStyle(borderStyle);
        
        // draw subtrees nested in children 
        for (TreeMapNode child : tree.getChildren()) {
            drawSubTree(child, mainComp);
        }
        // setup this component
        prepareGUI();
    }

    /**
     * This method prepares the layout for this component. 
     */
    private void prepareGUI() {
        setLayout(new BorderLayout());
        setBounds(mainComp.getBounds());
        setBorder(null);
        add(mainComp, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * This method prepares the main component which is the parent object where 
     * sub components will be placed. 
     * @param tree the tree's root used to prepare the main component.
     */
    private void drawMainComp(TreeMapNode tree) {
        cachedLabel = new Label(TITLE + tree.getLabel());
        cachedLabel.setLocation(5, 5);

        mainComp = new Comp();
        mainComp.setLayout(null);
        mainComp.setBounds(tree.getRectangle().getBounds());        
        mainComp.add(cachedLabel);
        mainComp.setBorder(defaultBorder);
        mainComp.setNode(tree);
    }

    /**
     * Create a TreeMapComp from the given node. The component is not 
     * instantiated as a new component but is cloned from an existing one, in 
     * order to improve performance.
     * 
     * @param node the node to represent as a component.
     * @return the component representing the given node.
     */
    private Comp renderizeNode(TreeMapNode node) {
        // if the rectangle's node is too small to be viewed, don't draw it.
        if (node.getRectangle().getWidth() <= MIN_SIDE || 
                node.getRectangle().getHeight() <= MIN_SIDE) {
            return null;
        }

        Comp comp = (Comp) mainComp.clone();
        comp.setBounds(node.getRectangle().getBounds());

        return comp;
    }

    /**
     * This method checks if the given container has enough space to instantiate
     * a Label in it. If yes, a Label is cloned from an existing one, in order 
     * to improve performance. If not, it exits.
     * 
     * @param s the label text.
     * @param cont the parent container which will contain the new label.
     * @return the cloned label.
     */
    private Label addLabelIfPossible(String s, Container cont) {
        int componentW = cont.getSize().width;
        int componentH = cont.getSize().height;
        // get the rectangle associated to the area needed for the label's text
        Rectangle fontArea = FONT.getStringBounds(s, 
                new FontRenderContext(FONT.getTransform(),
                        false, false)).getBounds();

        // if the container is greater than the label, add it to the container
        if (componentW > fontArea.width && componentH > fontArea.height) {
            Label label = (Label) cachedLabel.clone();
            label.setBounds(5, 5, cont.getWidth(), 10);
            label.setText(s);
            cont.add(label);
            return label;
        }
        return null;
    }

    /**
     * Draw the whole {@param tree}'s subtree inside the given component.
     * @param tree the tree to draw
     * @param parent the component in which build the tree.
     */
    private void drawSubTree(TreeMapNode tree, JComponent parent) {
        Comp comp = addCompIfPossible(tree, parent);

        // if space was enough to draw a component, try to draw its children
        if (comp != null) {
            comp.setNode(tree);
            for (TreeMapNode child : tree.getChildren()) {
                drawSubTree(child, comp);
            }
        }
    }

    /**
     * Create and add to the {@link Container} given in input a 
     * {@link ComponentResized} listener.
     * @param c the container in to assign the listener.
     */
    private void addResizeListener(final Container container) {
        ComponentAdapter adapter = new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                // if enough time is passed from the last call, redraw the TreeMap
                if (canResize(MIN_DRAGGING_TIME)) {
                    Dimension newDim = container.getSize();

                    if (isChangedSize(newDim)) {
                        redrawTreeMap(tree); 
                    }
                } 
            }            
        };
        container.addComponentListener(adapter);
    }

    /**
     * This method checks if the given container has enough space to instantiate
     * a TreeMapComp object in it. If yes, a Label is cloned from an existing 
     * one, in order to improve performance. If not, it exits.
     * 
     * @param node the node to draw and add to the given container.
     * @param cont the parent container which will contain the new component.
     * @return true if the component was created and added, else false.
     */
    private Comp addCompIfPossible(TreeMapNode node, Container cont) {
        Rectangle2D rect = node.getRectangle();
        // if the ndoe's rectangle is smaller than the container, it is added
        if (cont.getWidth() > rect.getWidth() + X_PADDING && 
                cont.getHeight() > rect.getHeight() + Y_PADDING) {

            Comp toReturn = renderizeNode(node);
            if (toReturn == null) {
                return null;
            }
            addLabelIfPossible(TITLE + node.getLabel(), toReturn);

            // leaves some space from the parent's origin location
            Point loc = toReturn.getLocation();
            loc.x += X_PADDING;
            loc.y += Y_PADDING;
            toReturn.setLocation(loc);

            cont.add(toReturn);
            return toReturn;
        }
        return null;
    }


    /**
     * This method recalculates and redraws the TreeMap in according to the size
     * of this component and the actual {@link TreeMapNode} object.
     */
    private void redrawTreeMap(TreeMapNode newRoot) {
        tree = newRoot;
        Rectangle2D.Double newArea = tree.getRectangle();
        // give to the root node the size of this object so it can be recalculated
        newArea.width = getSize().width;
        newArea.height = getSize().height;

        // recalculate the tree
        TreeProcessor tp = new TreeProcessor(tree, newArea);
        tp.processTreeMap();

        removeAll();
        drawTreeMap(tree);        
    }



    public void zoomIn(TreeMapNode node) {
        if (node != null && node != this.tree) {
            fillZoomStack(node.getAncestors());
            redrawTreeMap(node);
            notifyZoomInToObservers(zoomStack.peek());
        } 
    }

    private void fillZoomStack(LinkedList<TreeMapNode> ancestors) {
        zoomStack.clear();
        while (!ancestors.isEmpty()) {
            zoomStack.push(ancestors.removeLast());
        }
    }

    
    public void zoomOut() {
        // if the actual root element is not the tree's original root
        if (zoomStack.size() > 1) {
            zoomStack.pop();
            redrawTreeMap(zoomStack.peek());
            notifyZoomOutToObservers(zoomStack.peek());
        }
    }

    /**
     * Zoom out the view directly to the original root.
     */
    public void zoomFull() {
        if (zoomStack.size() > 1) {
            clearZoomCallsStack();
            redrawTreeMap(zoomStack.peek());
            notifyZoomFullToObservers();
        }
    }

    /**
     * Returns the list of zoom operation calls.
     * @return the stack that holds the zoom calls.
     */
    public Stack<TreeMapNode> getZoomCallsStack() {
        return zoomStack;
    }

    /**
     * Clear the zoom calls of this object leaving the stack with just the root.
     */
    public void clearZoomCallsStack() {
        while (zoomStack.size() > 1) {
            zoomStack.pop();
        }
    }

    /**
     * check if last resize operation was called too closer to this
     * one. If so, ignore it: the container is being dragged. 
     * 
     * @return true if this method is invoked at distance of 
     * MIN_DRAGGING_TIME millisec, else false. 
     */
    private boolean canResize(int millisec) {
        long time = System.currentTimeMillis();
        if (time - lastCall >= millisec){
            lastCall = time;
            return true;
        }
        return false;
    }


    /**
     * Check if the dimension given in input differs from the last one stored
     * by 2. 
     * @param newDim the new dimension to check.
     * @return true if the dimensions are different, else false.
     */
    private boolean isChangedSize(Dimension newDim) {
        int minResizeDim = 2;
        int deltaX = Math.abs(newDim.width - lastDim.width);
        int deltaY = Math.abs(newDim.height - lastDim.height);

        if (deltaX > minResizeDim || deltaY > minResizeDim) {
            lastDim = newDim;
            return true;
        }
        return false;
    }

    /**
     * Switch the component's visualization mode to the one given in input. 
     * Use static constraints to set correctly a visualization mode.
     * @param constraint the UI visualization mode to set.
     */
    public void setBorderStyle(int UIMode) {
        this.borderStyle = UIMode;
        switch (borderStyle) {
            case 1 : {
                defaultBorder = new EmptyBorder(0, 0, 0, 0);
                break;
            }    
            case 2 : {                
                defaultBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.white, Color.darkGray);
                break;
            }
            case 3 : {
                defaultBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.white, Color.darkGray);
                break;
            }
            default : {
                defaultBorder = new LineBorder(Color.black, 1);
                break;
            }
        }
        applyBorderToSubtree(mainComp);
    }
    
    /**
     * Traverse recursively the tree from the given component applying to it 
     * the default border.
     * @param comp the subtree's root from which apply the border style.
     */
    private void applyBorderToSubtree(Comp comp) {
        comp.setBorder(defaultBorder);
        Component[] children = comp.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Comp) {
                applyBorderToSubtree((Comp) children[i]);
            }
        }
    }

    /**
     * Returns the list of observer objects registered on this TreeMap.
     * @return a list of Notifiable objects registered in this object.
     */
    public List<TreeMapObserver> getObservers() {
        return observers;
    }

    /**
     * Add the object in input to the list of registered objects to this TreeMap.
     * @param observer the Notifiable object to register to this object.
     */
    public void register(TreeMapObserver observer) {
        this.observers.add(observer);
    }
    
    /**
     * Remove the object in input from the list of registered objects to this TreeMap.
     * @param observer the Notifiable object to unregister from this object.
     */
    public void unregister(TreeMapObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Notify observers that an object in the TreeMap has been selected.
     * @param comp the selected component.
     */
    private void notifySelectionToObservers(TreeMapNode node) {
        for (TreeMapObserver observer : observers) {
            observer.notifySelection(node);
        }
    }

    /**
     * Notify observers that  TreeMap has been zoomed.
     * @param zoomedComponent 
     */
    private void notifyZoomInToObservers(TreeMapNode node) {
        for (TreeMapObserver observer : observers) {
            observer.notifyZoomIn(node);
        }
    }
    
    /**
     * Notify observers that  TreeMap has been zoomed.
     * @param zoomedComponent 
     */
    private void notifyZoomOutToObservers(TreeMapNode node) {
        for (TreeMapObserver observer : observers) {
            observer.notifyZoomOut();
        }
    }
    
    /**
     * Notify observers that  TreeMap has been zoomed.
     * @param zoomedComponent 
     */
    private void notifyZoomFullToObservers() {
        for (TreeMapObserver observer : observers) {
            observer.notifyZoomFull();
        }
    }
    
    
    
    

    /**
     * Return the last clicked component inside the TreeMap.
     * @return the last clicked {@Comp} object.
     */
    public Comp getClickedComponent() {
        return lastClicked;
    }

    /**
     * This class provides an extension of {@link JLabel} which main 
     * characteristic is to implement the {@link Cloneable} interface in order
     * to make his creation faster then JLabel class.
     */
    class Label extends JLabel implements Cloneable {
        private static final long serialVersionUID = 1L;

        public Label(String s) {
            super(s);
            setFont(FONT);
            setBounds(0, 0, getPreferredSize().width, FONT_SIZE);
        }

        @Override
        protected JLabel clone() {
            Label clone = new Label("");
            clone.setFont(getFont());
            clone.setText(getText());
            clone.setBackground(getBackground());
            clone.setBounds(getBounds());
            clone.setBorder(getBorder());
            return clone;
        }
    }    

    /**
     * This class provides an extension of {@link JLabel} which main 
     * characteristic is to implement {@link Cloneable} interface in order to
     * make his creation faster then JLabel class.
     */
    class Comp extends JComponent implements Cloneable {

        private static final long serialVersionUID = 1L;

        /**
         * The node represented by this component.
         */
        private TreeMapNode node;

        /**
         * The background color. It depends by the node's depth.
         */
        private Color color;

        /**
         * Reference to this.
         */
        private Comp thisComponent;

        public Comp() {
            super();
            thisComponent = this;
            addClickListener(this);
        }

        @Override
        public Comp clone() {
            Comp clone = new Comp();
            clone.setBounds(getBounds());
            clone.setBorder(getBorder());
            clone.setLayout(getLayout());
            clone.setOpaque(true);
            return clone;
        }

        public void setNode(TreeMapNode node) {
            this.node = node;
            this.color = node.getColor();
            this.setToolTipText("" + node.getLabel() + " - weight: " + node.getRealWeight());
        }

        public TreeMapNode getNode() {
            return this.node;
        }

        public Color getColor() {
            return this.color;
        }

        public void setColor(Color c) {
            this.color = c;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g); 
            if (this.color != null) {
                g.setColor(color);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }   

        /**
         * Add a mouse listener to this component. It allows to select it and
         * zoom it in and out. 
         * @param component the component which will have the mouse listener.
         */
        private void addClickListener(final JComponent component) {
            MouseListener click = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // one left click select the rectangle
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        selectComp();
                    }
                    // one right click zoom out
                    if (SwingUtilities.isRightMouseButton(e)) {
                        zoomOut();
                    }
                    // one middle click reset zoom
                    if (SwingUtilities.isMiddleMouseButton(e)) {
                        zoomFull();
                    }
                    // double left click zoom in
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                            zoomIn(getNode());
                    }
                }
            };
            component.addMouseListener(click);
        }
        

        /**
         * This method gives a darker color to this component and restore the
         * original color to the last selected component.
         */
        private void selectComp() {
            if (lastClicked != null) {
                lastClicked.setColor(lastClicked.getColor().brighter());
                lastClicked.repaint();
            } 
            lastClicked = thisComponent;
            setColor(getColor().darker());
            repaint();
            notifySelectionToObservers(node);
        }
    }
}





