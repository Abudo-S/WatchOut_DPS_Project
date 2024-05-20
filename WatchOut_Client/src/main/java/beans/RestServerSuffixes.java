/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

public final class RestServerSuffixes
{
    private static final String REGISTRATION_SERVICE = "registration";
    private static final String HEART_RATE_SERVICE = "heart_rate";
    
    //REGISTRATION
    public static final String GET_TOTAL_PLAYERS_NUMBER = REGISTRATION_SERVICE + "/get_total_players_number";
    public static final String GET_ALL_PLAYERS = REGISTRATION_SERVICE + "/get_all_players";
    
    //H.R.
    public static final String GET_PLAYER_AVG_N_HRS = HEART_RATE_SERVICE + "/get_player_avg_n_hrs/{playerId}/{n}";
    public static final String GET_PLAYER_AVG_TIMESTAMPED_HRS = HEART_RATE_SERVICE + "/get_player_avg_hrs/{ts1}/{ts2}";
    public static final String GET_PLAYERS_HRS = HEART_RATE_SERVICE + "/get_players_hrs/{ts1}";
    
}
