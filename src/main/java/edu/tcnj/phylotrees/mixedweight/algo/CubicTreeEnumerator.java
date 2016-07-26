package edu.tcnj.phylotrees.mixedweight.algo;

import edu.tcnj.phylotrees.mixedweight.data.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CubicTreeEnumerator extends TreeEnumerator {

    public CubicTreeEnumerator(List<Node> labelledNodes) {
        this.labelledNodes = labelledNodes;
    }

    public CubicTreeEnumerator(List<Node> labelledNodes, double[][] weights) {
        this.labelledNodes = labelledNodes;
        this.weights = weights;
    }

    /**
     * Generates the base topology for a tree with n leaves.
     * This method initializes a tree topology to give us a starting point for
     * enumerating arbitrarily rooted cubic trees.
     */
    protected void initializeTree() {
        //Base cases of cubic trees:
        if (labelledNodes.size() == 1) {
            //Only one node: make the node a root
            root = labelledNodes.get(0).clone();
        } else if (labelledNodes.size() == 2) {
            //Only two nodes: make and edge between the two
            root = labelledNodes.get(0).clone();
            Node.linkNodes(root, labelledNodes.get(1).clone());
        } else {
            //3+ nodes: make an unlabelled node the root, with the first 3 nodes as its children
            root = new Node("");

            for (int i = 0; i < 3; i++) {
                Node.linkNodes(root, labelledNodes.get(i).clone());
            }
        }
    }

    /**
     * Enumerates all possible tree topologies for a given set of species.
     *
     * @return the number of possible cubic trees with the given set of labelled nodes
     */
    public int enumerate() {
        // Reset the state of the algorithm by clearing trees.
        trees = new HashSet<>();
        treeCounter = 0;

        //Start with the base cases
        initializeTree();
        if (labelledNodes.size() < 4) {
            //If there are less than 4 labelled nodes, only one tree exists, so do not recurse
            treeCounter++;
        } else {
            //Otherwise, recursively build all possibilities
            enumerateRecursive(root, 3);
        }
        return treeCounter;
    }


    protected void enumerateRecursive(Node current, int size) {
        if (size == labelledNodes.size()) {
            //Base case: if the tree has all labelled nodes, increment the counter
            treeCounter++;
        } else {
            for (int i = 0; i < current.children.size(); i++) {
                //Recursively perform this method on all children
                enumerateRecursive(current.children.get(0), size);
            }
            if (current != root) {
                //Create an unlabelled node between the current node and its parent, and then add the next
                //leaf as another child of the new unlabelled node
                Node internal = new Node("");
                Node leaf = labelledNodes.get(size).clone();
                Node parent = current.parent;

                addNodeToEdge(current, parent, internal, leaf);

                enumerateRecursive(root, size + 1);

                //Undo the changed edge
                removeNodeFromEdge(current, parent, internal, leaf);
            }
        }
    }

    /**
     * Branch+bounded cubic tree enumeration using fitch to score the trees
     *
     * @return a set of the root nodes of all most parsimonious trees
     */
    public Set<Node> sankoffEnumerate() {
        // Reset the state of the algorithm by clearing trees.
        trees = new HashSet<>();
        parsimonyScore = -1;

        initializeTree();
        if (labelledNodes.size() < 4) {
            trees.add(root.clone());
        } else {
            sankoffEnumerateRecursive(root, 3);
        }
        return trees;
    }

    protected void sankoffEnumerateRecursive(Node current, int size) {
        if (size == labelledNodes.size()) {
            //Root the tree to make it bifurcating (to work in Fitch) and score it

            double score = Sankoff.bottomUp(root, weights);
            //Add it to the list of most parsimonious trees if its score is the best
            updateMPlist(score);
        } else {
            for (int i = 0; i < current.children.size(); i++) {
                //Recurse: add a new node between the current node and any of its children
                sankoffEnumerateRecursive(current.children.get(0), size);
            }

            //get its current parsimony score
            double thisScore = Sankoff.bottomUp(root, weights);

            //Same as enumerateRecursive but bounded: only continue if there is no best parsimony
            //score or if this tree is at least as good as the most parsimonious
            if (current != root && (thisScore <= parsimonyScore || parsimonyScore == -1)) {
                Node internal = new Node("");
                Node leaf = labelledNodes.get(size).clone();
                Node parent = current.parent;

                addNodeToEdge(current, parent, internal, leaf);

                sankoffEnumerateRecursive(root, size + 1);
                removeNodeFromEdge(current, parent, internal, leaf);
            }
        }
    }
}
