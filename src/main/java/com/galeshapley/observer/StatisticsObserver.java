package com.galeshapley.observer;

import com.galeshapley.model.*;
import java.util.*;

public class StatisticsObserver implements AlgorithmObserver {
    
    private int totalProposals = 0;
    private int totalAcceptances = 0;
    private int totalRejections = 0;
    private int totalIterationAttempts = 0; // Tracks all proposal attempts including those to empty sets
    private Map<Proposer, Integer> proposalCountByProposer = new HashMap<>();
    private Map<Proposer, Integer> rejectionCountByProposer = new HashMap<>();
    private Map<Proposer, Integer> iterationAttemptsByProposer = new HashMap<>();
    private Map<Proposee, Integer> proposalReceivedCount = new HashMap<>();
    private long startTime;
    private long endTime;
    
    @Override
    public void onAlgorithmStart(Set<Proposer> proposers, Set<Proposee> proposees) {
        startTime = System.currentTimeMillis();
        proposers.forEach(p -> {
            proposalCountByProposer.put(p, 0);
            rejectionCountByProposer.put(p, 0);
            iterationAttemptsByProposer.put(p, 0);
        });
        proposees.forEach(p -> proposalReceivedCount.put(p, 0));
    }
    
    @Override
    public void onIterationStart(int iteration) {
        // No stats to collect here
    }
    
    @Override
    public void onProposal(Proposer proposer, Proposee proposee) {
        totalProposals++;
        proposalCountByProposer.merge(proposer, 1, Integer::sum);
        proposalReceivedCount.merge(proposee, 1, Integer::sum);
    }
    
    @Override
    public void onAcceptance(Proposer proposer, Proposee proposee) {
        totalAcceptances++;
    }
    
    @Override
    public void onRejection(Proposer proposer, Proposee proposee) {
        totalRejections++;
        rejectionCountByProposer.merge(proposer, 1, Integer::sum);
    }
    
    @Override
    public void onIterationEnd(int iteration, Matching currentMatching) {
        // No stats to collect here
    }
    
    public void onProposalAttempt(Proposer proposer, Proposee proposee) {
        totalIterationAttempts++;
        iterationAttemptsByProposer.merge(proposer, 1, Integer::sum);
    }
    
    @Override
    public void onAlgorithmComplete(Matching finalMatching, int totalIterations) {
        endTime = System.currentTimeMillis();
    }
    
    public Statistics getStatistics() {
        return new Statistics(this);
    }
    
    public static class Statistics {
        private final int totalProposals;
        private final int totalAcceptances;
        private final int totalRejections;
        private final int totalIterationAttempts;
        private final Map<Proposer, Integer> proposalCountByProposer;
        private final Map<Proposer, Integer> rejectionCountByProposer;
        private final Map<Proposer, Integer> iterationAttemptsByProposer;
        private final Map<Proposee, Integer> proposalReceivedCount;
        private final long executionTimeMs;
        
        private Statistics(StatisticsObserver observer) {
            this.totalProposals = observer.totalProposals;
            this.totalAcceptances = observer.totalAcceptances;
            this.totalRejections = observer.totalRejections;
            this.totalIterationAttempts = observer.totalIterationAttempts;
            this.proposalCountByProposer = new HashMap<>(observer.proposalCountByProposer);
            this.rejectionCountByProposer = new HashMap<>(observer.rejectionCountByProposer);
            this.iterationAttemptsByProposer = new HashMap<>(observer.iterationAttemptsByProposer);
            this.proposalReceivedCount = new HashMap<>(observer.proposalReceivedCount);
            this.executionTimeMs = observer.endTime - observer.startTime;
        }
        
        public int getTotalProposals() {
            return totalProposals;
        }
        
        public int getTotalAcceptances() {
            return totalAcceptances;
        }
        
        public int getTotalRejections() {
            return totalRejections;
        }
        
        public int getTotalIterationAttempts() {
            return totalIterationAttempts;
        }
        
        public double getAverageProposalsPerProposer() {
            if (proposalCountByProposer.isEmpty()) return 0.0;
            return proposalCountByProposer.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        }
        
        public double getAverageRejectionsPerProposer() {
            if (rejectionCountByProposer.isEmpty()) return 0.0;
            return rejectionCountByProposer.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        }
        
        public double getAverageProposalsReceivedPerProposee() {
            if (proposalReceivedCount.isEmpty()) return 0.0;
            return proposalReceivedCount.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        }
        
        public double getAverageIterationAttemptsPerProposer() {
            if (iterationAttemptsByProposer.isEmpty()) return 0.0;
            return iterationAttemptsByProposer.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        }
        
        public long getExecutionTimeMs() {
            return executionTimeMs;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Statistics{\n" +
                "  Total Iteration Attempts: %d\n" +
                "  Total Proposals: %d\n" +
                "  Total Acceptances: %d\n" +
                "  Total Rejections: %d\n" +
                "  Avg Iteration Attempts/Proposer: %.2f\n" +
                "  Avg Proposals/Proposer: %.2f\n" +
                "  Avg Rejections/Proposer: %.2f\n" +
                "  Avg Proposals Received/Proposee: %.2f\n" +
                "  Execution Time: %d ms\n" +
                "}",
                totalIterationAttempts, totalProposals, totalAcceptances, totalRejections,
                getAverageIterationAttemptsPerProposer(),
                getAverageProposalsPerProposer(),
                getAverageRejectionsPerProposer(),
                getAverageProposalsReceivedPerProposee(),
                executionTimeMs
            );
        }
    }
}