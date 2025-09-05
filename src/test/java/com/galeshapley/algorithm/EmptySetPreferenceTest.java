package com.galeshapley.algorithm;

import com.galeshapley.model.*;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class EmptySetPreferenceTest {
    
    @Test
    void shouldHandleEmptySetAsFirstPreference() {
        // Create proposers and proposees
        Proposer singlePreferrer = new Proposer("m1", "SinglePreferrer");
        Proposer regularGuy = new Proposer("m2", "RegularGuy");
        Proposee alice = new Proposee("w1", "Alice");
        Proposee betty = new Proposee("w2", "Betty");
        
        // SinglePreferrer prefers Alice then Betty (but empty set will be inserted at position 0)
        List<Proposee> singlePreferrerList = Arrays.asList(alice, betty);
        PreferenceList<Proposee> singlePreferrerPrefs = new PreferenceList<>(singlePreferrer, singlePreferrerList);
        
        // RegularGuy prefers Alice then Betty
        List<Proposee> regularGuyList = Arrays.asList(alice, betty);
        PreferenceList<Proposee> regularGuyPrefs = new PreferenceList<>(regularGuy, regularGuyList);
        
        // Alice prefers SinglePreferrer then RegularGuy
        List<Proposer> aliceList = Arrays.asList(singlePreferrer, regularGuy);
        PreferenceList<Proposer> alicePrefs = new PreferenceList<>(alice, aliceList);
        
        // Betty prefers RegularGuy then SinglePreferrer  
        List<Proposer> bettyList = Arrays.asList(regularGuy, singlePreferrer);
        PreferenceList<Proposer> bettyPrefs = new PreferenceList<>(betty, bettyList);
        
        Map<Proposer, PreferenceList<Proposee>> proposerPreferences = new HashMap<>();
        proposerPreferences.put(singlePreferrer, singlePreferrerPrefs);
        proposerPreferences.put(regularGuy, regularGuyPrefs);
        
        Map<Proposee, PreferenceList<Proposer>> proposeePreferences = new HashMap<>();
        proposeePreferences.put(alice, alicePrefs);
        proposeePreferences.put(betty, bettyPrefs);
        
        // Set empty set preference for SinglePreferrer at position 0 (prefers being single)
        Map<Proposer, Integer> emptySetPreferences = new HashMap<>();
        emptySetPreferences.put(singlePreferrer, 0);
        
        // Run algorithm
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            proposerPreferences, proposeePreferences, emptySetPreferences);
        
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        
        // SinglePreferrer should be matched to EmptySet (choosing to be single)
        assertThat(result.getFinalMatching().isMatched(singlePreferrer))
            .as("SinglePreferrer should be matched (to EmptySet)")
            .isTrue();
        assertThat(result.getFinalMatching().getMatch(singlePreferrer))
            .as("SinglePreferrer should be matched to EmptySet")
            .hasValue(EmptySet.getInstance());
            
        // RegularGuy should be matched to Alice (his first preference)
        assertThat(result.getFinalMatching().isMatched(regularGuy))
            .as("RegularGuy should be matched")
            .isTrue();
        assertThat(result.getFinalMatching().getMatch(regularGuy))
            .as("RegularGuy should be matched to Alice")
            .hasValue(alice);
            
        // Betty should be unmatched
        assertThat(result.getFinalMatching().isMatched(betty))
            .as("Betty should be unmatched")
            .isFalse();
    }
}