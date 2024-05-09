package beans;

import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class AllPlayerHRsResponse
{
    private HashMap<Integer, HashMap<Double, Double>> allPlayerHrs;

    public AllPlayerHRsResponse(HashMap<Integer, HashMap<Double, Double>> allPlayerHrs)
    {
        this.allPlayerHrs = allPlayerHrs;
    }

    public HashMap<Integer, HashMap<Double, Double>> getAllPlayerHrs()
    {
        return this.allPlayerHrs;
    }
}
