package sk.mung.sentience.zoterocommuter.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sk.mung.zoteroapi.entities.CollectionEntity;

public class ZoteroCollection extends CollectionEntity
{
	private final List<ZoteroCollection> children = new ArrayList<ZoteroCollection>();
	private int level = 0;
	
	public List<ZoteroCollection> getChildren(){ return children;}
	
	public int getNestedLevel()
	{
		return level;
	}
	
	public void updateLevels()
	{
		updateLevels(0);
	}
	
	private void updateLevels(int nesting)
	{
		level = nesting;
		for( ZoteroCollection entry: children)
		{
			entry.updateLevels( level + 1);
		}		
	}
	private Comparator<ZoteroCollection> comparator = new Comparator<ZoteroCollection>()
	{

		@Override
		public int compare(ZoteroCollection left, ZoteroCollection right) {
			return left.getName().compareToIgnoreCase(right.getName());
	}};
	
	public void sort()
	{
		Collections.sort(children, comparator);
		for( ZoteroCollection entry: children)
		{
			entry.sort();
		}
	}
}
