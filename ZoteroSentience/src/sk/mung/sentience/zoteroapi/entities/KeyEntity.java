package sk.mung.sentience.zoteroapi.entities;

/**
 * Created by sk1u00e5 on 20.6.2013.
 */
public interface KeyEntity extends Entity
{
    String getKey();
    void setKey(String key);

    int getVersion();
    void setVersion(int version);

    boolean isSynced();
    SyncStatus getSynced();
    void setSynced(SyncStatus synced);
}
