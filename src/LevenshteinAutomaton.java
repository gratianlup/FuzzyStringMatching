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

public class LevenshteinAutomaton {
    // Represents an execution point in the evaluation of a NFA or DFA.
    private static class ExecutionState {
        public State state;      // The reached state.
        public int wordPosition; // The reached position in the word.

        public ExecutionState(State state, int wordPosition) {
            this.state = state;
            this.wordPosition = wordPosition;
        }
    }


    // Helper used to store and query information about a set of states.
    // Used during NFA -> DFA conversion.
    public static class StateGroup {
        public Set<State> states;
        public StateGroup anyTransition;
        public Map<Character, StateGroup> letterTransitions;

        public StateGroup(Set<State> states) {
            this.states = new HashSet<State>(states);
            this.letterTransitions = new HashMap<Character, StateGroup>();
        }

        // Returns the set of states reachable by transitions on any letters
        // by considering all states found in the group.
        public Set<State> getStatesForAny() {
            Set<State> outputStates = new HashSet<State>();

            for(State state : states) {
                Transition anyTransition = state.getAnyTransition(false);

                if(anyTransition != null) {
                    for(State nextState : anyTransition.getNextStates()) {
                        outputStates.add(nextState);
                    }
                }
            }

            return outputStates;
        }

        // Returns the set of states reachable by transitions on the specified letter
        // by considering all states found in the group. If the states have transitions
        // on any letters, they are considered reachable states too and added to the set.
        public Set<State> getStatesForLetter(char letter) {
            Set<State> outputStates = new HashSet<State>();

            for(State state : states) {
                for(Transition transition : state.getLetterTransitions()) {
                    if(transition.getLetter() == letter) {
                        for(State nextState : transition.getNextStates()) {
                            outputStates.add(nextState);
                        }
                    }
                }

                // Check for transition on any letter.
                Transition anyTransition = state.getAnyTransition(false);

                if(anyTransition != null) {
                    for(State nextAnyState : anyTransition.getNextStates()) {
                        outputStates.add(nextAnyState);
                    }
                }
            }

            return outputStates;
        }

        // Returns the set of transition letters from all states in the group.
        public List<Character> getTransitionLetters() {
            List<Character> letters = new ArrayList<Character>();

            for(State state : states) {
                for(Transition transition : state.getLetterTransitions()) {
                    if(!letters.contains(transition.getLetter())) {
                        letters.add(transition.getLetter());
                    }
                }
            }

            return letters;
        }

