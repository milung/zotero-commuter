package sk.mung.sentience.zoterosentience.storage;

import android.content.Context;

import sk.mung.sentience.zoterosentience.R;

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
}
