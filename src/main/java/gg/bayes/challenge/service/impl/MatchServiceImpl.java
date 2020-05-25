package gg.bayes.challenge.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import gg.bayes.challenge.entity.Casts;
import gg.bayes.challenge.entity.Hits;
import gg.bayes.challenge.entity.Items;
import gg.bayes.challenge.entity.Kills;
import gg.bayes.challenge.entity.Match;
import gg.bayes.challenge.repository.CastsRepository;
import gg.bayes.challenge.repository.HitsRepository;
import gg.bayes.challenge.repository.ItemsRepository;
import gg.bayes.challenge.repository.KillerRepository;
import gg.bayes.challenge.repository.MatchRepository;
import gg.bayes.challenge.rest.model.HeroDamage;
import gg.bayes.challenge.rest.model.HeroItems;
import gg.bayes.challenge.rest.model.HeroKills;
import gg.bayes.challenge.rest.model.HeroSpells;
import gg.bayes.challenge.service.MatchService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static gg.bayes.challenge.utils.Constants.ABILITY;
import static gg.bayes.challenge.utils.Constants.ARROW;
import static gg.bayes.challenge.utils.Constants.BUYS;
import static gg.bayes.challenge.utils.Constants.BY_NPC_DOTA;
import static gg.bayes.challenge.utils.Constants.BY_NPC_DOTA_HERO;
import static gg.bayes.challenge.utils.Constants.CASTS;
import static gg.bayes.challenge.utils.Constants.CLOSE_BRACKET;
import static gg.bayes.challenge.utils.Constants.CLOSE_SQ_BRACKET;
import static gg.bayes.challenge.utils.Constants.DAMAGE;
import static gg.bayes.challenge.utils.Constants.FOR;
import static gg.bayes.challenge.utils.Constants.HERO;
import static gg.bayes.challenge.utils.Constants.HITS;
import static gg.bayes.challenge.utils.Constants.HITS_NPC_DOTA_HERO;
import static gg.bayes.challenge.utils.Constants.IS;
import static gg.bayes.challenge.utils.Constants.ITEM;
import static gg.bayes.challenge.utils.Constants.LEVEL;
import static gg.bayes.challenge.utils.Constants.NPC_DOTA;
import static gg.bayes.challenge.utils.Constants.NPC_DOTA_HERO;
import static gg.bayes.challenge.utils.Constants.ON_NPC_DOTA;
import static gg.bayes.challenge.utils.Constants.ON_DATA;
import static gg.bayes.challenge.utils.Constants.OPEN_BRACKET;
import static gg.bayes.challenge.utils.Constants.WITH;

@Slf4j
@Service
@AllArgsConstructor
@NoArgsConstructor
public class MatchServiceImpl implements MatchService {

    @Autowired
    private ItemsRepository itemsRepository;

    @Autowired
    private HitsRepository hitsRepository;

    @Autowired
    private KillerRepository killerRepository;

    @Autowired
    private CastsRepository castsRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Value("${pattern.killed.1}")
    private String killsPattern1;

    @Value("${pattern.killed.2}")
    private String killsPattern2;

    @Value("${pattern.hit.1}")
    private String hitsPattern1;

    @Value("${pattern.hit.2}")
    private String hitsPattern2;

    @Value("${pattern.item}")
    private String itemsPattern;

    @Value("${pattern.casts}")
    private String castsPattern;

    /**
     * Method to process payload
     */
    @Override
    public Long ingestMatch(String payload) {
        try {
            log.info("processing payload");
            return processPayload(payload);
        } catch (Exception e) {
            log.error("Exception occurred :{}", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Occurred while processing payload", e);
        }
    }

    /**
     *
     */
    @Override
    public List<HeroKills> heroKills(Long matchId) { //TODO
        List<HeroKills> list = killerRepository.findHeroCount(matchId);
        return list;
    }

    /**
     * Method to get list of items purchased by hero
     */
    @Override
    public List<HeroItems> heroItems(Long matchId, String heroName) {
        try {
            List<Items> items = itemsRepository.getByNameAndMatchId(heroName, matchId);
            validateResult(items);
            return items.stream().map(p -> HeroItems.builder()
                    .item(p.getItem()).timestamp(p.getTimeOfEvent()).build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Exception occurred :{}", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Occurred while processing payload", e);
        }
    }

    /**
     * Method to get spells
     */
    @Override
    public List<HeroSpells> heroSpells(Long matchId, String heroName) {
        try {
            List<Casts> casts = castsRepository.getByNameAndMatchId(heroName, matchId);
            validateResult(casts);

            return casts.stream().map(p -> HeroSpells.builder().casts(Integer.parseInt(p.getLevel()))
                    .spell(p.getCasts()).build()).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Exception occurred :{}", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Occurred while processing payload", e);
        }
    }

    /**
     * Method to get damages
     */
    @Override
    public List<HeroDamage> heroDamage(Long matchId, String heroName) {
        try {
            List<Hits> hits = hitsRepository.getByNameAndMatchId(heroName, matchId);
            validateResult(hits);
            return hits.stream().map(p -> HeroDamage.builder()
                    .damageInstances(p.getDamageCount().intValue())
                    .target(p.getWeapon()).totalDamage(p.getTotalDamages().intValue()).build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Exception occurred :{}", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Occurred while processing payload", e);
        }
    }

