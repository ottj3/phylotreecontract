package edu.tcnj.phylotrees.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@code Node} represents a single vertex of a tree.
 * <p>A node stores references to both its parent and its children, if it has either,
 * so it can also be used to represent both an entire (sub)tree, and also used to
 * scale up a tree to find the true root of a tree.</p>
 * <p>A node may also store {@link CharacterList}s for {@link #root},
 * {@link #upper}, and {@link #lower} sets used during the calculation of
 * parsimony scores via {@link edu.tcnj.phylotrees.algo.Fitch}s or
 * {@link edu.tcnj.phylotrees.algo.Hartigan}'s algorithms.</p>
 */
public class Node<S> implements Cloneable {

    /**
     * The number of characters in each species. This should always be kept the same
     * when operating on a single set of input data. All the algorithms assume that
     * all input species have the same number of Characters, and bad things will
     * happen if they do not.
     */
    public static int chars;

    /**
     * A "name" string for the node, used purely for humans to identify nodes.
     */
    public String label;

    /**
     * The cost of the node. At the moment this is preserved by the parser for Newick
     * Tree format strings, but is not used internally.
     */
    public int cost;

    /**
     * Whether this node has a known label. Sometimes useful when running algorithms
     * on nodes that have root sets, but may not be known labelled nodes.
     */
    public boolean labelled;

    /**
     * The root set of a node. This may also be used as a label set for known characters,
     * or as the only set when using Fitch's scoring algorithm.
     */
    public CharacterList<S> root;
    /**
     * The upper set of a node. This is generally used in Hartigan's algorithm.
     */
    public CharacterList<S> upper;
    /**
     * The lower set of a node. This is generally used in Hartigan's algorithm.
     */
    public CharacterList<S> lower;

    /**
     * The parent of this node, or null if this node has no parent.
     */
    public Node<S> parent;
    /**
     * A list of this node's children. This should never be null, although may be empty for leaves.
     */
    public List<Node<S>> children = new ArrayList<>();

    /**
     * Construct a node with the given name.
     *
     * @param label a name for the node, or an empty string for an unlabelled node
     */
    public Node(String label) {
        this.label = label;
        this.labelled = !label.isEmpty();
    }

    /**
     * Construct a node with the given name and cost.
     *
     * @param label a name for the node, or an empty string for an unlabelled node
     * @param cost the cost of the node, generally representing the weight of the edge to its parent
     */
    public Node(String label, int cost) {
        this(label);
        this.cost = cost;
    }

    /**
     * Utility method to generate a list of sets, one set for each Character in a species.
     * <p>Callers should be responsible for ensuring that {@link #chars} is set before
     * calling this method.</p>
     *
     * @return an empty but initialized {@link CharacterList} for a node with sets for each character
     */
    public static <S> CharacterList<S> sets() {
        List<Set<S>> sets = new ArrayList<>(chars);
        for (int i = chars; i-- > 0;) {
            sets.add(new HashSet<S>());
        }
        return new CharacterList<>(sets);
    }

    /**
     * Creates a parent-child relationship between two nodes in both directions.
     *
     * @param parent the node to be the parent
     * @param child the node to be the child
     */
    public static <S> void linkNodes(Node<S> parent, Node<S> child) {
        parent.children.add(child);
        child.parent = parent;
    }

    /**
     * Removes a parent-child relationship between two nodes in both directions.
     *
     * @param parent the original parent node
     * @param child the child node
     */
    public static <S> void unlinkNodes(Node<S> parent, Node<S> child) {
        parent.children.remove(child);
        if (parent == child.parent) {
            child.parent = null;
        }
    }

    /**
     * Clones a Node and all sub-tree nodes as new Objects, but maintains references to
     * the original {@link CharacterList}s to preserve space.
     * <p>This method preserves the entire structure of a tree (or subtree) in the new Node
     * objects by recursively cloning each child node and re-linking the new objects.</p>
     *
     * @return a copy of a node or tree structure with references to original data sets
     */
    @Override
    public Node<S> clone() {
        Node<S> newNode = new Node<>(this.label, this.cost);
        for (Node<S> child : children) {
            Node<S> newChild = child.clone();
            newChild.parent = newNode;
            newNode.children.add(newChild);
        }
        newNode.root = this.root;
        newNode.upper = this.upper;
        newNode.lower = this.lower;
        return newNode;
    }

    /**
     * Utility method to calculate the size of a sub-tree, including the given node.
     * <p>The size is defined to be the total number of nodes in the tree.</p>
     *
     * @param node the root of the tree or subtree to find the size of
     * @return the number of nodes in the tree
     */
    public static <S> int size(Node<S> node) {
        int size = 1;
        for (Node<S> child : node.children) {
            size += size(child);
        }
        return size;
    }

    /**
     * Calculates the {@link #size(Node)} of the current node object.
     *
     * @return size of the current node's subtree, including itself
     */
    public int size() {
        return size(this);
    }

}
