package sk.mung.sentience.zoteroapi.entities;

/**
 * Created by sk1u00e5 on 19.6.2013.
 */
public class Tag implements Entity
{
    private long id;
    private String tag;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }
}
