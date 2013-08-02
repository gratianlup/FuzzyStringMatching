Fuzzy String Matching
=====================

#### Source code will be released in the near future.  

  
Fuzzy string matching in a dictionary using a Levenshtein Automaton, implemented in Java.  
It retrieves all words that are similar to an incorrect query word.  

Uses a pre-built dictionary (represented by a [Trie](http://en.wikipedia.org/wiki/Trie)) and a DFA built from the query word which accepts candidates  
with at most K edit distance errors (insertion, deletion, substitution). For the most common edit distance (K = 2) it uses an inverted dictionary and parallel search in both dictionaries to reduce the number of tested candidates and substantially increase the speed.
  
More details about the principles and algorithm can be found in the following blog post:  
[Damn Cool Algorithms: Levenshtein Automata](http://blog.notdot.net/2010/07/Damn-Cool-Algorithms-Levenshtein-Automata)
  
A more theoretical presentation of the algorithm can be found in the following article:  
[Fast string correction with Levenshtein automata](http://csi.ufs.ac.za/resres/files/Schultz.pdf)
