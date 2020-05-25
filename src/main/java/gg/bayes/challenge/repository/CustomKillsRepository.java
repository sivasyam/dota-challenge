package gg.bayes.challenge.repository;

import java.util.List;

import gg.bayes.challenge.rest.model.HeroKills;

public interface CustomKillsRepository {
    List<HeroKills> findHeroCount(Long matchId);
}