    /**
     * Method to process payload
     */
    private Long processPayload(String payload) throws IOException {
        List<String> lines = Arrays.asList(payload.split("\\["));

        Match match = matchRepository.save(Match.builder().build());
        List<Kills> kills = new LinkedList();
        List<Items> items = new LinkedList();
        List<Casts> casts = new LinkedList();
        List<Hits> hits = new LinkedList();
        lines.stream().forEach(p -> {
            String stringToSearch = p;
            if (stringToSearch.matches(killsPattern1)) {
                buildKills(stringToSearch, match, kills);
                killerRepository.saveAll(kills);
            } else if (stringToSearch.matches(itemsPattern)) {
                buildItems(stringToSearch, match, items);
                itemsRepository.saveAll(items);
            } else if (stringToSearch.matches(castsPattern)) {
                buildCasts(stringToSearch, match, casts);
                castsRepository.saveAll(casts);
            } else if (stringToSearch.matches(hitsPattern1)
                    || stringToSearch.matches(hitsPattern2)) {
                buildHits(stringToSearch, match, hits);
                hitsRepository.saveAll(hits);
            } else {
                log.warn("Ignored lines:  " + stringToSearch);
            }
        });
        return match.getId();
    }

    /**
     *
     */
    private void validateResult(List resultList) {
        if (resultList.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data found");
        }
    }

    /**
     * Method to build kills
     */
    private void buildKills(String stringToSearch, Match match, List<Kills> entity) {

        String killedPerson = StringUtils.contains(StringUtils.substringBetween(stringToSearch, NPC_DOTA, IS).trim(), HERO)
                ? StringUtils.substringBetween(stringToSearch, NPC_DOTA_HERO, IS).trim() :
                StringUtils.substringBetween(stringToSearch, NPC_DOTA, IS).trim();
        String hero = StringUtils.isNotEmpty(StringUtils.substringAfterLast(stringToSearch, BY_NPC_DOTA_HERO).trim()) ?
                StringUtils.substringAfterLast(stringToSearch, BY_NPC_DOTA_HERO).trim()
                : StringUtils.substringAfterLast(stringToSearch, BY_NPC_DOTA).trim();
        entity.add(Kills.builder()
                .timeOfEvent(buildTimeStamp(StringUtils.substringBefore(stringToSearch, CLOSE_SQ_BRACKET).trim()))
                .hero(hero)
                .match(match)
                .killed(killedPerson).build());
    }

    /**
     * Method to build hits entity
     */
    private void buildHits(String stringToSearch, Match match, List<Hits> hits) {
        String totalDamages = null;

        if (stringToSearch != null && StringUtils.isNotEmpty(StringUtils.substringAfterLast(stringToSearch, ARROW).trim())) {
            totalDamages = StringUtils.substringBetween(stringToSearch, OPEN_BRACKET, ARROW).trim();
        }
        hits.add(Hits.builder()
                .timeOfEvent(buildTimeStamp(StringUtils.substringBefore(stringToSearch, CLOSE_SQ_BRACKET).trim()))
                .name(StringUtils.substringBetween(stringToSearch, NPC_DOTA_HERO, HITS).trim())
                .match(match)
                .hit(StringUtils.substringBetween(stringToSearch, HITS_NPC_DOTA_HERO, WITH).trim())
                .weapon(StringUtils.substringBetween(stringToSearch, WITH, FOR).trim())
                .damageCount(Long.parseLong(StringUtils.substringBetween(stringToSearch, FOR, DAMAGE).trim()))
                .totalDamages(Long.parseLong(totalDamages != null ? totalDamages : "0"))
                .build());
    }

    /**
     * Method to build timestamp
     */
    private long buildTimeStamp(String timeOfEvent) {
        LocalDateTime timeStamp = LocalDateTime.now();
        String[] time = StringUtils.substringBefore(timeOfEvent, CLOSE_SQ_BRACKET).trim().split(":");
        if (time.length > 2) {
            timeStamp.plusHours(Long.parseLong(time[0])).plusMinutes(Long.parseLong(time[1]))
                    .plusSeconds(Long.parseLong(time[2].substring(0, time[2].lastIndexOf("."))))
                    .plusNanos(Long.parseLong(time[2].substring(time[2].lastIndexOf(".") + 1, time[2].length())));
            return Timestamp.valueOf(timeStamp).getTime();
        }
        return 0L;
    }

    /**
     * Method to build items
     */
    private void buildItems(String stringToSearch, Match match, List<Items> items) {
        items.add(Items.builder()
                .timeOfEvent(buildTimeStamp(StringUtils.substringBefore(stringToSearch, CLOSE_SQ_BRACKET).trim()))
                .match(match)
                .name(StringUtils.substringBetween(stringToSearch, NPC_DOTA_HERO, BUYS).trim())
                .item(StringUtils.substringAfterLast(stringToSearch, ITEM).trim()).build());
    }

    /**
     * Method to build casts
     */
    private void buildCasts(String stringToSearch, Match match, List<Casts> casts) {
        String levelOn = StringUtils.substringAfterLast(stringToSearch,") on");
        String[] levelAr = StringUtils.isNotEmpty(levelOn) ? levelOn.split("dota_")
                : new String[]{};
        String levelOnStr = StringUtils.isNotEmpty(levelOn) &&  levelAr.length > 0 ? levelAr[levelAr.length-1] : levelOn;
        casts.add(Casts.builder()
                .match(match)
                .timeOfEvent(buildTimeStamp(StringUtils.substringBefore(stringToSearch, CLOSE_SQ_BRACKET).trim()))
                .name(StringUtils.isNotEmpty(StringUtils.substringBetween(stringToSearch, NPC_DOTA_HERO, CASTS).trim())
                        ? StringUtils.substringBetween(stringToSearch, NPC_DOTA_HERO, CASTS).trim()
                        : StringUtils.substringBetween(stringToSearch, NPC_DOTA, CASTS).trim())
                .casts(StringUtils.substringBetween(stringToSearch, ABILITY, OPEN_BRACKET).trim())
                .level(StringUtils.substringBetween(stringToSearch, LEVEL, CLOSE_BRACKET).trim())
                .levelOn(levelOnStr).build());
    }
}
