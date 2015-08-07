package com.redhat.thermostat.treemap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Random;

import javax.swing.JFrame;

import com.redhat.thermostat.treemap.impl.TreeMapComponent;
import com.redhat.thermostat.treemap.impl.TreeMapToolbar;
import com.redhat.thermostat.treemap.model.TreeMapNode;

/**
 * This class provides a demo for Thermostat TreeMap instantiation and usage.
 */
public class Demo {

    /**
     * Launch a TreeMap
     */
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setSize(800, 400);

        TreeMapNode root = new TreeMapNode(0);
        generateTree(root, 6, 6, false);

        TreeMapComponent treemap = new TreeMapComponent(root, new Dimension(100, 100));

        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(treemap, BorderLayout.CENTER);
        
        TreeMapToolbar tb = new TreeMapToolbar(treemap);
        f.getContentPane().add(tb, BorderLayout.NORTH);
        f.setVisible(true);
    }

    
    static int generatorCounter;
    static int id = 0;
    public static void generateTree(TreeMapNode root, int levels, int childrenNumber, boolean random) {        
        TreeMapNode node;
        if (levels == 0) {
        } else {
            Random rand = new Random();
            int children = random ? rand.nextInt(childrenNumber) + 1 : childrenNumber;
            for (int i = 0; i < children; i++) {
                int val = rand.nextInt(50);
                id++;
                node = new TreeMapNode("Node #" + id, val);
                root.addChild(node);
                generatorCounter++;
            }
            for (TreeMapNode child : root.getChildren()) {
                generateTree(child, levels-1, childrenNumber, random);
            }
        }
    }

}
