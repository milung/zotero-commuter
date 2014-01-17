package sk.mung.zoteroapi.entities;

import java.util.ArrayList;
import java.util.List;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;
import de.undercouch.citeproc.csl.CSLType;

public class CslItemProxy extends CSLItemData
{
    private final Item adaptee;

    public CslItemProxy(Item adaptee)
    {
        this.adaptee = adaptee;
    }



    private String getFieldValue(ItemField... fields  )
    {
            for(ItemField fieldType : fields)
            {
                String fieldValue = getFieldValue(fieldType);
                if(fieldValue != null) return fieldValue;
            }
        return null;
    }

    @Override
    public String getId()
    {
        return adaptee.getKey();
    }

    @Override
    public CSLType getType()
    {
        return adaptee.getItemType().getCslType();
    }

    @Override
    public String[] getCategories()
    {
        return super.getCategories();
    }

    @Override
    public String getLanguage()
    {
        return getFieldValue(ItemField.LANGUAGE);
    }

    @Override
    public String getJournalAbbreviation()
    {
        return getFieldValue(ItemField.JOURNAL_ABBREVIATION);
    }

    @Override
    public String getShortTitle()
    {
        return getFieldValue(ItemField.SHORT_TITLE);
    }

    @Override
    public CSLName[] getAuthor()
    {
        return getCslNames(CreatorType.AUTHOR);
    }

    private CSLName[] getCslNames(CreatorType type)
    {
        List<CSLName> names = new ArrayList<CSLName>();
        for(Creator creator: adaptee.getCreators())
        {
            if(creator.getType() == type)
            {
                names.add(
                        new CSLNameBuilder()
                                .family(creator.getLastName())
                                .given(creator.getFirstName())
                                .build());
            }
        }
        return names.toArray(new CSLName[names.size()]);
    }

    @Override
    public CSLName[] getCollectionEditor()
    {
        return getCslNames(CreatorType.EDITOR);
    }

    @Override
    public CSLName[] getComposer()
    {
        return getCslNames(CreatorType.COMPOSER);
    }

    @Override
    public CSLName[] getContainerAuthor()
    {
        return getCslNames(CreatorType.AUTHOR);
    }

    @Override
    public CSLName[] getDirector()
    {
        return getCslNames(CreatorType.DIRECTOR);
    }

    @Override
    public CSLName[] getEditor()
    {
        return getCslNames(CreatorType.EDITOR);
    }

    @Override
    public CSLName[] getEditorialDirector()
    {
        return getCslNames(CreatorType.EDITOR);
    }

    @Override
    public CSLName[] getInterviewer()
    {
        return getCslNames(CreatorType.INTERVIEWER);
    }

    @Override
    public CSLName[] getIllustrator()
    {
        return getCslNames(CreatorType.ARTIST);
    }

    @Override
    public CSLName[] getOriginalAuthor()
    {
        return null;
    }

    @Override
    public CSLName[] getRecipient()
    {
        return getCslNames(CreatorType.RECIPIENT);
    }

    @Override
    public CSLName[] getReviewedAuthor()
    {
        return getCslNames(CreatorType.REVIEWED_AUTHOR);
    }

    @Override
    public CSLName[] getTranslator()
    {
        return getCslNames(CreatorType.TRANSLATOR);
    }

    @Override
    public CSLDate getAccessed()
    {
        String date = getFieldValue(ItemField.ACCESS_DATE);
        return new CSLDate(null,null,false,date,date);
    }

    @Override
    public CSLDate getContainer()
    {
        return null;
    }

    @Override
    public CSLDate getEventDate()
    {
        String date = getFieldValue(ItemField.DATE);
        return new CSLDate(null,null,false,date,date);
    }

    @Override
    public CSLDate getIssued()
    {
        String date = getFieldValue(ItemField.ISSUE_DATE);
        return new CSLDate(null,null,false,date,date);
    }

    @Override
    public CSLDate getOriginalDate()
    {
        return null;
    }

    @Override
    public CSLDate getSubmitted()
    {
        return null;
    }

    @Override
    public String getAbstrct()
    {
        return getFieldValue(ItemField.ABSTRACT_NOTE);
    }

    @Override
    public String getAnnote()
    {
        return null;
    }

    @Override
    public String getArchive()
    {
        return getFieldValue(ItemField.ARCHIVE);
    }

    @Override
    public String getArchiveLocation()
    {
        return getFieldValue(ItemField.ARCHIVE_LOCATION);
    }

    @Override
    public String getArchivePlace()
    {
        return getFieldValue(ItemField.ARCHIVE_LOCATION);
    }

    @Override
    public String getAuthority()
    {
        return getFieldValue(ItemField.ISSUING_AUTHORITY);
    }

    @Override
    public String getCallNumber()
    {
        return getFieldValue(ItemField.CALL_NUMBER);
    }

    @Override
    public String getChapterNumber()
    {
        return null;
    }

    @Override
    public String getCitationNumber()
    {
        return null;
    }

    @Override
    public String getCitationLabel()
    {
        return null;
    }

    @Override
    public String getCollectionNumber()
    {
        return getFieldValue(ItemField.SERIES_NUMBER);
    }

