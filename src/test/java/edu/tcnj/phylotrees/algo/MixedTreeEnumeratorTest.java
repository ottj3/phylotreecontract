package edu.tcnj.phylotrees.algo;

import edu.tcnj.phylotrees.Parser;
import edu.tcnj.phylotrees.data.CharacterList;
import edu.tcnj.phylotrees.data.Node;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RunWith(Parameterized.class)
public class MixedTreeEnumeratorTest extends TreeEnumeratorTest {
    @Parameterized.Parameters
    public static Iterable<? extends Object> data() {
        return Arrays.asList(2, 3, 4, 5);//, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);
    }

    @Parameterized.Parameter
    public int treeSize = 0;

    @Ignore
    @Test
    public void testMixedEnumerator() {
//        CharacterList<Integer> worldSet = new CharacterList<>();
//        worldSet.add(new HashSet<Integer>());
        List<Node<Integer>> treeNodes = new ArrayList<>();
        MixedTreeEnumerator<Integer> treeEnumerator;

        for (int i = 1; i <= treeSize; i++) {
            treeNodes.add(new Node<Integer>(((Integer) i).toString()));
//            worldSet.get(0).add(i);
        }
        treeEnumerator = new MixedTreeEnumerator<>(new ArrayList<>(treeNodes), new CharacterList<>(), 0);
        double denominator = Math.sqrt(2) * Math.exp(treeSize / 2) * Math.pow((2 - Math.exp(0.5)), treeSize - 1.5);
        double expectedSize = Math.pow(treeSize, treeSize - 2) / denominator;
        //if (i == 1) expectedScore = 1;
        int treeListSize = treeEnumerator.enumerate();

//        Parser parser = new Parser();
//        for (Node<Integer> tree : treeList) {
//            System.out.println(parser.toString(tree, false));
//        }
        //System.out.println("Size: " + treeEnumerator.completed + " Expected: " + expectedSize);
    }

    @Test
    public void testHartigan() {
        long start = System.currentTimeMillis();
        getData(treeSize);
        int chars = species.iterator().next().root.size();
        Parser parser = new Parser();
        MixedTreeEnumerator<Character> treeEnumerator = new MixedTreeEnumerator<>(species, worldSet, chars);
        Set<Node<Character>> treeList = treeEnumerator.hartiganEnumerate();
//        System.out.println("Hartigan enumerate: ");
        for (Node<Character> tree : treeList) {
            System.out.println(parser.toString(tree, false) + " Score: " + Hartigan.bottomUp(tree, worldSet, chars) + " Size: " + tree.size());
        }
        System.out.println("Took " + (System.currentTimeMillis() - start) + "ms for trees of size " + treeSize + ".");
    }

}
