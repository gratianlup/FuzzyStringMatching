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
import java.util.Collections;
import java.util.List;

public class CompactTrie extends Trie {
    // Used to create links into the main Trie 
    // which hols the actual node and edge data.
    public class CompactTrieProxy extends Trie {
        private CompactTrie trie;
        private int nodeId;

        public CompactTrieProxy(CompactTrie trie, int nodeId) {
            this.trie = trie;
            this.nodeId = nodeId;
        }

        @Override
        public void addWords(List<String> words) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TrieChildren getChildren() {
            return trie.getChildren(nodeId);
        }

        @Override
        public boolean isTerminator() {
             return trie.isTerminator(nodeId);
        }
    }

    // The entire trie is stored in one place using simple arrays.
    // This representation is much more compact, and also is
    // at least twice as fast, due to abetter cache usage.
    private int[] firstChildren;
    private byte[] childrenCount;
    private boolean[] terminatorNodes;
    private char[] childrenLetters;
    private int[] children;
    private int lastNodeId;
    private int lastChildId;

    public CompactTrie() {
        firstChildren = new int[1024];
        childrenCount = new byte[1024];
        terminatorNodes = new boolean[1024];
        children = new int[1024];
        childrenLetters = new char[1024];
        lastNodeId = 0;
        lastChildId = 0;
    }

    private void resizeNodeInfoIfRequired() {
        if(lastNodeId == firstChildren.length - 1) {
            int[] newFirstChildren = new int[firstChildren.length * 2];
            System.arraycopy(firstChildren, 0, newFirstChildren, 0, firstChildren.length);
            firstChildren = newFirstChildren;

            byte[] newChildrenCount = new byte[childrenCount.length * 2];
            System.arraycopy(childrenCount, 0, newChildrenCount, 0, childrenCount.length);
            childrenCount = newChildrenCount;

            boolean[] newTerminatorNodes = new boolean[terminatorNodes.length * 2];
            System.arraycopy(terminatorNodes, 0, newTerminatorNodes, 0, terminatorNodes.length);
            terminatorNodes = newTerminatorNodes;
        }
    }

    private void resizeChildInfoIfRequired() {
        if(lastChildId == children.length - 1) {
            int[] newChildren = new int[children.length * 2];
            System.arraycopy(children, 0, newChildren, 0, children.length);
            children = newChildren;

            char[] newChildrenLetters = new char[childrenLetters.length * 2];
            System.arraycopy(childrenLetters, 0, newChildrenLetters, 0, childrenLetters.length);
            childrenLetters = newChildrenLetters;
        }
    }

    public boolean isTerminatorNode(int nodeId) {
        return terminatorNodes[nodeId];
    }

    public void setTerminatorNode(int nodeId) {
        terminatorNodes[nodeId] = true;
    }

    public int getChildrenCount(int nodeId) {
        return childrenCount[nodeId];
    }

    public int getChildAt(int nodeId, int childIndex) {
        int firstIndex = firstChildren[nodeId];
        return children[firstIndex + childIndex];
    }

    public void setChildAt(int nodeId, int childIndex, int newChildId) {
        int firstIndex = firstChildren[nodeId];
        children[firstIndex + childIndex] = newChildId;
    }

    public char getChildLetterAt(int nodeId, int childIndex) {
        int firstIndex = firstChildren[nodeId];
        return childrenLetters[firstIndex + childIndex];
    }

    public void setChildLetterAt(int nodeId, int childIndex, char newLetter) {
        int firstIndex = firstChildren[nodeId];
        childrenLetters[firstIndex + childIndex] = newLetter;
    }

    public void addWord(String word, int position, int maxPosition, int nodeId) {
        if(position == maxPosition) {
            // Mark as terminator if the entire word has been processed.
            if(position == word.length()) {
                setTerminatorNode(nodeId);
            }
        }
        else {
            char letter = word.charAt(position);
            int childNodeId = -1;
            int actualChildCount = getChildrenCount(nodeId);

            if(actualChildCount > 0) {
                // A previous word having the current letter
                // might have been inserted by a previous step.
                for(int i = 0; i < actualChildCount; i++) {
                    if(getChildLetterAt(nodeId, i) == letter) {
                        childNodeId = getChildAt(nodeId, i);
                        break;
                    }
                }
            }

            if(childNodeId == -1) {
                // First time this letter is inserted as a child.
                childNodeId = addNode(nodeId);
                addChildNode(nodeId, childNodeId, letter);
            }

            addWord(word, position + 1, maxPosition, childNodeId);
        }
    }

    @Override
    public void addWords(List<String> words) {
        // The words must be added in layers: first the first letter from all words,
        // then the second letter, and so on, until nu suffix part remains.
        // This algorithm always builds the children array correctly,
        // but only if the words are sorted lexicographically.
        int layer = 0;
        boolean changed = true;
        int rootNode = addNode(-1);
        Collections.sort(words);

        while(changed) {
            changed = false;
            layer++;

            for(String word : words) {
                if(layer <= word.length()) {
                    // At least another iteration is required.
                    changed = true;
                    addWord(word, 0, layer, rootNode);
                }
            }
        }
    }

    public int addNode(int parentNodeId) {
        resizeNodeInfoIfRequired();
        firstChildren[lastNodeId] = (-1);
        childrenCount[lastNodeId] = ((byte)0);
        lastNodeId++;
        return lastNodeId - 1;
    }

    public void addChildNode(int parentNodeId, int childNodeId, char letter) {
        resizeChildInfoIfRequired();
        childrenLetters[lastChildId] = (letter);
        children[lastChildId] = (childNodeId);
        lastChildId++;

        int firstIndex = firstChildren[parentNodeId];

        if(firstIndex == -1) {
            firstChildren[parentNodeId] =  lastChildId - 1;
            childrenCount[parentNodeId] = (byte)1;
        }
        else {
            byte actualCount = childrenCount[parentNodeId];
            childrenCount[parentNodeId] = (byte)(actualCount + 1);
        }
    }

    protected TrieChildren getChildren(int nodeId) {
        int childCount = getChildrenCount(nodeId);
        TrieChildren result = new TrieChildren(childCount);

        for(int i = 0; i < childCount; i++) {
            char letter = getChildLetterAt(nodeId, i);
            int childId = getChildAt(nodeId, i);
            result.putPair(i, letter, new CompactTrieProxy(this, childId));
        }

        return result;
    }

    @Override
    public TrieChildren getChildren() {
        return getChildren(0);
    }

    protected  boolean isTerminator(int nodeId) {
        return terminatorNodes[nodeId];
    }

    @Override
    public boolean isTerminator() {
        return isTerminator(0);
    }
}
