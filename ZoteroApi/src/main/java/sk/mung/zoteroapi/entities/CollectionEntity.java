package sk.mung.zoteroapi.entities;

import java.util.ArrayList;
import java.util.List;

public class CollectionEntity implements KeyEntity
{
	private long id;
    private String name;
    private String key;
    private int version;
    private String parentKey;
    private SyncStatus synced;

    private List<Item> items = new ArrayList<Item>();
    
    public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public boolean isSynced()
    {
        return synced == SyncStatus.SYNC_OK;
    }

    public void setSynced(SyncStatus synced)
    {
        this.synced = synced;
    }

    @Override
    public SyncStatus getSynced()
    {
        return synced;
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
        synced = SyncStatus.SYNC_LOCALLY_UPDATED;
    }
    public String getKey()
    {
        return key;
    }
    public void setKey(String key)
    {
        this.key = key;
        synced = SyncStatus.SYNC_LOCALLY_UPDATED;
    }
    public int getVersion()
    {
        return version;
    }
    public void setVersion(int version)
    {
        this.version = version;
        synced = SyncStatus.SYNC_LOCALLY_UPDATED;
    }
    public String getParentKey()
    {
        return parentKey;
    }
    public void setParentKey(String parentKey)
    {
        this.parentKey = parentKey;
        synced = SyncStatus.SYNC_LOCALLY_UPDATED;
    }

    public List<Item> getItems() { return items;}
    public void addItem(Item item) { items.add(item);}

    public int getItemsCount()
    {
        return items.size();
    }

}
