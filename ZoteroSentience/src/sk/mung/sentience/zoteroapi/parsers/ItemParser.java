package sk.mung.sentience.zoteroapi.parsers;

import java.util.Map;

import sk.mung.sentience.zoteroapi.items.Creator;
import sk.mung.sentience.zoteroapi.items.CreatorType;
import sk.mung.sentience.zoteroapi.items.Item;
import sk.mung.sentience.zoteroapi.items.ItemField;
import sk.mung.sentience.zoteroapi.items.ItemType;

public class ItemParser extends AbstractAtomParser<Item> 
{

	@Override
	protected Item processContent(Map<String, Object> content)
	{
		String itemType = (String) content.get("itemType");
		
		try 
		{
			Item item = ItemType.createItem(itemType);
			item.setVersion((Integer) content.get("itemVersion"));
			item.setKey((String) content.get("itemKey"));
			item.setParentKey((String)content.get("parentItem"));
			
			processItemFields(content, item);
			processCreators(content, item);			
			processCollections(content, item);
			processTags(content, item);
			
			return item;
		} 
		catch (InstantiationException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	@SuppressWarnings("unchecked")
	private void processTags(Map<String, Object> content, Item item) {
		if(content.containsKey("tags"))
		{
			for( Map<String,String> tagFields : (Iterable<Map<String,String>>)content.get("tags"))
			{
				item.addTag(tagFields.get("tag"));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processCollections(Map<String, Object> content, Item item) {
		if(content.containsKey("collections"))
		{
			for( String key : (Iterable<String>)content.get("collections"))
			{
				item.addCollectionKey(key);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processCreators(Map<String, Object> content, Item item) {
		if(content.containsKey("creators"))
		{
			for( Map<String,String> creatorFields : (Iterable<Map<String,String>>)content.get("creators"))
			{
				Creator creator = new Creator();
				creator.setType(CreatorType.forZoteroName(creatorFields.get("creatorType")));
				creator.setFirstName(creatorFields.get("firstName"));
				creator.setLastName(creatorFields.get("lastName"));
				creator.setShortName(creatorFields.get("shortName"));
				item.addCreator(creator);
			}
		}
	}

	private void processItemFields(Map<String, Object> content, Item item) {
		for(ItemField field : item.getSupportedFields())
		{
			if(content.containsKey(field.getZoteroName()))
			{
				item.addField(field, (String) content.get(field.getZoteroName()));
			}
		}
	}

}
