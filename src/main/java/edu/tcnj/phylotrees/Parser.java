package edu.tcnj.phylotrees;

import edu.tcnj.phylotrees.data.CharacterList;
import edu.tcnj.phylotrees.data.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class Parser {

    private static final String SPECIALS = "():,;";

    public Parser() {
    }

    private static <S> CharacterList<S> charactersFromString(String s) {
        int i = 0;
        int t;
        List<Set<S>> cList = new ArrayList<>();
        while ((t = s.indexOf('|', i)) != -1) { // move end cursor to the next '|' delimiter if it exists
            Set<S> set = stateSetFromString(s.substring(i, t));
            if (set != null) cList.add(set); // substring from start cursor to end cursor
            i = t + 1; // move start cursor past the last end cursor position (i.e. after the '|')
        }
        Set<S> set = stateSetFromString(s.substring(i, s.length()));
        if (set != null) cList.add(set); // substring to end of characters
        return new CharacterList<>(cList);
    }

    @SuppressWarnings("unchecked")
    private static <S> Set<S> stateSetFromString(String s) {
        if (s.isEmpty()) return null;
        int i = 0;
        int t;
        Set<S> set = new HashSet<>();
        while ((t = s.indexOf(',', i)) != -1) { // move end cursor to next ',' if exists
            Object o = stateFromString(s.substring(i, t));  // parse single state object
            set.add((S) o);
            i = t + 1; // move start past end
        }
        set.add((S) stateFromString(s.substring(i, s.length()))); // substring to end of states
        return set;
    }

    @SuppressWarnings("unchecked")
    private static <S> S stateFromString(String s) {
        S o;
        try {
            o = (S) Integer.valueOf(s);
        } catch (NumberFormatException e) {
            try {
                o = (S) Double.valueOf(s);
            } catch (NumberFormatException e2) {
                o = (S) (s.length() == 1 ? s.charAt(0) : s);
            }
        }
        return o;
    }

    private static void checkMissingSet(int index, String label) {
        if (index == -1)
            throw new IllegalArgumentException("Found node with data but missing character sets: " + label);
    }

    private static <S> String nodeToCharacterString(Node<S> node) {
        return "[" +
                (node.root.isEmpty() ? "[]" : listToCharacterString(node.root)) +
                (node.upper.isEmpty() ? "[]" : listToCharacterString(node.upper)) +
                (node.lower.isEmpty() ? "[]" : listToCharacterString(node.lower)) +
                "]";
    }

    private static <S> String listToCharacterString(CharacterList<S> set) {
        Iterator<Set<S>> it = set.iterator();
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean firstOut = true;
        while (it.hasNext()) { // for each character
            if (!firstOut) {
                sb.append('|');
            }
            Iterator<S> intIt = it.next().iterator();
            boolean firstIn = true;
            while (intIt.hasNext()) { // for each state in potential set of states
                if (!firstIn) {
                    sb.append(',');
                }
                sb.append(intIt.next()); /// append type of S as string
                firstIn = false;
            }
            firstOut = false;
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Converts a single node to a full node string, but does not recursively build a tree.
     *
     * @param node the {@link Node} to stringify
     * @param data {@code true} to append the root/upper/lower sets to the label
     * @return string of the Node's label and cost
     */
    public <S> String nodeToString(Node<S> node, boolean data) {
        if (node == null) return "";
        return node.label + (data ? nodeToCharacterString(node) : "") + ":" + node.cost;
    }

    /**
     * Converts a root (or sub-tree root) node into a Newick-format string.
     *
     * @param root top-most node of tree to convert
     * @param data true to add root/upper/lower sets to label
     * @return Newick-format tree representation
     */
    public <S> String toString(Node<S> root, boolean data) {
        if (root == null) return ";";
        String string = nodeToString(root, data) + ";";
        return toStringRecursive(root, data) + string;
    }

    private <S> String toStringRecursive(Node<S> root, boolean data) {
        String string = "";

        if (root.children.size() > 0) {
            string = ")" + string;
        }

        ListIterator<Node<S>> it = root.children.listIterator(root.children.size());
        while (it.hasPrevious()) {
            Node<S> child = it.previous();
            string = nodeToString(child, data) + string;
            string = (it.hasPrevious() ? "," : "") + toStringRecursive(child, data) + string;
        }

        if (root.children.size() > 0) {
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
    public <S> Node<S> fromString(String s) {
        if (s.isEmpty())
            throw new IllegalArgumentException("Empty string can't be a Newick tree, needs at least a ';'");
        if (s.length() == 1 && s.charAt(0) == ';') return null; // empty tree is technically a valid tree
        if (s.charAt(s.length() - 1) != ';') {
            throw new IllegalArgumentException("Invalid Newick string, missing ';'");
        }
        return fromStringRecursive(s.substring(0, s.length() - 1), null);
    }

    private <S> Node<S> fromStringRecursive(String s, Node<S> parent) {
        int brackets = 0;
        int topLabelPos = s.lastIndexOf(':');
        if (topLabelPos == -1) return null; // empty tree, but should be handled already
        if (topLabelPos == s.length()) throw new IllegalArgumentException("Found node with no cost");

        // label is everything between the colon and the closest special character on the left
        int prevSpecial = -1;
        for (int i = topLabelPos - 1; i >= 0; i--) {
            // scan left until we find one of our special characters
            int ch = s.charAt(i);
            if (ch == ']') brackets++;
            else if (ch == '[') brackets--;

            // ignore special characters between brackets
            if (brackets == 0 && SPECIALS.indexOf(ch) != -1) {
                prevSpecial = i;
                break;
            }
        }
        String label = s.substring(prevSpecial == -1 ? 0 : prevSpecial + 1, topLabelPos);

        brackets = 0;
        // cost is everything between the colon and the closest special character on the right
        int nextSpecial = -1;
        for (int i = topLabelPos + 1; i < s.length(); i++) {
            // scan right until we find one of our special characters
            int ch = s.charAt(i);
            if (ch == ']') brackets--;
            else if (ch == '[') brackets++;

            // ignore special characters between brackets
            if (brackets == 0 && SPECIALS.indexOf(ch) != -1) {
                nextSpecial = i;
                break;
            }
        }
        // if the next character after the colon was a special character, it means the cost substring is empty
        if (nextSpecial == topLabelPos + 1)
            throw new IllegalArgumentException("Found label with no cost: " + (label.isEmpty() ? "[Empty]" : label));
        String costStr = s.substring(topLabelPos + 1, nextSpecial == -1 ? s.length() : nextSpecial);
        int cost;
        try {
            cost = Integer.valueOf(costStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Node " + (label.isEmpty() ? "[Empty]" : label) + " has non-number cost: " + costStr);
        }

        Node<S> current = nodeFromLabel(label, cost);
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
        brackets = 0;
        for (int i = prevSpecial; i >= 0; i--) { // first will always be a close paren
            char ch = s.charAt(i);
            if (ch == ']') brackets++;
            else if (ch == '[') brackets--;
            else if (brackets == 0 && ch == ')') parens++; // don't count parens inside brackets
            else if (brackets == 0 && ch == '(') parens--; // as above

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
        brackets = parens = 0;
        for (int i = 0; i < sC.length(); i++) {
            char ch = sC.charAt(i);
            if (ch == '[') brackets++;
            else if (ch == ']') brackets--;
            else if (brackets == 0 && ch == '(') parens++;
            else if (brackets == 0 && ch == ')') parens--;

            if (brackets == 0 && parens == 0 && ch == ',') {
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
                fromStringRecursive(sC.substring(start, next + 1), current); // children up to the last ','
                start = next;
            }
            fromStringRecursive(sC.substring(next, sC.length()), current); // child from last ',' to the ')'
        }

        return current;

    }

    protected <S> Node<S> nodeFromLabel(String label, int cost) {
        int i = label.indexOf('[');
        Node<S> node = new Node<>("");
        if (i != -1) {
            node.label = label.substring(0, i);
            node.labelled = true;
            int rootStart, upperStart, lowerStart;
            // separate root, upper, and lower sets by their '[]'s
            rootStart = label.indexOf('[', i + 1);
            checkMissingSet(rootStart, label);
            upperStart = label.indexOf('[', rootStart + 1);
            checkMissingSet(upperStart, label);
            lowerStart = label.indexOf('[', upperStart + 1);
            checkMissingSet(lowerStart, label);

            try {
                // parse character strings for each potential substring
                node.root = charactersFromString(label.substring(rootStart + 1, upperStart - 1));
                node.upper = charactersFromString(label.substring(upperStart + 1, lowerStart - 1));
                node.lower = charactersFromString(label.substring(lowerStart + 1, label.length() - 2)); // don't include the last two ']'s
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Found non-matchable state types in sets: " + label);
            }
        } else if (!label.isEmpty()) {
            node.label = label;
            node.labelled = true;
        }
        node.cost = cost;
        return node;
    }

    /**
     * Takes an input list of species and fills a list of nodes representing those species and a world set
     * for each character's possible states.
     *
     * @param input    list of a string for each species in the form "L:XYZ" where L is the name and each
     *                 X, Y, and Z is the state of the character at the given position
     * @param species  a list to be filled with nodes
     * @param worldSet a {@link CharacterList} to be filled with all states for each character
     */
    public <S> void speciesList(List<String> input, List<Node<S>> species, List<Set<S>> worldSet) {
        List<String> labels = new ArrayList<>();
        List<String> data = new ArrayList<>();
        for (String s : input) {
            String[] sp = s.split(":");
            labels.add(sp[0]);
            data.add(sp[1]);
        }
        removeAllUninformative(data);
        for (int i = 0; i < input.size(); i++) {
            Node<S> node = new Node<>(labels.get(i));
            int chars = data.get(i).length();
            node.root = Node.sets(chars);

            char[] charArray = data.get(i).toCharArray();
            for (int j = 0; j < charArray.length; j++) {
                if (worldSet.size() <= j) {
                    worldSet.add(new HashSet<S>());
                }
                S state = (S) Character.valueOf(charArray[j]);
                worldSet.get(j).add(state);
                node.root.get(j).add(state);
            }
            species.add(node);
        }
    }

    private void removeAllUninformative(List<String> data) {
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
}
