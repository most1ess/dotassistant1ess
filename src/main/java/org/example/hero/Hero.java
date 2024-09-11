package org.example.hero;

import org.example.hero.stats.Stats;

import java.util.HashMap;
import java.util.Map;

public class Hero {
    private final int heroId;
    private final int complexity;
    private final Stats generalGlobalStats = new Stats();
    private final Stats[] globalStats = new Stats[5];
    private final Stats generalSingleStats = new Stats();
    private final Stats[] singleStats = new Stats[5];
    private double globalWinRateForPosition;
    private double localWinRate;
    private double localWinRateForPosition;
    private HashMap<Integer, Double> winRatesWith = new HashMap<>();

    public Hero(int heroId, int complexity) {
        this.heroId = heroId;
        this.complexity = complexity;
        for(int i = 0; i<5; i++) {
            globalStats[i] = new Stats();
            singleStats[i] = new Stats();
        }
    }

    public void addGeneralGlobalStats(double matchCount, double winCount) {
        generalGlobalStats.add(matchCount, winCount);
    }

    public void addGlobalStats(int position, double matchCount, double winCount) {
        globalStats[position-1].add(matchCount, winCount);
    }

    public void setGeneralSingleStats(double matchCount, double winCount) {
        generalSingleStats.setMatchCount(matchCount);
        generalSingleStats.setWinCount(winCount);
    }

    public void setSingleStats(int position, double matchCount, double winCount) {
        singleStats[position-1].setMatchCount(matchCount);
        singleStats[position-1].setWinCount(winCount);
    }

    public void show(int position) {
        System.out.println("Hero: " + getName() + ", predicted win rate: " + getWinRateForPosition(position));
    }

    public double getWinRateForPosition(int position) {
        countGlobalWinRateForPosition(position);
        countLocalWinRate();
        countLocalWinRate(position);

        return globalWinRateForPosition*0.45 + localWinRate*0.1 + localWinRateForPosition*0.45;
    }

    private void countGlobalWinRateForPosition(int position) {
        double affection = getAffection(globalStats[position-1].getMatchCount(), 500);

        double startingWinRate = 0.4 + 0.1*affection;
        this.globalWinRateForPosition = startingWinRate +
                (globalStats[position-1].getWinRate() - startingWinRate)*affection;
    }

    private void countLocalWinRate() {
        double matchesToMaster = switch (complexity) {
            case 1 -> 7;
            case 2 -> 14;
            default -> 30;
        };
        double startingWinRate = switch (complexity) {
            case 1 -> 0.48;
            case 2 -> 0.46;
            default -> 0.44;
        };

        double masteringAffection = getAffection(generalSingleStats.getMatchCount(), matchesToMaster);
        startingWinRate = startingWinRate + (0.5-startingWinRate)*masteringAffection;

        double affection = getAffection(generalSingleStats.getMatchCount(), 30);
        this.localWinRate = startingWinRate + (generalSingleStats.getWinRate() - startingWinRate)*affection;
    }

    private double getAffection(double matchCount, double maxAccMatchCount) {
        if(matchCount > maxAccMatchCount) {
            return 1;
        } else {
            return matchCount / maxAccMatchCount;
        }
    }

    private void countLocalWinRate(int position) {
        double matchesToMaster = switch (complexity) {
            case 1 -> 7;
            case 2 -> 14;
            default -> 30;
        };
        double startingWinRate = switch (complexity) {
            case 1 -> 0.48;
            case 2 -> 0.46;
            default -> 0.44;
        };

        double masteringAffection = getAffection(singleStats[position-1].getMatchCount(), matchesToMaster);
        startingWinRate = startingWinRate + (0.5-startingWinRate)*masteringAffection;

        double affection = getAffection(singleStats[position-1].getMatchCount(), 30);
        this.localWinRateForPosition = startingWinRate + (generalSingleStats.getWinRate() - startingWinRate)*affection;
    }

    public String getName() {
        Map<Integer, String> map = IdToNameHeroMap.get();

        if (map.containsKey(heroId)) {
            return map.get(heroId);
        } else return String.valueOf(heroId);
    }
}
