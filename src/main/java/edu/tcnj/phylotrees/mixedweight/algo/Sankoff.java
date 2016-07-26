package edu.tcnj.phylotrees.mixedweight.algo;

import edu.tcnj.phylotrees.mixedweight.data.DNABase;
import edu.tcnj.phylotrees.mixedweight.data.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Sankoff {
    /**
     * Recursive bottom-up method to calculate the costs of each base at a given node
     *
     * @param current the node being worked on currently
     * @param weights the matrix of mutation costs
     * @return
     */
    public static double bottomUp(Node current, double[][] weights) {
        //Recursively go bottom-up (post-order traversal)
        for (Node child : current.children) {
            bottomUp(child, weights);
        }

        //Outside the if statement because magic reasons
        //Reset the node's costs in case bottomUp has been run before
        current.initializeCosts();
        //If the node is not a leaf, score it using sankoff()
        if (current.children.size() >= 1) {
            sankoff(current, weights);
        }
        //Only important for the root: for each character, find the minimum cost,
        //And return the sum of those costs.
        double totalCost = 0;
        for (double[] cost : current.costs) {
            double minCost = Double.MAX_VALUE;
            for (int i = 0; i < cost.length; i++) {
                if (cost[i] < minCost) minCost = cost[i];
            }
            totalCost += minCost;
        }
        return totalCost;
    }

    //Sankoff's algorithm for scoring a given node based on its children's scores and mutation costs
    private static void sankoff(Node current, double[][] weights) {
        //For every child
        for (Node child : current.children) {
            //For every character
            for (int i = 0; i < Node.chars; i++) {
                //For every one of the parent's states
                for (DNABase currentBase : DNABase.values()) {
                    //Find min(current cost + mutation cost) to generate the given parent base
                    double minScore = Double.MAX_VALUE;
                    //used to help with the top down: tracks the child's state(s) that lead to the min cost
                    Set<DNABase> childContributions = new HashSet<>();
                    //For each of the child bases, find (current cost + mutation cost) to see which is min
                    for (DNABase childBase : DNABase.values()) {
                        double score = child.costs.get(i)[childBase.value]
                                + weights[currentBase.value][childBase.value];
                        if (score < minScore) {
                            minScore = score;
                            //If a new lowest score is found, clear the set of bases
                            childContributions.clear();
                        }
                        if (score == minScore) {
                            //If this base has a score of minScore, add it as a potential contribution to the parent
                            childContributions.add(childBase);
                        }
                    }
                    //If the current node does not have Inf as its score (which would mean it's labelled),
                    //add the minScore to this character's cost and set the child's contributions
                    if (!Double.valueOf(Double.POSITIVE_INFINITY).equals(current.costs.get(i)[currentBase.value])) {
                        current.costs.get(i)[currentBase.value] += minScore;
                        child.parentFits.get(i)[currentBase.value] = childContributions;
                    }
                }
            }
        }
    }

    /*FIND 0 min-cost edges:
    For each character, if a state contributed to the same state in a parent,
    and that parent's state contributed to an optimal assignment of the root,
    it is a potential zero-cost edge. If all characters have non-empty sets of potential zero-cost states,
    consider every permutation of sequences built from those sets as a zero min-cost edge.
     */

    /**
     * Top down function to assign sets of possible fits to return zero-cost edges.
     * currently works like hartigan's in terms of finding edges, so the Node.data sets
     * are not particularly useful outside of finding zero-cost edges. This algorithm
     * assumes that the only zero-cost mutations are between a base and itself
     *
     * @param current the current node being worked on
     * @return a list of zero-cost edges
     */
    public static List<List<Node>> topDown(Node current, double[][] weights) {
        List<List<Node>> edges = new ArrayList<>();
        //Special case for the root: assign it any states that had the minimum cost
        if (current.parent == null) {
            for (int i = 0; i < Node.chars; i++) {
                double[] costArr = current.costs.get(i);
                double minCost = Double.POSITIVE_INFINITY;
                for (DNABase base : DNABase.values()) {
                    double cost = costArr[base.value];
                    if (cost < minCost) {
                        minCost = cost;
                        current.data.get(i).clear();
                    }
                    if (cost == minCost) {
                        current.data.get(i).add(base);
                    }
                }
            }
        } else {
            //For everything but the root, assign it any states that lead to any of the parent's states
            int cost = 0;
            current.data = Node.sets();
            for (int i = 0; i < Node.chars; i++) {
                Set<DNABase> parentSet = current.parent.data.get(i);
                for (DNABase dnaBase : parentSet) {
                    current.data.get(i).addAll(current.parentFits.get(i)[dnaBase.value]);
                }
                //Check for zero-cost edges: if the parent and child have some states in common for this character,
                //then the edge could still be zero cost. Otherwise, increase the cost.
                boolean hasPotentialZero = false;
                for (DNABase currentBase : current.data.get(i)) {
                    for (DNABase parentBase : current.parent.data.get(i)) {
                        if (weights[currentBase.value][parentBase.value] == 0.0d) {
                            hasPotentialZero = true;
                            break;
                        }
                    }
                    if (hasPotentialZero) break;
                }

                if (!hasPotentialZero) {
                    cost++;
                }
            }
            //If the edge is zero cost, add it to the list of edges returned.
            //Exceptions: do not count two labelled nodes as a zero cost edge.
            //This shouldn't ever happen, as all species should be
            //unique, unless only part of their dna sequences are used.
            if (cost == 0 && !(current.parent.labelled && current.labelled)) {
                List<Node> newEdge = new ArrayList<>();
                newEdge.add(current.parent);
                newEdge.add(current);

                edges.add(newEdge);
            }
        }
        //Recursively call the algorithm in a top-down (preorder) fashion
        for (Node child : current.children) {
            edges.addAll(topDown(child, weights));
        }
        return edges;
    }
}
