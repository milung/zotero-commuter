package sk.mung.sentience.zoteroapi;

public class CollectionEntity
{
	private int id;
    private String name;
    private String key;
    private int version;
    private String parentKey;
    private boolean isSynced;
    
    
    public int getId() {
		return id;
	}
	public void setId(int id) {
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
    
}
