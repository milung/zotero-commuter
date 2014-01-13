package sk.mung.sentience.zoterocommuter.storage;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Creator;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.zoteroapi.entities.Relation;
import sk.mung.zoteroapi.entities.Tag;


public class ItemLazyProxy extends BaseLazyKeyProxy<Item> implements Item, BaseDao.UpdateListener
{
    private static final String TAG = "ItemLazyProxy";
    private final CreatorsDao creatorsDao;
    private final FieldsDao fieldsDao;
    private final TagsDao tagsDao;
    private final CollectionsDao collectionsDao;

    private boolean areCreatorsLoaded = false;
    private boolean areChildrenLoaded = false;
    private boolean areFieldsLoaded = false;
    private boolean areTagsLoaded = false;
    private boolean areCollectionsLoaded = false;

    public ItemLazyProxy(
            Item adaptee,
            CreatorsDao creatorsDao,
            ItemsDao itemsDao,
            FieldsDao fieldsDao,
            TagsDao tagsDao,
            CollectionsDao collectionsDao)
    {
        super(itemsDao, adaptee);
        this.creatorsDao = creatorsDao;
        this.fieldsDao = fieldsDao;
        this.tagsDao = tagsDao;
        this.collectionsDao = collectionsDao;
        itemsDao.addUpdateListener(this);
    }

    @NotNull
    @Override
    public Item createCopy()
    {
        Item adaptee = getAdaptee().createCopy();
        return new ItemLazyProxy(adaptee, creatorsDao, getItemsDao(),fieldsDao, tagsDao, collectionsDao);
    }

    @Override
    public void copyState(Item template)
    {
        getAdaptee().copyState(template);
    }

    @Override
    public ItemType getItemType()
    {
        return getAdaptee().getItemType();
    }

    @Override
    public List<Creator> getCreators()
    {
        loadCreators();
        return getAdaptee().getCreators();
    }

    @Override
    public List<Field> getFields()
    {
        loadFields();
        return getAdaptee().getFields();
    }

    @Override
    public void addCollection(CollectionEntity col)
    {
        loadCollections();
        getAdaptee().addCollection(col);
    }

    @Override
    public void removeCollection(CollectionEntity col)
    {
        loadCollections();
        getAdaptee().removeCollection(col);
    }

    @Override
    public void setItemType(ItemType itemType)
    {
        getAdaptee().setItemType(itemType);
    }

    @Override
    public void addCreator(Creator creator)
    {
        loadCreators();
        getAdaptee().addCreator(creator);
    }

    private synchronized void loadCreators()
    {
        if(!areCreatorsLoaded)
        {
            Log.d(TAG, "loading creators for item ID:" + getId());
            getAdaptee().getCreators().clear();
            for(Creator creator : creatorsDao.findByItem(getAdaptee()))
            {
                getAdaptee().addCreator(creator);
            }
            areCreatorsLoaded = true;
        }
    }

    @Override
    public List<Tag> getTags()
    {
        loadTags();
        return getAdaptee().getTags();
    }

    @Override
    public List<CollectionEntity> getCollections()
    {
        loadCollections();
        return getAdaptee().getCollections();
    }

    @Override
    public ItemField[] getSupportedFields()
    {
        return getAdaptee().getSupportedFields();
    }

    @Override
    public String getTitle()
    {
        return getAdaptee().getTitle();
    }

    @Override
    public String getParentKey()
    {
        return getAdaptee().getParentKey();
    }

    @Override
    public void addTag(Tag tag)
    {
        loadTags();
        getAdaptee().addTag(tag);
    }

    private void loadCollections()
    {
        if(!areCollectionsLoaded)
        {
            Log.d(TAG, "loading collections for item ID:" + getId());
            getAdaptee().getCollections().clear();
            for( CollectionEntity collectionEntity : collectionsDao.findByItem(this))
            {
                getAdaptee().addCollection(collectionEntity);
            }
            areCollectionsLoaded = true;
        }
    }

    private void loadTags()
    {
        if(!areTagsLoaded)
        {
            Log.d(TAG, "loading tags for item ID:" + getId());
            getAdaptee().getTags().clear();
            for( Tag tag : tagsDao.findByItem(this))
            {
                getAdaptee().addTag(tag);
            }
            areTagsLoaded = true;
        }
    }

    @Override
    public void setTitle(String title)
    {
        getAdaptee().setTitle(title);
    }

    @Override
    public void setParentKey(String parentKey)
    {
        getAdaptee().setParentKey(parentKey);
    }

    @Override
    public List<Item> getChildren()
    {
        loadChildren();
        return getAdaptee().getChildren();
    }

    @Override
    public void addChild(Item item)
    {
        loadChildren();
        getAdaptee().addChild(item);
    }

    @Override
    public void removeChild(Item item)
    {
        loadChildren();
        getAdaptee().removeChild(item);
    }

    @Override
    public void clearChildren()
    {
        getAdaptee().clearChildren();
    }

    @Override
    public List<Relation> getRelations()
    {
        return getAdaptee().getRelations();
    }

    @Override
    public void addRelation(Relation relation)
    {
        getAdaptee().addRelation(relation);
    }

    @Override
    public void clearRelations()
    {
        getAdaptee().clearRelations();
    }

    private ItemsDao getItemsDao()
    {
        return (ItemsDao) getDao();
    }

    private synchronized void loadChildren()
    {
        if(!areChildrenLoaded)
        {
            Log.d(TAG, "loading children for item ID:" + getId());
            getAdaptee().clearChildren();
            for( Item item : getItemsDao().findByParent(this))
            {
                getAdaptee().addChild(item);
            }
            areChildrenLoaded = true;
        }
    }

    @Override
    public void addField(Field field)
    {
        loadFields();
        field.setItem(this);
        fieldsDao.upsert(field);
        getAdaptee().addField(field);
    }

    @Override
    public Field getField(ItemField fieldType)
    {
        loadFields();
        return getAdaptee().getField(fieldType);
    }

    private synchronized void loadFields()
    {
        if(!areFieldsLoaded)
        {
            Log.d(TAG, "loading fields for item ID:" + getId());
            getAdaptee().getFields().clear();
            for( Field field : fieldsDao.findByItem(this))
            {
                getAdaptee().addField(field);
            }
            areFieldsLoaded = true;
        }
    }

    @Override
    public void onDataUpdated(BaseDao sender, Long entityId)
    {
        if(entityId == null || entityId.equals(getId()))
        {
            areFieldsLoaded = false;
            areTagsLoaded = false;
            areChildrenLoaded = false;
            areCreatorsLoaded = false;
            areCollectionsLoaded = false;
        }
    }
}
