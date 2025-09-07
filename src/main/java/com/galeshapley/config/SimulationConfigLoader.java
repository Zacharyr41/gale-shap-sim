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
        return buildSimulationConfig(yamlConfig, null);
    }
    
    public SimulationConfig loadFromFile(String filePath, RuntimeOptions runtimeOptions) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromFile(filePath);
        return buildSimulationConfig(yamlConfig, runtimeOptions);
    }
    
    public SimulationConfig loadFromFile(File file) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromFile(file);
        return buildSimulationConfig(yamlConfig, null);
    }
    
    public SimulationConfig loadFromFile(File file, RuntimeOptions runtimeOptions) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromFile(file);
        return buildSimulationConfig(yamlConfig, runtimeOptions);
    }
    
    public SimulationConfig loadFromStream(InputStream stream) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromStream(stream);
        return buildSimulationConfig(yamlConfig, null);
    }
    
    public SimulationConfig loadFromStream(InputStream stream, RuntimeOptions runtimeOptions) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromStream(stream);
        return buildSimulationConfig(yamlConfig, runtimeOptions);
    }
    
    public SimulationConfig loadFromString(String yamlContent) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromString(yamlContent);
        return buildSimulationConfig(yamlConfig, null);
    }
    
    public SimulationConfig loadFromString(String yamlContent, RuntimeOptions runtimeOptions) throws IOException {
        YamlConfig yamlConfig = YamlConfig.loadFromString(yamlContent);
        return buildSimulationConfig(yamlConfig, runtimeOptions);
    }
    
    private SimulationConfig buildSimulationConfig(YamlConfig yamlConfig, RuntimeOptions runtimeOptions) {
        YamlConfig.SimulationData simData = yamlConfig.getSimulation();
        
        // Create proposers
        Map<String, Proposer> proposerMap = new HashMap<>();
        
        // Check if we're using bulk generation or explicit agents
        if (simData.getProposerConfig() != null) {
            // Bulk generation mode
            int proposerCount = simData.getProposerConfig().getCount();
            for (int i = 0; i < proposerCount; i++) {
                String id = "p" + i;
                String name = "Proposer" + i;
                Proposer proposer = new Proposer(id, name);
                proposerMap.put(id, proposer);
            }
        } else if (simData.getProposers() != null) {
            // Explicit agents mode
            for (YamlConfig.AgentData agentData : simData.getProposers()) {
                Proposer proposer = new Proposer(agentData.getId(), agentData.getName());
                proposerMap.put(agentData.getId(), proposer);
            }
        }
        
        // Create proposees
        Map<String, Proposee> proposeeMap = new HashMap<>();
        
        // Check if we're using bulk generation or explicit agents
        if (simData.getProposeeConfig() != null) {
            // Bulk generation mode
            int proposeeCount = simData.getProposeeConfig().getCount();
            for (int i = 0; i < proposeeCount; i++) {
                String id = "e" + i;
                String name = "Proposee" + i;
                Proposee proposee = new Proposee(id, name);
                proposeeMap.put(id, proposee);
            }
        } else if (simData.getProposees() != null) {
            // Explicit agents mode
            for (YamlConfig.AgentData agentData : simData.getProposees()) {
                Proposee proposee = new Proposee(agentData.getId(), agentData.getName());
                proposeeMap.put(agentData.getId(), proposee);
            }
        }
        
        // Build configuration
        SimulationConfig.Builder builder = SimulationConfig.builder();
        
        // Add all agents
        proposerMap.values().forEach(builder::addProposer);
        proposeeMap.values().forEach(builder::addProposee);
        
        // Initialize seed generator if global seed is provided
        Random seedGenerator = null;
        if (runtimeOptions != null && runtimeOptions.getGlobalSeed() != null) {
            seedGenerator = new Random(runtimeOptions.getGlobalSeed());
        }
        
        // Set proposer preferences
        if (simData.getProposerConfig() != null) {
            // Bulk generation mode - generate preferences for each proposer
            YamlConfig.GeneratorConfig defaultGen = simData.getProposerConfig().getGenerator();
            
            for (Map.Entry<String, Proposer> entry : proposerMap.entrySet()) {
                String proposerId = entry.getKey();
                Proposer proposer = entry.getValue();
                int proposerIndex = Integer.parseInt(proposerId.substring(1)); // Remove 'p' prefix
                
                // Check for overrides
                YamlConfig.GeneratorConfig genConfig = defaultGen;
                if (simData.getProposerOverrides() != null) {
                    for (YamlConfig.AgentOverride override : simData.getProposerOverrides()) {
                        if (override.getIndex() != null && override.getIndex() == proposerIndex) {
                            genConfig = override.getGenerator();
                            break;
                        } else if (override.getRange() != null) {
                            if (proposerIndex >= override.getRange().getStart() && 
                                proposerIndex <= override.getRange().getEnd()) {
                                genConfig = override.getGenerator();
                                break;
                            }
                        }
                    }
                }
                
                // Generate preferences using the appropriate config
                PreferenceConfig prefConfig = PreferenceConfig.random(
                    genConfig.getEmptySetProbability(), 
                    null  // Don't pass individual seed, let resolvePreferences handle seeding
                );
                List<String> rawPreferences = resolvePreferences(prefConfig, proposeeMap.keySet(), seedGenerator);
                
                // Process preferences as before
                int emptySetPosition = rawPreferences.indexOf("∅");
                List<Proposee> preferenceList = rawPreferences.stream()
                    .filter(id -> !id.equals("∅"))
                    .map(id -> {
                        Proposee proposee = proposeeMap.get(id);
                        if (proposee == null) {
                            throw new IllegalArgumentException("Unknown proposee ID in preferences: " + id);
                        }
                        return proposee;
                    })
                    .collect(Collectors.toList());
                
                builder.setProposerPreferences(proposer, preferenceList);
                
                if (emptySetPosition != -1) {
                    builder.setEmptySetPreference(proposer, emptySetPosition);
                }
            }
        } else if (simData.getProposerPreferences() != null) {
            // Explicit preferences mode (existing code)
            for (Map.Entry<String, PreferenceConfig> entry : simData.getProposerPreferences().entrySet()) {
                Proposer proposer = proposerMap.get(entry.getKey());
                if (proposer == null) {
                    throw new IllegalArgumentException("Unknown proposer ID in preferences: " + entry.getKey());
                }
                
                PreferenceConfig prefConfig = entry.getValue();
                List<String> rawPreferences = resolvePreferences(prefConfig, proposeeMap.keySet(), seedGenerator);
                
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
        }
        
        // Set proposee preferences
        if (simData.getProposeeConfig() != null) {
            // Bulk generation mode - generate preferences for each proposee
            YamlConfig.GeneratorConfig defaultGen = simData.getProposeeConfig().getGenerator();
            
            for (Map.Entry<String, Proposee> entry : proposeeMap.entrySet()) {
                String proposeeId = entry.getKey();
                Proposee proposee = entry.getValue();
                int proposeeIndex = Integer.parseInt(proposeeId.substring(1)); // Remove 'e' prefix
                
                // Check for overrides
                YamlConfig.GeneratorConfig genConfig = defaultGen;
                if (simData.getProposeeOverrides() != null) {
                    for (YamlConfig.AgentOverride override : simData.getProposeeOverrides()) {
                        if (override.getIndex() != null && override.getIndex() == proposeeIndex) {
                            genConfig = override.getGenerator();
                            break;
                        } else if (override.getRange() != null) {
                            if (proposeeIndex >= override.getRange().getStart() && 
                                proposeeIndex <= override.getRange().getEnd()) {
                                genConfig = override.getGenerator();
                                break;
                            }
                        }
                    }
                }
                
                // Generate preferences using the appropriate config
                PreferenceConfig prefConfig = PreferenceConfig.random(
                    genConfig.getEmptySetProbability(), 
                    null  // Don't pass individual seed, let resolvePreferences handle seeding
                );
                List<String> rawPreferences = resolvePreferences(prefConfig, proposerMap.keySet(), seedGenerator);
                
                // Process preferences as before
                int emptySetPosition = rawPreferences.indexOf("∅");
                List<Proposer> preferenceList = rawPreferences.stream()
                    .filter(id -> !id.equals("∅"))
                    .map(id -> {
                        Proposer proposer = proposerMap.get(id);
                        if (proposer == null) {
                            throw new IllegalArgumentException("Unknown proposer ID in preferences: " + id);
                        }
                        return proposer;
                    })
                    .collect(Collectors.toList());
                
                builder.setProposeePreferences(proposee, preferenceList);
                
                if (emptySetPosition != -1) {
                    builder.setProposeeEmptySetPreference(proposee, emptySetPosition);
                }
            }
        } else if (simData.getProposeePreferences() != null) {
            // Explicit preferences mode (existing code)
            for (Map.Entry<String, PreferenceConfig> entry : simData.getProposeePreferences().entrySet()) {
                Proposee proposee = proposeeMap.get(entry.getKey());
                if (proposee == null) {
                    throw new IllegalArgumentException("Unknown proposee ID in preferences: " + entry.getKey());
                }
                
                PreferenceConfig prefConfig = entry.getValue();
                List<String> rawPreferences = resolvePreferences(prefConfig, proposerMap.keySet(), seedGenerator);
                
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
        }
        
        return builder.build();
    }
    
    /**
     * Resolve preferences from either explicit list or generator configuration.
     */
    private List<String> resolvePreferences(PreferenceConfig config, Set<String> candidateIds, Random seedGenerator) {
        if (config.isExplicit()) {
            return config.getExplicit();
        } else {
            PreferenceConfig.GeneratorConfig genConfig = config.getGenerator();
            if (genConfig.isRandom()) {
                Random random;
                if (seedGenerator != null) {
                    // Use global seed to generate individual seed for this agent
                    random = new Random(seedGenerator.nextLong());
                } else if (genConfig.getSeed() != null) {
                    // Fall back to individual seed if no global seed
                    random = new Random(genConfig.getSeed());
                } else {
                    random = new Random();
                }
                
                PreferenceGenerator generator = new PreferenceGenerator(
                    random, false, PreferenceGenerator.EmptySetPlacement.RANDOM);
                
                boolean includeEmptySet = random.nextDouble() < genConfig.getEmptySetProbability();
                return generator.generatePreferencesFromIds(candidateIds, includeEmptySet);
            } else {
                throw new IllegalArgumentException("Only random preference generation is currently supported");
            }
        }
    }
}