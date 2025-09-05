package com.galeshapley.model;

import java.util.*;

public class Matching {
    private final Map<Proposer, Proposee> proposerToProposee;
    private final Map<Proposee, Proposer> proposeeToProposer;
    private final Set<Proposer> unmatchedProposers;
    private final Set<Proposee> unmatchedProposees;

    public Matching() {
        this.proposerToProposee = new HashMap<>();
        this.proposeeToProposer = new HashMap<>();
        this.unmatchedProposers = new HashSet<>();
        this.unmatchedProposees = new HashSet<>();
    }

    public void addProposer(Proposer proposer) {
        unmatchedProposers.add(proposer);
    }

    public void addProposee(Proposee proposee) {
        unmatchedProposees.add(proposee);
    }

    public void match(Proposer proposer, Proposee proposee) {
        Proposee previousProposeeMatch = proposerToProposee.get(proposer);
        if (previousProposeeMatch != null) {
            proposeeToProposer.remove(previousProposeeMatch);
            unmatchedProposees.add(previousProposeeMatch);
        }

        Proposer previousProposerMatch = proposeeToProposer.get(proposee);
        if (previousProposerMatch != null) {
            proposerToProposee.remove(previousProposerMatch);
            unmatchedProposers.add(previousProposerMatch);
        }

        proposerToProposee.put(proposer, proposee);
        proposeeToProposer.put(proposee, proposer);
        unmatchedProposers.remove(proposer);
        unmatchedProposees.remove(proposee);
    }

    public void unmatch(Proposer proposer, Proposee proposee) {
        if (isMatched(proposer, proposee)) {
            proposerToProposee.remove(proposer);
            proposeeToProposer.remove(proposee);
            unmatchedProposers.add(proposer);
            unmatchedProposees.add(proposee);
        }
    }

    public boolean isMatched(Proposer proposer, Proposee proposee) {
        return proposee.equals(proposerToProposee.get(proposer)) &&
               proposer.equals(proposeeToProposer.get(proposee));
    }

    public boolean isMatched(Proposer proposer) {
        return proposerToProposee.containsKey(proposer);
    }

    public boolean isMatched(Proposee proposee) {
        return proposeeToProposer.containsKey(proposee);
    }

    public Optional<Proposee> getMatch(Proposer proposer) {
        return Optional.ofNullable(proposerToProposee.get(proposer));
    }

    public Optional<Proposer> getMatch(Proposee proposee) {
        return Optional.ofNullable(proposeeToProposer.get(proposee));
    }

    public Set<Proposer> getUnmatchedProposers() {
        return Collections.unmodifiableSet(unmatchedProposers);
    }

    public Set<Proposee> getUnmatchedProposees() {
        return Collections.unmodifiableSet(unmatchedProposees);
    }

    public Map<Proposer, Proposee> getAllMatches() {
        return Collections.unmodifiableMap(proposerToProposee);
    }

    public int getMatchCount() {
        return proposerToProposee.size();
    }

    public boolean isComplete() {
        return unmatchedProposers.isEmpty() || unmatchedProposees.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Matching{\n");
        proposerToProposee.forEach((proposer, proposee) -> {
            sb.append("  ").append(proposer.getName());
            if (proposee instanceof EmptySet) {
                sb.append(" -> single\n");
            } else {
                sb.append(" <-> ").append(proposee.getName()).append("\n");
            }
        });
        if (!unmatchedProposers.isEmpty()) {
            sb.append("  Unmatched Proposers: ");
            unmatchedProposers.forEach(p -> sb.append(p.getName()).append(" "));
            sb.append("\n");
        }
        if (!unmatchedProposees.isEmpty()) {
            sb.append("  Unmatched Proposees: ");
            unmatchedProposees.forEach(p -> sb.append(p.getName()).append(" "));
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}