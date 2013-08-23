package sk.mung.sentience.zoterosentience.renderers;

import android.content.Context;
import android.content.res.Resources;
import java.text.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.sentience.zoterosentience.R;


public class FieldRenderer
{
    interface Reformatter
    {
        String reformat(String in);
    }

    static Reformatter identityReformatter = new Reformatter()
    {
        @Override
        public String reformat(String in)
        {
            return in;
        }
    };

    static Reformatter modificationTimeReformatter = new Reformatter()
    {
        @Override
        public String reformat(String in)
        {
            long timestamp = Long.valueOf(in);
            return DateFormat.getDateTimeInstance().format(new Date(timestamp));
        }
    };



    static class RenderingAttributes
    {
        boolean isVisible()
        {
            return isVisible;
        }

        int getLayoutId()
        {
            return layoutId;
        }

        int getTitleId()
        {
            return titleId;
        }

        private final boolean isVisible;
        private final int layoutId;
        private final int titleId;
        private final Reformatter formatter;

        int getSortingPosition()
        {
            return sortingPosition;
        }

        private final int sortingPosition;

        RenderingAttributes(boolean visible, int sortingPosition, int layoutId, int titleId)
        {
            isVisible = visible;
            this.sortingPosition = sortingPosition;
            this.layoutId = layoutId;
            this.titleId = titleId;
            this.formatter = identityReformatter;
        }

        RenderingAttributes(boolean visible, int sortingPosition, int layoutId, int titleId, Reformatter formatter)
        {
            isVisible = visible;
            this.sortingPosition = sortingPosition;
            this.layoutId = layoutId;
            this.titleId = titleId;
            this.formatter = formatter;
        }


        Reformatter getFormatter()
        {
            return formatter;
        }
    }

    private final static Map<ItemField, RenderingAttributes> attributes = new HashMap<ItemField, RenderingAttributes>();
    private final LayoutInflater inflater;
    private final Resources resources;
    private final ViewGroup parent;

