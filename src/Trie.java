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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {
    private String terminatorWord;
    private Map<Character, Trie> children;

    public Trie() {
        this.children = new HashMap<Character, Trie>();
    }

    public boolean isTerminator() {
        return terminatorWord != null;
    }

    public String getWord() {
        return terminatorWord;
    }

    public Map<Character, Trie> getChildren() {
        return children;
    }

    public Trie getChildForLetter(char letter) {
        for(Map.Entry<Character, Trie> entry : children.entrySet()) {
            if(entry.getKey() == letter) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void addWordHelper(String word, int position) {
        if(position < word.length()) {
            // Add a child reachable from an edge labeled by the current
            // letter, then continue with the rest of the word.
            Character letter = word.charAt(position);
            Trie child = children.get(letter);

            if(child == null) {
                child = new Trie();
                children.put(letter, child);
            }

            child.addWordHelper(word, position + 1);
        }
        else {
            // No letters left, mark the terminal node.
            this.terminatorWord = word;
        }
    }

    public void addWord(String word) {
        if(word == null) {
            throw new IllegalArgumentException("Word to add is null");
        }

        addWordHelper(word, 0);
    }

    private int findWordHelper(String word, int position) {
        // Check if the current letter is found on one of the edges leaving
        // from this node. If it is, continue searching with the rest of the word.
        int maxPosition = isTerminator() ? position : 0;

        if(position < word.length()) {
            Character letter = word.charAt(position);
            Trie child = children.get(letter);

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

        for(Map.Entry<Character, Trie> entry : children.entrySet()) {
            findSimilarWordsImpl(pattern, maxError,
                                 entry.getKey(), entry.getValue(),
                                 row, similarWords);
        }

        return similarWords;
    }

    private void findSimilarWordsImpl(String pattern, int maxError, char letter, Trie node,
                                      int[] previousRow, List<String> similarWords) {
        int[] currentRow = new int[pattern.length() + 1];
        currentRow[0] = previousRow[0] + 1; // Compared to the empty string.
        int minError = currentRow[0];

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
            similarWords.add(node.getWord());
        }

        // Process the children if valid words could still be found.
        if(minError <= maxError) {
            for(Map.Entry<Character, Trie> entry : node.getChildren().entrySet()) {
                findSimilarWordsImpl(pattern, maxError,
                                     entry.getKey(), entry.getValue(),
                                     currentRow, similarWords);
            }
        }
    }
}
