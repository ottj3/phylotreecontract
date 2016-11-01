# Phylogenetic Tree Contractor


### Building

You must have JDK version 7 or newer installed to use this.
To build, run ```./gradlew build``` for *nix, or ```gradlew.bat build``` for Windows.

The completed java program will be in ```build/libs/``` and can be run with ```java -jar phylotreecontract-all.jar```. (Make sure you use the jar with ```-all```.)

You can also find a prebuilt jar in the ```example``` folder, along with a sample _species.txt_.

### Usage

Upon starting the program, you should be presented with a series of prompts.

The first prompt will ask you if you want to use simple or mixed weights. Simple weights assume that every mutation of a character state has the same cost (namely, 1), and works with Fitch's and Hartigan's algorithms to calculate tree parsimony. Mixed weights allow a cost matrix to be input to assign costs between each pair of potential state changes, and uses Sankoff's algorithm.
The caveat with mixed weights is that the cost matrix can, at the moment, only be specified in terms on DNA bases, so character states are restricted to A/C/T/G. Simple weights, on the other hand, support any number and any format of character states for each character.

The second prompt will ask you what you want to do. There are three options.

1. Find the best tree from a set of input species.

  This option will read in a list of species, enumerate (exhaustively, but branch-and-bounding bad trees) the most parsimonious cubic trees, then compact them into mixed-labelled, multifurcating trees as applicable. For large sizes of input, this can be slow and option 2 might be better.
2. Compact existing MP trees to their most compact form.

  This option will read in a list of species as above, but instead of enumerating cubic trees, it will also read in a list of trees to compact. For medium input sizes, programs (such as PAUP* or phylip) which are much more optimized can generally enumerate cubic trees much faster than this program (in the aformentioned programs, via BandB and dnapenny, respectively). For even larger input sizes, heuristic searches (HSearch/dnapars, respectively) may be more effective to find (approximate) MP cubic trees.
3. Enumerate mixed-labelled/multifurcating trees AND cubic trees, and compare times.

  This option will read in a list of species as in option 1, but in addition will also enumerate mixed-labelled, multifurcating trees instead of just the cubic trees. This is just to be used as a comparison  and correctness check of the algorithms, and will be very slow for larger data sets (generally starting around 9-10 species).

There are three main files that are used as input.

1. _species.txt_

  This is the main input for all modes of operation. It is a list (one per line) of the input species, formatted as ```1:ABCDEF...``` where ```1``` is the label for each species, ```A``` the state of the first character, ```B``` the state of the second character, and so on. The label can be any length, but cannot include ```:```. The states may only be one textual character each, but can be any number, letter, or symbol. For example:
  ```
  A:GAGGACCCCAGATATTACGCGGGTCGAACA
  B:GAAGATCCCAGATACTTTGCCGGAGAACAA
  C:GAGGATCCGCGTTACTTTAGCGGTATTCAA
  D:GAGGACCCCCGTTACTTTGCCGGCGAGGCC
  ```
2. _trees.txt_

  When using the second option, "compact existing trees", the input trees will be read from this file. Input trees, one per line, each line ending with a semicolon, must be in Newick format, and the labels in the trees must match the labels in the _species.txt_ file. For example:
  ```
  ((B,C),A,D);
  ((A,D),B,C);
  ((C,D),A,B);
  ((A,B),C,D);
  ```
3. _weights.txt_

  When using any of the options with mixed weights, the mutation cost matrix will be read from this file. It must be four lines long, each line must be 4 numbers (which can be decimal), separated by a single space each, indicating the cost between the 4 bases, in order: A G T C. For example:
  ```
  0 1 2.5 2.5
  1 0 2.5 2.5
  2.5 2.5 0 1
  2.5 2.5 1 0
  ```
  Note that a table that has non-symmetric costs, or non-zero costs for no change in state, may produce unexpected results.
