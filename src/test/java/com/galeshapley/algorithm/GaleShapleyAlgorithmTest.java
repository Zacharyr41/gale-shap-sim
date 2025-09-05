package com.galeshapley.algorithm;

import com.galeshapley.model.*;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class GaleShapleyAlgorithmTest {
    
    private Proposer m1, m2, m3;
    private Proposee w1, w2, w3;
    
    @BeforeEach
    void setUp() {
        m1 = new Proposer("m1", "Man 1");
        m2 = new Proposer("m2", "Man 2");
        m3 = new Proposer("m3", "Man 3");
        
        w1 = new Proposee("w1", "Woman 1");
        w2 = new Proposee("w2", "Woman 2");
        w3 = new Proposee("w3", "Woman 3");
    }
    
    @Test
    void shouldProduceStableMatchingSimpleCase() {
        Map<Proposer, PreferenceList<Proposee>> proposerPrefs = new HashMap<>();
        proposerPrefs.put(m1, new PreferenceList<>(m1, Arrays.asList(w1, w2, w3)));
        proposerPrefs.put(m2, new PreferenceList<>(m2, Arrays.asList(w2, w3, w1)));
        proposerPrefs.put(m3, new PreferenceList<>(m3, Arrays.asList(w3, w1, w2)));
        
        Map<Proposee, PreferenceList<Proposer>> proposeePrefs = new HashMap<>();
        proposeePrefs.put(w1, new PreferenceList<>(w1, Arrays.asList(m1, m2, m3)));
        proposeePrefs.put(w2, new PreferenceList<>(w2, Arrays.asList(m2, m3, m1)));
        proposeePrefs.put(w3, new PreferenceList<>(w3, Arrays.asList(m3, m1, m2)));
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(proposerPrefs, proposeePrefs);
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        
        assertThat(result.getFinalMatching().isComplete()).isTrue();
        assertThat(result.getFinalMatching().getMatchCount()).isEqualTo(3);
        assertThat(result.getFinalMatching().isMatched(m1, w1)).isTrue();
        assertThat(result.getFinalMatching().isMatched(m2, w2)).isTrue();
        assertThat(result.getFinalMatching().isMatched(m3, w3)).isTrue();
    }
    
    @Test
    void shouldHandleComplexPreferences() {
        Map<Proposer, PreferenceList<Proposee>> proposerPrefs = new HashMap<>();
        proposerPrefs.put(m1, new PreferenceList<>(m1, Arrays.asList(w2, w1, w3)));
        proposerPrefs.put(m2, new PreferenceList<>(m2, Arrays.asList(w1, w2, w3)));
        proposerPrefs.put(m3, new PreferenceList<>(m3, Arrays.asList(w1, w2, w3)));
        
        Map<Proposee, PreferenceList<Proposer>> proposeePrefs = new HashMap<>();
        proposeePrefs.put(w1, new PreferenceList<>(w1, Arrays.asList(m2, m3, m1)));
        proposeePrefs.put(w2, new PreferenceList<>(w2, Arrays.asList(m3, m1, m2)));
        proposeePrefs.put(w3, new PreferenceList<>(w3, Arrays.asList(m3, m2, m1)));
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(proposerPrefs, proposeePrefs);
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        
        assertThat(result.getFinalMatching().isComplete()).isTrue();
        assertThat(result.getFinalMatching().getMatchCount()).isEqualTo(3);
        
        // Verify stability
        Matching matching = result.getFinalMatching();
        assertThat(isStable(matching, proposerPrefs, proposeePrefs)).isTrue();
    }
    
    @Test
    void shouldCollectStatistics() {
        Map<Proposer, PreferenceList<Proposee>> proposerPrefs = new HashMap<>();
        proposerPrefs.put(m1, new PreferenceList<>(m1, Arrays.asList(w1, w2, w3)));
        proposerPrefs.put(m2, new PreferenceList<>(m2, Arrays.asList(w1, w2, w3)));
        proposerPrefs.put(m3, new PreferenceList<>(m3, Arrays.asList(w1, w2, w3)));
        
        Map<Proposee, PreferenceList<Proposer>> proposeePrefs = new HashMap<>();
        proposeePrefs.put(w1, new PreferenceList<>(w1, Arrays.asList(m3, m2, m1)));
        proposeePrefs.put(w2, new PreferenceList<>(w2, Arrays.asList(m2, m1, m3)));
        proposeePrefs.put(w3, new PreferenceList<>(w3, Arrays.asList(m1, m3, m2)));
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(proposerPrefs, proposeePrefs);
        StatisticsObserver statsObserver = new StatisticsObserver();
        algorithm.addObserver(statsObserver);
        
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        StatisticsObserver.Statistics stats = statsObserver.getStatistics();
        
        assertThat(result.getFinalMatching().isComplete()).isTrue();
        assertThat(stats.getTotalProposals()).isGreaterThan(0);
        assertThat(stats.getTotalAcceptances()).isGreaterThan(0);
        assertThat(stats.getExecutionTimeMs()).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    void shouldHandleUnequalNumbers() {
        Proposer m4 = new Proposer("m4", "Man 4");
        
        Map<Proposer, PreferenceList<Proposee>> proposerPrefs = new HashMap<>();
        proposerPrefs.put(m1, new PreferenceList<>(m1, Arrays.asList(w1, w2, w3)));
        proposerPrefs.put(m2, new PreferenceList<>(m2, Arrays.asList(w2, w3, w1)));
        proposerPrefs.put(m3, new PreferenceList<>(m3, Arrays.asList(w3, w1, w2)));
        proposerPrefs.put(m4, new PreferenceList<>(m4, Arrays.asList(w1, w2, w3)));
        
        Map<Proposee, PreferenceList<Proposer>> proposeePrefs = new HashMap<>();
        proposeePrefs.put(w1, new PreferenceList<>(w1, Arrays.asList(m1, m2, m3, m4)));
        proposeePrefs.put(w2, new PreferenceList<>(w2, Arrays.asList(m2, m3, m4, m1)));
        proposeePrefs.put(w3, new PreferenceList<>(w3, Arrays.asList(m3, m4, m1, m2)));
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(proposerPrefs, proposeePrefs);
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        
        assertThat(result.getFinalMatching().getMatchCount()).isEqualTo(3);
        assertThat(result.getFinalMatching().getUnmatchedProposers()).hasSize(1);
        assertThat(result.getFinalMatching().isComplete()).isTrue();
    }
    
    private boolean isStable(Matching matching, 
                             Map<Proposer, PreferenceList<Proposee>> proposerPrefs,
                             Map<Proposee, PreferenceList<Proposer>> proposeePrefs) {
        for (Map.Entry<Proposer, PreferenceList<Proposee>> entry : proposerPrefs.entrySet()) {
            Proposer proposer = entry.getKey();
            PreferenceList<Proposee> prefs = entry.getValue();
            Optional<Proposee> currentMatch = matching.getMatch(proposer);
            
            if (currentMatch.isPresent()) {
                for (Proposee proposee : prefs.getPreferences()) {
                    if (prefs.prefers(proposee, currentMatch.get())) {
                        Optional<Proposer> proposeeMatch = matching.getMatch(proposee);
                        if (proposeeMatch.isPresent()) {
                            PreferenceList<Proposer> proposeePref = proposeePrefs.get(proposee);
                            if (proposeePref.prefers(proposer, proposeeMatch.get())) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}