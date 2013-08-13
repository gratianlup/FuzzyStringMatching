// Copyright (c) 2013 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "FuzzyStringMatching" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "FuzzyStringMatching" nor
// may "FuzzyStringMatching" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
import java.io.BufferedWriter;
import java.util.*;

public class FuzzyMatching {
    // Represents an execution point in the fuzzy matching algorithm.
    private static class ExecutionState {
        public Trie trieNode;      // The reached trie node.
        public State state;        // The reached state in the automaton.
        public String matchedWord; // The word built up to this point.

        public ExecutionState() { }

        public ExecutionState(Trie trieNode, State state, String matchedWord) {
            this.trieNode = trieNode;
            this.state = state;
            this.matchedWord = matchedWord;
        }
    }


    private Trie trie;
    private Trie reversedTrie;
    private int maxError;
    private AutomatonCache cache;

    public FuzzyMatching(Trie trie, Trie reversedTrie, int maxError, AutomatonCache cache) {
        this.trie = trie;
        this.reversedTrie = reversedTrie;
        this.maxError = maxError;

        if(reversedTrie != null && maxError != 2) {
            throw new IllegalArgumentException("Reveresed-word dictionary can be used " +
                                               "only with maximum error of two!");
        }
    }

    public FuzzyMatching(Trie trie, Trie reversedTrie, int maxError) {
        this(trie, reversedTrie, maxError, null);
    }

    public FuzzyMatching(Trie trie, int maxError, AutomatonCache cache) {
        this(trie, null, maxError, cache);
    }

    public FuzzyMatching(Trie trie, int maxError) {
        this(trie, maxError, null);
    }

    private LevenshteinAutomaton createAutomaton(String word, int maxError) {
        // Check if the automaton has previously been requested and still
        // is available in the cache. Caching the automaton (instead of the found words)
        // is useful when using the reversed-word dictionary because many words share
        // the same first or second half, which must not be recreated.
        if(cache != null) {
            LevenshteinAutomaton automaton = cache.get(word, maxError);

            if(automaton != null) {
                return automaton;
            }
        }

        LevenshteinAutomaton automaton = null;
        automaton = new LevenshteinAutomaton(maxError);
        automaton.buildNFA(word);
        automaton.convertToDFA();

        // Cache the automaton for subsequent requests.
        if(cache != null) {
            cache.add(automaton, word, maxError);
        }

        return automaton;
    }

    private String reverseWord(String word) {
        return new StringBuilder(word).reverse().toString();
    }

    private Trie findMatchingState(String word, Trie trie) {
        Trie trieNode = trie;
        int position = 0;

        while((position < word.length()) && (trieNode != null)) {
            char letter = word.charAt(position);
            trieNode = trieNode.getChildForLetter(letter);
            position++;
        }

        if((position == word.length()) && (trieNode != null)) {
            return trieNode;
        }
        else return null;
    }

    private void findFuzzyStates(Trie state, LevenshteinAutomaton automaton,
                                 List<Trie> fuzzyStates, List<String> fuzzyWords) {
        List<ExecutionState> worklist = new ArrayList<ExecutionState>();
        ExecutionState startState = new ExecutionState(state, automaton.startState, "");
        worklist.add(startState);

        while(!worklist.isEmpty()) {
            ExecutionState currentState = worklist.remove(worklist.size() - 1);

            for(Map.Entry<Character, Trie> child : currentState.trieNode.getChildren().entrySet()) {
                char trieLetter = child.getKey();
                Trie trieChild = child.getValue();
                State nextState = currentState.state.getStateForLetter(trieLetter);

                if(nextState != null) {
                    String newWord = currentState.matchedWord + trieLetter;
                    ExecutionState newState = new ExecutionState(trieChild, nextState, newWord);
                    worklist.add(newState);

                    if(trieChild.isTerminator() && nextState.isFinal) {
                        fuzzyStates.add(trieChild);
                        fuzzyWords.add(newWord);
                    }
                }
            }
        }
    }

    private List<String> findFuzzyWords(Trie state, LevenshteinAutomaton automaton) {
        List<String> matchingWords = new ArrayList<String>();
        List<ExecutionState> worklist = new ArrayList<ExecutionState>();
        ExecutionState startState = new ExecutionState(state, automaton.startState, "");
        worklist.add(startState);

        while(!worklist.isEmpty()) {
            ExecutionState currentState = worklist.remove(worklist.size() - 1);

            for(Map.Entry<Character, Trie> child : currentState.trieNode.getChildren().entrySet()) {
                char trieLetter = child.getKey();
                Trie trieChild = child.getValue();
                State nextState = currentState.state.getStateForLetter(trieLetter);

                if(nextState != null) {
                    String newWord = currentState.matchedWord + trieLetter;
                    ExecutionState newState = new ExecutionState(trieChild, nextState, newWord);
                    worklist.add(newState);

                    if(trieChild.isTerminator() && nextState.isFinal) {
                        matchingWords.add(newWord);
                    }
                }
            }
        }

        return matchingWords;
    }

