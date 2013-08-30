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

// Represents a state in a NFA or DFA.
// For a DFA no epsilon transitions exist anymore.
public class State {
    private boolean isFinal;              // Set if the state is an acceptance state.
    private List<Transition> transitions; // Transitions on letters.
    private Transition epsilonTransition; // Transitions on epsilon (multiple targets possible).
    private Transition anyTransition;     // Transitions on any letter (multiple targets possible).

    public State(boolean isFinal) {
        this.isFinal = isFinal;
        this.transitions = new ArrayList<Transition>();
    }

    // Returns (or creates if requested) the epsilon transition.
    // Multiple target states are possible in a NFA.
    public Transition getEpsilonTransition(boolean create) {
        if(epsilonTransition != null) {
            return epsilonTransition;
        }
        else if(create) {
            epsilonTransition = Transition.createEpsilon();
            return epsilonTransition;
        }
        else return null;
    }

    // Returns (or creates if requested) the transition on any letter.
    // Multiple target states are possible in a NFA.
    public Transition getAnyTransition(boolean create) {
        if(anyTransition != null) {
            return anyTransition;
        }
        else if(create) {
            anyTransition = Transition.createAny();
            return anyTransition;
        }
        else return null;
    }

    // Returns (or creates if requested) the transition associated
    // with the specified letter. Multiple target states are possible in a NFA.
    public Transition getTransition(char letter, boolean create) {
        for(Transition transition : getLetterTransitions()) {
            if(transition.getLetter() == letter) {
                return transition;
            }
        }

        if(create) {
            Transition transition = Transition.createLetter(letter);
            getLetterTransitions().add(transition);
            return transition;
        }
        else return null;
    }

    // Removes the transition on any letter.
    public void removeAnyTransition() {
        anyTransition = null;
    }

    // Removes the transition on epsilon.
    public void removeEpsilonTransition() {
        epsilonTransition = null;
    }

    public boolean isFinal() {
        return isFinal;
    }

    // Returns the next state associated with the specified letter.
    // If no such state exists, but the state has a transition for any letter,
    // the corresponding state is returned instead, otherwise null.
    // If multiple states can be reached the first one is returned,
    public State getStateForLetter(char letter) {
        for(Transition transition : getLetterTransitions()) {
            if(transition.getLetter() == letter) {
                return transition.getNextStates().get(0);
            }
        }

        if(anyTransition != null) {
            return anyTransition.getNextStates().get(0);
        }
        else return null;
    }

    // Returns the transitions associated with letters.
    public List<Transition> getLetterTransitions() {
        return transitions;
    }
}
