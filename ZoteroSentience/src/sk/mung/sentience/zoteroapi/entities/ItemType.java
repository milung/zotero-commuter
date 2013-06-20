package sk.mung.sentience.zoteroapi.entities;

public enum ItemType 
{
	ARTWORK (1,"artwork", ItemEntity.class),
	AUDIO(2,"audioRecording", ItemEntity.class),
	ATTACHMENT(3,"attachment", ItemEntity.class),
	BILL(4,"bill", ItemEntity.class),
	BLOG_POST(5,"blogPost", ItemEntity.class),
	BOOK(6,"book", ItemEntity.class),
	BOOK_SECTION(7,"bookSection", ItemEntity.class),
	CASE(8,"case", ItemEntity.class),
	COMPUTER_PROGRAM(9,"computerProgram", ItemEntity.class),
	CONFERENCE_PAPER(10,"conferencePaper", ItemEntity.class),
	DICTIONARY_ENTRY(11,"dictionaryEntry", ItemEntity.class),
    DOCUMENT(36,"document", ItemEntity.class),
	EMAIL(12,"email", ItemEntity.class),
	ENCYCLOPEDIA_ARTICLE(13,"encyclopediaArticle", ItemEntity.class),
	FILM(14,"film", ItemEntity.class),
	FORUM_POST(15,"forumPost", ItemEntity.class),
	HEARING(16,"hearing", ItemEntity.class),
	INSTANT_MESSAGE(17,"instantMessage", ItemEntity.class),
	INTERVIEW(18,"interview", ItemEntity.class),
	JOURNAL_ARTICLE(19,"journalArticle", ItemEntity.class),
	LETTER(20,"letter", ItemEntity.class),
	MAGAZINE_ARTICLE(21,"magazineArticle", ItemEntity.class),
	MANUSCRIPT(22,"manuscript", ItemEntity.class),
	MAP(23,"map", ItemEntity.class),
	NEWSPAPER_ARTICLE(24,"newspaperArticle", ItemEntity.class),
	NOTE(25,"note", ItemEntity.class),
	PATENT(26,"patent", ItemEntity.class),
	PODCAST(27,"podcast", ItemEntity.class),
	PRESENTATION(28,"presentation", ItemEntity.class),
	RADIO_BROADCAST(29,"radioBroadcast", ItemEntity.class),
	REPORT(30,"report", ItemEntity.class),
	STATUTE(31,"statute", ItemEntity.class),
	THESIS(32,"thesis", ItemEntity.class),
	TV_BROADCAST(33,"tvBroadcast", ItemEntity.class),
	VIDEO_RECORDING(34,"videoRecording", ItemEntity.class),
	WEB_PAGE(35,"webpage", ItemEntity.class),
    OTHER(999,"other", ItemEntity.class);
	
	private final String zoteroName;
	private final Class<? super ItemEntity> clazz;
	private final int id;
	
	ItemType( int id, String name, Class<? super ItemEntity> clazz)
	{
		this.id = id;
		zoteroName = name;
		this.clazz = clazz;
	}
	
	public ItemEntity createItem() throws InstantiationException, IllegalAccessException
	{
		ItemEntity item = (ItemEntity) clazz.newInstance();
		item.setItemType( this);
		return item;
	}
	
	public static ItemEntity createItem(String itemType)
			throws InstantiationException, IllegalAccessException
	{
		for( ItemType type : ItemType.values())
		{
			if(type.zoteroName.equalsIgnoreCase(itemType))
			{
				return type.createItem();
			}
		}

        //fallback
        return OTHER.createItem();
	}

	public int getId() {
		return id;
	}

    public static ItemType valueWithId(int id)
    {
        for( ItemType itemType : values())
        {
            if(itemType.getId() == id)
            {
                return itemType;
            }
        }
        return null;
    }
}
