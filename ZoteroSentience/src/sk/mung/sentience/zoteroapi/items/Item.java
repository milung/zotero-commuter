package sk.mung.sentience.zoteroapi.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item
{

    private long id;
    private String title;
    private int version;
    private ItemType itemType;
    private boolean isSynced = true;
    private String key;
    private String parentKey;
    
    private final Map<ItemField, String> fields = new HashMap<ItemField,String>();
    private final List<Creator> creators = new ArrayList<Creator>();
    private final List<String> collectionKeys = new ArrayList<String>();
    private final List<String> tags = new ArrayList<String>();

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public ItemType getItemType() { return itemType; }

	public void setItemType(ItemType itemType) { this.itemType = itemType; }

	public String getKey() { return key; }

	public void setKey(String key) { this.key = key; }

    public int getVersion() { return version; }

	public void setVersion(int version) { this.version = version; }

	public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

	public Map<ItemField, String> getFields() { return fields; 	}

	public ItemField[] getSupportedFields() { return ItemField.values(); }

	public void addField(ItemField field, String value) {
		fields.put( field, value );
		if(title == null && field.isTitle())
		{
			setTitle( value);
		}
	}

	public boolean isSynced() { return isSynced; }

	public void setSynced(boolean isSynced) { this.isSynced = isSynced; }

	public List<Creator> getCreators() { return creators; }

	public void addCreator(Creator creator) { creators.add(creator); }

	public List<String> getCollectionKeys() { return collectionKeys; }

	public List<String> getTags() { return tags; }

	public void addCollectionKey(String key) { collectionKeys.add(key); }
	
	public void addTag(String tag) { tags.add(tag); }

	public String getParentKey() { return parentKey; }

	public void setParentKey(String parentKey) { this.parentKey = parentKey; }
}
