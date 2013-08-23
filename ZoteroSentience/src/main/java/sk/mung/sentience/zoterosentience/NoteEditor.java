package sk.mung.sentience.zoterosentience;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.method.KeyListener;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class NoteEditor extends FragmentActivity
{
    private KeyListener originalKeyListener;
    private View.OnClickListener originalClickListener;

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
        String text = getIntent().getStringExtra(Intent.EXTRA_HTML_TEXT);
        final EditText editText = ((EditText)findViewById(R.id.editTextNote));
        editText.setText(Html.fromHtml(text));
    }

    private void setupActionBar()
    {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.hide();
        actionBar.setTitle(R.string.note_editor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_editor, menu);
        return true;
    }

    public void onSaveSelected( View button)
    {
        Intent intent = new Intent();
        EditText editText = ((EditText)findViewById(R.id.editTextNote));
        intent.putExtra(Intent.EXTRA_HTML_TEXT, Html.toHtml(editText.getText()));
        intent.setType("text/html");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onBoldClicked( View button)
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

    public void onItalicClicked( View button)
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

    public void onUnderlineClicked( View button)
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

    public void onStrikeClicked( View button)
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
        Spannable s = editText.getText();
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


