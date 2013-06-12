package sk.mung.sentience.zoterosentience.storage;

import java.util.List;
import java.util.Map;

import sk.mung.sentience.zoteroapi.items.Creator;
import sk.mung.sentience.zoteroapi.items.Item;
import sk.mung.sentience.zoteroapi.items.ItemEntity;
import sk.mung.sentience.zoteroapi.items.ItemField;
import sk.mung.sentience.zoteroapi.items.ItemType;


public class ItemLazyProxy implements Item
{
    private final Item adaptee;
    private final ZoteroStorage storage;
    private boolean areCreatorsLoaded = false;

    public ItemLazyProxy(Item adaptee, ZoteroStorage storage)
    {
        this.adaptee = adaptee;
        this.storage = storage;
    }

    @Override
    public long getId()
    {
        return adaptee.getId();
    }

    @Override
    public ItemType getItemType()
    {
        return adaptee.getItemType();
    }

    @Override
    public List<Creator> getCreators()
    {
        loadCreators();
        return adaptee.getCreators();
    }

    @Override
    public String getKey()
    {
        return adaptee.getKey();
    }

    @Override
    public Map<ItemField, String> getFields()
    {
        return adaptee.getFields();
    }

    @Override
    public int getVersion()
    {
        return adaptee.getVersion();
    }

    @Override
    public void setVersion(int version)
    {
        adaptee.setVersion(version);
    }

    @Override
    public void addCollectionKey(String key)
    {
        adaptee.addCollectionKey(key);
    }

    @Override
    public void setItemType(ItemType itemType)
    {
        adaptee.setItemType(itemType);
    }

    @Override
    public void addCreator(Creator creator)
    {
        loadCreators();
        adaptee.addCreator(creator);
    }

    private synchronized void loadCreators()
    {
        if(!areCreatorsLoaded)
        {
            List<Creator> creators = storage.getItemCreators(adaptee.getId());
            for(Creator creator : creators)
            {
                adaptee.addCreator(creator);
            }
            areCreatorsLoaded = true;
        }
    }

    @Override
    public List<String> getTags()
    {
        return adaptee.getTags();
    }

    @Override
    public List<String> getCollectionKeys()
    {
        return adaptee.getCollectionKeys();
    }

    @Override
    public ItemField[] getSupportedFields()
    {
        return adaptee.getSupportedFields();
    }

    @Override
    public String getTitle()
    {
        return adaptee.getTitle();
    }

    @Override
    public String getParentKey()
    {
        return adaptee.getParentKey();
    }

    @Override
    public boolean isSynced()
    {
        return adaptee.isSynced();
    }

    @Override
    public void addTag(String tag)
    {
        adaptee.addTag(tag);
    }

    @Override
    public void setTitle(String title)
    {
        adaptee.setTitle(title);
    }

    @Override
    public void setParentKey(String parentKey)
    {
        adaptee.setParentKey(parentKey);
    }

    @Override
    public void addField(ItemField field, String value)
    {
        adaptee.addField(field, value);
    }

    @Override
    public void setKey(String key)
    {
        adaptee.setKey(key);
    }

    @Override
    public void setSynced(boolean isSynced)
    {
        adaptee.setSynced(isSynced);
    }

    @Override
    public void setId(long id)
    {
        adaptee.setId(id);
    }
}
