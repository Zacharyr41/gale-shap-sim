package com.galeshapley.algorithm;

import com.galeshapley.model.*;
import com.galeshapley.observer.AlgorithmObserver;

import java.util.*;

public class GaleShapleyAlgorithm {
    private final Map<Proposer, PreferenceList<Proposee>> proposerPreferences;
    private final Map<Proposee, PreferenceList<Proposer>> proposeePreferences;
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
        this.proposerPreferences = processEmptySetPreferences(proposerPreferences, emptySetPreferences);
        this.proposeePreferences = setupEmptySetAsProposee(proposeePreferences, proposerPreferences.keySet());
        this.nextProposalIndex = new HashMap<>();
        this.observers = new ArrayList<>();
        this.iterationCount = 0;
    }
    
    private Map<Proposer, PreferenceList<Proposee>> processEmptySetPreferences(
            Map<Proposer, PreferenceList<Proposee>> originalPreferences,
            Map<Proposer, Integer> emptySetPositions) {
        
        Map<Proposer, PreferenceList<Proposee>> processedPreferences = new HashMap<>();
        
        for (Map.Entry<Proposer, PreferenceList<Proposee>> entry : originalPreferences.entrySet()) {
            Proposer proposer = entry.getKey();
            PreferenceList<Proposee> originalList = entry.getValue();
            
            List<Proposee> newPreferences = new ArrayList<>();
            Integer emptySetPosition = emptySetPositions.get(proposer);
            
            if (emptySetPosition != null) {
                // Insert EmptySet at the specified position
                for (int i = 0; i < originalList.size(); i++) {
                    if (i == emptySetPosition) {
                        newPreferences.add(EmptySet.getInstance());
                    }
                    newPreferences.add(originalList.getPreferredAt(i));
                }
                // If empty set position is at the end
                if (emptySetPosition >= originalList.size()) {
                    newPreferences.add(EmptySet.getInstance());
                }
            } else {
                // No empty set preference, keep original list
                newPreferences.addAll(originalList.getPreferences());
            }
            
            processedPreferences.put(proposer, new PreferenceList<>(proposer, newPreferences));
        }
        
        return processedPreferences;
    }
    
    private Map<Proposee, PreferenceList<Proposer>> setupEmptySetAsProposee(
            Map<Proposee, PreferenceList<Proposer>> originalPreferences,
            Set<Proposer> allProposers) {
        
        Map<Proposee, PreferenceList<Proposer>> processedPreferences = new HashMap<>(originalPreferences);
        
        // EmptySet always accepts any proposal (ranks everyone equally at position 0)
        // This ensures anyone who proposes to EmptySet will be "matched" with being single
        List<Proposer> emptySetPreferences = new ArrayList<>(allProposers);
        processedPreferences.put(EmptySet.getInstance(), 
            new PreferenceList<>(EmptySet.getInstance(), emptySetPreferences));
        
        return processedPreferences;
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
            // Don't add EmptySet to the regular proposees collection
            if (!(proposee instanceof EmptySet)) {
                currentMatching.addProposee(proposee);
            }
        }
    }

    private boolean hasMoreProposals() {
        for (Proposer proposer : currentMatching.getUnmatchedProposers()) {
            PreferenceList<Proposee> prefs = proposerPreferences.get(proposer);
            int nextIndex = nextProposalIndex.get(proposer);
            
            // Check if proposer has more preferences
            if (nextIndex < prefs.size()) {
                return true;
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
            
            if (proposalIndex < prefs.size()) {
                Proposee proposee = prefs.getPreferredAt(proposalIndex);
                makeProposal(proposer, proposee);
                nextProposalIndex.put(proposer, proposalIndex + 1);
            }
        }
        
        notifyIterationEnd(iterationCount);
    }

    private void makeProposal(Proposer proposer, Proposee proposee) {
        notifyProposal(proposer, proposee);
        
        // Special handling for EmptySet - always accepts (represents choosing to be single)
        if (proposee instanceof EmptySet) {
            currentMatching.match(proposer, proposee);
            notifyAcceptance(proposer, proposee);
            return;
        }
        
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