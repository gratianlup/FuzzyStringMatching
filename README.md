Fuzzy String Matching
=====================

#### Source code will be released in the near future.  

  
Fuzzy string matching in a dictionary using a Levenshtein Automaton, implemented in Java.  
It retrieves all words that are similar to an incorrect query word. It can be used for spell checking, automatic correction of query words in search engines (Google's "Did you mean: X") and other NLP tasks. Compared to the classic dynamic-programming algorithm for computing the [Levenshtein distance](http://en.wikipedia.org/wiki/Levenshtein_distance), this approach scales very well to dictionaries with more than 2 million words.

Uses a pre-built dictionary (represented by a [Trie](http://en.wikipedia.org/wiki/Trie)) and a [DFA](http://en.wikipedia.org/wiki/Deterministic_finite_automaton) built from the query word which accepts candidates with at most K edit distance errors (insertion, deletion, substitution). For the most common edit distance (K = 2) it uses an inverted-word dictionary and parallel search in both dictionaries to reduce the number of tested candidates and substantially increase the speed.
  
More details about the algorithm can be found in the following blog post:  
[Damn Cool Algorithms: Levenshtein Automata](http://blog.notdot.net/2010/07/Damn-Cool-Algorithms-Levenshtein-Automata)
  
A more theoretical presentation of the algorithm can be found in the following article:  
[Fast string correction with Levenshtein automata](http://csi.ufs.ac.za/resres/files/Schultz.pdf)
