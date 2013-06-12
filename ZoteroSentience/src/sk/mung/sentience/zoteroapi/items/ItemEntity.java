package sk.mung.sentience.zoteroapi.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemEntity implements Item
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

    @Override
    public long getId()
    {
        return id;
    }

    @Override
    public void setId(long id)
    {
        this.id = id;
    }

    @Override
    public ItemType getItemType() { return itemType; }

	@Override
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

	@Override
    public String getKey() { return key; }

	@Override
    public void setKey(String key) { this.key = key; }

    @Override
    public int getVersion() { return version; }

	@Override
    public void setVersion(int version) { this.version = version; }

	@Override
    public String getTitle() { return title; }

    @Override
    public void setTitle(String title) { this.title = title; }

	@Override
    public Map<ItemField, String> getFields() { return fields; 	}

	@Override
    public ItemField[] getSupportedFields() { return ItemField.values(); }

	@Override
    public void addField(ItemField field, String value) {
		fields.put( field, value );
		if(title == null && field.isTitle())
		{
			setTitle( value);
		}
	}

	@Override
    public boolean isSynced() { return isSynced; }

	@Override
    public void setSynced(boolean isSynced) { this.isSynced = isSynced; }

	@Override
    public List<Creator> getCreators() { return creators; }

	@Override
    public void addCreator(Creator creator) { creators.add(creator); }

	@Override
    public List<String> getCollectionKeys() { return collectionKeys; }

	@Override
    public List<String> getTags() { return tags; }

	@Override
    public void addCollectionKey(String key) { collectionKeys.add(key); }
	
	@Override
    public void addTag(String tag) { tags.add(tag); }

	@Override
    public String getParentKey() { return parentKey; }

	@Override
    public void setParentKey(String parentKey) { this.parentKey = parentKey; }
}
