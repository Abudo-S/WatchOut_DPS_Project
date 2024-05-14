package beans;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class GenericRestResponse
{
    private String result;

    public GenericRestResponse(String result)
    {
        this.result = result;
    }

    public String getResult()
    {
        return this.result;
    }
}