        // Returns whether the group can be considered a final state.
        // This happens when a least one state in the group is final.
        public boolean isFinalState() {
            for(State state : states) {
                if(state.isFinal()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int hashCode() {
            return states.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            StateGroup otherGroup = (StateGroup)object;
            return otherGroup.states.equals(states);
        }
    }


    public State startState; // The start state of the automaton.
    public int maxError;     // The maximum accepted Levenshtein distance.

    public LevenshteinAutomaton(int maxError) {
        this.startState = new State(false);
        this.maxError = maxError;
    }

    // Builds the NFA that accepts any word with a Levenshtein distance
    // no larger than the maximum one when compared to the pattern word.
    public void buildNFA(String patternWord) {
        // Creates an NFA that accepts all words that have at most
        // 'maxError' Levenshtein distance compared to the pattern word.
        // See "Fast string correction with Levenshtein automata" by K. Schulz, S. Mihov
        // for an explanation of the concept and construction algorithm.
        State[][] states = new State[maxError + 1][patternWord.length() + 1];

        // (N + 1) * (E + 1) states are created, where N is the number of letters
        // in the pattern word and E the maximum accepted Levenshtein distance.
        for(int k = 0; k <= maxError; k++) {
            for(int i = 0; i <= patternWord.length(); i++) {
                // The states on the last column are accepting states.
                states[k][i] = new State(i == patternWord.length());
            }
        }

        // Create the transitions between the states.
        for(int k = 0; k <= maxError; k++) {
            for(int i = 0; i < patternWord.length(); i++) {
                // Matching letter.
                Transition match = states[k][i].getTransition(patternWord.charAt(i), true);
                match.addNextState(states[k][i + 1]);

                if(k < maxError) {
                    // Inserted or substituted letter.
                    Transition insertion = states[k][i].getAnyTransition(true);
                    insertion.addNextState(states[k + 1][i]);
                    insertion.addNextState(states[k + 1][i + 1]);

                    // Deleted letter.
                    Transition deletion = states[k][i].getEpsilonTransition(true);
                    deletion.addNextState(states[k + 1][i + 1]);
                }
            }
        }

        startState = states[0][0];
    }

    private String getEdgeLabel(Transition transition) {
        switch(transition.getType()) {
            case Epsilon: return "Eps";
            case Any: return "*";
            default: return String.valueOf(transition.getLetter());
        }
    }

    private DotPrinter.Color getNodeColor(State state) {
        if(state.isFinal()) {
            return DotPrinter.Color.Green;
        }
        else return DotPrinter.Color.Gray;
    }

    private void exportStateToDOT(State state, DotPrinter printer,
                                  Set<State> visitedStates) {
        if(visitedStates.contains(state)) {
            return;
        }
        else visitedStates.add(state);

        String label = state.isFinal() ? "F" : " ";
        printer.createNode(state, label, DotPrinter.Shape.Circle,
                           getNodeColor(state));
        for(Transition transition : state.getLetterTransitions()) {
            for(State nextState : transition.getNextStates()) {
                exportStateToDOT(nextState, printer, visitedStates);
                printer.createLink(state, nextState, getEdgeLabel(transition));
            }
        }
    }

    // Exports the automaton as a Graphviz DOT file.
    public void exportToDOT(BufferedWriter writer) {
        Set<State> visitedStates = new HashSet<State>();
        DotPrinter printer = new DotPrinter(writer);
        printer.beginGraph();
        exportStateToDOT(startState, printer, visitedStates);
        printer.endGraph();
    }

    // Evaluates the NFA and checks if the candidate word is accepted or not
    // (its Levenshtein distance is at most the maximum accepted by the automaton).
    // Can be used to validate the NFA construction algorithm.
    public boolean evaluateNFA(String candidateWord) {
        List<ExecutionState> worklist = new ArrayList<ExecutionState>();
        worklist.add(new ExecutionState(startState, 0));

        while(!worklist.isEmpty()) {
            ExecutionState candidate = worklist.remove(worklist.size() - 1);

            if(candidate.state.isFinal() &&
               candidate.wordPosition == candidateWord.length()) {
                // Reached an end state, word is accepted.
                return true;
            }

            for(Transition transition : candidate.state.getLetterTransitions()) {
                evaluateNFATransition(transition, candidate, candidateWord, worklist);
            }
        }

        return false;
    }

    private void evaluateNFATransition(Transition transition, ExecutionState candidate,
                                       String candidateWord, List<ExecutionState> worklist) {
        // Add to the worklist any state according to the current state
        // and the position reached into the candidate word.
        switch(transition.getType()) {
            case Any: {
                if(candidate.wordPosition < candidateWord.length()) {
                    for(State nextState : transition.getNextStates()) {
                        worklist.add(new ExecutionState(nextState, candidate.wordPosition + 1));
                    }
                }
                break;
            }
            case Epsilon: {
                for(State nextState : transition.getNextStates()) {
                    worklist.add(new ExecutionState(nextState, candidate.wordPosition));
                }
                break;
            }
            case Letter: {
                if(candidate.wordPosition < candidateWord.length() &&
                   candidateWord.charAt(candidate.wordPosition) == transition.getLetter()) {
                    for(State nextState : transition.getNextStates()) {
                        worklist.add(new ExecutionState(nextState, candidate.wordPosition + 1));
                    }
                }
                break;
            }
        }
    }

    private Set<State> expandEpsilon(Set<State> inputStates) {
        // Perform a transitive closure on the Epsilon transitions
        // for all input states, remembering all reachable states by one
        // or more Epsilon transitions. The algorithm stops when no new
        // states are added to the reachable (output) states.
        Set<State> outputStates = new HashSet<State>();
        Set<State> uncheckedStates = new HashSet<State>(inputStates);

        while(!uncheckedStates.isEmpty()) {
            Iterator<State> iterator = uncheckedStates.iterator();
            State state = iterator.next(); iterator.remove();
            outputStates.add(state); // State is reachable.

            // Check if any states are reachable from this one
            // through Epsilon transitions and add them to the worklist.
            Transition epsilon = state.getEpsilonTransition(false);

            if(epsilon != null) {
                for(State nextState : epsilon.getNextStates()) {
                    if(!outputStates.contains(nextState)) {
                        uncheckedStates.add(nextState);
                    }
                }
            }
        }

        return outputStates;
    }

    private Set<State> expandEpsilon(State state) {
        Set<State> states = new HashSet<State>();
        states.add(state);
        return expandEpsilon(states);
    }

    // Converts the NFA to a DFA. The conversion is required before
    // running the fuzzy matching algorithm and greatly speeds up searching.
    public void convertToDFA() {
        // The standard "powerset construction" algorithm is implemented here.
        // The NFA is "evaluated in parallel" and groups of reachable states
        // are converted into DFA states.
        Set<StateGroup> dfaStates = new HashSet<StateGroup>();
        Set<StateGroup> worklist = new HashSet<StateGroup>();

        StateGroup startGroup = new StateGroup(expandEpsilon(startState));
        dfaStates.add(startGroup);
        worklist.add(startGroup);

        while(!worklist.isEmpty()) {
            Iterator<StateGroup> iterator = worklist.iterator();
            StateGroup currentGroup = iterator.next(); iterator.remove();

            // Add next state for "any" transition.
            Set<State> nextAnyStates = expandEpsilon(currentGroup.getStatesForAny());
            StateGroup newAnyStateGroup = new StateGroup(nextAnyStates);
            currentGroup.anyTransition = newAnyStateGroup;

            if(!dfaStates.contains(newAnyStateGroup)) {
                dfaStates.add(newAnyStateGroup);
                worklist.add(newAnyStateGroup);
            }

            // Add next state for each transition letter.
            List<Character> transitionLetters = currentGroup.getTransitionLetters();

            for(char letter : transitionLetters) {
                Set<State> nextStates = expandEpsilon(currentGroup.getStatesForLetter(letter));
                StateGroup newStateGroup = new StateGroup(nextStates);
                currentGroup.letterTransitions.put(letter, newStateGroup);

                if(!dfaStates.contains(newStateGroup)) {
                    dfaStates.add(newStateGroup);
                    worklist.add(newStateGroup);
                }
            }
        }

        buildDFA(dfaStates, startGroup);
    }

    private boolean isDeadState(State state) {
        // A state is not required if there is no transition that can leave
        // the state (i.e. all transitions, if any, lead back to the state).
        for(Transition transition : state.getLetterTransitions()) {
            for(State nextState : transition.getNextStates()) {
                if(nextState != state) {
                    return false;
                }
            }
        }

        return true;
    }

    private void buildDFA(Set<StateGroup> stateGroups, StateGroup startStateGroup) {
        // Create a new state for each group of states.
        Map<StateGroup, State> groupToState = new HashMap<StateGroup, State>();

        for(StateGroup group : stateGroups) {
            groupToState.put(group, new State(group.isFinalState()));
        }

        // Build the automaton using the new states.
        List<StateGroup> worklist = new ArrayList<StateGroup>();
        worklist.add(startStateGroup);

        while(!worklist.isEmpty()) {
            StateGroup currentGroup = worklist.remove(worklist.size() - 1);
            State currentState = groupToState.get(currentGroup);

            // Add the transition for any letter.
            if(currentGroup.anyTransition != null) {
                State anyState = groupToState.get(currentGroup.anyTransition);
                currentState.getAnyTransition(true).addNextState(anyState);
                worklist.add(currentGroup.anyTransition);
            }

            // Add the transition for each letter.
            for(Map.Entry<Character, StateGroup> transition :
                    currentGroup.letterTransitions.entrySet()) {
                char letter = transition.getKey();
                State letterState = groupToState.get(transition.getValue());
                currentState.getTransition(letter, true).addNextState(letterState);
                worklist.add(transition.getValue());
            }
        }

        // Sometimes dead states are created. Any state that leads
        // to a dead state and that is not marked as accepting can have
        // this transition removed, reducing the DFA size.
        for(State state : groupToState.values()) {
            Transition anyTransition = state.getAnyTransition(false);
            boolean leadsToDeadState = true;

            if(anyTransition != null) {
                for(State nextState : anyTransition.getNextStates()) {
                    if(!isDeadState(nextState)) {
                        leadsToDeadState = false;
                        break;
                    }
                }

                if(leadsToDeadState && !state.isFinal()) {
                    state.removeAnyTransition();
                }
            }
        }

        startState = groupToState.get(startStateGroup);
    }

    // Evaluates the DFA and checks if the candidate word is accepted or not
    // (its Levenshtein distance is at most the maximum accepted by the automaton).
    // Can be used to validate the NFA -> DFA conversion algorithm.
    public boolean evaluateDFA(String candidateWord) {
        List<ExecutionState> worklist = new ArrayList<ExecutionState>();
        worklist.add(new ExecutionState(startState, 0));

        while(!worklist.isEmpty()) {
            ExecutionState candidate = worklist.remove(worklist.size() - 1);

            if(candidate.wordPosition == candidateWord.length()) {
                if(candidate.state.isFinal()) {
                    // Reached an end state, word is accepted.
                    return true;
                }
                else continue;
            }

            for(Transition transition : candidate.state.getLetterTransitions()) {
                if(transition.isLetterTransition(candidateWord.charAt(candidate.wordPosition)) ||
                   transition.isAnyTransition()) {
                    for(State nextState : transition.getNextStates()) {
                        worklist.add(new ExecutionState(nextState, candidate.wordPosition + 1));
                    }
                }
            }
        }

        return false;
    }
}
