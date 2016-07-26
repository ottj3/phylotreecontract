package edu.tcnj.phylotrees.mixedweight.algo;

import edu.tcnj.phylotrees.mixedweight.data.Node;

import java.util.List;

/**
 * A class to contract a given tree to its smallest possible size
 * by removing any edges with a minimum cost of 0, and trying all
 * orderings of edge removal to get the smallest possible tree
 * with the same parsimony score as the parent.
 */
public class EdgeContractor {
    //The smallest size of a tree seen yet
    private int bestSize = Integer.MAX_VALUE;
    //The latest tree to be seen of the smallest size
    private Node bestTree;
    //(Used for Sankoff) the cost of each mutation
    private double[][] weights;

    public EdgeContractor(double[][] weights) {
        this.weights = weights;
    }

    /**
     * A method to contract a cubic tree into the smallest possible mixed tree
     * by contracting edges in every possible order to find the smallest
     *
     * @param root the root of the tree to be compacted
     * @return the root of the compacted tree
     */
    public Node edgeContraction(Node root) {
        bestSize = Integer.MAX_VALUE;
        Sankoff.bottomUp(root, weights);
        edgeContractionRecursive(root);
        return bestTree;
    }

    private void edgeContractionRecursive(Node root) {
        //get list of zero-cost edges
        List<List<Node>> edgeList = Sankoff.topDown(root, weights);
        //bound the method: if the tree can never become the most compact, break out of recursion
        if (root.size() - edgeList.size() >= bestSize) {
            return;
        }
//        if (edgeList.size() + 1 > oldSize) {
//            System.out.println("Made more 0-cost edges than we had before.");
//        }
        //if there are no 0 cost edges, edge contraction is done
        if (edgeList.size() == 0) {
            int treeSize = root.size();
            //If the tree size is at least as compact as the best seen so far, set it as bestSize and bestTree
            if (treeSize <= bestSize) {
                bestSize = treeSize;
                bestTree = root.clone();
            }
        } else {
            //else, for every edge in list, contract edge and then recurse
            for (List<Node> edge : edgeList) {
                contractEdge(edge);

                edgeContractionRecursive(root);

                //undo the contraction to try a different order
                uncontractEdge(edge);
            }
        }
    }


    private void contractEdge(List<Node> edge) {
        //Given an edge (parent, child), contract the edge between them
        Node parent = edge.get(0);
        Node child = edge.get(1);
        //Forward the child's children to the parent
        for (Node childsChild : child.children) {
            Node.linkNodes(parent, childsChild);
        }
        //Remove the child (child will retain all of its data, but will be disconnected from the tree)
        Node.unlinkNodes(parent, child);
        //Special case: if the child is labelled, the contracted node should take on its label and values
        if (child.labelled) {
            parent.labelled = true;
            parent.label = child.label;
            parent.costs = child.costs;
            parent.data = child.data;
        }

        //Find the root, call bottom up to fix all the scores/parentFits
        while (parent.parent != null) {
            parent = parent.parent;
        }
        Sankoff.bottomUp(parent, weights);
    }

    private void uncontractEdge(List<Node> edge) {
        //Undo an edge contraction to try the possible edge contractions in a different order
        Node parent = edge.get(0);
        Node child = edge.get(1);
        for (Node childsChild : child.children) {
            //remove all the added children of parent
            Node.unlinkNodes(parent, childsChild);
            //child.children has not changed, but the childsChild parent is null after unlink so it needs to be reset
            childsChild.parent = child;
        }
        //Restore the connection between parent and child
        Node.linkNodes(parent, child);
        //An edge contraction cannot occur between two labelled node (as a labelled node
        //represents a species; no two unique species would have the same character states).
        //So, if the child was labelled, the parent must not have been before contraction.
        if (child.labelled) {
            parent.labelled = false;
            parent.label = "";
            parent.data = Node.sets();
            parent.costs = null;
        }

        //Find the root, call bottom up to fix all the scores/parentFits
        while (parent.parent != null) {
            parent = parent.parent;
        }
        Sankoff.bottomUp(parent, weights);
    }
}
