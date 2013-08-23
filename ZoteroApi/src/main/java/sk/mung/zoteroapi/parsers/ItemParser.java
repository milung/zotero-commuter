package sk.mung.zoteroapi.parsers;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Map;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Creator;
import sk.mung.zoteroapi.entities.CreatorType;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemEntity;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.zoteroapi.entities.Relation;
import sk.mung.zoteroapi.entities.SyncStatus;
import sk.mung.zoteroapi.entities.Tag;

public class ItemParser extends AbstractAtomParser<Item>
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
            item.setSynced(SyncStatus.SYNC_OK);
			
			processItemFields(content, item);
			processCreators(content, item);			
			processCollections(content, item);
			processTags(content, item);
            processRelations(content, item);
			
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

    private void processRelations(Map<String, Object> content, Item item)
    {
        if(content.containsKey("relations"))
        {
            //noinspection unchecked
            for( Map.Entry<String,String> relation : ((Map<String,String>)content.get("relations")).entrySet())
            {
                    Relation relationEntity = new Relation();
                    relationEntity.setSubject(item);
                    relationEntity.setPredicate(relation.getKey());
                    relationEntity.setObject(relation.getValue());
                    item.addRelation(relationEntity);
            }
        }
    }


    @SuppressWarnings("unchecked")
	private void processTags(Map<String, Object> content, Item item) {
		if(content.containsKey("tags"))
		{
            for( Map<String,Object> tagFields : (Iterable<Map<String,Object>>)(content.get("tags")))
			{
                if(tagFields.containsKey("tag") && tagFields.get("tag") != null)
                {
                    Tag tag = new Tag();
                    tag.setTag(tagFields.get("tag").toString());

                    if(tagFields.containsKey("type"))
                    {
                        tag.setType(Integer.valueOf(tagFields.get("type").toString()));
                    }
                    item.addTag(tag);
                }

			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processCollections(Map<String, Object> content, Item item) {
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
                if(creator.isValid())
                {
                    item.addCreator(creator);
                }

			}
		}
	}

	private void processItemFields(Map<String, Object> content, Item item) {
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

    public void itemToJson(Item item, JsonGenerator generator ) throws IOException
    {
        generator.writeStartObject(); // item
        {
            generator.writeStringField("itemType", item.getItemType().getZoteroName());
            generator.writeStringField("itemKey", item.getKey());
            if(item.getParentKey()!=null)
            {
                generator.writeStringField("parentItem", item.getParentKey());
            }

            generator.writeArrayFieldStart("creators");
            {
                for(Creator creator : item.getCreators())
                {
                    generator.writeStartObject();

                    generator.writeStringField("creatorType", creator.getType().getZoteroName());
                    generator.writeStringField("firstName", creator.getFirstName());
                    generator.writeStringField("lastName", creator.getLastName());
                    generator.writeStringField("shortName", creator.getShortName());

                    generator.writeEndObject(); //creator
                }
            }
            generator.writeEndArray(); //creators

            for(Field field : item.getFields())
            {
                generator.writeStringField(field.getType().getZoteroName(),field.getValue());
            }

            generator.writeArrayFieldStart("tags");
            {
                for( Tag tag : item.getTags())
                {
                    if(tag.getTag() != null && !tag.getTag().trim().isEmpty())
                    {
                        generator.writeStartObject();
                        {
                            generator.writeStringField("tag", tag.getTag());
                        }
                        generator.writeEndObject(); //tag
                    }
                }
            }
            generator.writeEndArray(); //tags

            generator.writeArrayFieldStart("collections");
            {
                for(CollectionEntity collection : item.getCollections())
                {
                    generator.writeString(collection.getKey());
                }
            }
            generator.writeEndArray(); //collections

            generator.writeObjectFieldStart("relations");
            {
                for(Relation relation : item.getRelations())
                {
                    generator.writeStringField(relation.getPredicate(), relation.getObject());
                }
            }
            generator.writeEndObject(); //relations
        }
        generator.writeEndObject(); // item
    }

}
