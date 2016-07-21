package edu.tcnj.phylotrees.algo;

import edu.tcnj.phylotrees.Parser;
import edu.tcnj.phylotrees.data.Node;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@Ignore
@RunWith(Parameterized.class)
public class CubicTreeEnumeratorTest extends TreeEnumeratorTest {

    @Parameterized.Parameters
    public static Iterable<? extends Object> data() {
        return Arrays.asList(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);
    }

    @Parameterized.Parameter
    public int treeSize = 0;

    @Test
    public void testEnumerator() {
        List<Node<Integer>> treeNodes = new ArrayList<>();
        CubicTreeEnumerator<Integer> treeEnumerator;
        for (int i = 1; i <= treeSize; i++) {
            treeNodes.add(new Node<Integer>(((Integer) i).toString()));
        }
        treeEnumerator = new CubicTreeEnumerator<>(new ArrayList<>(treeNodes));
        int expectedScore = 1;
        for (int j = 2 * treeSize - 5; j > 0; j -= 2) {
            expectedScore *= j;
        }
//        Parser parser = new Parser();
//        if (i == 1) expectedScore = 1;
//        for (Node<Integer> tree : treeList) {
//            System.out.println(parser.toString(tree, false));
//        }
        assertEquals("Size: " + treeNodes.size(), expectedScore, treeEnumerator.enumerate());
    }

    @Test
    public void testFitch() {
        long start = System.currentTimeMillis();
        getData(treeSize);
        Parser parser = new Parser();
        CubicTreeEnumerator<Character> treeEnumerator = new CubicTreeEnumerator<>(species);
        Set<Node<Character>> treeList = treeEnumerator.fitchEnumerate();
//        System.out.println("Fitch enumerate: ");
        for (Node<Character> tree : treeList) {
            System.out.println(parser.toString(tree, false) + " Score: " + Fitch.bottomUp(tree));
            EdgeContractor<Character> edgeContractor = new EdgeContractor<>(worldSet);
            System.out.println("Old size: " + tree.size() + ", new size: " + edgeContractor.edgeContraction(tree).size());
        }
        System.out.println("Took " + (System.currentTimeMillis() - start) + "ms for trees of size " + treeSize + ".");
    }

    @Test
    public void testHartigan() {
        getData(treeSize);
        CubicTreeEnumerator<Character> treeEnumerator = new CubicTreeEnumerator<>(species, worldSet);
        Set<Node<Character>> treeList = treeEnumerator.hartiganEnumerate();
        Parser parser = new Parser();
        System.out.println("Hartigan enumerate: ");
        for (Node<Character> tree : treeList) {
            System.out.println(parser.toString(tree, false) + " Score: " + Hartigan.bottomUp(tree, worldSet));
        }
    }


}