    static
    {
        attributes.put(
                ItemField.ABSTRACT_NOTE,
                new RenderingAttributes(true,100, R.layout.listitem_field,R.string.zotero_field_abstract_note ));
        attributes.put(
                ItemField.ACCESS_DATE,
                new RenderingAttributes(false, 0, 0,0));
        attributes.put(
                ItemField.APPLICATION_NUMBER,
                new RenderingAttributes(true,1300, R.layout.listitem_field_inline,R.string.zotero_field_application_number));
        attributes.put(
                ItemField.ARCHIVE,
                new RenderingAttributes(true, 1200,R.layout.listitem_field_inline,R.string.zotero_field_archive));
        attributes.put(
                ItemField.ARCHIVE_LOCATION,
                new RenderingAttributes(true, 1210,R.layout.listitem_field_inline,R.string.zotero_field_archive_location));
        attributes.put(
                ItemField.ARTWORK_MEDIUM,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_artwork_medium));
        attributes.put(
                ItemField.ARTWORK_SIZE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_artwork_size));
        attributes.put(
                ItemField.ASSIGNEE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_assignee));
        attributes.put(
                ItemField.AUDIO_FILETYPE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_audio_file_type));
        attributes.put(
                ItemField.AUDIO_RECORDING_FORMAT,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_audio_recording_format));
        attributes.put(
                ItemField.BILL_NUMBER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_bill_number));
        attributes.put(
                ItemField.BLOG_TITLE,
                new RenderingAttributes(false,0, 0,0));
        attributes.put(
                ItemField.BOOK_TITLE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_book_title));
        attributes.put(
                ItemField.CALL_NUMBER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_call_number));
        attributes.put(
                ItemField.CASE_NAME,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_case_name));
        attributes.put(
                ItemField.CODE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_code));
        attributes.put(
                ItemField.CODE_NUMBER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_code_number));
        attributes.put(
                ItemField.CODE_PAGES,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_code_volume));
        attributes.put(
                ItemField.CODE_VOLUME,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_language));
        attributes.put(
                ItemField.COMMITTEE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_committee));
        attributes.put(
                ItemField.COMPANY,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_company));
        attributes.put(
                ItemField.CONFERENCE_NAME,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_conference_name));
        attributes.put(
                ItemField.CONTENT_TYPE,
                new RenderingAttributes(false,0, 0,0));
        attributes.put(
                ItemField.COUNTRY,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_country));
        attributes.put(
                ItemField.COURT,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_court));
        attributes.put(
                ItemField.DATE,
                new RenderingAttributes(true,600, R.layout.listitem_field_inline,R.string.zotero_field_date));
        attributes.put(
                ItemField.DATE_DECIDED,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_date_decided));
        attributes.put(
                ItemField.DATE_ENACTED,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_date_enacted));
        attributes.put(
                ItemField.DICTIONARY_TITLE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_dictionary));
        attributes.put(
                ItemField.DISTRIBUTOR,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_distributor));
        attributes.put(
                ItemField.DOCKET_NUMBER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_docket_number));
        attributes.put(
                ItemField.DOCUMENT_NUMBER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_document_number));
        attributes.put(
                ItemField.DOI,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_doi));
        attributes.put(
                ItemField.DOWNLOAD_TIME,
                new RenderingAttributes(false,0, 0,0));
        attributes.put(
                ItemField.DOWNLOAD_MD5,
                new RenderingAttributes(false,0, 0,0));
        attributes.put(
                ItemField.EDITION,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_edition));
        attributes.put(
                ItemField.ENCYCLOPEDIA_TITLE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_encyclopedia_title));
        attributes.put(
                ItemField.EPISODE_NUMBER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_episode_number));
        attributes.put(
                ItemField.EXTRA,
                new RenderingAttributes(true,99999, R.layout.listitem_field_inline,R.string.zotero_field_extra));
        attributes.put(
                ItemField.FILE_NAME,
                new RenderingAttributes(false, 0, 0, 0));
        attributes.put(
                ItemField.FILING_DATE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_filing_date));
        attributes.put(
                ItemField.FIRST_PAGE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_first_page));
        attributes.put(
                ItemField.FORUM_TITLE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_forum_title));
        attributes.put(
                ItemField.GENRE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_genre));
        attributes.put(
                ItemField.HISTORY,
                new RenderingAttributes(true,300, R.layout.listitem_field,R.string.zotero_field_history));
        attributes.put(
                ItemField.INSTITUTION,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_institution));
        attributes.put(
                ItemField.INTERVIEW_MEDIUM,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_interview_medium));
         attributes.put(
                ItemField.ISSN,
                new RenderingAttributes(true, 1000,R.layout.listitem_field_inline,R.string.zotero_field_issn));
        attributes.put(
                ItemField.ISBN,
                new RenderingAttributes(true, 1010, R.layout.listitem_field_inline,R.string.zotero_field_isbn));
        attributes.put(
                ItemField.ISSUE,
                new RenderingAttributes(true, 400,R.layout.listitem_field_inline,R.string.zotero_field_issue));
        attributes.put(
                ItemField.ISSUE_DATE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_issue_date));
        attributes.put(
                ItemField.ISSUING_AUTHORITY,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_issuing_authority));
        attributes.put(
                ItemField.JOURNAL_ABBREVIATION,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_journal_abbreviation));
        attributes.put(
                ItemField.LABEL,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_label));
        attributes.put(
                ItemField.LANGUAGE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_language));
        attributes.put(
                ItemField.LEGAL_STATUS,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_legal_status));
        attributes.put(
                ItemField.LETTER_TYPE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_letter_type));
        attributes.put(
                ItemField.LIBRARY_CATALOG,
                new RenderingAttributes(true, 1100,R.layout.listitem_field_inline,R.string.zotero_field_library_catalog));
        attributes.put(
                ItemField.LEGISLATIVE_BODY,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_legislative_body));
        attributes.put(
                ItemField.LINK_MODE,
                new RenderingAttributes(false,0, 0,0));
        attributes.put(
                ItemField.LOCAL_TIME,
                new RenderingAttributes(false,0, 0,0));
        attributes.put(
                ItemField.MANUSCRIPT_TYPE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_manusript_type));
        attributes.put(
                ItemField.MAP_TYPE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_map_type));
        attributes.put(
                ItemField.MD5,
                new RenderingAttributes(false,0, 0,0));
        attributes.put(
                ItemField.MEETING_NAME,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_meeting_name));
        attributes.put(
                ItemField.MODIFICATION_TIME,
                new RenderingAttributes(
                        true,1000, R.layout.listitem_field_inline,
                        R.string.zotero_field_modification_time, modificationTimeReformatter));
        attributes.put(
                ItemField.NAME_OF_ACT,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_name_of_act));
        attributes.put(
                ItemField.NETWORK,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_network));
        attributes.put(
                ItemField.NUMBER_OF_VOLUMES,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_number_of_volumes));
        attributes.put(
                ItemField.NUM_PAGES,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_num_pages));
        attributes.put(
                ItemField.NOTE,
                new RenderingAttributes(false, 0,0,0));
        attributes.put(
                ItemField.PAGES,
                new RenderingAttributes(true,300, R.layout.listitem_field_inline,R.string.zotero_field_pages));
        attributes.put(
                ItemField.PATENT_NUMBER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_patent_number));
        attributes.put(
                ItemField.PLACE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_language));
        attributes.put(
                ItemField.POST_TYPE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_post_type));
        attributes.put(
                ItemField.PRESENTATION_TYPE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_presentation_type));
        attributes.put(
                ItemField.PRIORITY_NUMBERS,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_priority_numbers));
        attributes.put(
                ItemField.PROCEEDINGS_TITLE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_proceedings_title));
        attributes.put(
                ItemField.PROGRAM_TITLE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_program_title));
        attributes.put(
                ItemField.PROGRAMMING_LANGUAGE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_programming_language));
        attributes.put(
                ItemField.PUBLIC_LAW_NUMBER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_public_law_number));
        attributes.put(
                ItemField.PUBLICATION_TITLE,
                new RenderingAttributes(true,200, R.layout.listitem_field_inline,R.string.zotero_field_publication_title));
        attributes.put(
                ItemField.PUBLISHER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_publisher));
        attributes.put(
                ItemField.REFERENCES,
                new RenderingAttributes(true,800, R.layout.listitem_field,R.string.zotero_field_references));
        attributes.put(
                ItemField.REPORT_TYPE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_report_type));
        attributes.put(
                ItemField.REPORTER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_reporter));
        attributes.put(
                ItemField.REPORTER_VOLUME,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_reporter_volume));
        attributes.put(
                ItemField.RIGHTS,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_rights));
        attributes.put(
                ItemField.RUNNING_TIME,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_running_time));
        attributes.put(
                ItemField.SCALE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_scale));
        attributes.put(
                ItemField.SECTION,
                new RenderingAttributes(true,300, R.layout.listitem_field_inline,R.string.zotero_field_section));
        attributes.put(
                ItemField.SERIES,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_series));
        attributes.put(
                ItemField.SERIES_NUMBER,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_series_number));
        attributes.put(
                ItemField.SERIES_TEXT,
                new RenderingAttributes(true,800, R.layout.listitem_field ,R.string.zotero_field_series_text));
        attributes.put(
                ItemField.SERIES_TITLE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_series_title));
        attributes.put(
                ItemField.SESSION,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_session));
        attributes.put(
                ItemField.SHORT_TITLE,
                new RenderingAttributes(true,900, R.layout.listitem_field_inline,R.string.zotero_field_short_title));
        attributes.put(
                ItemField.STUDIO,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_studio));
        attributes.put(
                ItemField.SUBJECT,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_subject));
        attributes.put(
                ItemField.SYSTEM,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_system));
        attributes.put(
                ItemField.THESIS_TYPE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_thesis_type));
        attributes.put(
                ItemField.TITLE,
                new RenderingAttributes(false, 0,0,0));
        attributes.put(
                ItemField.UNIVERSITY,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_university));
        attributes.put(
                ItemField.URL,
                new RenderingAttributes(true,700, R.layout.listitem_field_inline,R.string.zotero_field_url));
        attributes.put(
                ItemField.VERSION,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_version));
        attributes.put(
                ItemField.VIDEO_RECORDING_FORMAT,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_video_recording_format));
        attributes.put(
                ItemField.VOLUME,
                new RenderingAttributes(true, 500,R.layout.listitem_field_inline,R.string.zotero_field_volume));
        attributes.put(
                ItemField.WEBSITE_TITLE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_website_title));
        attributes.put(
                ItemField.WEBSITE_TYPE,
                new RenderingAttributes(true,800, R.layout.listitem_field_inline,R.string.zotero_field_website_type));

    }

    public FieldRenderer(Context context, ViewGroup parent)
    {
        this.parent = parent;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = context.getResources();
    }

    public List<Field> sortFields(List<Field> input)
    {
        Comparator<Field> comparator = new Comparator<Field>(){
            @Override
            public int compare(Field lhs, Field rhs)
            {
                RenderingAttributes l = attributes.get(lhs.getType());
                RenderingAttributes r = attributes.get(rhs.getType());
                int lPos = l == null ? 999999 : l.getSortingPosition();
                int rPos = r == null ? 999999 : r.getSortingPosition();

                return lPos - rPos;
            }
        };

        Collections.sort(input,comparator);
        return input;
    }

    public View createView(Field field)
    {
        int layoutResource = R.layout.listitem_field_inline;
        String title = field.getType().getZoteroName();
        String value = field.getValue();
        if (attributes.containsKey(field.getType()))
        {
            RenderingAttributes attr = attributes.get(field.getType());
            if (!attr.isVisible) return null;
            layoutResource = attr.getLayoutId();
            title = resources.getString(attr.getTitleId());
            value = attr.getFormatter().reformat(value);
        }
        View view = inflater.inflate(layoutResource, parent, false);

        assert view != null;
        TextView textView = (TextView) view.findViewById(R.id.textViewFieldName);
        if (textView != null)
        {
            textView.setText(title);
        }
        textView = (TextView) view.findViewById(R.id.textViewFieldValue);
        if (textView != null)
        {
            textView.setText(value);
        }
        return view;
    }
}
