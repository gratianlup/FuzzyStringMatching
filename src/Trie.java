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
import java.util.ArrayList;
import java.util.List;

public abstract class Trie {
    // Used to store the <letter, child> pairs
    // in a format independent from the actual Trie implementation.
    public static class TrieChildren {
        private char[] letters;
        private Trie[] children;

        public TrieChildren(int capacity) {
            letters = new char[capacity];
            children = new Trie[capacity];
        }

        public int size() {
            return letters.length;
        }

        public void putPair(int index, char letter, Trie child) {
            letters[index] = letter;
            children[index] = child;
        }

        public char getLetter(int index) {
            return letters[index];
        }

        public Trie getChild(int index) {
            return children[index];
        }

        public Trie getChildForLetter(char letter) {
            for(int i = 0; i < letters.length; i++) {
                if(letters[i] == letter) {
                    return children[i];
                }
            }

            return null;
        }
    }

    // Builds a Trie contatining all specified words.
    public abstract void addWords(List<String> words);

    // Retrieves the children of the Trie node.
    public abstract TrieChildren getChildren();

    public abstract boolean isTerminator();

    // Returns the Trie node assocaited with the specified letter,
    // or null if such a node does not exist.
    public Trie getChildForLetter(char letter) {
        TrieChildren children = getChildren();
        return children.getChildForLetter(letter);
    }

    private int findWordHelper(String word, int position) {
        // Check if the current letter is found on one of the edges leaving
        // from this node. If it is, continue searching with the rest of the word.
        int maxPosition = isTerminator() ? position : 0;

        if(position < word.length()) {
            Character letter = word.charAt(position);
            Trie child = getChildForLetter(letter);

            if(child != null) {
                // Continue searching with the rest of the word and select
                // the longest prefix in case this node is a terminal node.
                maxPosition = Math.max(maxPosition, child.findWordHelper(word, position + 1));
            }
        }

        return maxPosition;
    }

    public int findWord(String word) {
        if(word == null) {
            throw new IllegalArgumentException("Word to be searched is null");
        }

        return findWordHelper(word, 0);
    }

    public List<String> findSimilarWords(String pattern, int maxError) {
        // Implements a fairly simple search methods that runs
        // the classic dynamic programming directly on the Trie.
        List<String> similarWords = new ArrayList<String>();
        int[] row = new int[pattern.length() + 1];

        for(int i = 0; i <= pattern.length(); i++) {
            row[i] = i;
        }

        // Walk each branch starting from the root node.
        List<Character> wordLetters = new ArrayList<Character>();
        TrieChildren children = getChildren();

        for(int i = 0; i < children.size(); i++) {
            findSimilarWordsImpl(pattern, maxError,
                                 children.getLetter(i), children.getChild(i),
                                 wordLetters, row, similarWords);
        }

        return similarWords;
    }

    private void findSimilarWordsImpl(String pattern, int maxError, char letter, Trie node,
                                      List<Character> wordLetters, int[] previousRow,
                                      List<String> similarWords) {
        int[] currentRow = new int[pattern.length() + 1];
        int minError = previousRow[0] + 1;
        currentRow[0] = previousRow[0] + 1; // Compared to the empty string.
        wordLetters.add(letter); // Append current letter to candidate.

        for(int i = 1; i <= pattern.length(); i++) {
            int insertionConst = currentRow[i - 1] + 1;
            int deletionCost = previousRow[i] + 1;
            int substitutionCost = previousRow[i - 1] +
                                   (pattern.charAt(i - 1) == letter ? 0 : 1);
            currentRow[i] = Math.min(insertionConst, Math.min(deletionCost, substitutionCost));
            minError = Math.min(minError, currentRow[i]);
        }

        // Check if an accepted word has been found.
        if(currentRow[pattern.length()] <= maxError && node.isTerminator()) {
            similarWords.add(buildWord(wordLetters));
        }

        // Process the children if valid words could still be found.
        if(minError <= maxError) {
            TrieChildren children = node.getChildren();

            for(int i = 0; i < children.size(); i++) {
                findSimilarWordsImpl(pattern, maxError,
                                     children.getLetter(i), children.getChild(i),
                                     wordLetters, currentRow, similarWords);
            }
        }

        wordLetters.remove(wordLetters.size() - 1);
    }
    
    private String buildWord(List<Character> letters) {
        StringBuilder builder = new StringBuilder(letters.size());

        for(char letter : letters) {
            builder.append(letter);
        }

        return builder.toString();
    }
}
