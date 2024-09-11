package org.example.client;

public class QueryBuilder {
    public static String heroesInitQuery() {
        return """
                query {
                  constants {
                    heroes {
                      id
                      stats {
                        complexity
                      }
                    }
                  }
                }
                """;
    }

    public static String patchesDateQuery() {
        return """
                query {
                  constants {
                    gameVersions {
                      asOfDateTime
                    }
                  }
                }
                """;
    }

    public static String generalGlobalStatsQuery(String timeUnit) {
        return """
                query {
                  heroStats{
                    win""" + timeUnit.substring(0,1).toUpperCase() + timeUnit.substring(1) + """
                      (bracketIds: IMMORTAL, gameModeIds: ALL_PICK_RANKED){
                      heroId
                      winCount
                      matchCount
                      """ + timeUnit + """
                  }
                  }
                }
                """;
    }

    public static String globalStatsForPositionQuery(String timeUnit, int position) {
        return """
                query {
                  heroStats{
                    win""" + timeUnit.substring(0, 1).toUpperCase() + timeUnit.substring(1) + """
                (bracketIds: IMMORTAL, gameModeIds: ALL_PICK_RANKED, positionIds: POSITION_""" + position + """
                      ){
                          heroId
                          winCount
                          matchCount
                          """ + timeUnit + """
                    }
                  }
                }
                """;
    }

    public static String matchUpWithQuery(int heroId) {
        return """
                """;
    }

    // 88530551 - most1ess
    // 370933458 - lesya

    public static String generalSingleStatsQuery(long unixTime) {
        String query = """
                    {
                    player(steamAccountId: 	88530551) {
                matchesGroupBy(request:{
                    startDateTime:
                """;

        query += unixTime;
        query += "\n";

        query += """
                playerList: SINGLE
                              groupBy: STEAM_ACCOUNT_ID_HERO_ID
                              take: 50000
                            }){
                              ... on MatchGroupBySteamAccountIdHeroIdType{
                                winCount
                                matchCount
                                heroId
                              }
                            }
                          }
                        }
                """;

        return query;
    }

    public static String singleStatsForPositionQuery(long unixTime, int position) {
        String query = """
                    {
                    player(steamAccountId: 	88530551) {
                matchesGroupBy(request:{
                    startDateTime:
                """;

        query += unixTime;
        query += "\n";

        query += """
                playerList: SINGLE
                              groupBy: STEAM_ACCOUNT_ID_HERO_ID
                              take: 50000
                              positionIds: POSITION_""" + position + """
                            }){
                              ... on MatchGroupBySteamAccountIdHeroIdType{
                                winCount
                                matchCount
                                heroId
                              }
                            }
                          }
                        }
                """;

        return query;
    }
}
