package beans;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Player
{
    private int id;
    private String endpoint;
    
    public Player(String endpoint)
    {
        this.id = endpoint.hashCode();
        this.endpoint = endpoint;
    }

    public int getId()
    {
        return this.id;
    }

    public String getEndpoint()
    {
        return this.endpoint;
    }
    
    @Override
    public String toString()
    {
        return "id: " + this.id + ", endpoint : " + this.endpoint;
    }

}
