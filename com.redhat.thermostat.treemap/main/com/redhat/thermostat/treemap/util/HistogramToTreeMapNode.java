package com.redhat.thermostat.treemap.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.redhat.thermostat.treemap.model.HistogramRecord;
import com.redhat.thermostat.treemap.model.ObjectHistogram;
import com.redhat.thermostat.treemap.model.TreeMapNode;

/**
 * This class provides statics function to create a {@link TreeMapNode} tree
 * from an ObjectHistrogram.
 */
public class HistogramToTreeMapNode {
    /**
     * The tree's root
     */
    private static TreeMapNode root;
    
    /**
     * The histogram's records to process
     */
    private static List<HistogramRecord> records;
    
    /**
     * The splitter regular expression to split records' className
     */
    private static final String SPLIT_REG_EXP = "\\."; //escaped dot
    
    /**
     * Key used to put into nodes the <i>number of instances</i> information 
     * stored in histogram records.
     */
    private static final String NUMBER_OF = "Number Of Instances"; //escaped dot
    
    /**
     * Call this method to create the full TreeMapNode object corresponding to
     * the {@link ObjectHistogram} histogram given in input.
     * @param histrogram the histogram to represent as TreeMapNode
     * @return the resulting tree
     */
    public static TreeMapNode processHistogram(ObjectHistogram histrogram) {
        return processHistogram(histrogram.getHistogram());
    }

    public static TreeMapNode processHistogram(Collection<HistogramRecord> list) {
        root = new TreeMapNode("Heap root", 0);
        records = new ArrayList<>();
        records.addAll(list);

        // build the tree from the histogram object
        processRecords();
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
     */
    private static void processRecords() {
        
        for (int i = 0; i < records.size(); i++) {
            
            TreeMapNode lastProcessed = root;
            String className = records.get(i).getClassname();

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
