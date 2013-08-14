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

public class LevenshteinDistance {
    public static int computeDistance(String word, String pattern) {
        // Implements the classic dynamic-programming algorithm for
        // the Levenshtein distance using equal penalty for all edit operations.
        int[][] distance = new int[word.length() + 1][pattern.length() + 1];

        for(int i = 0; i <= word.length(); i++) {
            distance[i][0] = i;
        }

        for(int i = 0; i <= pattern.length(); i++) {
            distance[0][i] = i;
        }

        for(int i = 1; i <= word.length(); i++) {
            for(int j = 1; j <= pattern.length(); j++) {
                int insertionCost = distance[i - 1][j] + 1;
                int deletionCost = distance[i][j - 1] + 1;
                int substitutionCost = distance[i - 1][j - 1] +
                                       ((word.charAt(i - 1) == pattern.charAt(j - 1)) ? 0 : 1);
                distance[i][j] = Math.min(insertionCost, Math.min(deletionCost, substitutionCost));
            }
        }

        return distance[word.length()][pattern.length()];
    }

    public static boolean isAcceptedWord(String word, String pattern, int maxError) {
        return computeDistance(word, pattern) <= maxError;
    }

    public static List<String> findAcceptedWords(List<String> words, String pattern, int maxError) {
        List<String> acceptedWords = new ArrayList<String>();

        for(String word : words) {
            if(isAcceptedWord(word, pattern, maxError)) {
                acceptedWords.add(word);
            }
        }

        return acceptedWords;
    }
}
