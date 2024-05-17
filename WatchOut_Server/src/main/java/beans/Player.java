package beans;

import java.util.ArrayList;
import java.util.Arrays;

public class Player
{
    private int id;
    private String endpoint;
    private int[] position;
    
    public Player(String endpoint, int[] position)
    {
        this.id = endpoint.hashCode();
        this.endpoint = endpoint;
        this.position = position;
    }
    
    public int getId()
    {
        return this.id;
    }

    public String getEndpoint()
    {
        return this.endpoint;
    }
    
    public int[] getPosition()
    {
        return this.position;
    }
    
    @Override
    public String toString()
    {
        return "id: " + this.id + ", endpoint : " + this.endpoint + ", position: " + Arrays.toString(this.position);
    }

}
