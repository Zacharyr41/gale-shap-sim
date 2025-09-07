package com.galeshapley.algorithm;

import com.galeshapley.model.*;
import com.galeshapley.observer.AlgorithmObserver;
import com.galeshapley.observer.StatisticsObserver;
import com.galeshapley.config.RuntimeOptions;

import java.util.*;

public class GaleShapleyAlgorithm {
    private final Map<Proposer, PreferenceList<Proposee>> proposerPreferences;
    private final Map<Proposee, PreferenceList<Proposer>> proposeePreferences;
    private final Map<Proposer, Integer> nextProposalIndex;
    private final List<AlgorithmObserver> observers;
    private final Map<Proposee, Integer> proposeeEmptySetPreferences;
    private Matching currentMatching;
    private int iterationCount;
    private RuntimeOptions runtimeOptions;

    public GaleShapleyAlgorithm(
            Map<Proposer, PreferenceList<Proposee>> proposerPreferences,
            Map<Proposee, PreferenceList<Proposer>> proposeePreferences) {
        this(proposerPreferences, proposeePreferences, new HashMap<>());
    }
    
    public GaleShapleyAlgorithm(
            Map<Proposer, PreferenceList<Proposee>> proposerPreferences,
            Map<Proposee, PreferenceList<Proposer>> proposeePreferences,
            Map<Proposer, Integer> emptySetPreferences) {
        this(proposerPreferences, proposeePreferences, emptySetPreferences, new HashMap<>());
    }
    
    public GaleShapleyAlgorithm(
            Map<Proposer, PreferenceList<Proposee>> proposerPreferences,
            Map<Proposee, PreferenceList<Proposer>> proposeePreferences,
            Map<Proposer, Integer> emptySetPreferences,
            Map<Proposee, Integer> proposeeEmptySetPreferences) {
        this(proposerPreferences, proposeePreferences, emptySetPreferences, proposeeEmptySetPreferences, RuntimeOptions.defaultOptions());
    }
    
    public GaleShapleyAlgorithm(
            Map<Proposer, PreferenceList<Proposee>> proposerPreferences,
            Map<Proposee, PreferenceList<Proposer>> proposeePreferences,
            Map<Proposer, Integer> emptySetPreferences,
            Map<Proposee, Integer> proposeeEmptySetPreferences,
            RuntimeOptions runtimeOptions) {
        this.proposerPreferences = processEmptySetPreferences(proposerPreferences, emptySetPreferences);
        this.proposeePreferences = setupEmptySetHandling(proposeePreferences, proposerPreferences.keySet(), proposeeEmptySetPreferences);
        this.proposeeEmptySetPreferences = new HashMap<>(proposeeEmptySetPreferences);
        this.nextProposalIndex = new HashMap<>();
        this.observers = new ArrayList<>();
        this.iterationCount = 0;
        this.runtimeOptions = runtimeOptions;
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
    
    private Map<Proposee, PreferenceList<Proposer>> setupEmptySetHandling(
            Map<Proposee, PreferenceList<Proposer>> originalPreferences,
            Set<Proposer> allProposers,
            Map<Proposee, Integer> proposeeEmptySetPreferences) {
        
        Map<Proposee, PreferenceList<Proposer>> processedPreferences = new HashMap<>();
        
        // Process each proposee's preferences
        for (Map.Entry<Proposee, PreferenceList<Proposer>> entry : originalPreferences.entrySet()) {
            Proposee proposee = entry.getKey();
            PreferenceList<Proposer> originalList = entry.getValue();
            
            List<Proposer> newPreferences = new ArrayList<>();
            Integer emptySetPosition = proposeeEmptySetPreferences.get(proposee);
            
            if (emptySetPosition != null) {
                // Insert EmptySet at the specified position for proposee preferences
                // This represents the proposee's willingness to remain single at this preference level
                for (int i = 0; i < originalList.size(); i++) {
                    if (i == emptySetPosition) {
                        // EmptySet is conceptually added, but we handle it specially in the algorithm logic
                        // We don't actually add it to the preference list here
                    }
                    newPreferences.add(originalList.getPreferredAt(i));
                }
            } else {
                // No empty set preference, keep original list but implicitly add empty set at the end
                newPreferences.addAll(originalList.getPreferences());
            }
            
            processedPreferences.put(proposee, new PreferenceList<>(proposee, newPreferences));
        }
        
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
        return execute(this.runtimeOptions);
    }
    
    public AlgorithmResult execute(RuntimeOptions options) {
        this.runtimeOptions = options;
        initialize();
        notifyStart();

        while (!currentMatching.isComplete() && hasMoreProposals() && iterationCount < runtimeOptions.getMaxIterations()) {
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
            if (!proposee.isEmptySet()) {
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
        // Track all proposal attempts, including those to empty sets
        notifyProposalAttempt(proposer, proposee);
        
        // Special handling for EmptySet - always accepts (represents choosing to be single)
        if (proposee.isEmptySet()) {
            currentMatching.match(proposer, proposee);
            // Don't notify proposals or acceptances for empty set as they're not "real"
            return;
        }
        
        // Check if the proposee prefers to remain single over this proposer
        Integer emptySetPosition = proposeeEmptySetPreferences.get(proposee);
        PreferenceList<Proposer> proposeePrefs = proposeePreferences.get(proposee);
        
        if (emptySetPosition != null) {
            // Find proposer's position in proposee's preference list
            // If proposer is not in list or their position is worse than empty set position, reject
            if (!proposeePrefs.contains(proposer)) {
                // Don't count this as a real proposal since it will be immediately rejected
                notifyRejection(proposer, proposee);
                return;
            }
            
            int proposerPosition = proposeePrefs.getRank(proposer);
            
            // If proposer's position is worse than empty set position, reject immediately
            if (proposerPosition >= emptySetPosition) {
                // Don't count this as a real proposal since it will be immediately rejected
                notifyRejection(proposer, proposee);
                return;
            }
        }
        
        // Only notify proposal if it won't be immediately rejected due to empty set preference
        notifyProposal(proposer, proposee);
        
        Optional<Proposer> currentMatch = currentMatching.getMatch(proposee);
        
        if (!currentMatch.isPresent()) {
            currentMatching.match(proposer, proposee);
            notifyAcceptance(proposer, proposee);
        } else {
            Proposer currentProposer = currentMatch.get();
            
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
    
    private void notifyProposalAttempt(Proposer proposer, Proposee proposee) {
        observers.forEach(observer -> {
            if (observer instanceof StatisticsObserver) {
                ((StatisticsObserver) observer).onProposalAttempt(proposer, proposee);
            }
        });
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