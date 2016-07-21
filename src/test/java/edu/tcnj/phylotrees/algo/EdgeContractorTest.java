package edu.tcnj.phylotrees.algo;

import edu.tcnj.phylotrees.Parser;
import edu.tcnj.phylotrees.data.CharacterList;
import edu.tcnj.phylotrees.data.Node;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

@Ignore
public class EdgeContractorTest {

    private Parser parser = new Parser();

    @Test
    public void testContraction() {
        int NUM_TRIALS = 10;
        ExecutorService executorService = Executors.newSingleThreadExecutor(); // change to newFixedThreadPool(x) for parallel processing on x cores
        List<Callable<long[]>> callables = new ArrayList<>();
        List<Future<long[]>> futures;
        int[] treeSizes = {4, 5, 6, 7, 8/*, 9, 10, 11, 12, 13, 14, 15*/};

        List<List<String>> dataPerTrial = new ArrayList<>();
        for (int i = 0; i < NUM_TRIALS; i++) {
            Collections.shuffle(TreeEnumeratorTest.testData);
            dataPerTrial.add(new ArrayList<>(TreeEnumeratorTest.testData));
        }

        for (final int treeSize : treeSizes) {
            for (int i = 0; i < NUM_TRIALS; i++) {
                final int trialNum = i;
                final CharacterList<Character> worldSet;
                final List<Node<Character>> species = new ArrayList<>();
                List<Set<Character>> worldSet0 = new ArrayList<>();
                parser.speciesList(dataPerTrial.get(i).subList(0, treeSize), species, worldSet0);
                worldSet = new CharacterList<>(worldSet0);
                callables.add(new Callable<long[]>() {
                    @Override
                    public long[] call() throws Exception {
                        return runMixed(species, worldSet, trialNum);
                    }
                });

                final CharacterList<Character> worldSet1;
                final List<Node<Character>> species0 = new ArrayList<>();
                List<Set<Character>> worldSet2 = new ArrayList<>();
                parser.speciesList(dataPerTrial.get(i).subList(0, treeSize), species0, worldSet2);
                worldSet1 = new CharacterList<>(worldSet2);
                callables.add(new Callable<long[]>() {
                    @Override
                    public long[] call() throws Exception {
                        return runCubic(species0, worldSet1, trialNum);
                    }
                });
            }

        }

        try {
            futures = executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("n\tmixed time\tmixed stddev\tcubic time\tcubic stddev\t# compact mixed\t# mp cubic\t# compact\t# contractions");
        long[][] totalResults = new long[treeSizes.length][7];
        long[][][] deviation = new long[treeSizes.length][NUM_TRIALS][2];
        for (int i = 0; i < futures.size(); i += 2) {
            long[] res1;
            long[] res2;
            try {
                res1 = futures.get(i).get(); //{n, mixed time, # compact mixed}
                res2 = futures.get(i + 1).get(); //{cubic time, # mp cubic, # compact, # contractions}
                int treeSize = (int) res1[0];
                int index = treeSize - treeSizes[0];

                //I'm so sorry.
                totalResults[index][0] += treeSize; //n
                totalResults[index][1] += res1[1]; //mixed time
                deviation[index][(i / 2) % NUM_TRIALS][0] = res1[1];
                totalResults[index][2] += res2[0]; //cubic time
                deviation[index][(i / 2) % NUM_TRIALS][1] = res2[0];
                totalResults[index][3] += res1[2]; //# compact mixed
                totalResults[index][4] += res2[1]; //# mp cubic
                totalResults[index][5] += res2[2]; //# compact
                totalResults[index][6] += res2[3]; //# contractions
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < deviation.length; i++) {
            long mixedDeviation = 0;
            long cubicDeviation = 0;
            //            System.out.println("Size: " + (i+treeSizes[0]));
            for (int j = 0; j < deviation[i].length; j++) {
                //                System.out.print(deviation[i][j][0] + " " + deviation[i][j][1] + "\n");
                deviation[i][j][0] -= totalResults[i][1] / NUM_TRIALS;
                mixedDeviation += Math.pow(deviation[i][j][0], 2);
                deviation[i][j][1] -= totalResults[i][2] / NUM_TRIALS;
                cubicDeviation += Math.pow(deviation[i][j][1], 2);
            }
            mixedDeviation /= NUM_TRIALS;
            cubicDeviation /= NUM_TRIALS;
            deviation[i][0][0] = (long) Math.sqrt(mixedDeviation);
            deviation[i][0][1] = (long) Math.sqrt(cubicDeviation);
        }
        for (int i = 0; i < totalResults.length; i++) {
            System.out.print(Math.round((float) totalResults[i][0] / NUM_TRIALS) + "\t");
            for (int i1 = 1; i1 < 3; i1++) {
                System.out.print(Math.round((float) totalResults[i][i1] / NUM_TRIALS) + "\t");
                System.out.print(deviation[i][0][i1 - 1] + "\t");
            }
            for (int i1 = 3; i1 < totalResults[i].length; i1++) {
                System.out.print(((float) totalResults[i][i1] / NUM_TRIALS) + "\t");
            }
            System.out.println();
        }
    }

    public long[] runMixed(List<Node<Character>> species, CharacterList<Character> worldSet, int trialNum) {
        long before = System.currentTimeMillis();
        MixedTreeEnumerator<Character> treeEnumerator = new MixedTreeEnumerator<>(species, worldSet);
        Set<Node<Character>> mostParsimonious = treeEnumerator.hartiganEnumerate();
        Set<Node<Character>> mostCompact = new HashSet<>();
        int mostCompactSize = -1;
        for (Node<Character> tree : mostParsimonious) {
            int thisSize = tree.size();
            if (thisSize <= mostCompactSize || mostCompactSize == -1) {
                if (tree.size() < mostCompactSize) {
                    mostCompact.clear();
                }
                mostCompact.add(tree);
                mostCompactSize = thisSize;
            }
        }

        long time = System.currentTimeMillis() - before;
        System.out.println("Mixed #" + species.size() + "-" + trialNum + "\t" + species.size() + "\t" + time + "\t" + mostCompact.size());
        return new long[]{species.size(), time, mostCompact.size()};
    }

    public long[] runCubic(List<Node<Character>> species, CharacterList<Character> worldSet, int trialNum) {
        long before = System.currentTimeMillis();
        CubicTreeEnumerator<Character> treeEnumerator = new CubicTreeEnumerator<>(species);
        Set<Node<Character>> mostParsimonious = treeEnumerator.fitchEnumerate();
        List<Node<Character>> mostCompact = new ArrayList<>();
        int numContractions = 0;
        int mostCompactSize = Integer.MAX_VALUE;
        int initialSize = mostParsimonious.iterator().next().size();
        for (Node<Character> tree : mostParsimonious) {
            EdgeContractor<Character> edgeContractor = new EdgeContractor<>(worldSet);
            for (Node<Character> compactTree : edgeContractor.edgeContraction(tree)) {
                int thisSize = compactTree.size();
                if (thisSize <= mostCompactSize) {
                    if (compactTree.size() < mostCompactSize) {
                        mostCompact.clear();
                    }
                    mostCompact.add(compactTree);
                    numContractions = initialSize - compactTree.size();
                    mostCompactSize = thisSize;
                }
            }
        }

        long time = System.currentTimeMillis() - before;
        System.out.println("Cubic #" + species.size() + "-" + trialNum + "\t" + species.size() + "\t" + time + "\t" + mostParsimonious.size() +
                "\t" + mostCompact.size() + "\t" + numContractions);
        return new long[]{time, mostParsimonious.size(), mostCompact.size(), numContractions};
    }
}
