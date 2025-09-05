package com.galeshapley.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PreferenceListTest {
    
    private Proposer owner;
    private Proposee p1, p2, p3;
    private List<Proposee> preferences;
    
    @BeforeEach
    void setUp() {
        owner = new Proposer("prop1", "Proposer 1");
        p1 = new Proposee("p1", "Proposee 1");
        p2 = new Proposee("p2", "Proposee 2");
        p3 = new Proposee("p3", "Proposee 3");
        preferences = Arrays.asList(p1, p2, p3);
    }
    
    @Test
    void shouldCreatePreferenceList() {
        PreferenceList<Proposee> prefList = new PreferenceList<>(owner, preferences);
        
        assertThat(prefList.getOwner()).isEqualTo(owner);
        assertThat(prefList.getPreferences()).containsExactly(p1, p2, p3);
        assertThat(prefList.size()).isEqualTo(3);
    }
    
    @Test
    void shouldGetCorrectRanks() {
        PreferenceList<Proposee> prefList = new PreferenceList<>(owner, preferences);
        
        assertThat(prefList.getRank(p1)).isEqualTo(0);
        assertThat(prefList.getRank(p2)).isEqualTo(1);
        assertThat(prefList.getRank(p3)).isEqualTo(2);
    }
    
    @Test
    void shouldDeterminePreference() {
        PreferenceList<Proposee> prefList = new PreferenceList<>(owner, preferences);
        
        assertThat(prefList.prefers(p1, p2)).isTrue();
        assertThat(prefList.prefers(p1, p3)).isTrue();
        assertThat(prefList.prefers(p2, p3)).isTrue();
        assertThat(prefList.prefers(p2, p1)).isFalse();
        assertThat(prefList.prefers(p3, p1)).isFalse();
        assertThat(prefList.prefers(p3, p2)).isFalse();
    }
    
    @Test
    void shouldGetPreferredAtIndex() {
        PreferenceList<Proposee> prefList = new PreferenceList<>(owner, preferences);
        
        assertThat(prefList.getPreferredAt(0)).isEqualTo(p1);
        assertThat(prefList.getPreferredAt(1)).isEqualTo(p2);
        assertThat(prefList.getPreferredAt(2)).isEqualTo(p3);
    }
    
    @Test
    void shouldThrowExceptionForInvalidIndex() {
        PreferenceList<Proposee> prefList = new PreferenceList<>(owner, preferences);
        
        assertThatThrownBy(() -> prefList.getPreferredAt(-1))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> prefList.getPreferredAt(3))
            .isInstanceOf(IndexOutOfBoundsException.class);
    }
    
    @Test
    void shouldCheckContains() {
        PreferenceList<Proposee> prefList = new PreferenceList<>(owner, preferences);
        Proposee notInList = new Proposee("p4", "Proposee 4");
        
        assertThat(prefList.contains(p1)).isTrue();
        assertThat(prefList.contains(p2)).isTrue();
        assertThat(prefList.contains(p3)).isTrue();
        assertThat(prefList.contains(notInList)).isFalse();
    }
    
    @Test
    void shouldThrowExceptionForUnknownAgentRank() {
        PreferenceList<Proposee> prefList = new PreferenceList<>(owner, preferences);
        Proposee unknown = new Proposee("unknown", "Unknown");
        
        assertThatThrownBy(() -> prefList.getRank(unknown))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not in preference list");
    }
}