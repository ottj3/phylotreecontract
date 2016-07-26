package edu.tcnj.phylotrees.mixedweight.algo;

import edu.tcnj.phylotrees.mixedweight.data.Node;

import java.util.List;
import java.util.Set;

public class MixedTreeEnumerator extends TreeEnumerator {

    public MixedTreeEnumerator(List<Node> labelledNodes) {
        this.labelledNodes = labelledNodes;
    }

    public MixedTreeEnumerator(List<Node> labelledNodes, double[][] weights) {
        this.labelledNodes = labelledNodes;
        this.weights = weights;
    }

    /**
     * Generates the base topology for a tree with n leaves.
     * This method initializes a tree topology to give us a starting point for
     * enumerating arbitrarily rooted mixed trees.
     */
    protected void initializeTree() {
        //Base cases for mixed trees:
        if (labelledNodes.size() >= 1) {
            //Only one node: make it the root
            root = labelledNodes.get(0).clone();
            if (labelledNodes.size() >= 2) {
                //At least two nodes: pair them, making the first the root
                Node.linkNodes(root, labelledNodes.get(1).clone());
            }
        }
    }

    /**
     * Enumerate all mixed trees for the given list of labelled nodes
     *
     * @return the number of possible mixed trees
     */
    public int enumerate() {
        treeCounter = 0;
        initializeTree();
        if (labelledNodes.size() <= 2) {
            //For sizes less than two, only one tree is possible
            treeCounter++;
        } else {
            //Recursively build all other possibilities
            enumerateRecursive(root, 2);
        }
        return treeCounter;
    }

    protected void enumerateRecursive(Node current, int size) {
        if (size == labelledNodes.size()) {
            //Once a tree has all labelled nodes, increase the number of trees made
            //and stop the recursion
            treeCounter++;
        } else {
            //Otherwise, go through any of the four possible ways to
            //add a node to the tree
            case1(current, size, false);
            case2(current, size, false);
            case3(current, size, false);
            case4(current, size, false);
        }
    }

    /**
     * Branch+bounded enumeration of all mixed trees, scored using hartigan
     *
     * @return a set of root nodes of all most parsimonious trees
     */
    public Set<Node> sankoffEnumerate() {
        parsimonyScore = -1;
        trees.clear();
        initializeTree();
        if (labelledNodes.size() <= 2) {
            trees.add(root.clone());
        } else {
            sankoffEnumerateRecursive(root, 2);
        }
        return trees;
    }

    protected void sankoffEnumerateRecursive(Node current, int size) {
        //Same as enumerateRecursive, but bounded using hartigan to score the trees in-progress
        if (size == labelledNodes.size()) {
            double score = Sankoff.bottomUp(root, weights);
//            double score = Sankoff.bottomUp(rootAtUnlabelled(), weights);
//            if (normalScore != score) {
//                System.out.println(normalScore + " " + score);
//            }
            updateMPlist(score);
        } else if (Sankoff.bottomUp(root, weights) <= parsimonyScore || parsimonyScore == -1) {
            case1(current, size, true);
            case2(current, size, true);
            case3(current, size, true);
            case4(current, size, true);
        }
    }

    //The four cases for adding a node to the tree. The isScored parameter determines
    //whether to return to the enumerateRecursive or sankoffEnumerateRecursive method

    /***************************************************************************
     * Case 1: Enumerates bifurcating trees.
     **************************************************************************/
    private void case1(Node current, int size, boolean isScored) {
        for (int index = 0; index < current.children.size(); index++) {
            case1(current.children.get(0), size, isScored);
        }
        if (current != root) {
            Node internal = new Node("");
            Node leaf = labelledNodes.get(size).clone();
            Node parent = current.parent;

            addNodeToEdge(current, parent, internal, leaf);

            if (isScored) {
                sankoffEnumerateRecursive(root, size + 1);
            } else {
                enumerateRecursive(root, size + 1);
            }

            removeNodeFromEdge(current, parent, internal, leaf);
        }
    }

    /***************************************************************************
     * Case 2: Enumerates multifurcating trees. Inserts a labelled node into any
     * edge.
     **************************************************************************/
    private void case2(Node current, int size, boolean isScored) {
        for (int index = 0; index < current.children.size(); index++) {
            case2(current.children.get(0), size, isScored);
        }
        if (current != root) {
            Node internal = labelledNodes.get(size).clone();
            Node parent = current.parent;

            Node.unlinkNodes(parent, current);
            Node.linkNodes(parent, internal);
            Node.linkNodes(internal, current);

            if (isScored) {
                sankoffEnumerateRecursive(root, size + 1);
            } else {
                enumerateRecursive(root, size + 1);
            }

            Node.unlinkNodes(internal, current);
            Node.unlinkNodes(parent, internal);
            Node.linkNodes(parent, current);
        }
    }

    /***************************************************************************
     * Case 3: Enumerates multifurcating trees. Inserts a child into any
     * labelled or unlabelled node (including the root).
     **************************************************************************/

    private void case3(Node current, int size, boolean isScored) {
        for (int index = 0; index < current.children.size(); index++) {
            case3(current.children.get(index), size, isScored);
        }
        Node leaf = labelledNodes.get(size).clone();

        Node.linkNodes(current, leaf);

        if (isScored) {
            sankoffEnumerateRecursive(root, size + 1);
        } else {
            enumerateRecursive(root, size + 1);
        }

        Node.unlinkNodes(current, leaf);

    }

    /***************************************************************************
     * Enumerates multifurcating trees. Labels any unlabelled node but the root.
     **************************************************************************/
    private void case4(Node current, int size, boolean isScored) {
        for (int index = 0; index < current.children.size(); index++) {
            case4(current.children.get(index), size, isScored);
        }
        if (!current.labelled && current.parent != null) {
            Node newNode = labelledNodes.get(size).clone();

            current.label = newNode.label;
            current.labelled = true;
            current.data = newNode.data;
            current.costs = newNode.costs;

            if (isScored) {
                sankoffEnumerateRecursive(root, size + 1);
            } else {
                enumerateRecursive(root, size + 1);
            }

            current.data = Node.sets();
            current.costs = null;
            current.labelled = false;
            current.label = "";
        }
    }

//    private Node rootAtUnlabelled() {
//        Node current = root.clone();
//        current = findUnlabelled(current);
//        List<Node> nodesToRoot = new ArrayList<>();
//        while(current != null) {
//            nodesToRoot.add(current);
//            current = current.parent;
//        }
//
//        for (int i = nodesToRoot.size()-2; i >= 0; i--) {
//            Node child = nodesToRoot.get(i);
//            Node ancestor = nodesToRoot.get(i+1);
//            Node.unlinkNodes(ancestor, child);
//            Node.linkNodes(child, ancestor);
//        }
//        if (nodesToRoot.isEmpty()) return root;
//        else return nodesToRoot.get(0);
//    }
//    private Node findUnlabelled(Node current) {
//        if(current.labelled) {
//            for (Node child : current.children) {
//                Node unlabelled = findUnlabelled(child);
//                if (unlabelled != null) return unlabelled;
//            }
//            return null;
//        } else {
//            return current;
//        }
//    }
}
