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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class provide a tree recursive implementation used in
 * {@link SquarifiedTreeMap}. It contains a reference to the parent node and to
 * a node list, which represent the node's children. It is also 
 * possible to store generic information inside the node using a {@link Map} 
 * object. Furthermore, the main property of this class is the chance to have a
 * weight for the node and associate to it a {@link Rectangle2D.Double} object.
 * 
 * <p>When an instance of this class is created, it will automatically be 
 * assigned a unique id.
 * 
 * <p>By default, this class' comparator is based on the nodes' weight.
 * 
 * <p>A static Quick Sort algorithm implementation is also provided by this 
 * class.
 * 
 * @see Rectangle2D.Double
 */
public class TreeMapNode {
        
    /**
     * Counter for assign unique id to nodes.
     */
    private static int idCounter = 0;

    /**
     * The rectangle which will graphically represent this node.
     */
    private Rectangle2D.Double rectangle;

    /**
     * This node's id.
     */
    private int id;
    
    /**
     * A Map in which store information for this node.
     */
    private Map<String, String> info;
    
    /**
     * Reference to the parent.
     */
    private TreeMapNode parent;
    
    /**
     * Reference to children.
     */
    private List<TreeMapNode> children;
    
    /**
     * The node's weight.
     */
    private double weight;
    
    /**
     * The node's label. It can be the same of another node.
     */
    private String label;
    
    /**
     * The node's weight which has been set inside the constructor. Note that
     * this value can be assigned just one time, using the constructor. All 
     * operations which refers to node's weight work on the weight field, that 
     * is used to make calcs.
     */
    private double realWeight;
    
    /**
     * This flag indicates if weight value can be a non positive number.
     */
    static boolean allowNonPositiveWeight = false;
    
    /**
     * The color of this node.
     */
    private Color color;
    
    /**
     * Colors available on which iterate
     */
    static final Color[] colors = {
            Color.decode("#FACED2"), // red
            Color.decode("#B9D6FF"), // blue
            Color.decode("#E5E5E5"), // grey
            Color.decode("#FFE7C7"), // orange
            Color.decode("#ABEBEE"), // aqua
            Color.decode("#E4D1FC"), // purple
            Color.decode("#FFFFFF"), // white
            Color.decode("#CDF9D4")  // green
    };
    
    public final Color START_COLOR = colors[0];
    
    /**
     * 
     * Constructor that allow to set the nodes' real weight. Others fields are 
     * initialized to their default value.
     * It automatically set the node's id.
     *
     * <p>
     * @param realWeight the nodes real weight, which will be not affected 
     * during node processing.
     * 
     */
    public TreeMapNode(double realWeight) {
        this("", realWeight);
    }

    /**
     * 
     * Constructor that allow to set the nodes' real weight and the label. 
     * Others fields are initialized to their default value.
     * It automatically set the node's id.
     *
     * <p>
     * @param label the node's label.
     * @param realWeight the nodes real weight, which will be not affected 
     * during node processing.
     * 
     */
    public TreeMapNode(String label, double realWeight) {
        this.id = idCounter++;
        this.label = label;
        this.parent = null;
        this.children = new ArrayList<TreeMapNode>();
        this.rectangle = new Rectangle2D.Double();
        this.info = new HashMap<String, String>();
        this.weight = realWeight;
        this.realWeight = realWeight;
    }

    /**
     * Return the id of this object.
     * @return the id automatically assigned at this object initialization.
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Set this node's label.
     * @param newLabel the new label to set.
     */
    public void setLabel(String newLabel) {
        this.label = newLabel;
    }
    
    /**
     * Return the label of this object.
     * @return the label assigned at instantiation time to this object.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Return the reference to the node parent of this object.
     * @return the parent of this node. It can be null.
     */
    public TreeMapNode getParent() {
        return this.parent;
    }

    /**
     * Set as parent of this object the node given in input.
     * @param parent the new parent of this object. No checks are made for null
     * value.
     */
    public void setParent(TreeMapNode parent) {
        this.parent = parent;
    }

    /**
     * Return the list of nodes representing this node's children.
     * @return a list of {@link TreeMapNode} objects.
     */
    public List<TreeMapNode> getChildren() {
        return this.children;
    }
    
    /**
     * Set as children list of this object the list given in input.
     * @param children the new list of children for this node.
     */
    public void setChildren(List<TreeMapNode> children) {
        this.children = children;
        for (TreeMapNode child : this.children) {
            child.setParent(this);
        }
    }

    /**
     * Return the {@link Map} object containing all information of this node.
     * @return a {@link Map} object.
     */
    public Map<String, String> getInfo() {
        return this.info;
    }
    
    /**
     * Store the given information into this object.
     * @param key the key searching value for the information to store.
     * @param value the information to store into this object.
     * @return the old value for the given key.
     */
    public String addInfo(String key, String value) {
        return this.info.put(key, value);
    }

    /**
     * Return the information stored in this object, corresponding to the key 
     * given in input.
     * @param key the key value for the search information.
     * @return the corresponding value for the given key.
     */
    public String getInfo(String key) {
        return this.info.get(key);
    }

