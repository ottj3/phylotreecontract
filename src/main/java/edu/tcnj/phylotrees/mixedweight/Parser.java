package edu.tcnj.phylotrees.mixedweight;

import edu.tcnj.phylotrees.mixedweight.data.Node;

import java.util.*;

public class Parser {

    private static final String SPECIALS = "(),;";

    /**
     * Converts a single node to a full node string, but does not recursively build a tree.
     *
     * @param node the {@link Node} to stringify
     * @return string of the Node's label and cost
     */
    private static String nodeToString(Node node) {
        if (node == null) return "";
        return node.label;
    }

    /**
     * Converts a root (or sub-tree root) node into a Newick-format string.
     *
     * @param root top-most node of tree to convert
     * @return Newick-format tree representation
     */
    public static String toString(Node root) {
        if (root == null) return ";";
        String string = nodeToString(root) + ";";
        return toStringRecursive(root) + string;
    }

    private static String toStringRecursive(Node root) {
        String string = "";

        if (!root.children.isEmpty()) {
            string = ")" + string;
        }

        ListIterator<Node> it = root.children.listIterator(root.children.size());
        while (it.hasPrevious()) {
            Node child = it.previous();
            string = nodeToString(child) + string;
            string = (it.hasPrevious() ? "," : "") + toStringRecursive(child) + string;
        }

        if (!root.children.isEmpty()) {
            string = "(" + string;
        }

        return string;
    }

    /**
     * Converts a Newick tree string to a {@link Node} object which roots the entire tree.
     *
     * @param s Newick-formatted string
     * @return root {@link Node} of the tree
     */
    public static Node fromString(String s) {
        if (s.isEmpty())
            throw new IllegalArgumentException("Empty string can't be a Newick tree, needs at least a ';'");
        if (s.length() == 1 && s.charAt(0) == ';') return null; // empty tree is technically a valid tree
        if (s.charAt(s.length() - 1) != ';') {
            throw new IllegalArgumentException("Invalid Newick string, missing ';'");
        }
        return fromStringRecursive(s.substring(0, s.length() - 1), null);
    }

    private static Node fromStringRecursive(String s, Node parent) {
        int prevSpecial = -1;
        for (int i = s.length() - 1; i >= 0; i--) {
            // scan left until we find one of our special characters
            char ch = s.charAt(i);

            if (SPECIALS.indexOf(ch) != -1) {
                prevSpecial = i;
                break;
            }
        }
        String label = s.substring(prevSpecial == -1 ? 0 : prevSpecial + 1, s.length());

        Node current = nodeFromLabel(label);
        if (parent != null) {
            current.parent = parent;
            parent.children.add(current);
        }

        // current node has no children
        if (prevSpecial == -1 || s.charAt(prevSpecial) != ')') {
            return current;
        }
        // otherwise
        // find the matching ( for all of current's children
        int childrenEnd = -1;
        int parens = 0;
        for (int i = prevSpecial; i >= 0; i--) { // first will always be a close paren
            char ch = s.charAt(i);
            if (ch == ')') parens++;
            else if (ch == '(') parens--;

            // when parens hits 0, it means the initial (prevSpecial) ')' has been closed
            if (parens == 0) {
                childrenEnd = i;
                break;
            }
        }
        if (childrenEnd == -1) {
            throw new IllegalArgumentException("Missing opening parenthesis for children of " + current.label);
        }
        String sC = s.substring(childrenEnd + 1, prevSpecial); // string containing all current children
        List<Integer> childSeps = new ArrayList<>();
        parens = 0;
        for (int i = 0; i < sC.length(); i++) {
            char ch = sC.charAt(i);
            if (ch == '(') parens++;
            else if (ch == ')') parens--;

            if (parens == 0 && ch == ',') {
                childSeps.add(i);
            }
        }
        if (childSeps.isEmpty()) {
            fromStringRecursive(sC, current); // single child
        } else {
            int start = 0;
            int next = 0;
            for (int i : childSeps) {
                next = i;
                fromStringRecursive(sC.substring(start, next), current); // children up to the last ','
                start = next;
            }
            fromStringRecursive(sC.substring(next + 1, sC.length()), current); // child from last ',' to the ')'
        }

        return current;

    }

    protected static Node nodeFromLabel(String label) {
        return nodeFromLabel(label, 0.0D);
    }

    protected static Node nodeFromLabel(String label, double cost) {
        Node node = new Node(label, 0);
        node.edgeCost = cost;
        return node;
    }

    /**
     * Takes an input list of species and fills a list of nodes representing those species and a world set
     * for each character's possible states.
     *
     * @param input    list of a string for each species in the form "L:XYZ" where L is the name and each
     *                 X, Y, and Z is the state of the character at the given position
     */
    public static List<Node> speciesList(List<String> input) {
        List<Node> species = new ArrayList<>();
        for (String line : input) {
            String[] sp = line.split(":");
            String label = sp[0];
            String data = sp[1];
            Node node = new Node(label, data);
            species.add(node);
        }
        return species;
    }

    private static void removeAllUninformative(List<String> data) {
        if (data == null || data.isEmpty()) return;

        for (int i = data.get(0).length() - 1; i >= 0; i--) {
            boolean allMatch = true;
            char match = data.get(0).charAt(i);
            for (String s : data) {
                if (s.charAt(i) != match) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                for (int j = 0; j < data.size(); j++) {
                    String current = data.get(j);
                    data.set(j, current.substring(0, i) + current.substring(i + 1));
                }
            }
        }
    }

    /**
     * Recursively sets data on all labelled nodes in a tree.
     * @param root node at root of tree
     * @param labelToSequence map of node labels to the dna sequence of that species
     */
    public static void fillNodes(Node root, Map<String, String> labelToSequence) {
        for (Node child : root.children) {
            fillNodes(child, labelToSequence);
        }
        if (root.labelled && labelToSequence.containsKey(root.label)) {
            root.setData(labelToSequence.get(root.label));
        }
    }

    public static void fillNodes(Node root, List<String> labelToSequence) {
        Map<String, String> map = new HashMap<>();
        for (String l : labelToSequence) {
            String[] s = l.split(":");
            map.put(s[0], s[1]);
        }
        fillNodes(root, map);
    }
}
