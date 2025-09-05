package com.galeshapley.algorithm;

import com.galeshapley.model.*;
import com.galeshapley.observer.AlgorithmObserver;

import java.util.*;

public class GaleShapleyAlgorithm {
    private final Map<Proposer, PreferenceList<Proposee>> proposerPreferences;
    private final Map<Proposee, PreferenceList<Proposer>> proposeePreferences;
    private final Map<Proposer, Integer> emptySetPreferences;
    private final Map<Proposer, Integer> nextProposalIndex;
    private final List<AlgorithmObserver> observers;
    private Matching currentMatching;
    private int iterationCount;

    public GaleShapleyAlgorithm(
            Map<Proposer, PreferenceList<Proposee>> proposerPreferences,
            Map<Proposee, PreferenceList<Proposer>> proposeePreferences) {
        this(proposerPreferences, proposeePreferences, new HashMap<>());
    }
    
    public GaleShapleyAlgorithm(
            Map<Proposer, PreferenceList<Proposee>> proposerPreferences,
            Map<Proposee, PreferenceList<Proposer>> proposeePreferences,
            Map<Proposer, Integer> emptySetPreferences) {
        this.proposerPreferences = new HashMap<>(proposerPreferences);
        this.proposeePreferences = new HashMap<>(proposeePreferences);
        this.emptySetPreferences = new HashMap<>(emptySetPreferences);
        this.nextProposalIndex = new HashMap<>();
        this.observers = new ArrayList<>();
        this.iterationCount = 0;
    }

    public void addObserver(AlgorithmObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(AlgorithmObserver observer) {
        observers.remove(observer);
    }

    public AlgorithmResult execute() {
        initialize();
        notifyStart();

        while (!currentMatching.isComplete() && hasMoreProposals()) {
            iterationCount++;
            performIteration();
        }

        notifyComplete();
        return new AlgorithmResult(currentMatching, iterationCount);
    }

    private void initialize() {
        currentMatching = new Matching();
        
        for (Proposer proposer : proposerPreferences.keySet()) {
            currentMatching.addProposer(proposer);
            nextProposalIndex.put(proposer, 0);
        }
        
        for (Proposee proposee : proposeePreferences.keySet()) {
            currentMatching.addProposee(proposee);
        }
    }

    private boolean hasMoreProposals() {
        for (Proposer proposer : currentMatching.getUnmatchedProposers()) {
            PreferenceList<Proposee> prefs = proposerPreferences.get(proposer);
            int nextIndex = nextProposalIndex.get(proposer);
            
            // Check if proposer has more preferences and hasn't reached empty set cutoff
            if (nextIndex < prefs.size()) {
                // If proposer has empty set preference, check if we've reached that position
                Integer emptySetPosition = emptySetPreferences.get(proposer);
                if (emptySetPosition == null || nextIndex < emptySetPosition) {
                    return true;
                }
            }
        }
        return false;
    }

    private void performIteration() {
        notifyIterationStart(iterationCount);
        
        Set<Proposer> unmatchedProposers = new HashSet<>(currentMatching.getUnmatchedProposers());
        
        for (Proposer proposer : unmatchedProposers) {
            PreferenceList<Proposee> prefs = proposerPreferences.get(proposer);
            int proposalIndex = nextProposalIndex.get(proposer);
            
            // Check if proposer can make this proposal (hasn't reached empty set cutoff)
            if (proposalIndex < prefs.size()) {
                Integer emptySetPosition = emptySetPreferences.get(proposer);
                if (emptySetPosition == null || proposalIndex < emptySetPosition) {
                    Proposee proposee = prefs.getPreferredAt(proposalIndex);
                    makeProposal(proposer, proposee);
                    nextProposalIndex.put(proposer, proposalIndex + 1);
                }
            }
        }
        
        notifyIterationEnd(iterationCount);
    }

    private void makeProposal(Proposer proposer, Proposee proposee) {
        notifyProposal(proposer, proposee);
        
        Optional<Proposer> currentMatch = currentMatching.getMatch(proposee);
        
        if (!currentMatch.isPresent()) {
            currentMatching.match(proposer, proposee);
            notifyAcceptance(proposer, proposee);
        } else {
            Proposer currentProposer = currentMatch.get();
            PreferenceList<Proposer> proposeePrefs = proposeePreferences.get(proposee);
            
            if (proposeePrefs.prefers(proposer, currentProposer)) {
                currentMatching.unmatch(currentProposer, proposee);
                currentMatching.match(proposer, proposee);
                notifyRejection(currentProposer, proposee);
                notifyAcceptance(proposer, proposee);
            } else {
                notifyRejection(proposer, proposee);
            }
        }
    }

    private void notifyStart() {
        observers.forEach(observer -> observer.onAlgorithmStart(
            new HashSet<>(proposerPreferences.keySet()),
            new HashSet<>(proposeePreferences.keySet())
        ));
    }

    private void notifyIterationStart(int iteration) {
        observers.forEach(observer -> observer.onIterationStart(iteration));
    }

    private void notifyProposal(Proposer proposer, Proposee proposee) {
        observers.forEach(observer -> observer.onProposal(proposer, proposee));
    }

    private void notifyAcceptance(Proposer proposer, Proposee proposee) {
        observers.forEach(observer -> observer.onAcceptance(proposer, proposee));
    }

    private void notifyRejection(Proposer proposer, Proposee proposee) {
        observers.forEach(observer -> observer.onRejection(proposer, proposee));
    }

    private void notifyIterationEnd(int iteration) {
        observers.forEach(observer -> observer.onIterationEnd(iteration, new Matching()));
    }

    private void notifyComplete() {
        observers.forEach(observer -> observer.onAlgorithmComplete(currentMatching, iterationCount));
    }

    public static class AlgorithmResult {
        private final Matching finalMatching;
        private final int iterations;

        public AlgorithmResult(Matching finalMatching, int iterations) {
            this.finalMatching = finalMatching;
            this.iterations = iterations;
        }

        public Matching getFinalMatching() {
            return finalMatching;
        }

        public int getIterations() {
            return iterations;
        }

        @Override
        public String toString() {
            return String.format("AlgorithmResult[iterations=%d, matching=%s]", 
                iterations, finalMatching);
        }
    }
}