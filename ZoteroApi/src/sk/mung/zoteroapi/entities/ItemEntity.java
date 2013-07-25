package sk.mung.zoteroapi.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemEntity implements Item
{

    private long id;
    private String title;
    private int version;
    private ItemType itemType = ItemType.OTHER;
    private SyncStatus synced = SyncStatus.SYNC_UNKNOWN;
    private String key;
    private String parentKey;
    
    private final List<Field> fields = new ArrayList<Field>();
    private final List<Creator> creators = new ArrayList<Creator>();
    private final List<CollectionEntity> collections = new ArrayList<CollectionEntity>();
    private final List<Tag> tags = new ArrayList<Tag>();
    private final List<Item> children = new ArrayList<Item>();
    private final List<Relation> relations = new ArrayList<Relation>();

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
    public List<Field> getFields() { return fields; 	}

    public Field getField(ItemField fieldType)
    {
        for(Field f : getFields())
        {
            if(f.getType().equals(fieldType)) return f;
        }
        return null;
    }

	@Override public ItemField[] getSupportedFields() { return ItemField.values(); }

	@Override
    public void addField(Field field)
    {
        for(Field f : fields)
        {
            if(f.getType() == field.getType())
            {
                fields.remove(f);
                break;
            }
        }

		fields.add(field);
		if(title == null && field.getType().isTitle())
		{
			setTitle( field.getValue());
		}
	}

	@Override public boolean isSynced() { return synced == SyncStatus.SYNC_OK; }
	@Override public void setSynced(SyncStatus status) { this.synced = status; }
    @Override public SyncStatus getSynced() { return synced; }

    @Override public List<Creator> getCreators() { return creators; }
	@Override public void addCreator(Creator creator) { creators.add(creator); }

	@Override public List<Tag> getTags() { return tags; }
	@Override public void addTag(Tag tag) { tags.add(tag); }

    @Override public void addCollection(CollectionEntity key) { collections.add(key); }
    @Override public List<CollectionEntity> getCollections() { return collections; }

    @Override public String getParentKey() { return parentKey; }
	@Override public void setParentKey(String parentKey) { this.parentKey = parentKey; }

    @Override public List<Item> getChildren() { return Collections.unmodifiableList(children); }
    @Override public void addChild(Item item) { children.add(item); }
    @Override public void clearChildren() { children.clear(); }

    @Override
    public List<Relation> getRelations()
    {
        return Collections.unmodifiableList(relations);
    }

    @Override
    public void addRelation(Relation relation)
    {
        relations.add(relation);
    }

    @Override
    public void clearRelations()
    {
        relations.clear();
    }
}
