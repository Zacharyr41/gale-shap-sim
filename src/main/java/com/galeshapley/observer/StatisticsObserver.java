package com.galeshapley.observer;

import com.galeshapley.model.*;
import java.util.*;

public class StatisticsObserver implements AlgorithmObserver {
    
    private int totalProposals = 0;
    private int totalAcceptances = 0;
    private int totalRejections = 0;
    private int totalBrokenEngagements = 0; // When someone gets displaced by a better proposal
    private int totalIterationAttempts = 0; // Tracks all proposal attempts including those to empty sets
    private Map<Proposer, Integer> proposalCountByProposer = new HashMap<>();
    private Map<Proposer, Integer> rejectionCountByProposer = new HashMap<>();
    private Map<Proposer, Integer> iterationAttemptsByProposer = new HashMap<>();
    private Map<Proposee, Integer> proposalReceivedCount = new HashMap<>();
    private Set<Proposer> previouslyMatchedProposers = new HashSet<>(); // Track who was previously matched
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
        previouslyMatchedProposers.add(proposer); // Track that this proposer has been matched
    }
    
    @Override
    public void onRejection(Proposer proposer, Proposee proposee) {
        totalRejections++;
        rejectionCountByProposer.merge(proposer, 1, Integer::sum);
        
        // If this proposer was previously matched, this is a broken engagement
        if (previouslyMatchedProposers.contains(proposer)) {
            totalBrokenEngagements++;
        }
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
        private final int totalBrokenEngagements;
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
            this.totalBrokenEngagements = observer.totalBrokenEngagements;
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
        
        public int getTotalBrokenEngagements() {
            return totalBrokenEngagements;
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
                "Algorithm Execution Statistics:\n" +
                "══════════════════════════════════════════════════════════════════\n" +
                "\n" +
                "📊 PROPOSAL ACTIVITY:\n" +
                "  • Meaningful Proposals Made: %d (actual romantic proposals)\n" +
                "  • Proposals Accepted: %d (successful matches formed)\n" +
                "  • Total Rejections: %d (all declined offers)\n" +
                "    ├─ Broken Engagements: %d (displaced by better offer)\n" +
                "    └─ Direct Rejections: %d (immediate 'no' without engagement)\n" +
                "\n" +
                "🔄 ALGORITHM MECHANICS:\n" +
                "  • Total Decision Points: %d (includes evaluating 'stay single')\n" +
                "\n" +
                "📈 COMPETITION METRICS:\n" +
                "  • Avg Proposals per Proposer: %.2f (how hard they had to try)\n" +
                "  • Avg Rejections per Proposer: %.2f (how much competition faced)\n" +
                "  • Avg Proposals per Proposee: %.2f (how popular/in-demand)\n" +
                "\n" +
                "⏱️  PERFORMANCE:\n" +
                "  • Execution Time: %d ms\n",
                totalProposals, totalAcceptances, totalRejections, totalBrokenEngagements, (totalRejections - totalBrokenEngagements),
                totalIterationAttempts,
                getAverageProposalsPerProposer(),
                getAverageRejectionsPerProposer(),
                getAverageProposalsReceivedPerProposee(),
                executionTimeMs
            );
        }
    }
}