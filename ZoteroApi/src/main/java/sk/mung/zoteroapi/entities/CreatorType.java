package sk.mung.zoteroapi.entities;

public enum CreatorType {
	AUTHOR(1,"author", true),
	CONTRIBUTOR(2,"contributor", false),
	EDITOR(3,"editor", false),
	TRANSLATOR(4,"translator", false),
	SERIES_EDITOR(5,"seriesEditor", false),
	INTERVIEWEE(6,"interviewee", false),
	INTERVIEWER(7,"interviewer", true),
	DIRECTOR(8,"director", true),
	SCRIPTWRITER(9,"scriptwriter", true),
	PRODUCER(10,"producer", false),
	CAST_MEMBER(11,"castMember", false),
	SPONSOR(12,"sponsor", false),
	COUNSEL(13,"counsel", false),
	INVENTOR(14,"inventor", true),
	ATTORNEY_AGENT(15,"attorneyAgent", false),
	RECIPIENT(16,"recipient", false),
	PERFORMER(17,"performer", true),
	COMPOSER(18,"composer", true),
	WORDS_BY(19,"wordsBy", true),
	CARTOGRAPHER(20,"cartographer", true),
	PROGRAMMER(21,"programmer", true),
	ARTIST(22,"artist", true),
	COMMENTER(23,"commenter", true),
	PRESENTER(24,"presenter", true),
	GUEST(25,"guest", false),
	PODCASTER(26,"podcaster", true),
	REVIEWED_AUTHOR(27,"reviewedAuthor", false),
	COSPONSOR(28,"cosponsor", false),
	BOOK_AUTHOR(29,"bookAuthor", false);
	
	private final String zoteroName;
	private final int id;
    private final boolean isAuthor;
	
	CreatorType(int id, String name, boolean isAuthor)
	{
		this.id = id;
		this.zoteroName = name;
        this.isAuthor = isAuthor;
	}

	public String getZoteroName() 
	{
		return zoteroName;
	}
	
	public static CreatorType forZoteroName( String name)
	{
		for(CreatorType type : CreatorType.values())
		{
			if( type.getZoteroName().equalsIgnoreCase(name))
			{
				return type;
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

    public static CreatorType forId(int id)
    {
        for(CreatorType type : CreatorType.values())
        {
            if( type.getId() == id )
            {
                return type;
            }
        }
        return null;
    }

    public boolean isAuthor()
    {
        return isAuthor;
    }
}
