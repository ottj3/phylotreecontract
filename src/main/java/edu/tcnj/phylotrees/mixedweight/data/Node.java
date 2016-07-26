package edu.tcnj.phylotrees.mixedweight.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node implements Cloneable {

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
     * The sets assigned to a node during top down (or during node creation for a labelled node)
     * that function similarly to hartigan's root sets with regards to finding zero-cost edges
     */
    public CharacterList data = sets();

    /**
     * The cost of the node.
     */
    public double edgeCost = 0;

    /**
     * The running costs for every character state of this node
     * (based on its children's costs + mutation costs)
     */
    public List<double[]> costs;

    /**
     * (Used in Sankoff's top-down)
     * The bases selected to contribute to a parent's base costs
     * The array is indexed by the parent's base, and the set at
     * each index is the set of all of the current node's bases
     * that yielded the minimum cost for the parent's base
     */
    public List<Set<DNABase>[]> parentFits;

    /**
     * Whether this node has a known label. Sometimes useful when running algorithms
     * on nodes that have root sets, but may not be known labelled nodes.
     */
    public boolean labelled;

    /**
     * The parent of this node, or null if this node has no parent.
     */
    public Node parent;

    /**
     * A list of this node's children. This should never be null, although may be empty for leaves.
     */
    public List<Node> children = new ArrayList<>();

    /**
     * Construct a node with the given name.
     *
     * @param label a name for the node, or an empty string for an unlabelled node
     */
    public Node(String label) {
        initializeFits();
        initializeCosts();
        this.label = label;
        this.labelled = !label.isEmpty();
    }

    /**
     * Construct a node with the given name and cost.
     *
     * @param label a name for the node, or an empty string for an unlabelled node
     */
    public Node(String label, String data) {
        this(label);
        this.setData(data);
    }

    /**
     * For a labelled node, set its data from the given tree. Also set its costs to zero
     * for whatever base it has at the current character, or infinity for the other bases
     * @param data A string of character states from some known species
     */
    public void setData(String data) {
        chars = data.length();
        for (int i = 0; i < Node.chars; i++) {
            for (DNABase dnaBase : DNABase.values()) {
                if (DNABase.valueOf(data.substring(i, i + 1)) == dnaBase) {
                    costs.get(i)[dnaBase.value] = 0;
                    this.data.get(i).add(dnaBase);
                } else {
                    costs.get(i)[dnaBase.value] = Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    /**
     * Reset the costs that are generated in Sankoff's bottom-up.
     * If costs is null or empty, initialize it, otherwise set all
     * non-infinite values to zero.
     */
    public void initializeCosts() {
        if (costs == null || costs.isEmpty()) {
            costs = new ArrayList<>(chars);
            for (int i = 0; i < chars; i++) {
                costs.add(new double[DNABase.values().length]);
                for (int j = 0; j < DNABase.values().length; j++) {
                    costs.get(i)[j] = 0;
                }
            }
        } else {
            for (int i = 0; i < chars; i++) {
                for (int j = 0; j < DNABase.values().length; j++) {
                    if (!Double.valueOf(Double.POSITIVE_INFINITY).equals(costs.get(i)[j])) costs.get(i)[j] = 0;
                }
            }
        }
    }

    /**
     * Initialize the parentFits of a node to an empty list
     */
    public void initializeFits() {
        parentFits = new ArrayList<>(chars);
        for (int i = 0; i < chars; i++) {
            parentFits.add(new HashSet[DNABase.values().length]);
//            TODO: is this initialization necessary?
//            for (int j = 0; j < parentFits.get(i).length; j++) {
//                parentFits.get(i)[j] = new HashSet<>();
//            }
        }
    }


    /**
     * Utility method to generate a list of sets, one set for each Character in a species.
     * <p>Callers should be responsible for ensuring that {@link #chars} is set before
     * calling this method.</p>
     *
     * @return an empty but initialized {@link CharacterList} for a node with sets for each character
     */
    public static CharacterList sets() {
        List<Set<DNABase>> sets = new ArrayList<>(chars);
        for (int i = chars; i-- > 0; ) {
            sets.add(new HashSet<DNABase>());
        }
        return new CharacterList(sets);
    }

    /**
     * Creates a parent-child relationship between two nodes in both directions.
     *
     * @param parent the node to be the parent
     * @param child  the node to be the child
     */
    public static void linkNodes(Node parent, Node child) {
        parent.children.add(child);
        child.parent = parent;
    }

    /**
     * Removes a parent-child relationship between two nodes in both directions.
     *
     * @param parent the original parent node
     * @param child  the child node
     */
    public static void unlinkNodes(Node parent, Node child) {
        parent.children.remove(child);
        if (parent == child.parent) {
            child.parent = null;
        }
    }

    /**
     * Clones a Node and all sub-tree nodes as new Objects, but maintains references to
     * the original costs to preserve space.
     * <p>This method preserves the entire structure of a tree (or subtree) in the new Node
     * objects by recursively cloning each child node and re-linking the new objects.</p>
     *
     * @return a copy of a node or tree structure with references to original data sets
     */
    @Override
    public Node clone() {
        Node newNode = new Node(this.label);
        for (Node child : children) {
            Node newChild = child.clone();
            newChild.parent = newNode;
            newNode.children.add(newChild);
        }

        //Copy the data rather than referencing the same list
        List<Set<DNABase>> list = new ArrayList<>();
        for (Set<DNABase> dnaBases : this.data) {
            list.add(new HashSet<>(dnaBases));
        }
        newNode.data = new CharacterList(list);

        //Copy the costs rather than referencing the same list
        newNode.costs = new ArrayList<>(this.costs.size());
        for (double[] cost : this.costs) {
            double[] newCost = new double[cost.length];
            System.arraycopy(cost, 0, newCost, 0, cost.length);
            newNode.costs.add(newCost);
        }
        return newNode;
    }

    /**
     * Utility method to calculate the size of a sub-tree, including the given node.
     * <p>The size is defined to be the total number of nodes in the tree.</p>
     *
     * @param node the root of the tree or subtree to find the size of
     * @return the number of nodes in the tree
     */
    public static int size(Node node) {
        int size = 1;
        for (Node child : node.children) {
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
