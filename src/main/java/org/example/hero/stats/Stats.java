package org.example.hero.stats;

public class Stats {
    private double matchCount;
    private double winCount;

    public Stats() {
        matchCount = 0;
        winCount = 0;
    }

    public void setMatchCount(double matchCount) {
        this.matchCount = matchCount;
    }

    public void setWinCount(double winCount) {
        this.winCount = winCount;
    }

    public double getWinRate() {
        if(matchCount>0) return winCount/matchCount;
        else return 0.5;
    }

    public double getMatchCount() {
        return matchCount;
    }

    public void add(double matchCount, double winCount) {
        this.matchCount += matchCount;
        this.winCount += winCount;
    }
}
