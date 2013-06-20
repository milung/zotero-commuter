package sk.mung.sentience.zoteroapi.entities;

public enum CreatorType {
	AUTHOR(1,"author"),
	CONTRIBUTOR(2,"contributor"),
	EDITOR(3,"editor"),
	TRANSLATOR(4,"translator"),
	SERIES_EDITOR(5,"seriesEditor"),
	INTERVIEWEE(6,"interviewee"),
	INTERVIEWER(7,"interviewer"),
	DIRECTOR(8,"director"),
	SCRIPTWRITER(9,"scriptwriter"),
	PRODUCER(10,"producer"),
	CAST_MEMBER(11,"castMember"),
	SPONSOR(12,"sponsor"),
	COUNSEL(13,"counsel"),
	INVENTOR(14,"inventor"),
	ATTORNEY_AGENT(15,"attorneyAgent"),
	RECIPIENT(16,"recipient"),
	PERFORMER(17,"performer"),
	COMPOSER(18,"composer"),
	WORDS_BY(19,"wordsBy"),
	CARTOGRAPHER(20,"cartographer"),
	PROGRAMMER(21,"programmer"),
	ARTIST(22,"artist"),
	COMMENTER(23,"commenter"),
	PRESENTER(24,"presenter"),
	GUEST(25,"guest"),
	PODCASTER(26,"podcaster"),
	REVIEWED_AUTHOR(27,"reviewedAuthor"),
	COSPONSOR(28,"cosponsor"),
	BOOK_AUTHOR(29,"bookAuthor");
	
	private final String zoteroName;
	private final int id;
	
	CreatorType(int id, String name)
	{
		this.id = id;
		this.zoteroName = name;
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
}