    /**
     * Add the object given in input to the children list of this object. It 
     * also add this object as its parent.
     * @param child the new child to add at this object.
     */
    public void addChild(TreeMapNode child) {
        if (child != null) {
            this.children.add(child);
            child.setParent(this);
        }
    }

    @Override
    /**
     * Return a {@link String}  representing this object.
     */
    public String toString() {
        return  getClass().getSimpleName() + " [" + "label = " + getLabel() + 
                "; weight =" + getRealWeight() + 
                "; rectangle=" + rectangle.getBounds() + "]";
    }

    
    /**
     * Search into the tree the node with id = key.
     * @param key the id of the node to search.
     * @return the node of exists, else null.
     */
    public TreeMapNode searchNodeByLabel(String key) {
        if (this.getLabel().equals(key)) {
            return this;
        }
        
        TreeMapNode result = null;
        for (TreeMapNode child : getChildren()) {
            result = child.searchNodeByLabel(key) ;
            if (result != null) {
                return result;
            }
        }
        return result;
    }

    /**
     * Return the weight of this object. In case of allowNonPositiveWeight is 
     * set to false and the weight is 0, less than 0 or not a number 
     * ({@link Double.Nan}), this method returns a value that can be transformed
     * by external objects, so if you need the real weight you have to
     * invoke getrealWeight().
     * 
     * @return the node's weight.
     */
    public double getWeight() {
        if ((weight <= 0 || weight == Double.NaN) && !allowNonPositiveWeight) {
            return realWeight;
        }
        return this.weight;
    }
    
    /**
     * Use this method to retrieve the real weight assigned to this node.
     * @return the weight corresponding to this node.
     */
    public double getRealWeight() {
        return this.realWeight;
    }
    
    
    /**
     * Use this method to set the real weight of this node.
     */
    public void setRealWeight(double w) {
        this.realWeight = w;
    }

    /**
     * Set the weight of this object. If a negative value is given, it is set 
     * automatically to 0.
     * @param weight the new weight for this object.
     */
    public void setWeight(int w) {
        this.weight = w < 0 && !allowNonPositiveWeight ? 0 : w;
    }


    /**
     * Return the rectangle representing this object.
     * @return a {@link Rectangle2D.Double} object.
     */
    public Rectangle2D.Double getRectangle() {
        if (this.rectangle == null) {
            throw new RuntimeException();
        }
        return this.rectangle;
    }

    /**
     * Set a new rectangle for this object.
     * @param rectangle the new rectangle that represent this node.
     */
    public void setRectangle(Rectangle2D.Double rectangle) {
        this.rectangle = rectangle;
    }    

    /**
     * 
     * @return true if non positive value can be used as weight, else false.
     */
    public static boolean isAllowNonPositiveWeight() {
        return allowNonPositiveWeight;
    }

    /**
     * Set this value to false and nodes will be not able to manage non positive
     * values for weight field, otherwise set to true.
     * @param allowed the flag value for managing non positive values as weight
     */
    public static void setAllowNonPositiveWeight(boolean allowed) {
        allowNonPositiveWeight = allowed;
    }

    /**
     * This method assess if the rectangle associated to this node is drawable,
     * which means that its sides are greater than 1.
     * @return true if the rectangle associated to this node  is drawable, 
     * else false.
     */
    public boolean isDrawable() {
        if (rectangle.width >= 1 && rectangle.height >= 1) {
            return true;
        }
        return false;
    }
    
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Returns this node's next color.
     * @return the color which came after this node's color in the color list.
     * If this node has no color assigned then the START_COLOR is returned.
     */
    public Color getNextColor() {
        if (this.color != null) {
            for (int i = 0; i < colors.length; i++) {
                if (this.color.equals(colors[i])) {
                    return colors[(i + 1) % colors.length];
                }
            }
        }
        return START_COLOR;
    }


    public int getDepth() {
        if (this.parent == null) {
            return 0;
        } else {
            return 1 + parent.getDepth();
        }
    }

    /**
     * This method sorts the given list in <b>descending<b> way.
     * 
     * @param nodes the list of {@link TreeMapNode} to sort.
     */
    public static void sort(List<TreeMapNode> nodes) {
        Comparator<TreeMapNode> c = new Comparator<TreeMapNode>() {
          @Override
          public int compare(TreeMapNode o1, TreeMapNode o2) {
              // inverting the result to descending sort the list
              return -(Double.compare(o1.getWeight(), o2.getWeight()));
          }
      };
      Collections.sort(nodes, c);
    }
    
    /**
     * Return the list of ancestors node of this object. The first one is this 
     * node itself, the last one the root.
     * @return a list of ancestors nodes.
     */
    public LinkedList<TreeMapNode> getAncestors() {
        LinkedList<TreeMapNode> toReturn = new LinkedList<TreeMapNode>();
        TreeMapNode tmp = this;
        do {
            toReturn.add(tmp);
        } while ((tmp = tmp.getParent()) != null);
        return toReturn;
    }
}
