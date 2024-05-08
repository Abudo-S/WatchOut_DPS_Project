package beans;


import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@XmlRootElement
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
