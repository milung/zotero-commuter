package sk.mung.zoteroapi.entities;

import de.undercouch.citeproc.csl.CSLType;


public enum ItemType 
{
	ARTWORK (1,"artwork", CSLType.FIGURE,ItemEntity.class),
	AUDIO(2,"audioRecording", CSLType.SONG,ItemEntity.class),
	ATTACHMENT(3,"attachment", CSLType.ENTRY, ItemEntity.class),
	BILL(4,"bill", CSLType.BILL,ItemEntity.class),
	BLOG_POST(5,"blogPost", CSLType.POST_WEBLOG, ItemEntity.class),
	BOOK(6,"book", CSLType.BOOK, ItemEntity.class),
	BOOK_SECTION(7,"bookSection", CSLType.CHAPTER, ItemEntity.class),
	CASE(8,"case", CSLType.LEGAL_CASE, ItemEntity.class),
	COMPUTER_PROGRAM(9,"computerProgram", CSLType.BOOK, ItemEntity.class),
	CONFERENCE_PAPER(10,"conferencePaper", CSLType.PAPER_CONFERENCE, ItemEntity.class),
	DICTIONARY_ENTRY(11,"dictionaryEntry", CSLType.ENTRY_DICTIONARY, ItemEntity.class),
    DOCUMENT(36,"document", CSLType.ARTICLE, ItemEntity.class),
	EMAIL(12,"email", CSLType.PERSONAL_COMMUNICATION, ItemEntity.class),
	ENCYCLOPEDIA_ARTICLE(13,"encyclopediaArticle", CSLType.ENTRY_DICTIONARY, ItemEntity.class),
	FILM(14,"film", CSLType.MOTION_PICTURE, ItemEntity.class),
	FORUM_POST(15,"forumPost", CSLType.POST_WEBLOG, ItemEntity.class),
	HEARING(16,"hearing", CSLType.BILL, ItemEntity.class),
	INSTANT_MESSAGE(17,"instantMessage", CSLType.PERSONAL_COMMUNICATION, ItemEntity.class),
	INTERVIEW(18,"interview", CSLType.INTERVIEW, ItemEntity.class),
	JOURNAL_ARTICLE(19,"journalArticle", CSLType.ARTICLE_JOURNAL, ItemEntity.class),
	LETTER(20,"letter", CSLType.PERSONAL_COMMUNICATION, ItemEntity.class),
	MAGAZINE_ARTICLE(21,"magazineArticle", CSLType.ARTICLE_MAGAZINE, ItemEntity.class),
	MANUSCRIPT(22,"manuscript", CSLType.MANUSCRIPT, ItemEntity.class),
	MAP(23,"map", CSLType.MAP, ItemEntity.class),
	NEWSPAPER_ARTICLE(24,"newspaperArticle", CSLType.ARTICLE_NEWSPAPER, ItemEntity.class),
	NOTE(25,"note", CSLType.POST, ItemEntity.class),
	PATENT(26,"patent", CSLType.PATENT, ItemEntity.class),
	PODCAST(27,"podcast", CSLType.SONG, ItemEntity.class),
	PRESENTATION(28,"presentation", CSLType.SPEECH, ItemEntity.class),
	RADIO_BROADCAST(29,"radioBroadcast", CSLType.BROADCAST, ItemEntity.class),
	REPORT(30,"report", CSLType.REPORT, ItemEntity.class),
	STATUTE(31,"statute", CSLType.BILL, ItemEntity.class),
	THESIS(32,"thesis", CSLType.THESIS, ItemEntity.class),
	TV_BROADCAST(33,"tvBroadcast", CSLType.BROADCAST, ItemEntity.class),
	VIDEO_RECORDING(34,"videoRecording", CSLType.MOTION_PICTURE, ItemEntity.class),
	WEB_PAGE(35,"webpage", CSLType.WEBPAGE, ItemEntity.class),
    OTHER(999,"other", CSLType.ENTRY, ItemEntity.class);
	
	private final String zoteroName;
	private final Class<? super ItemEntity> clazz;
	private final int id;
    private final CSLType csl;
	
	ItemType( int id, String name, CSLType csl, Class<? super ItemEntity> clazz)
	{
		this.id = id;
		zoteroName = name;
		this.clazz = clazz;
        this.csl = csl;
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

    public String getZoteroName()
    {
        return zoteroName;
    }

    public CSLType getCslType()
    {
        return csl;
    }
}
