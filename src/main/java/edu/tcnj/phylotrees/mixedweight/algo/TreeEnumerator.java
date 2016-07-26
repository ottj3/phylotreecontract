package edu.tcnj.phylotrees.mixedweight.algo;

import edu.tcnj.phylotrees.mixedweight.data.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TreeEnumerator {

    double[][] weights = {
            {0, 1, 1, 1},
            {1, 0, 1, 1},
            {1, 1, 0, 1},
            {1, 1, 1, 0}
    };

    //The current best parsimony score (used for branch+bound and maintaining list of most parsimonious trees)
    protected double parsimonyScore = Double.POSITIVE_INFINITY;

    //The list of labelled nodes received from the input
    protected List<Node> labelledNodes = new ArrayList<>();

    //The set of all tree topologies
    protected Set<Node> trees = new HashSet<>();

    //The count of all trees enumerated (only maintained in the basic enumerate methods, not in fitch or hartigan)
    protected int treeCounter = 0;

    //The root of the current tree
    protected Node root = new Node("");

    //Add internal between current and parent, and then make leaf a child of internal
    protected void addNodeToEdge(Node current, Node parent, Node internal, Node leaf) {
        Node.unlinkNodes(parent, current);

        Node.linkNodes(parent, internal);
        Node.linkNodes(internal, current);
        Node.linkNodes(internal, leaf);
    }

    //Remove internal and leaf, leaving just parent and current
    protected void removeNodeFromEdge(Node current, Node parent, Node internal, Node leaf) {
        Node.unlinkNodes(internal, leaf);
        Node.unlinkNodes(internal, current);
        Node.unlinkNodes(parent, internal);

        Node.linkNodes(parent, current);
    }

    //Keep only the most parsimonious trees found so far
    protected void updateMPlist(double thisParsimonyScore) {
        if (thisParsimonyScore <= parsimonyScore) {
            if (thisParsimonyScore < parsimonyScore) {
                //Clear the list if a new best parsimony score is found
                parsimonyScore = thisParsimonyScore;
                trees.clear();
            }
            //Add the tree if it matches the current best parsimony score
            trees.add(root.clone());
        } else if (parsimonyScore == -1) {
            //Initialize the parsimony score if it is -1
            parsimonyScore = thisParsimonyScore;
            trees.add(root.clone());
        }
    }
}
