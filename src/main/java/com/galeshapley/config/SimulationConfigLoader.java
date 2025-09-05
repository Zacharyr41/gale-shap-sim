package com.galeshapley.config;

import com.galeshapley.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class SimulationConfigLoader {
    
    private final ObjectMapper yamlMapper;
    
    public SimulationConfigLoader() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }
    
    public SimulationConfig loadFromFile(String filePath) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromFile(filePath);
        return buildSimulationConfig(yamlConfig);
    }
    
    public SimulationConfig loadFromFile(File file) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromFile(file);
        return buildSimulationConfig(yamlConfig);
    }
    
    public SimulationConfig loadFromStream(InputStream stream) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromStream(stream);
        return buildSimulationConfig(yamlConfig);
    }
    
    public SimulationConfig loadFromString(String yamlContent) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromString(yamlContent);
        return buildSimulationConfig(yamlConfig);
    }
    
    private SimulationConfig buildSimulationConfig(YamlConfig yamlConfig) {
        YamlConfig.SimulationData simData = yamlConfig.getSimulation();
        
        // Create proposers
        Map<String, Proposer> proposerMap = new HashMap<>();
        for (YamlConfig.AgentData agentData : simData.getProposers()) {
            Proposer proposer = new Proposer(agentData.getId(), agentData.getName());
            proposerMap.put(agentData.getId(), proposer);
        }
        
        // Create proposees
        Map<String, Proposee> proposeeMap = new HashMap<>();
        for (YamlConfig.AgentData agentData : simData.getProposees()) {
            Proposee proposee = new Proposee(agentData.getId(), agentData.getName());
            proposeeMap.put(agentData.getId(), proposee);
        }
        
        // Build configuration
        SimulationConfig.Builder builder = SimulationConfig.builder();
        
        // Add all agents
        proposerMap.values().forEach(builder::addProposer);
        proposeeMap.values().forEach(builder::addProposee);
        
        // Set proposer preferences
        for (Map.Entry<String, PreferenceConfig> entry : simData.getProposerPreferences().entrySet()) {
            Proposer proposer = proposerMap.get(entry.getKey());
            if (proposer == null) {
                throw new IllegalArgumentException("Unknown proposer ID in preferences: " + entry.getKey());
            }
            
            PreferenceConfig prefConfig = entry.getValue();
            List<String> rawPreferences = resolvePreferences(prefConfig, proposeeMap.keySet());
            
            // Find position of empty set (if any) in original preference list
            int emptySetPosition = rawPreferences.indexOf("∅");
            
            List<Proposee> preferenceList = rawPreferences.stream()
                .filter(id -> !id.equals("∅"))  // Filter out empty set symbol
                .map(id -> {
                    Proposee proposee = proposeeMap.get(id);
                    if (proposee == null) {
                        throw new IllegalArgumentException("Unknown proposee ID in preferences: " + id);
                    }
                    return proposee;
                })
                .collect(Collectors.toList());
            
            builder.setProposerPreferences(proposer, preferenceList);
            
            // If empty set was found, set the preference cutoff
            if (emptySetPosition != -1) {
                builder.setEmptySetPreference(proposer, emptySetPosition);
            }
        }
        
        // Set proposee preferences
        for (Map.Entry<String, PreferenceConfig> entry : simData.getProposeePreferences().entrySet()) {
            Proposee proposee = proposeeMap.get(entry.getKey());
            if (proposee == null) {
                throw new IllegalArgumentException("Unknown proposee ID in preferences: " + entry.getKey());
            }
            
            PreferenceConfig prefConfig = entry.getValue();
            List<String> rawPreferences = resolvePreferences(prefConfig, proposerMap.keySet());
            
            // Find position of empty set (if any) in original preference list
            int emptySetPosition = rawPreferences.indexOf("∅");
            
            List<Proposer> preferenceList = rawPreferences.stream()
                .filter(id -> !id.equals("∅"))  // Filter out empty set symbol
                .map(id -> {
                    Proposer proposer = proposerMap.get(id);
                    if (proposer == null) {
                        throw new IllegalArgumentException("Unknown proposer ID in preferences: " + id);
                    }
                    return proposer;
                })
                .collect(Collectors.toList());
            
            builder.setProposeePreferences(proposee, preferenceList);
            
            // If empty set was found, set the proposee empty set preference
            if (emptySetPosition != -1) {
                builder.setProposeeEmptySetPreference(proposee, emptySetPosition);
            }
        }
        
        return builder.build();
    }
    
    /**
     * Resolve preferences from either explicit list or generator configuration.
     */
    private List<String> resolvePreferences(PreferenceConfig config, Set<String> candidateIds) {
        if (config.isExplicit()) {
            return config.getExplicit();
        } else {
            PreferenceConfig.GeneratorConfig genConfig = config.getGenerator();
            if (genConfig.isRandom()) {
                Random random;
                if (genConfig.getSeed() != null) {
                    random = new Random(genConfig.getSeed());
                } else {
                    random = new Random();
                }
                
                PreferenceGenerator generator = new PreferenceGenerator(
                    random, true, PreferenceGenerator.EmptySetPlacement.RANDOM);
                
                boolean includeEmptySet = random.nextDouble() < genConfig.getEmptySetProbability();
                return generator.generatePreferencesFromIds(candidateIds, includeEmptySet);
            } else {
                throw new IllegalArgumentException("Only random preference generation is currently supported");
            }
        }
    }
}