    @Override
    public String getCollectionTitle()
    {
        return getFieldValue(ItemField.SERIES_TITLE);
    }

    @Override
    public String getContainerTitle()
    {
        return getFieldValue(
                ItemField.BLOG_TITLE,ItemField.BOOK_TITLE,ItemField.DICTIONARY_TITLE,
                ItemField.ENCYCLOPEDIA_TITLE, ItemField.FORUM_TITLE, ItemField.PROCEEDINGS_TITLE,
                ItemField.PROGRAM_TITLE,ItemField.PUBLICATION_TITLE, ItemField.SERIES_TITLE,
                ItemField.WEBSITE_TITLE);

    }

    @Override
    public String getContainerTitleShort()
    {
        return null;
    }

    @Override
    public String getDimensions()
    {
        return getFieldValue(ItemField.ARTWORK_SIZE, ItemField.CODE_VOLUME);
    }

    @Override
    public String getDOI()
    {
        return getFieldValue(ItemField.DOI);
    }

    @Override
    public String getEdition()
    {
        return getFieldValue(ItemField.EDITION);
    }

    @Override
    public String getEvent()
    {
        return getFieldValue(ItemField.CONFERENCE_NAME);
    }

    @Override
    public String getEventplace()
    {
        return getFieldValue(ItemField.COUNTRY);
    }

    @Override
    public String getFirstReferenceNoteNumber()
    {
        return null;
    }

    @Override
    public String getGenre()
    {
        return getFieldValue(ItemField.GENRE)   ;
    }

    @Override
    public String getISBN()
    {
        return getFieldValue(ItemField.ISBN);
    }

    @Override
    public String getISSN()
    {
        return getFieldValue(ItemField.ISSN);
    }

    @Override
    public String getIssue()
    {
        return getFieldValue(ItemField.ISSUE);
    }

    @Override
    public String getJurisdiction()
    {
        return null;
    }

    @Override
    public String getKeyword()
    {
        return null;
    }

    @Override
    public String getLocator()
    {
        return null;
    }

    @Override
    public String getMedium()
    {
        return getFieldValue(ItemField.ARTWORK_MEDIUM,ItemField.INTERVIEW_MEDIUM);
    }

    @Override
    public String getNote()
    {
        return getFieldValue(ItemField.NOTE);
    }

    @Override
    public String getNumber()
    {
        return getFieldValue(ItemField.BILL_NUMBER,ItemField.CALL_NUMBER, ItemField.CODE_NUMBER,
                ItemField.DOCKET_NUMBER, ItemField.DOCUMENT_NUMBER, ItemField.EPISODE_NUMBER,
                ItemField.PATENT_NUMBER, ItemField.PUBLIC_LAW_NUMBER, ItemField.APPLICATION_NUMBER,
                ItemField.REPORT_NUMBER, ItemField.SERIES_NUMBER);
    }

    @Override
    public String getNumberOfPages()
    {
        return getFieldValue(ItemField.NUM_PAGES);
    }

    @Override
    public String getNumberOfVolumes()
    {
        return getFieldValue(ItemField.NUMBER_OF_VOLUMES);
    }

    @Override
    public String getOriginalPublisher()
    {
        return null;
    }

    @Override
    public String getOriginalPublisherPlace()
    {
        return null;
    }

    @Override
    public String getOriginalTitle()
    {
        return null;
    }

    @Override
    public String getPage()
    {
        return getFieldValue(ItemField.FIRST_PAGE);
    }

    @Override
    public String getPageFirst()
    {
        return getFieldValue(ItemField.FIRST_PAGE);
    }

    @Override
    public String getPMCID()
    {
        return null;
    }

    @Override
    public String getPMID()
    {
        return null;
    }

    @Override
    public String getPublisher()
    {
        return getFieldValue(ItemField.PUBLISHER);
    }

    @Override
    public String getPublisherPlace()
    {
        return null;
    }

    @Override
    public String getReferences()
    {
        return getFieldValue(ItemField.REFERENCES);
    }

    @Override
    public String getReviewedTitle()
    {
        return null;
    }

    @Override
    public String getScale()
    {
        return getFieldValue(ItemField.SCALE);
    }

    @Override
    public String getSection()
    {
        return getFieldValue(ItemField.SECTION);
    }

    @Override
    public String getSource()
    {
        return getFieldValue(ItemField.LIBRARY_CATALOG);
    }

    @Override
    public String getStatus()
    {
        return getFieldValue(ItemField.LEGAL_STATUS);
    }

    @Override
    public String getTitle()
    {
        return getFieldValue(ItemField.TITLE);
    }

    @Override
    public String getTitleShort()
    {
        return getFieldValue(ItemField.SHORT_TITLE);
    }

    @Override
    public String getURL()
    {
        return getFieldValue(ItemField.URL);
    }

    @Override
    public String getVersion()
    {
        return getFieldValue(ItemField.VERSION);
    }

    @Override
    public String getVolume()
    {
        return getFieldValue(ItemField.VOLUME);
    }

    @Override
    public String getYearSuffix()
    {
        return null;
    }
}
