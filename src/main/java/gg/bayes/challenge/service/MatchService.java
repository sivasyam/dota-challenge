package gg.bayes.challenge.service;

import java.util.List;

import gg.bayes.challenge.rest.model.HeroDamage;
import gg.bayes.challenge.rest.model.HeroItems;
import gg.bayes.challenge.rest.model.HeroKills;
import gg.bayes.challenge.rest.model.HeroSpells;

public interface MatchService {
    Long ingestMatch(String payload);
    List<HeroKills> heroKills(Long matchId);
    List<HeroItems> heroItems(Long matchId, String heroName);
    List<HeroSpells> heroSpells(Long matchId, String heroName);
    List<HeroDamage> heroDamage(Long matchId, String heroName);
}
