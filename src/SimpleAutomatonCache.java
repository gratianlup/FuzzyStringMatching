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

// A very naive implementation of a cache for automatons for 1 and 2 errors.
// Could be improved a lot by using a limit on the amount of automaton
// and by keeping only the most frequently requested ones
public class SimpleAutomatonCache implements AutomatonCache {
    private Map<String, LevenshteinAutomaton> automatonOneErrorCache;
    private Map<String, LevenshteinAutomaton> automatonTwoErrorsCache;

    public SimpleAutomatonCache() {
        this.automatonOneErrorCache = new HashMap<String, LevenshteinAutomaton>();
        this.automatonTwoErrorsCache = new HashMap<String, LevenshteinAutomaton>();
    }

    @Override
    public void add(LevenshteinAutomaton automaton, String word, int maxError) {
        if(maxError == 2) {
            automatonTwoErrorsCache.put(word, automaton);
        }
        else if(maxError == 1) {
            automatonOneErrorCache.put(word, automaton);
        }
    }

    @Override
    public LevenshteinAutomaton get(String word, int maxError) {
        if(maxError == 2) {
            return automatonTwoErrorsCache.get(word);
        }
        else if(maxError == 1) {
            return automatonOneErrorCache.get(word);
        }

        return null;
    }
}
