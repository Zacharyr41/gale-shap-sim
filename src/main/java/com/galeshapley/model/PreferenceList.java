package com.galeshapley.model;

import java.util.*;

public class PreferenceList<T extends Agent> {
    private final Agent owner;
    private final List<T> preferences;
    private final Map<T, Integer> rankMap;

    public PreferenceList(Agent owner, List<T> preferences) {
        this.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        this.preferences = new ArrayList<>(Objects.requireNonNull(preferences, "Preferences cannot be null"));
        this.rankMap = new HashMap<>();
        
        for (int i = 0; i < preferences.size(); i++) {
            rankMap.put(preferences.get(i), i);
        }
    }

    public Agent getOwner() {
        return owner;
    }

    public List<T> getPreferences() {
        return Collections.unmodifiableList(preferences);
    }

    public int getRank(T agent) {
        Integer rank = rankMap.get(agent);
        if (rank == null) {
            throw new IllegalArgumentException("Agent " + agent + " is not in preference list");
        }
        return rank;
    }

    public boolean prefers(T agent1, T agent2) {
        return getRank(agent1) < getRank(agent2);
    }

    public T getPreferredAt(int index) {
        if (index < 0 || index >= preferences.size()) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
        }
        return preferences.get(index);
    }

    public int size() {
        return preferences.size();
    }

    public boolean contains(T agent) {
        return rankMap.containsKey(agent);
    }

    @Override
    public String toString() {
        return String.format("PreferenceList[owner=%s, preferences=%s]", 
            owner.getName(), preferences);
    }
}