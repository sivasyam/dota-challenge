package gg.bayes.challenge.repository;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import gg.bayes.challenge.rest.model.HeroKills;

public class CustomKillsRepositoryImpl implements CustomKillsRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<HeroKills> findHeroCount(Long matchId) {
        Query query = em.createNativeQuery("SELECT count(*), hero from kills where match_id=" + matchId + " group by hero");
        List<Object[]> list = query.getResultList();
        return list.stream().map(p -> HeroKills.builder().kills(Integer.parseInt(p[0].toString())).hero(p[1].toString()).build()).collect(Collectors.toList());
    }
}
