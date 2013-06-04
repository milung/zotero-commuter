package sk.mung.sentience.zoteroapi.items;

public enum ItemType 
{
	ARTWORK (1,"artwork", Item.class),
	AUDIO(2,"audioRecording", Item.class),
	ATTACHMENT(3,"attachment", Item.class),
	BILL(4,"bill", Item.class),
	BLOG_POST(5,"blogPost", Item.class),
	BOOK(6,"book", Item.class),
	BOOK_SECTION(7,"bookSection", Item.class),
	CASE(8,"case", Item.class),
	COMPUTER_PROGRAM(9,"computerProgram", Item.class),
	CONFERENCE_PAPER(10,"conferencePaper", Item.class),
	DICTIONARY_ENTRY(11,"dictionaryEntry", Item.class),
	EMAIL(12,"email", Item.class),
	ENCYCLOPEDIA_ARTICLE(13,"encyclopediaArticle", Item.class),
	FILM(14,"film", Item.class),
	FORUM_POST(15,"forumPost", Item.class),
	HEARING(16,"hearing", Item.class),
	INSTANT_MESSAGE(17,"instantMessage", Item.class),
	INTERVIEW(18,"interview", Item.class),
	JOURNAL_ARTICLE(19,"journalArticle", Item.class),
	LETTER(20,"letter", Item.class),
	MAGAZINE_ARTICLE(21,"magazineArticle", Item.class),
	MANUSCRIPT(22,"manuscript", Item.class),
	MAP(23,"map", Item.class),
	NEWSPAPER_ARTICLE(24,"newspaperArticle", Item.class),
	NOTE(25,"note", Item.class),
	PATENT(26,"patent", Item.class),
	PODCAST(27,"podcast", Item.class),
	PRESENTATION(28,"presentation", Item.class),
	RADIO_BROADCAST(29,"radioBroadcast", Item.class),
	REPORT(30,"report", Item.class),
	STATUTE(31,"statute", Item.class),
	THESIS(32,"thesis", Item.class),
	TV_BROADCAST(33,"tvBroadcast", Item.class),
	VIDEO_RECORDING(34,"videoRecording", Item.class),
	WEB_PAGE(35,"webpage", Item.class);
	
	private final String zoteroName;
	private final Class<? super Item> clazz;
	private final int id;
	
	ItemType( int id, String name, Class<? super Item> clazz)
	{
		this.id = id;
		zoteroName = name;
		this.clazz = clazz;
	}
	
	public Item createItem() throws InstantiationException, IllegalAccessException
	{
		Item item = (Item) clazz.newInstance();
		item.setItemType( this);
		return item;
	}
	
	public static Item createItem(String itemType) 
			throws InstantiationException, IllegalAccessException
	{
		for( ItemType type : ItemType.values())
		{
			if(type.zoteroName.equalsIgnoreCase(itemType))
			{
				return type.createItem();
			}
		}
		return new Item();
	}

	public int getId() {
		return id;
	}
}
