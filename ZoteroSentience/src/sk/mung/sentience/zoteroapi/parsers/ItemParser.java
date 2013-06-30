package sk.mung.sentience.zoteroapi.parsers;

import java.util.Map;

import sk.mung.sentience.zoteroapi.entities.CollectionEntity;
import sk.mung.sentience.zoteroapi.entities.Creator;
import sk.mung.sentience.zoteroapi.entities.CreatorType;
import sk.mung.sentience.zoteroapi.entities.Field;
import sk.mung.sentience.zoteroapi.entities.ItemEntity;
import sk.mung.sentience.zoteroapi.entities.ItemField;
import sk.mung.sentience.zoteroapi.entities.ItemType;
import sk.mung.sentience.zoteroapi.entities.Tag;

public class ItemParser extends AbstractAtomParser<ItemEntity>
{

	@Override
	protected ItemEntity processContent(Map<String, Object> content)
	{
		String itemType = (String) content.get("itemType");
		
		try 
		{
			ItemEntity item = ItemType.createItem(itemType);
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
	private void processTags(Map<String, Object> content, ItemEntity item) {
		if(content.containsKey("tags"))
		{
			for( Map<String,String> tagFields : (Iterable<Map<String,String>>)content.get("tags"))
			{
                Tag tag = new Tag();
                tag.setTag(tagFields.get("tag"));
				item.addTag(tag);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processCollections(Map<String, Object> content, ItemEntity item) {
		if(content.containsKey("collections"))
		{
			for( String key : (Iterable<String>)content.get("collections"))
			{
                CollectionEntity collection = new CollectionEntity();
                collection.setKey(key);
				item.addCollection(collection);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processCreators(Map<String, Object> content, ItemEntity item) {
		if(content.containsKey("creators"))
		{
			for( Map<String,String> creatorFields : (Iterable<Map<String,String>>)content.get("creators"))
			{
                    Creator creator = new Creator();
                    creator.setType(CreatorType.forZoteroName(creatorFields.get("creatorType")));
                    creator.setFirstName(creatorFields.get("firstName"));
                    creator.setLastName(creatorFields.get("lastName"));
                    creator.setShortName(creatorFields.get("shortName"));
                if(creator.isValid())
                {
                    item.addCreator(creator);
                }

			}
		}
	}

	private void processItemFields(Map<String, Object> content, ItemEntity item) {
		for(ItemField itemField : item.getSupportedFields())
		{
			if(content.containsKey(itemField.getZoteroName()))
			{
                Object value = content.get(itemField.getZoteroName());
                if(value !=null)
                {
                    Field field = new Field();
                    field.setItem(item);
                    field.setType(itemField);
                    field.setValue(value.toString());
                    item.addField(field );
                }
			}
		}
	}

}
