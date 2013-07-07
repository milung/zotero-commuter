package sk.mung.sentience.zoteroapi.entities;

import java.util.List;


public interface Item extends KeyEntity
{

    ItemType getItemType();
    void setItemType(ItemType itemType);

    String getTitle();
    void setTitle(String title);

    List<Field> getFields();
    ItemField[] getSupportedFields();

    void addField(Field field);
    Field getField(ItemField fieldType);

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
    void clearChildren();

    List<Relation> getRelations();
    void addRelation(Relation relation);
    void clearRelations();
}
