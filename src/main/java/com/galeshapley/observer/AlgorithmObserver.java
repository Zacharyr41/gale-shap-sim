package com.galeshapley.observer;

import com.galeshapley.model.*;
import java.util.Set;

public interface AlgorithmObserver {
    
    void onAlgorithmStart(Set<Proposer> proposers, Set<Proposee> proposees);
    
    void onIterationStart(int iteration);
    
    void onProposal(Proposer proposer, Proposee proposee);
    
    void onAcceptance(Proposer proposer, Proposee proposee);
    
    void onRejection(Proposer proposer, Proposee proposee);
    
    void onBrokenEngagement(Proposer brokenUpWith, Proposee proposee, Proposer newProposer);
    
    void onIterationEnd(int iteration, Matching currentMatching);
    
    void onAlgorithmComplete(Matching finalMatching, int totalIterations);
}