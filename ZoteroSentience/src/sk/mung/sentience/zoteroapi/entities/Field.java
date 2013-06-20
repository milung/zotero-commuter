package sk.mung.sentience.zoteroapi.entities;

/**
 * Created by sk1u00e5 on 19.6.2013.
 */
public class Field implements Entity
{
    private long id;
    private ItemField type;
    private String value;
    private Item item;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public ItemField getType()
    {
        return type;
    }

    public void setType(ItemField type)
    {
        this.type = type;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Item getItem()
    {
        return item;
    }

    public void setItem(Item item)
    {
        this.item = item;
    }
}
