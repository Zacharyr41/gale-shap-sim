package com.galeshapley.model;

import java.util.Objects;

public abstract class Agent {
    private final String id;
    private final String name;
    protected final boolean isEmptySet;

    protected Agent(String id, String name) {
        this(id, name, false);
    }
    
    protected Agent(String id, String name, boolean isEmptySet) {
        this.id = Objects.requireNonNull(id, "Agent ID cannot be null");
        this.name = Objects.requireNonNull(name, "Agent name cannot be null");
        this.isEmptySet = isEmptySet;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public boolean isEmptySet() {
        return isEmptySet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return Objects.equals(id, agent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, name=%s]", 
            getClass().getSimpleName(), id, name);
    }
}