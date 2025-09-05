package com.galeshapley.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MatchingTest {
    
    private Matching matching;
    private Proposer prop1, prop2;
    private Proposee prosee1, prosee2;
    
    @BeforeEach
    void setUp() {
        matching = new Matching();
        prop1 = new Proposer("prop1", "Proposer 1");
        prop2 = new Proposer("prop2", "Proposer 2");
        prosee1 = new Proposee("prosee1", "Proposee 1");
        prosee2 = new Proposee("prosee2", "Proposee 2");
        
        matching.addProposer(prop1);
        matching.addProposer(prop2);
        matching.addProposee(prosee1);
        matching.addProposee(prosee2);
    }
    
    @Test
    void shouldStartWithAllUnmatched() {
        assertThat(matching.getUnmatchedProposers()).containsExactlyInAnyOrder(prop1, prop2);
        assertThat(matching.getUnmatchedProposees()).containsExactlyInAnyOrder(prosee1, prosee2);
        assertThat(matching.getMatchCount()).isEqualTo(0);
        assertThat(matching.isComplete()).isFalse();
    }
    
    @Test
    void shouldCreateMatch() {
        matching.match(prop1, prosee1);
        
        assertThat(matching.isMatched(prop1)).isTrue();
        assertThat(matching.isMatched(prosee1)).isTrue();
        assertThat(matching.isMatched(prop1, prosee1)).isTrue();
        assertThat(matching.getMatch(prop1)).contains(prosee1);
        assertThat(matching.getMatch(prosee1)).contains(prop1);
        assertThat(matching.getMatchCount()).isEqualTo(1);
        assertThat(matching.getUnmatchedProposers()).containsExactly(prop2);
        assertThat(matching.getUnmatchedProposees()).containsExactly(prosee2);
    }
    
    @Test
    void shouldHandleMultipleMatches() {
        matching.match(prop1, prosee1);
        matching.match(prop2, prosee2);
        
        assertThat(matching.getMatchCount()).isEqualTo(2);
        assertThat(matching.getUnmatchedProposers()).isEmpty();
        assertThat(matching.getUnmatchedProposees()).isEmpty();
        assertThat(matching.isComplete()).isTrue();
        assertThat(matching.getAllMatches()).containsExactlyInAnyOrderEntriesOf(
            java.util.Map.of(prop1, prosee1, prop2, prosee2)
        );
    }
    
    @Test
    void shouldReplaceExistingMatch() {
        matching.match(prop1, prosee1);
        matching.match(prop1, prosee2);
        
        assertThat(matching.isMatched(prop1, prosee2)).isTrue();
        assertThat(matching.isMatched(prop1, prosee1)).isFalse();
        assertThat(matching.getMatch(prop1)).contains(prosee2);
        assertThat(matching.getMatch(prosee2)).contains(prop1);
        assertThat(matching.isMatched(prosee1)).isFalse();
        assertThat(matching.getUnmatchedProposees()).contains(prosee1);
    }
    
    @Test
    void shouldBreakMatchFromProposee() {
        matching.match(prop1, prosee1);
        matching.match(prop2, prosee1);
        
        assertThat(matching.isMatched(prop2, prosee1)).isTrue();
        assertThat(matching.isMatched(prop1, prosee1)).isFalse();
        assertThat(matching.getMatch(prosee1)).contains(prop2);
        assertThat(matching.getMatch(prop2)).contains(prosee1);
        assertThat(matching.isMatched(prop1)).isFalse();
        assertThat(matching.getUnmatchedProposers()).contains(prop1);
    }
    
    @Test
    void shouldUnmatch() {
        matching.match(prop1, prosee1);
        matching.unmatch(prop1, prosee1);
        
        assertThat(matching.isMatched(prop1)).isFalse();
        assertThat(matching.isMatched(prosee1)).isFalse();
        assertThat(matching.getMatch(prop1)).isEmpty();
        assertThat(matching.getMatch(prosee1)).isEmpty();
        assertThat(matching.getUnmatchedProposers()).contains(prop1);
        assertThat(matching.getUnmatchedProposees()).contains(prosee1);
    }
    
    @Test
    void shouldNotUnmatchNonExistentMatch() {
        matching.match(prop1, prosee1);
        matching.unmatch(prop1, prosee2);
        
        assertThat(matching.isMatched(prop1, prosee1)).isTrue();
        assertThat(matching.getMatchCount()).isEqualTo(1);
    }
}