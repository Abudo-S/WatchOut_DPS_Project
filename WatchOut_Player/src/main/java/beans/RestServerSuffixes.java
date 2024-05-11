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
    public static final String POST_ADD_PLAYER = REGISTRATION_SERVICE + "/add_player";
    
    //H.R.
    public static final String POST_ADD_PLAYER_HRS = HEART_RATE_SERVICE + "/add_player_hrs";
    
}