    private List<String> findMatchingWordsSplit(String word) {
        // See the second half of "Fast string correction with Levenshtein automata"
        // by K. Schulz, S. Mihov for a detailed explanation of the concept of using
        // a dictionary with inverted words to greatly reduce search time.
        List<String> matchingWords = new ArrayList<String>();
        String wordA = word.substring(0, word.length() / 2);
        String wordB = word.substring(word.length() / 2, word.length());
        String reversedWordA = reverseWord(wordA);
        String reversedWordB = reverseWord(wordB);

        // Case 1: Error(wordA) == 0 && Error(wordB) <= 2
        //    =>   Exact(wordA) && Fuzzy2(wordB)
        Trie matchingStateA = findMatchingState(wordA, trie);

        if(matchingStateA != null) {
            LevenshteinAutomaton wordAutomatonB = createAutomaton(wordB, 2);
            List<String> fuzzyWords = findFuzzyWords(matchingStateA, wordAutomatonB);

            for(String fuzzyWord : fuzzyWords) {
                matchingWords.add(wordA + fuzzyWord);
            }
        }

        // Case 2: Error(wordB) == 0 && 1 <= Error(wordA) <= 2
        //    =>   Exact(reversedWordB) && Fuzzy2(wordA)
        Trie reversedMatchingStateB = findMatchingState(reversedWordB, reversedTrie);

        if(reversedMatchingStateB != null) {
            LevenshteinAutomaton reversedWordAutomatonA = createAutomaton(reversedWordA, 2);
            List<String> fuzzyWords = findFuzzyWords(reversedMatchingStateB, reversedWordAutomatonA);

            for(String fuzzyWord : fuzzyWords) {
                matchingWords.add(reverseWord(fuzzyWord) + wordB);
            }
        }

        // Case 3: Error(wordA) == 1 && Error(wordB) == 1
        //    =>   Fuzzy1(wordA) && Fuzzy2(wordB)
        List<Trie> fuzzyStatesA = new ArrayList<Trie>();
        List<String> fuzzyWordsA = new ArrayList<String>();
        LevenshteinAutomaton wordAutomatonA = createAutomaton(wordA, 1);
        findFuzzyStates(trie, wordAutomatonA, fuzzyStatesA, fuzzyWordsA);

        if(fuzzyStatesA.size() > 0) {
            LevenshteinAutomaton wordAutomatonB = createAutomaton(wordB, 1);

            for(int i = 0; i < fuzzyStatesA.size(); i++) {
                Trie fuzzyStateA = fuzzyStatesA.get(i);
                String fuzzyWordA = fuzzyWordsA.get(i);
                List<String> fuzzyWordsB = findFuzzyWords(fuzzyStateA, wordAutomatonB);

                for(String fuzzyWordB : fuzzyWordsB) {
                    matchingWords.add(fuzzyWordA + fuzzyWordB);
                }
            }
        }

        return matchingWords;
    }

    public List<String> findMatchingWords(String word) {
        if(reversedTrie != null) {
            // If a reversed-word dictionary is used the search can be made
            // much more efficient by using exact search at the head/tail of the word.
            return findMatchingWordsSplit(word);
        }

        // Create an automaton accepting the word and start the search
        // in both the automaton and the trie, keeping them synchronized.
        // A transition is taken only if the automaton allows it
        // and the trie has a child state for the associated letter.
        LevenshteinAutomaton automaton = createAutomaton(word, maxError);
        List<String> matchingWords = new ArrayList<String>();

        List<ExecutionState> worklist = new ArrayList<ExecutionState>();
        ExecutionState startState = new ExecutionState(trie, automaton.startState, "");
        worklist.add(startState);

        while(!worklist.isEmpty()) {
            ExecutionState currentState = worklist.remove(worklist.size() - 1);

            for(Map.Entry<Character, Trie> child : currentState.trieNode.getChildren().entrySet()) {
                // Check which of the possible letters are accepted by the automaton.
                char trieLetter = child.getKey();
                Trie trieChild = child.getValue();
                State nextState = currentState.state.getStateForLetter(trieLetter);

                if(nextState != null) {
                    // Add the next state/trie pair to the worklist as a candidate.
                    String newWord = currentState.matchedWord + trieLetter;
                    ExecutionState newState = new ExecutionState(trieChild, nextState, newWord);
                    worklist.add(newState);

                    if(trieChild.isTerminator() && nextState.isFinal) {
                        // Found a final state/trie pair, remember the word.
                        matchingWords.add(newWord);
                    }
                }
            }

        }

        return matchingWords;
    }
}
