package com.galeshapley.config;

import com.galeshapley.model.*;
import java.util.*;

public class SimulationConfig {
    private final Set<Proposer> proposers;
    private final Set<Proposee> proposees;
    private final Map<Proposer, PreferenceList<Proposee>> proposerPreferences;
    private final Map<Proposee, PreferenceList<Proposer>> proposeePreferences;
    private final Map<Proposer, Integer> emptySetPreferences;
    
    private SimulationConfig(Builder builder) {
        this.proposers = Collections.unmodifiableSet(new HashSet<>(builder.proposers));
        this.proposees = Collections.unmodifiableSet(new HashSet<>(builder.proposees));
        this.proposerPreferences = Collections.unmodifiableMap(new HashMap<>(builder.proposerPreferences));
        this.proposeePreferences = Collections.unmodifiableMap(new HashMap<>(builder.proposeePreferences));
        this.emptySetPreferences = Collections.unmodifiableMap(new HashMap<>(builder.emptySetPreferences));
        validate();
    }
    
    private void validate() {
        if (proposers.isEmpty()) {
            throw new IllegalStateException("Configuration must have at least one proposer");
        }
        if (proposees.isEmpty()) {
            throw new IllegalStateException("Configuration must have at least one proposee");
        }
        
        for (Proposer proposer : proposers) {
            if (!proposerPreferences.containsKey(proposer)) {
                throw new IllegalStateException("Proposer " + proposer.getName() + " has no preferences");
            }
            PreferenceList<Proposee> prefs = proposerPreferences.get(proposer);
            // Skip validation for proposers with empty set preferences (they don't need all proposees)
            if (!emptySetPreferences.containsKey(proposer) && !prefs.getPreferences().containsAll(proposees)) {
                throw new IllegalStateException("Proposer " + proposer.getName() + 
                    " does not have preferences for all proposees");
            }
        }
        
        for (Proposee proposee : proposees) {
            if (!proposeePreferences.containsKey(proposee)) {
                throw new IllegalStateException("Proposee " + proposee.getName() + " has no preferences");
            }
            PreferenceList<Proposer> prefs = proposeePreferences.get(proposee);
            if (!prefs.getPreferences().containsAll(proposers)) {
                throw new IllegalStateException("Proposee " + proposee.getName() + 
                    " does not have preferences for all proposers");
            }
        }
    }
    
    public Set<Proposer> getProposers() {
        return proposers;
    }
    
    public Set<Proposee> getProposees() {
        return proposees;
    }
    
    public Map<Proposer, PreferenceList<Proposee>> getProposerPreferences() {
        return proposerPreferences;
    }
    
    public Map<Proposee, PreferenceList<Proposer>> getProposeePreferences() {
        return proposeePreferences;
    }
    
    public Map<Proposer, Integer> getEmptySetPreferences() {
        return emptySetPreferences;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final Set<Proposer> proposers = new HashSet<>();
        private final Set<Proposee> proposees = new HashSet<>();
        private final Map<Proposer, PreferenceList<Proposee>> proposerPreferences = new HashMap<>();
        private final Map<Proposee, PreferenceList<Proposer>> proposeePreferences = new HashMap<>();
        private final Map<Proposer, Integer> emptySetPreferences = new HashMap<>();
        
        public Builder addProposer(Proposer proposer) {
            proposers.add(proposer);
            return this;
        }
        
        public Builder addProposers(Proposer... proposers) {
            this.proposers.addAll(Arrays.asList(proposers));
            return this;
        }
        
        public Builder addProposee(Proposee proposee) {
            proposees.add(proposee);
            return this;
        }
        
        public Builder addProposees(Proposee... proposees) {
            this.proposees.addAll(Arrays.asList(proposees));
            return this;
        }
        
        public Builder setProposerPreferences(Proposer proposer, List<Proposee> preferences) {
            if (!proposers.contains(proposer)) {
                throw new IllegalArgumentException("Proposer " + proposer + " not in configuration");
            }
            proposerPreferences.put(proposer, new PreferenceList<>(proposer, preferences));
            return this;
        }
        
        public Builder setProposeePreferences(Proposee proposee, List<Proposer> preferences) {
            if (!proposees.contains(proposee)) {
                throw new IllegalArgumentException("Proposee " + proposee + " not in configuration");
            }
            proposeePreferences.put(proposee, new PreferenceList<>(proposee, preferences));
            return this;
        }
        
        public Builder setEmptySetPreference(Proposer proposer, int position) {
            if (!proposers.contains(proposer)) {
                throw new IllegalArgumentException("Proposer " + proposer + " not in configuration");
            }
            emptySetPreferences.put(proposer, position);
            return this;
        }
        
        public SimulationConfig build() {
            return new SimulationConfig(this);
        }
    }
}