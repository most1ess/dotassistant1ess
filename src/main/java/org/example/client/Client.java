package org.example.client;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import org.apache.catalina.Manager;
import org.example.hero.Hero;
import org.example.hero.IdToNameHeroMap;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

public class Client {
    private final WebClientGraphQLClient client;
    private final HashMap<Integer, Hero> heroesMap = new HashMap<>();
    private int latestPatchDate;
    private String timeUnit;
    private static final Logger logger = LoggerFactory.getLogger(Manager.class);

    public Client() {
        String url = "https://api.stratz.com/graphql";

        WebClient webClient = WebClient.create(url);
        client = MonoGraphQLClient.createWithWebClient(webClient, headers ->
                headers.add("Authorization", "Bearer "+ System.getenv("stratz_token")));

        IdToNameHeroMap.fill();
    }

    public void start() {
        getLatestPatchDate();
        latestPatchDate = 1724878800;
        initTimeUnit();
        initHeroes();
        getGeneralGlobalStats();
        getGlobalStats();
        getGeneralSingleStats();
        getSingleStats();
        printBestHeroes();
    }

    private void printBestHeroes() {
        for(int position = 1; position<6; position++) {
            ArrayList<Hero> heroes = new ArrayList<>(heroesMap.values());
            int finalPosition = position;
            heroes.sort((o1, o2) -> Double.compare(o2.getWinRateForPosition(finalPosition), o1.getWinRateForPosition(finalPosition)));
            System.out.println("Your best heroes for position "+position+":");
            for(int i = 0; i<8; i++) {
                heroes.get(i).show(position);
            }
            System.out.println();
        }
    }

    private void initTimeUnit() {
        long currentTime = System.currentTimeMillis()/1000;

        int hour = 3600;
        int day = 86400;
        int week = 604800;

        if(latestPatchDate + 24*hour > currentTime) timeUnit = "hour";
        else if(latestPatchDate + 14*day > currentTime) timeUnit = "day";
        else if(latestPatchDate + 13*week > currentTime) timeUnit = "week";
        else timeUnit = "month";
    }

    private void getLatestPatchDate() {
        GraphQLResponse response = get(QueryBuilder.patchesDateQuery());

        latestPatchDate = response.extractValue("constants.gameVersions[0].asOfDateTime");
    }

    private void initHeroes() {
        GraphQLResponse response = get(QueryBuilder.heroesInitQuery());

        List<Integer> ids = response.extractValue("constants.heroes[*].id");
        List<Integer> complexities = response.extractValue("constants.heroes[*].stats.complexity");

        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            try {
                heroesMap.put(id, new Hero(id, complexities.get(i)));
            } catch (IndexOutOfBoundsException e) {
                logger.warn("Found a hero with no stats.");
            }
        }
    }

    private void getGeneralGlobalStats() {
        GraphQLResponse response = get(QueryBuilder.generalGlobalStatsQuery(timeUnit));

        String root = "heroStats.win" +
                timeUnit.substring(0, 1).toUpperCase() + timeUnit.substring(1) + "[*].";

        List<Integer> timeUnits = response.extractValue(root + timeUnit);
        List<Integer> heroIds = response.extractValue(root + "heroId");
        List<Integer> winCounts = response.extractValue(root + "winCount");
        List<Integer> matchCounts = response.extractValue(root + "matchCount");

        for(int i = 0; i<timeUnits.size(); i++) {
            if(timeUnits.get(i) >= latestPatchDate) {
                try {
                    heroesMap.get(heroIds.get(i)).addGeneralGlobalStats(matchCounts.get(i), winCounts.get(i));
                } catch (NullPointerException ignored) {}
            }
        }
    }

    private void getGlobalStats() {
        for(int position = 1; position<6; position++) {
            GraphQLResponse response = get(QueryBuilder.globalStatsForPositionQuery(timeUnit, position));

            String root = "heroStats.win" +
                    timeUnit.substring(0, 1).toUpperCase() + timeUnit.substring(1) + "[*].";

            List<Integer> timeUnits = response.extractValue(root + timeUnit);
            List<Integer> heroIds = response.extractValue(root + "heroId");
            List<Integer> winCounts = response.extractValue(root + "winCount");
            List<Integer> matchCounts = response.extractValue(root + "matchCount");

            for(int i = 0; i<timeUnits.size(); i++) {
                if(timeUnits.get(i) >= latestPatchDate) {
                    try {
                        heroesMap.get(heroIds.get(i)).addGlobalStats(position, matchCounts.get(i), winCounts.get(i));
                    } catch (NullPointerException ignored) {}
                }
            }
        }
    }

    private void getGeneralSingleStats() {
        long unixTime = System.currentTimeMillis() / 1000 - 7776000;
        GraphQLResponse response = get(QueryBuilder.generalSingleStatsQuery(unixTime));

        List<Integer> heroIds = response.extractValue("player.matchesGroupBy[*].heroId");
        List<Integer> winCounts = response.extractValue("player.matchesGroupBy[*].winCount");
        List<Integer> matchCounts = response.extractValue("player.matchesGroupBy[*].matchCount");

        for(int i = 0; i<heroIds.size(); i++) {
            try {
                heroesMap.get(heroIds.get(i)).setGeneralSingleStats(matchCounts.get(i), winCounts.get(i));
            } catch (NullPointerException ignored) {}
        }
    }

    private void getSingleStats() {
        for(int position = 1; position<6; position++) {
            long unixTime = System.currentTimeMillis() / 1000 - 7776000;
            GraphQLResponse response = get(QueryBuilder.singleStatsForPositionQuery(unixTime, position));

            List<Integer> heroIds = response.extractValue("player.matchesGroupBy[*].heroId");
            List<Integer> winCounts = response.extractValue("player.matchesGroupBy[*].winCount");
            List<Integer> matchCounts = response.extractValue("player.matchesGroupBy[*].matchCount");

            for(int i = 0; i<heroIds.size(); i++) {
                try {
                    heroesMap.get(heroIds.get(i)).setSingleStats(position, matchCounts.get(i), winCounts.get(i));
                } catch (NullPointerException ignored) {}
            }
        }
    }

    private GraphQLResponse get(@Language("graphql") String query) {
        Mono<GraphQLResponse> graphQLFirstResponseMono = client.reactiveExecuteQuery(query);
        return Objects.requireNonNull(graphQLFirstResponseMono.block());
    }
}
