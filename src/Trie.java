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
import java.util.HashMap;
import java.util.Map;

public class Trie {
    private boolean isTerminator;
    private Map<Character, Trie> children;

    public Trie() {
        this.isTerminator = false;
        this.children = new HashMap<Character, Trie>();
    }

    public boolean isTerminator() {
        return isTerminator;
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
            isTerminator = true;
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
        int maxPosition = isTerminator ? position : 0;

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
}
