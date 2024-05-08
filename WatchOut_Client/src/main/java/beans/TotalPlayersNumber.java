package beans;


import javax.xml.bind.annotation.XmlRootElement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@XmlRootElement
public class TotalPlayersNumber
{
    private int playersNum;

    public TotalPlayersNumber(int playersNum)
    {
        this.playersNum = playersNum;
    }

    public int getPlayersNum()
    {
        return this.playersNum;
    }
}
