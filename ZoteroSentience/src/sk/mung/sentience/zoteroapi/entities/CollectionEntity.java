package sk.mung.sentience.zoteroapi.entities;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoteroapi.entities.KeyEntity;

public class CollectionEntity implements KeyEntity
{
	private long id;
    private String name;
    private String key;
    private int version;
    private String parentKey;
    private boolean isSynced;

    private List<Item> items = new ArrayList<Item>();
    
    public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public boolean isSynced()
    {
        return isSynced;
    }
    public void setSynced(boolean isSynced)
    {
        this.isSynced = isSynced;
    }
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
        isSynced = false;
    }
    public String getKey()
    {
        return key;
    }
    public void setKey(String key)
    {
        this.key = key;
        isSynced = false;
    }
    public int getVersion()
    {
        return version;
    }
    public void setVersion(int version)
    {
        this.version = version;
        isSynced = false;
    }
    public String getParentKey()
    {
        return parentKey;
    }
    public void setParentKey(String parentKey)
    {
        this.parentKey = parentKey;
        isSynced = false;
    }

    public List<Item> getItems() { return items;}
    public void addItem(Item item) { items.add(item);}

}
