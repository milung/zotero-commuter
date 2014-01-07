package sk.mung.sentience.zoterocommuter.storage;

import sk.mung.zoteroapi.entities.KeyEntity;
import sk.mung.zoteroapi.entities.SyncStatus;


public class BaseLazyKeyProxy<T extends KeyEntity> implements KeyEntity
{
    private final T adaptee;
    private final BaseKeyDao<T> dao;

    public BaseLazyKeyProxy(BaseKeyDao<T> dao, T adaptee)
    {
        this.dao = dao;
        this.adaptee = adaptee;
    }

    protected T getAdaptee()
    {
        return adaptee;
    }

    protected BaseKeyDao<T> getDao()
    {
        return dao;
    }

    public long getId()
    {
        return adaptee.getId();
    }

    public String getKey()
    {
        return adaptee.getKey();
    }

    public int getVersion()
    {
        return adaptee.getVersion();
    }

    public void setVersion(int version)
    {
        adaptee.setVersion(version);
        if(adaptee.getVersion() != version)
        {
            dao.updateProperty(adaptee, BaseDao.COLUMN_VERSION, version);
            adaptee.setVersion(version);
        }
    }

    public boolean isSynced()
    {
        return adaptee.isSynced();
    }

    public void setKey(String key)
    {
        if(!key.equals(adaptee.getKey()))
        {
            dao.updateProperty(adaptee, BaseKeyDao.COLUMN_KEY, key);
            adaptee.setKey(key);
        }
    }

    public void setSynced(SyncStatus synced)
    {
        if(!adaptee.getSynced().equals(synced))
        {
            dao.updateProperty(adaptee, ItemsDao.COLUMN_SYNCED, synced.getStatusCode());
            adaptee.setSynced(synced);
        }
    }

    public SyncStatus getSynced() { return adaptee.getSynced(); }

    public void setId(long id) { adaptee.setId(id);  }
}
