package sk.mung.sentience.zoteroapi.entities;

import java.util.List;


public interface Item extends KeyEntity
{

    ItemType getItemType();

    void setItemType(ItemType itemType);

    String getKey();

    void setKey(String key);

    int getVersion();

    void setVersion(int version);

    String getTitle();

    void setTitle(String title);

    List<Field> getFields();

    ItemField[] getSupportedFields();

    void addField(Field field);

    Field getField(ItemField fieldType);

    boolean isSynced();

    void setSynced(boolean isSynced);

    List<Creator> getCreators();

    void addCreator(Creator creator);

    List<CollectionEntity> getCollections();

    void addCollection(CollectionEntity col);

    List<Tag> getTags();

    void addTag(Tag tag);

    String getParentKey();

    void setParentKey(String parentKey);

    List<Item> getChildren();

    void addChild(Item item);
}
