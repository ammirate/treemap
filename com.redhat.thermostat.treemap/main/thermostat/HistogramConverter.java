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

import java.util.ArrayList;
import java.util.List;

import com.redhat.thermostat.treemap.model.HistogramRecord;
import com.redhat.thermostat.treemap.model.ObjectHistogram;

/**
 * This class provides statics function to create a {@link TreeMapNode} tree
 * from an ObjectHistrogram.
 */
public class HistogramConverter {
    
    /**
     * The splitter regular expression to split records' className
     */
    private static final String SPLIT_REG_EXP = "\\."; //escaped dot
    
    /**
     * Key used to put into nodes the <i>number of instances</i> information 
     * stored in histogram records.
     */
    private static final String NUMBER_OF = "Number Of Instances";  
    
    /**
     * Call this method to create the full TreeMapNode object corresponding to
     * the {@link ObjectHistogram} histogram given in input.
     * @param histrogram the histogram to represent as TreeMapNode
     * @return the resulting tree
     */
    public static TreeMapNode convertToTreeMap(ObjectHistogram histrogram) {
       TreeMapNode root = new TreeMapNode("", 0);
        
        List<HistogramRecord> records = new ArrayList<>();
        records.addAll(histrogram.getHistogram());

        // build the tree from the histogram object
        processRecords(records, root);
        // calculates weights for inner nodes
        fillWeights(root);
        // collapse nodes with only one child 
        packTree(root);
        return root;
    }
    
    /**
     * This method is responsible for building correctly the histogram 
     * corresponding tree. For each histogram record, a tree branch is created
     * but only leaves node have a weight value.
     * Furthermore, additional information are added to the nodes' map.
     * 
     * @param records {@list} of HistogramRecord used to build the tree.
     * @param root the tree's root.
     */
    private static void processRecords(List<HistogramRecord> records, TreeMapNode root) {
        
        for (int i = 0; i < records.size(); i++) {
            
            TreeMapNode lastProcessed = root;
            String className = records.get(i).getClassname();
            
            // if className is a primitive type it is converted with its full name
            className = DescriptorConverter.toJavaType(className);

            while (!className.equals("")) {
               
                String nodeId = className.split(SPLIT_REG_EXP)[0];
                
                TreeMapNode child = lastProcessed.searchNodeByLabel(nodeId);
                
                if (child == null) {
                    child = new TreeMapNode(nodeId, 0);
                    lastProcessed.addChild(child);
                }
                
                lastProcessed = child;

                className = className.substring(nodeId.length());
                if (className.startsWith(".")) {
                    className = className.substring(1);
                }
                // removes semicolon from leaves
                if (className.endsWith(";")) {
                    className.replace(";", "");
                }
            }
            
            // at this point lastProcessed references to a leaf
            lastProcessed.setRealWeight(records.get(i).getTotalSize());
            lastProcessed.addInfo(NUMBER_OF, 
                    Long.toString(records.get(i).getNumberOf()));
        }
    }

    /**
     * This method calcs the real weights using a bottom-up traversal. From leaves, 
     * weights are passed to parent nodem which will have as weight the sum of
     * the children's weights.
     * 
     * @param node the subtree's root from which start to calc weights.
     * @return the node's real weight.
     */
    private static double fillWeights(TreeMapNode node) {
        if (node.getChildren().size() == 0) {
            return node.getRealWeight();
        }

        double sum = 0;
        for (TreeMapNode child : node.getChildren()) {
            sum += fillWeights(child);
        }
        node.setRealWeight(sum);
        return node.getRealWeight();
    }

    /**
     * This method allows to collapse nodes which have only one child.
     * E.g. nodes labeled <i>com</i> and <i>example</i> are collapsed in the 
     * parent node, which will have as label <i>com.example</i>.
     * @param node the subree's root from which start packing.
     */
    private static void packTree(TreeMapNode node) {
        if (node.getChildren().size() == 1) {
            TreeMapNode child = node.getChildren().get(0);
            node.setLabel(node.getLabel() + "." + child.getLabel());
            node.setChildren(child.getChildren());
            packTree(node);
        } else {
            for (TreeMapNode child : node.getChildren()) {
                packTree(child);
            }
        }
    }
}
