package gg.bayes.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import gg.bayes.challenge.entity.Match;

public interface MatchRepository extends JpaRepository<Match, String> {
}
