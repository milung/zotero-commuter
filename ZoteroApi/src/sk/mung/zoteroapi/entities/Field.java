package sk.mung.zoteroapi.entities;

import static sk.mung.sanity.SanityCheck.*;

public class Field implements Entity
{
    private long id;
    private ItemField type;
    private String value;
    private Item item;

    public static Field create(ItemField type, String value)
    {
        checkArgumentNotNull(type,"type");
        checkArgumentNotNull(value,"value");

        Field field = new Field();
        field.setType(type);
        field.setValue(value);
        return field;
    }
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
