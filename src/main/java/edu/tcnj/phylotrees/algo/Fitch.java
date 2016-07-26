package edu.tcnj.phylotrees.algo;

import edu.tcnj.phylotrees.Parser;
import edu.tcnj.phylotrees.data.CharacterList;
import edu.tcnj.phylotrees.data.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Fitch {

    // internal method to calculate score from a node's children to itself
    private static <S> int fitch(Node<S> current, int chars) {
        // if we are a leaf node, our current root set is already
        // correct, and the score is 0
        if (current.children.isEmpty()) return 0;

        int score = 0;
        // create a list of the root sets of all our children
        List<CharacterList<S>> childRoots = new ArrayList<>();
        for (Node<S> child : current.children) {
            childRoots.add(child.root);
        }

        // initialize our new root set as a bunch of empty sets
        CharacterList<S> root = Node.sets(chars);

        // for each character
        for (int i = 0; i < chars; i++) {
            boolean first = true;
            boolean union = false;
            // get the root set for that character
            Set<S> currentStatesForChar = root.get(i);
            // for each child root set
            for (CharacterList<S> childRoot : childRoots) {
                if (first) {
                    // initialize our root set to the child's root for the first
                    currentStatesForChar.addAll(childRoot.get(i));
                    first = false;
                } else {
                    // intersect each remaining child root set with our root set
                    currentStatesForChar.retainAll(childRoot.get(i));
                    if (currentStatesForChar.isEmpty()) {
                        // if at any point the intersection becomes empty,
                        // it means our root set needs to be the union of them all
                        union = true;
                        break;
                    }
                }
            }
            if (union) {
                // this character had a change, so increase the score
                score += 1;
                // and set our root set to the union of all children's root sets
                for (CharacterList<S> childRoot : childRoots) {
                    currentStatesForChar.addAll(childRoot.get(i));
                }
            }
        }

        current.root = root;

        return score;
    }

    /**
     * Performs Fitch's bottom up algorithm to score a tree.
     * <p>
     * Given a root of a tree (or sub-tree), this method will return a parsimony score equal to the
     * sum of the number of character state mutations between each node and its children
     *
     * @param root the (sub)tree root node to score using Fitch's parsimony algorithm
     * @param chars the number of characters a species has (Node.chars, passed in to avoid overhead)
     * @return the parsimony score of the tree
     */
    public static <S> int bottomUp(Node<S> root, int chars) {
        int score = 0;

        // recursive down the to bottom of the tree first
        for (Node<S> child : root.children) {
            score += bottomUp(child, chars);
        }

        if (root.children.size() > 2) {
            throw new IllegalArgumentException("Can only perform Fitch on cubic tree - got node of degree > 3: "
                    + (new Parser()).toString(root));
        }

        score += fitch(root, chars);

        return score;
    }

    /**
     * A utility method to re-root a cubic tree (i.e. one where the root has three
     * children) to make it binary, using a new unlabelled node as the new root.
     *
     * @param root the old cubic tree root
     * @return the new binary tree root
     */
    public static <S> Node<S> cubicToBinary(Node<S> root) {
        Node<S> newRoot = new Node<>("");
        Node.linkNodes(newRoot, root);
        Node.linkNodes(newRoot, root.children.get(root.children.size() - 1));
        Node.unlinkNodes(root, newRoot.children.get(1));
        return newRoot;
    }

    /**
     * A utility method reversing the process in {@link #cubicToBinary}.
     * <p>
     * Re-links the left child of the current root (i.e. the old root) to
     * the right child, and unlinks the current (binary) root re-establish
     * the cubic tree structure.
     *
     * @param root a binary tree root
     * @return a cubic tree root
     */
    public static <S> Node<S> binaryToCubic(Node<S> root) {
        Node<S> oldRoot = root.children.get(0);
        Node.linkNodes(oldRoot, root.children.get(1));
        Node.unlinkNodes(root, root.children.get(1));
        Node.unlinkNodes(root, oldRoot);
        return oldRoot;
    }
}
