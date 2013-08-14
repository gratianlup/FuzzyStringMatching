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

// Represents a transition in a NFA or DFA.
// For DFA the Epsilon transitions are not used anymore.
public class Transition {
    public static enum TransitionType {
        Epsilon,
        Any,
        Letter
    }


    private TransitionType type;
    private char letter;            // Used only for TransitionType.Letter.
    private List<State> nextStates; // A NFA can have multiple destination for the same transition.

    private Transition(TransitionType type, char letter) {
        this.type = type;
        this.letter = letter;
        this.nextStates = new ArrayList<State>();
    }

    public static Transition createEpsilon() {
        return new Transition(TransitionType.Epsilon, ' ' /* letter not used */);
    }

    public static Transition createAny() {
        return new Transition(TransitionType.Any, ' ' /* letter not used */);
    }

    public static Transition createLetter(char letter) {
        return new Transition(TransitionType.Letter, letter);
    }

    public TransitionType getType() {
        return type;
    }

    public boolean isAnyTransition() {
        return type == TransitionType.Any;
    }

    public boolean isEpsilonTransition() {
        return type == TransitionType.Epsilon;
    }

    public boolean isLetterTransition() {
        return type == TransitionType.Letter;
    }

    public boolean isLetterTransition(char transitionLetter) {
        return (type == TransitionType.Letter) &&
                (letter == transitionLetter);

    }

    public char getLetter() {
        return letter;
    }

    public List<State> getNextStates() {
        return nextStates;
    }

    public void addNextState(State state) {
        nextStates.add(state);
    }
}
