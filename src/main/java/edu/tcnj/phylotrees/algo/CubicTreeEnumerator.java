package edu.tcnj.phylotrees.algo;

import edu.tcnj.phylotrees.data.CharacterList;
import edu.tcnj.phylotrees.data.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CubicTreeEnumerator<S> extends TreeEnumerator<S> {

    public CubicTreeEnumerator(List<Node<S>> labelledNodes, int chars) {
        this.labelledNodes = labelledNodes;
        this.chars = chars;
    }

    public CubicTreeEnumerator(List<Node<S>> labelledNodes, CharacterList<S> worldSet, int chars) {
        this(labelledNodes, chars);
        this.worldSet = worldSet;
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
            root = new Node<>("");

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


    protected void enumerateRecursive(Node<S> current, int size) {
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
                Node<S> internal = new Node<>("");
                Node<S> leaf = labelledNodes.get(size).clone();
                Node<S> parent = current.parent;

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
    public Set<Node<S>> fitchEnumerate() {
        // Reset the state of the algorithm by clearing trees.
        trees = new HashSet<>();
        parsimonyScore = -1;

        initializeTree();
        if (labelledNodes.size() < 4) {
            trees.add(root.clone());
        } else {
            fitchEnumerateRecursive(root, 3);
        }
        return trees;
    }

    protected void fitchEnumerateRecursive(Node<S> current, int size) {
        if (size == labelledNodes.size()) {
            //Root the tree to make it bifurcating (to work in Fitch) and score it
            root = Fitch.cubicToBinary(root);
            int score = Fitch.bottomUp(root, chars);
            //Add it to the list of most parsimonious trees if its score is the best
            updateMPlist(score);

            //Remove the root from the binary tree to make it cubic
            root = Fitch.binaryToCubic(root);
        } else {
            for (int i = 0; i < current.children.size(); i++) {
                //Recurse: add a new node between the current node and any of its children
                fitchEnumerateRecursive(current.children.get(0), size);
            }

            //Root the tree, get its current parsimony score, and unroot it
            root = Fitch.cubicToBinary(root);
            int thisScore = Fitch.bottomUp(root, chars);
            root = Fitch.binaryToCubic(root);

            //Same as enumerateRecursive but bounded: only continue if there is no best parsimony
            //score or if this tree is at least as good as the most parsimonious
            if (current != root && (thisScore <= parsimonyScore || parsimonyScore == -1)) {
                Node<S> internal = new Node<>("");
                Node<S> leaf = labelledNodes.get(size).clone();
                Node<S> parent = current.parent;

                addNodeToEdge(current, parent, internal, leaf);

                fitchEnumerateRecursive(root, size + 1);

                removeNodeFromEdge(current, parent, internal, leaf);
            }
        }
    }

    /**
     * Branch+bounded cubic tree enumeration using hartigan to score the trees
     *
     * @return a set of the root nodes of all most parsimonious trees
     */
    public Set<Node<S>> hartiganEnumerate() {
        // Reset the state of the algorithm by clearing trees.
        trees = new HashSet<>();
        parsimonyScore = -1;

        if (worldSet.isEmpty() || worldSet == null) return trees;

        initializeTree();
        if (labelledNodes.size() < 4) {
            trees.add(root.clone());
        } else {
            hartiganEnumerateRecursive(root, 3);
        }
        return trees;
    }


    protected void hartiganEnumerateRecursive(Node<S> current, int size) {
        if (size == labelledNodes.size()) {
            //If the tree contains all labelled nodes, score it with hartigans
            //and update the list of most parsimonious trees.
            int score = Hartigan.bottomUp(root, worldSet, chars);
            updateMPlist(score);
        } else {
            for (int i = 0; i < current.children.size(); i++) {
                hartiganEnumerateRecursive(current.children.get(0), size);
            }
            //Same as enumerateRecursive, but use Hartigan to score the tree and stop when the tree
            //cannot be a most parsimonious tree. Same as Fitch, but no need to root the tree first
            if (current != root && (Hartigan.bottomUp(root, worldSet, chars) <= parsimonyScore || parsimonyScore == -1)) {
                Node<S> internal = new Node<>("");
                Node<S> leaf = labelledNodes.get(size).clone();
                Node<S> parent = current.parent;

                addNodeToEdge(current, parent, internal, leaf);

                hartiganEnumerateRecursive(root, size + 1);

                removeNodeFromEdge(current, parent, internal, leaf);
            }
        }
    }
}
