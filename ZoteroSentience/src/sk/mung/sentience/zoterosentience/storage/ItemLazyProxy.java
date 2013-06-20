package sk.mung.sentience.zoterosentience.storage;

import java.util.List;

import sk.mung.sentience.zoteroapi.entities.CollectionEntity;
import sk.mung.sentience.zoteroapi.entities.Creator;
import sk.mung.sentience.zoteroapi.entities.Field;
import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoteroapi.entities.ItemField;
import sk.mung.sentience.zoteroapi.entities.ItemType;
import sk.mung.sentience.zoteroapi.entities.Tag;


public class ItemLazyProxy implements Item
{
    private final Item adaptee;
    private final CreatorsDao creatorsDao;
    private final ItemsDao itemsDao;
    private final FieldsDao fieldsDao;

    private boolean areCreatorsLoaded = false;
    private boolean areChildrenLoaded = false;
    private boolean areFieldsLoaded = false;

    public ItemLazyProxy(Item adaptee, CreatorsDao creatorsDao, ItemsDao itemsDao, FieldsDao fieldsDao)
    {
        this.adaptee = adaptee;
        this.creatorsDao = creatorsDao;
        this.itemsDao = itemsDao;
        this.fieldsDao = fieldsDao;
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
    public List<Field> getFields()
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
    public void addCollection(CollectionEntity key)
    {
        adaptee.addCollection(key);
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
            for(Creator creator : creatorsDao.findByItem(adaptee))
            {
                adaptee.addCreator(creator);
            }
            areCreatorsLoaded = true;
        }
    }

    @Override
    public List<Tag> getTags()
    {
        return adaptee.getTags();
    }

    @Override
    public List<CollectionEntity> getCollections()
    {
        return adaptee.getCollections();
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
    public void addTag(Tag tag)
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
    public List<Item> getChildren()
    {
        loadChildren();
        return adaptee.getChildren();
    }

    @Override
    public void addChild(Item item)
    {
        loadChildren();
        adaptee.addChild(item);
    }

    private synchronized void loadChildren()
    {
        if(!areChildrenLoaded)
        {
            for( Item item : itemsDao.findByParent(this))
            {
                adaptee.addChild(item);
            }
            areChildrenLoaded = true;
        }
    }

    @Override
    public void addField(Field field)
    {
        adaptee.addField(field);
    }

    @Override
    public Field getField(ItemField fieldType)
    {
        loadFields();
        return adaptee.getField(fieldType);
    }

    private synchronized void loadFields()
    {
        if(!areFieldsLoaded)
        {
            for( Field field : fieldsDao.findByItem(this))
            {
                adaptee.addField(field);
            }
            areFieldsLoaded = true;
        }
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
