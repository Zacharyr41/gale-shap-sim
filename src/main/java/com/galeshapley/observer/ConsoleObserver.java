package com.galeshapley.observer;

import com.galeshapley.model.*;
import java.util.Set;

public class ConsoleObserver implements AlgorithmObserver {
    
    private boolean verbose;
    
    public ConsoleObserver() {
        this(false);
    }
    
    public ConsoleObserver(boolean verbose) {
        this.verbose = verbose;
    }
    
    @Override
    public void onAlgorithmStart(Set<Proposer> proposers, Set<Proposee> proposees) {
        System.out.println("=== Gale-Shapley Algorithm Started ===");
        System.out.println("Proposers: " + proposers.size());
        System.out.println("Proposees: " + proposees.size());
        System.out.println();
    }
    
    @Override
    public void onIterationStart(int iteration) {
        if (verbose) {
            System.out.println("--- Iteration " + iteration + " ---");
        }
    }
    
    @Override
    public void onProposal(Proposer proposer, Proposee proposee) {
        if (verbose) {
            if (proposee instanceof EmptySet) {
                System.out.println("  " + proposer.getName() + " chooses to remain single");
            } else {
                System.out.println("  " + proposer.getName() + " proposes to " + proposee.getName());
            }
        }
    }
    
    @Override
    public void onAcceptance(Proposer proposer, Proposee proposee) {
        if (proposee instanceof EmptySet) {
            System.out.println("  ✓ " + proposer.getName() + " will remain single");
        } else {
            System.out.println("  ✓ " + proposee.getName() + " accepts " + proposer.getName());
        }
    }
    
    @Override
    public void onRejection(Proposer proposer, Proposee proposee) {
        if (verbose) {
            System.out.println("  ✗ " + proposee.getName() + " rejects " + proposer.getName());
        }
    }
    
    @Override
    public void onIterationEnd(int iteration, Matching currentMatching) {
        if (verbose) {
            System.out.println("  Iteration " + iteration + " complete");
            System.out.println();
        }
    }
    
    @Override
    public void onAlgorithmComplete(Matching finalMatching, int totalIterations) {
        System.out.println("\n=== Algorithm Complete ===");
        System.out.println("Total iterations: " + totalIterations);
        System.out.println("\nFinal Matching:");
        finalMatching.getAllMatches().forEach((proposer, proposee) -> {
            if (proposee instanceof EmptySet) {
                System.out.println("  " + proposer.getName() + " → single");
            } else {
                System.out.println("  " + proposer.getName() + " ↔ " + proposee.getName());
            }
        });
        
        if (!finalMatching.getUnmatchedProposers().isEmpty()) {
            System.out.println("\nUnmatched Proposers:");
            finalMatching.getUnmatchedProposers().forEach(p -> 
                System.out.println("  - " + p.getName())
            );
        }
        
        if (!finalMatching.getUnmatchedProposees().isEmpty()) {
            System.out.println("\nUnmatched Proposees:");
            finalMatching.getUnmatchedProposees().forEach(p -> 
                System.out.println("  - " + p.getName())
            );
        }
    }
}