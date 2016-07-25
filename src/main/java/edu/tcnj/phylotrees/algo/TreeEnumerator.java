package edu.tcnj.phylotrees.algo;

import edu.tcnj.phylotrees.data.CharacterList;
import edu.tcnj.phylotrees.data.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TreeEnumerator<S> {

    //The set of all possible characters (used in Hartigan)
    protected CharacterList<S> worldSet = new CharacterList<>();

    //The current best parsimony score (used for branch+bound and maintaining list of most parsimonious trees)
    protected int parsimonyScore = -1;

    //The list of labelled nodes received from the input
    protected List<Node<S>> labelledNodes = new ArrayList<>();

    //The set of all tree topologies
    protected Set<Node<S>> trees = new HashSet<>();

    //The count of all trees enumerated (only maintained in the basic enumerate methods, not in fitch or hartigan)
    protected int treeCounter = 0;

    //The root of the current tree
    protected Node<S> root = new Node<>("");

    //the number of characters a species has (Node.chars, passed in to avoid overhead)
    protected int chars = 0;

    //Add internal between current and parent, and then make leaf a child of internal
    protected void addNodeToEdge(Node<S> current, Node<S> parent, Node<S> internal, Node<S> leaf) {
        Node.unlinkNodes(parent, current);

        Node.linkNodes(parent, internal);
        Node.linkNodes(internal, current);
        Node.linkNodes(internal, leaf);
    }

    //Remove internal and leaf, leaving just parent and current
    protected void removeNodeFromEdge(Node<S> current, Node<S> parent, Node<S> internal, Node<S> leaf) {
        Node.unlinkNodes(internal, leaf);
        Node.unlinkNodes(internal, current);
        Node.unlinkNodes(parent, internal);

        Node.linkNodes(parent, current);
    }

    //Keep only the most parsimonious trees found so far
    protected void updateMPlist(int thisParsimonyScore) {
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
