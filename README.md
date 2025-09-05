# Gale-Shapley Algorithm Simulator

A Java-based simulator for the Gale-Shapley stable matching algorithm with support for configuration via YAML files and comprehensive observation capabilities.

## Features

- **Object-Oriented Design**: Clean separation of concerns with models for Proposers, Proposees, Preferences, and Matchings
- **YAML Configuration**: Easy configuration of simulation scenarios using YAML files
- **Observer Pattern**: Monitor algorithm execution with built-in console and statistics observers
- **Test-Driven Development**: Comprehensive unit tests using JUnit 5
- **Extensible Architecture**: Easy to add constraints and edge cases in future iterations

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/galeshapley/
│   │       ├── Main.java                 # Entry point
│   │       ├── model/                    # Domain models
│   │       │   ├── Agent.java
│   │       │   ├── Proposer.java
│   │       │   ├── Proposee.java
│   │       │   ├── PreferenceList.java
│   │       │   └── Matching.java
│   │       ├── algorithm/                # Algorithm implementation
│   │       │   └── GaleShapleyAlgorithm.java
│   │       ├── config/                   # Configuration handling
│   │       │   ├── SimulationConfig.java
│   │       │   ├── SimulationConfigLoader.java
│   │       │   └── YamlConfig.java
│   │       └── observer/                 # Algorithm observers
│   │           ├── AlgorithmObserver.java
│   │           ├── ConsoleObserver.java
│   │           └── StatisticsObserver.java
│   └── resources/
│       └── example-config.yaml           # Example configuration
└── test/
    └── java/
        └── com/galeshapley/              # Unit tests
```

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

```bash
mvn clean compile
```

## Running Tests

```bash
mvn test
```

## Running the Simulator

### Using the example configuration:

```bash
mvn exec:java -Dexec.mainClass="com.galeshapley.Main"
```

### Using a custom YAML configuration:

```bash
mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="path/to/your/config.yaml"
```

## YAML Configuration Format

```yaml
simulation:
  proposers:
    - id: m1
      name: Alice
    - id: m2
      name: Bob
  proposees:
    - id: w1
      name: Eve
    - id: w2
      name: Fiona
  proposerPreferences:
    m1: [w1, w2]
    m2: [w2, w1]
  proposeePreferences:
    w1: [m2, m1]
    w2: [m1, m2]
```

## Key Components

### Models
- **Agent**: Base class for participants in the matching
- **Proposer**: Agents who propose in the algorithm
- **Proposee**: Agents who receive proposals
- **PreferenceList**: Ordered preferences for each agent
- **Matching**: Represents the current state of matchings

### Algorithm
- **GaleShapleyAlgorithm**: Core implementation of the stable matching algorithm
- Supports observers for monitoring execution
- Returns results including final matching and iteration count

### Configuration
- **SimulationConfig**: Internal configuration representation
- **SimulationConfigLoader**: Loads configuration from YAML files
- **YamlConfig**: YAML-specific data structures

### Observers
- **AlgorithmObserver**: Interface for observing algorithm events
- **ConsoleObserver**: Prints algorithm progress to console
- **StatisticsObserver**: Collects statistics about the execution

## Future Enhancements

- Support for incomplete preference lists
- Constraints on matching (e.g., capacity constraints)
- Multiple stable matching exploration
- Visualization of the matching process
- Performance optimization for large-scale simulations
- REST API for remote simulation execution

## Dependencies

- Jackson (for YAML parsing)
- JUnit 5 (for testing)
- AssertJ (for fluent assertions)
- Mockito (for mocking in tests)
