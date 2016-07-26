package edu.tcnj.phylotrees.simpleweight.algo;

import edu.tcnj.phylotrees.simpleweight.data.CharacterList;
import edu.tcnj.phylotrees.simpleweight.data.Node;
import edu.tcnj.phylotrees.simpleweight.data.WordCountMap;

import java.util.*;

/*******************************************************************************
 * This class contains methods to perform Hartigan's bottom-up and town-down
 * algorithms for species with m characters. It also contains the fastHartigan
 * method for performing Hartigan's algorithm with labelled internal nodes.
 *******************************************************************************/
public class Hartigan {

    //Performs the calculation of upper and lower sets as well as MP-score.
    // Used in bottom-up of Hartigan's algorithm.
    public static <S> int hartigan(Node<S> current, CharacterList<S> worldSet, int chars) {
        int score = 0; //initialize maximum parsimony score of node

        current.upper = Node.sets(chars);
        current.lower = Node.sets(chars);//create upper and lower sets for each char of node

        //array to store count of each char as it appears in current's children
        List<WordCountMap<S>> count = new ArrayList<>(chars);

        //Store the maximum count of states for each char
        int[] kOccurrences = new int[chars];

        //Initialize empty hashmap for each character in alignment
        for (int index = 0; index < chars; index++) {
            WordCountMap<S> map = WordCountMap.withExpectedSize(4);
            for (S s : worldSet.get(index)) {
                map.addValue(s, 0);
            }
            count.add(map);
            kOccurrences[index] = -1;
        }

        //for each child
        for (Node<S> child : current.children) {
            //for each character in species alignment
            for (int index = 0; index < chars; index++) {
                //for each character state in the child's upper set
                for (S state : child.upper.get(index)) {
                    //if alignment position contains the state, increase the count
                    int newCount = count.get(index).addValue(state, 1);

                    //if state appears more frequently than the current max, update kOccurrences
                    if (newCount > kOccurrences[index]) {
                        kOccurrences[index] = newCount;
                    }
                }
            }
        }

        //Find all states that occur K or K-1 times, add to VU or VL respectively
        for (int index = 0; index < chars; index++) {
            for (S state : count.get(index).keySet()) {
                int occurrences = count.get(index).getInt(state);
                if (occurrences == kOccurrences[index]) {
                    current.upper.get(index).add(state);
                } else if (occurrences == kOccurrences[index] - 1) {
                    current.lower.get(index).add(state);
                }
            }
            //Update parsimony score: K children have this state, so
            //((# total children) - K) children each add +1 to this node's parsimony score
            score += current.children.size() - kOccurrences[index];
        }
        return score;
    }

    //Special case of hartigan's: initialize upper and lower sets of labelled nodes
    private static <S> int fastHartigan(Node<S> current, int chars) {
        int score = 0; //initialize maximum parsimony score of node
        current.upper = Node.sets(chars);
        current.lower = Node.sets(chars); //create upper and lower sets for each char of node

        //for each character in alignment
        for (int index = 0; index < chars; index++) {
            //Make the node's upper set equal its root set (assumes root set only contains one character)
            S label = current.root.get(index).iterator().next();
            current.upper.get(index).add(label);

            //Update the parsimony score of this node
            for (Node<S> child : current.children) {
                //For every child whose upper set doesn't contain this node's value, the score goes up by one
                if (!child.upper.get(index).contains(label)) {
                    score += 1;
                }
            }
        }

        return score;
    }

    /**
     * Performs bottom up of Hartigan's algorithm (see Theorem2 of Hartigan's paper)
     *
     * @param current  the current node being used in the recursive call
     * @param worldSet this contains all possible character states
     * @param chars the number of characters a species has (Node.chars, passed in to avoid overhead)
     * @return the parsimony score of this subtree
     */
    public static <S> int bottomUp(Node<S> current, CharacterList<S> worldSet, int chars) {
        int score = 0;

        //Calculate the score of this node's children (bottom-up recursion)
        for (Node<S> child : current.children) {
            score += bottomUp(child, worldSet, chars);
        }

        //If a node is not a leaf, use hartigan's to calculate its score and upper/lower set
        if (current.children.size() >= 1) {
            //Special case: if the node is labelled, calculate the score using fastHartigan
            if (current.labelled) {
                score += fastHartigan(current, chars);
            } else {
                score += hartigan(current, worldSet, chars);
            }
        } else {
            //Assumes a leaf is labelled, so sets its upper set to be its root set and make an empty lower set
            current.upper = current.root;
            current.lower = Node.sets(chars);
        }

        return score;
    }

    /**
     * Performs top-down of Hartigan's algorithm. (See Theorem3 of Hartigan's paper.)
     *
     * @param current the current node being used in the recursive call
     * @param chars the number of characters a species has (Node.chars, passed in to avoid overhead)
     * @return a list of all zero-cost edges
     */
    public static <S> List<List<Node<S>>> topDown(Node<S> current, int chars) {
        //List of edges (inner list will always be a pair of nodes, to represent an edge)
        List<List<Node<S>>> edges = new ArrayList<>();
        if (current.parent == null) {
            current.root = current.upper;
        }
        if (current.children.size() > 0) {
            //for each child, calculate its root set and get the minimum cost of the edge
            for (Node<S> child : current.children) {
                int cost = 0;

                // initialize blank root set for child
                child.root = Node.sets(chars);

                //for each character in sequence
                for (int i = 0; i < chars; i++) {
                    //If current's root set is a subset of child's upper set
                    if (child.upper.get(i).containsAll(current.root.get(i))) {
                        //Child's root set = parent's root set for this character
                        child.root.get(i).addAll(current.root.get(i));
                    } else {
                        //Child's root set = (upper set) union (intersection of current's upper and child's lower)

                        // make a copy of the upper and lower set for this character since we modify them
                        Set<S> newVL = new HashSet<>(child.lower.get(i));
                        Set<S> newVU = new HashSet<>(child.upper.get(i));

                        newVL.retainAll(current.root.get(i)); //Intersection of current's upper and child's lower
                        newVU.addAll(newVL); //Union of child's upper and the above intersection
                        child.root.get(i).addAll(newVU); //Add this character's root set to the child
                    }

                    //Calculate the cost of this character

                    //Make copy of the child's root set because retainAll changes the set
                    Set<S> x, y;
                    x = new HashSet<>(child.root.get(i));
                    y = current.root.get(i);

                    //get intersection of current's and child's root sets
                    x.retainAll(y);

                    //The cost of this character is 1 if there is no commonality between the two root sets,
                    //and 0 otherwise
                    if (x.size() == 0) {
                        cost += 1;
                    }
                }

                //Add 0-min-cost edges to list.
                // a 0-cost edge between two labelled nodes should never happen - unless your data set truncates dna
                // sequences of two different species at the same 30 identical characters
                // this check is specifically for that "edge" case...
                if (cost == 0 && !(current.labelled && child.labelled)) {
                    List<Node<S>> newEdge = new ArrayList<>(2);
                    newEdge.add(current);
                    newEdge.add(child);

                    edges.add(newEdge);
                }

                //recursively perform top down algorithm to get all root sets and 0-min-cost edges
                edges.addAll(topDown(child, chars));
            }
        }

        return edges;
    }
}
