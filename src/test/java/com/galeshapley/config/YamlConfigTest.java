package com.galeshapley.config;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class YamlConfigTest {
    
    @Test
    void shouldLoadSimpleYamlConfig() throws IOException {
        String yamlContent = 
            "simulation:\n" +
            "  proposers:\n" +
            "    - id: m1\n" +
            "      name: Man 1\n" +
            "    - id: m2\n" +
            "      name: Man 2\n" +
            "  proposees:\n" +
            "    - id: w1\n" +
            "      name: Woman 1\n" +
            "    - id: w2\n" +
            "      name: Woman 2\n" +
            "  proposerPreferences:\n" +
            "    m1: [w1, w2]\n" +
            "    m2: [w2, w1]\n" +
            "  proposeePreferences:\n" +
            "    w1: [m2, m1]\n" +
            "    w2: [m1, m2]\n";
        
        YamlConfig yamlConfig = YamlConfig.loadFromString(yamlContent);
        
        assertThat(yamlConfig.getSimulation()).isNotNull();
        assertThat(yamlConfig.getSimulation().getProposers()).hasSize(2);
        assertThat(yamlConfig.getSimulation().getProposees()).hasSize(2);
        assertThat(yamlConfig.getSimulation().getProposerPreferences()).hasSize(2);
        assertThat(yamlConfig.getSimulation().getProposeePreferences()).hasSize(2);
    }
    
    @Test
    void shouldConvertToSimulationConfig() throws IOException {
        String yamlContent = 
            "simulation:\n" +
            "  proposers:\n" +
            "    - id: m1\n" +
            "      name: Man 1\n" +
            "    - id: m2\n" +
            "      name: Man 2\n" +
            "    - id: m3\n" +
            "      name: Man 3\n" +
            "  proposees:\n" +
            "    - id: w1\n" +
            "      name: Woman 1\n" +
            "    - id: w2\n" +
            "      name: Woman 2\n" +
            "    - id: w3\n" +
            "      name: Woman 3\n" +
            "  proposerPreferences:\n" +
            "    m1: [w1, w2, w3]\n" +
            "    m2: [w2, w3, w1]\n" +
            "    m3: [w3, w1, w2]\n" +
            "  proposeePreferences:\n" +
            "    w1: [m1, m2, m3]\n" +
            "    w2: [m2, m3, m1]\n" +
            "    w3: [m3, m1, m2]\n";
        
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromString(yamlContent);
        
        assertThat(config.getProposers()).hasSize(3);
        assertThat(config.getProposees()).hasSize(3);
        assertThat(config.getProposerPreferences()).hasSize(3);
        assertThat(config.getProposeePreferences()).hasSize(3);
        
        // Verify the configuration can be used with the algorithm
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            config.getProposerPreferences(),
            config.getProposeePreferences()
        );
        
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        assertThat(result.getFinalMatching().isComplete()).isTrue();
        assertThat(result.getFinalMatching().getMatchCount()).isEqualTo(3);
    }
    
    @Test
    void shouldThrowExceptionForInvalidProposerInPreferences() throws IOException {
        String yamlContent = 
            "simulation:\n" +
            "  proposers:\n" +
            "    - id: m1\n" +
            "      name: Man 1\n" +
            "  proposees:\n" +
            "    - id: w1\n" +
            "      name: Woman 1\n" +
            "  proposerPreferences:\n" +
            "    m2: [w1]\n" +  // m2 doesn't exist
            "  proposeePreferences:\n" +
            "    w1: [m1]\n";
        
        SimulationConfigLoader loader = new SimulationConfigLoader();
        
        assertThatThrownBy(() -> loader.loadFromString(yamlContent))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown proposer ID");
    }
}