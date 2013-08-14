package sk.mung.zoteroapi.entities;


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
