package sk.mung.sentience.zoterocommuter;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class NoteEditor extends FragmentActivity
{
    static final String textIntent;

    private final ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            assert inflater != null;
            inflater.inflate(R.menu.contextual_note_editor, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            for(int ix = 0; ix<menu.size();ix++)
            {
                MenuItem item = menu.getItem(ix);
                item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);


                if(item.getItemId() == android.R.id.selectAll || item.getItemId() == android.R.id.paste)
                {
                    item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId())
            {
                case R.id.bold_text:
                    onBoldClicked();
                    return true;
                case R.id.italic_text:
                    onItalicClicked();
                    return true;
                case R.id.underline_text:
                    onUnderlineClicked();
                    return true;
                case R.id.strike_text:
                    onStrikeClicked();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {

        }
    };

    public static String getTextIntent() { return textIntent;}

    static
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            textIntent = Intent.EXTRA_HTML_TEXT;
        }
        else textIntent = Intent.EXTRA_TEXT;
    }


    private interface SpanMatcher<T>
    {
        Class<T> getSpanType();

        boolean isSpanMatched(T span);

        T getSpanInstance();
    }
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        setupActionBar();
        String text = getIntent().getStringExtra(textIntent);
        final EditText editText = ((EditText)findViewById(R.id.editTextNote));
        editText.setText(Html.fromHtml(text));
        editText.setCustomSelectionActionModeCallback(callback);
    }

    private void setupActionBar()
    {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.note_editor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Respond to clicks on the actions in the CAB
        switch (item.getItemId())
        {
            case R.id.save_text:
                onSaveSelected();
                return true;

            default:
                return false;
        }
    }

    public void onSaveSelected()
    {
        Intent intent = new Intent();
        EditText editText = ((EditText)findViewById(R.id.editTextNote));
        Editable text = editText.getText();
        assert text != null;
        intent.putExtra(textIntent, Html.toHtml(text));
        intent.setType("text/html");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onBoldClicked( )
    {
        setFormatting(new SpanMatcher<StyleSpan>()
        {
            @Override
            public Class<StyleSpan> getSpanType()
            {
                return StyleSpan.class;
            }

            @Override
            public boolean isSpanMatched(StyleSpan span)
            {
                return span.getStyle() == Typeface.BOLD;
            }

            @Override
            public StyleSpan getSpanInstance()
            {
                return new StyleSpan(Typeface.BOLD);
            }
        });
    }

    public void onItalicClicked()
    {
        setFormatting(new SpanMatcher<StyleSpan>()
        {
            @Override
            public Class<StyleSpan> getSpanType()
            {
                return StyleSpan.class;
            }

            @Override
            public boolean isSpanMatched(StyleSpan span)
            {
                return span.getStyle() == Typeface.ITALIC;
            }

            @Override
            public StyleSpan getSpanInstance()
            {
                return new StyleSpan(Typeface.ITALIC);
            }
        });
    }

    public void onUnderlineClicked( )
    {
        setFormatting(new SpanMatcher<UnderlineSpan>()
        {
            @Override
            public Class<UnderlineSpan> getSpanType()
            {
                return UnderlineSpan.class;
            }

            @Override
            public boolean isSpanMatched(UnderlineSpan span)
            {
                return true;
            }

            @Override
            public UnderlineSpan getSpanInstance()
            {
                return new UnderlineSpan();
            }
        });
    }

    public void onStrikeClicked( )
    {
        setFormatting(new SpanMatcher<StrikethroughSpan>()
        {
            @Override
            public Class<StrikethroughSpan> getSpanType()
            {
                return StrikethroughSpan.class;
            }

            @Override
            public boolean isSpanMatched(StrikethroughSpan span)
            {
                return true;
            }

            @Override
            public StrikethroughSpan getSpanInstance()
            {
                return new StrikethroughSpan();
            }
        });
    }

    private <T> void setFormatting(SpanMatcher<T> matcher)
    {
        EditText editText = ((EditText)findViewById(R.id.editTextNote));
        int selectionStart = editText.getSelectionStart();
        int selectionEnd = editText.getSelectionEnd();

        if (selectionStart > selectionEnd)
        {
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }

        if (selectionEnd > selectionStart)
        {
            Spannable text = editText.getText();
            assert text != null;
            T[] spans = text.getSpans(selectionStart, selectionEnd, matcher.getSpanType());
            boolean exists = false;
            for (T span : spans)
            {
                if (matcher.isSpanMatched(span))
                {
                    text.removeSpan(span);
                    exists = true;
                }
            }

            if (!exists)
            {
                text.setSpan(
                        matcher.getSpanInstance(),
                        selectionStart,
                        selectionEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        }
    }
}


