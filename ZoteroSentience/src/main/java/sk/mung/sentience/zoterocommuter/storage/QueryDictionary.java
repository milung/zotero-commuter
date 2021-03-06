package sk.mung.sentience.zoterocommuter.storage;

import android.content.Context;

import sk.mung.sentience.zoterocommuter.R;

public class QueryDictionary
{
    private final Context context;

    public QueryDictionary(Context context)
    {
        this.context = context;
    }

    public String createCollectionsTable()
    { return context.getResources().getString(R.string.create_collections_table); }

    public String createPersonsTable()
    { return context.getResources().getString(R.string.create_persons_table); }

    public String createCreatorsTable()
    { return context.getResources().getString(R.string.create_creators_table); }

    public String createTagsTable()
    { return context.getResources().getString(R.string.create_tags_table); }

    public String createVersionsTable()
    { return context.getResources().getString(R.string.create_versions_table); }

    public String createItemsTable()
    { return context.getResources().getString(R.string.create_items_table); }

    public String createFieldsTable()
    { return context.getResources().getString(R.string.create_fields_table); }

    public String createItemsToCreatorsTable()
    { return context.getResources().getString(R.string.create_items_to_creators_table); }

    public String createItemsToTagsTable()
    { return context.getResources().getString(R.string.create_items_to_tags_table); }

    public String createItemsToCollectionsTable()
    { return context.getResources().getString(R.string.create_items_to_collections_table); }

    public String getItemCreators()
    { return context.getResources().getString(R.string.get_item_creators); }

    public String getCollectionItems()
    { return context.getResources().getString(R.string.get_collection_items); }

    public String getLibraryItems() { return context.getResources().getString(R.string.get_library_items); }

    public String getChildrenItems()
    {
        return context.getResources().getString(R.string.get_item_children);
    }

    public String getItemTags()
    {
        return context.getResources().getString(R.string.get_item_tags);
    }

    public String getItemCollections()
    {
        return context.getResources().getString(R.string.get_item_collections);
    }

    public String createRelationsTable()
    {
        return context.getResources().getString(R.string.create_relations_table);
    }

    public String getDeleteTagOrphans()
    {
        return context.getResources().getString(R.string.delete_tag_orphans);
    }

    public String getTaggedItems()
    {
        return context.getResources().getString(R.string.get_tagged_items);
    }
}
