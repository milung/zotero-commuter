package sk.mung.sentience.zoteroapi.entities;

public class Relation implements Entity
{
    private long _id;
    private Item subject;
    private String predicate;
    private String object;

    public long getId()
    {
        return _id;
    }

    public void setId(long id)
    {
        this._id = id;
    }

    public Item getSubject()
    {
        return subject;
    }

    public void setSubject(Item subject)
    {
        this.subject = subject;
    }

    public String getPredicate()
    {
        return predicate;
    }

    public void setPredicate(String predicate)
    {
        this.predicate = predicate;
    }

    public String getObject()
    {
        return object;
    }

    public void setObject(String object)
    {
        this.object = object;
    }
}
