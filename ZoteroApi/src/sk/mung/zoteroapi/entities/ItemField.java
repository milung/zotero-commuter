package sk.mung.zoteroapi.entities;

public enum ItemField 
{
	ABSTRACT_NOTE(1,"abstractNote"),
	ACCESS_DATE(2,"accessDate"),
	EXTRA(3,"extra"),
	RIGHTS(4,"rights"),
	SHORT_TITLE(5,"shortTitle"),
	URL(6,"url"),
	TITLE(7,"title", true),
	DATE(8,"date"),
	LANGUAGE(9,"language"),
	ARCHIVE(10,"archive"),
	CALL_NUMBER(11,"callNumber"),
	LIBRARY_CATALOG(12,"libraryCatalog"),
	ARCHIVE_LOCATION(13,"archiveLocation"),
	PLACE(14,"place"),
	PAGES(15,"pages"),
	VOLUME(16,"volume"),
	ISBN(17,"ISBN"),
	NUMBER_OF_VOLUMES(18,"numberOfVolumes"),
	SERIES_TITLE(20,"seriesTitle"),
	PUBLISHER(21,"publisher"),
	SERIES(22,"series"),
	EDITION(23,"edition"),
	RUNNING_TIME(24,"runningTime"),
	SERIES_NUMBER(25,"seriesNumber"),
	NUM_PAGES(26,"numPages"),
	HISTORY(27,"history"),
	ISSN(28,"ISSN"),
	PUBLICATION_TITLE(29,"publicationTitle"),
	DOI(30,"DOI"),
	EPISODE_NUMBER(31,"episodeNumber"),
	VIDEO_RECORDING_FORMAT(32,"videoRecordingFormat"),
	ISSUE(33,"issue"),
	SECTION(34,"section"),
	SESSION(35,"session"),
	CODE(36,"code"),
	JOURNAL_ABBREVIATION(37,"journalAbbreviation"),
	LEGISLATIVE_BODY(38,"legislativeBody"),
	NETWORK(39,"network"),
	PROGRAM_TITLE(40,"programTitle"),
	SERIES_TEXT(41,"seriesText"),
	STUDIO(42,"studio"),
	WEBSITE_TYPE(43,"websiteType"),
	APPLICATION_NUMBER(44,"applicationNumber"),
	ARTWORK_SIZE(45,"artworkSize"),
	ASSIGNEE(46,"assignee"),
	BILL_NUMBER(47,"billNumber"),
	BLOG_TITLE(48,"blogTitle"),
	BOOK_TITLE(49,"bookTitle"),
	CASE_NAME(50,"caseName"),
	CODE_NUMBER(51,"codeNumber"),
	CODE_PAGES(52,"codePages"),
	CODE_VOLUME(53,"codeVolume"),
	COMMITTEE(54,"committee"),
	COMPANY(55,"company"),
	CONFERENCE_NAME(56,"conferenceName"),
	COUNTRY(57,"country"),
	COURT(58,"court"),
	DATE_DECIDED(59,"dateDecided"),
	DATE_ENACTED(60,"dateEnacted"),
	DICTIONARY_TITLE(61,"dictionaryTitle"),
	DISTRIBUTOR(62,"distributor"),
	DOCKET_NUMBER(63,"docketNumber"),
	DOCUMENT_NUMBER(64,"documentNumber"),
	ENCYCLOPEDIA_TITLE(65,"encyclopediaTitle"),
	AUDIO_FILETYPE(66,"audioFileType"),
	FILING_DATE(67,"filingDate"),
	FIRST_PAGE(68,"firstPage"),
	AUDIO_RECORDING_FORMAT(69,"audioRecordingFormat"),
	FORUM_TITLE(72,"forumTitle"),
	GENRE(73,"genre"),
	INSTITUTION(74,"institution"),
	ISSUE_DATE(75,"issueDate"),
	ISSUING_AUTHORITY(76,"issuingAuthority"),
	LABEL(77,"label"),
	PROGRAMMING_LANGUAGE(78,"programmingLanguage"),
	LEGAL_STATUS(79,"legalStatus"),
	ARTWORK_MEDIUM(80,"artworkMedium"),
	INTERVIEW_MEDIUM(81,"interviewMedium"),
	MEETING_NAME(82,"meetingName"),
	NAME_OF_ACT(83,"nameOfAct",true),
	PATENT_NUMBER(84,"patentNumber"),
	POST_TYPE(85,"postType"),
	PRIORITY_NUMBERS(86,"priorityNumbers"),
	PROCEEDINGS_TITLE(87,"proceedingsTitle"),
	PUBLIC_LAW_NUMBER(88,"publicLawNumber"),
	REFERENCES(89,"references"),
	REPORT_NUMBER(90,"reportNumber"),
	REPORT_TYPE(91,"reportType"),
	REPORTER(92,"reporter"),
	REPORTER_VOLUME(93,"reporterVolume"),
	SCALE(94,"scale"),
	SUBJECT(95,"subject", true),
	SYSTEM(96,"system"),
	LETTER_TYPE(97,"letterType"),
	MANUSCRIPT_TYPE(98,"manuscriptType"),
	MAP_TYPE(99,"mapType"),
	PRESENTATION_TYPE(100,"presentationType"),
	THESIS_TYPE(101,"thesisType"),
	UNIVERSITY(102,"university"),
	VERSION(104,"version"),
	WEBSITE_TITLE(105,"websiteTitle", true),
    CONTENT_TYPE(106,"contentType"),
    MD5(107, "md5"),
    LINK_MODE(108,"linkMode"),
    MODIFICATION_TIME(109,"mtime"),
    FILE_NAME(110,"filename"),
    NOTE(111,"note");
	
	private final String zoteroName;
	private final int id;
	private final boolean isTitle;
	
	public boolean isTitle() { return isTitle; }

	ItemField(int id, String name)
	{
		this.isTitle = false;
		this.id = id;
		zoteroName = name;
	}

	ItemField(int id, String name, boolean isTitle)
	{
		this.isTitle = isTitle;
		this.id = id;
		zoteroName = name;
	}
	
	public String getZoteroName() { return zoteroName; }

	public int getId() { return id; }

	public static ItemField fromZoteroName(String fieldName)
	{
		for( ItemField field : ItemField.values())
		{
			if( field.getZoteroName().equals(field.zoteroName))
			{
				return field;
			}
		}
		return null;
	}

    public static ItemField fromId(int id)
    {
        for( ItemField field : ItemField.values())
        {
            if( field.id == id)
            {
                return field;
            }
        }
        return null;
    }

}
