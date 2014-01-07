package sk.mung.pdfextraction;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertEquals;


public class PdfExtractorTests
{

    private final static String COMMENT_TEXT = "Example note\rline 2";
    private final static String COMMENT_HTML_TEXT = "<p dir=\"ltr\"><span dir=\"ltr\" style=\"font-size:10.0pt;text-align:left;color:#000000;font-weight:normal;font-style:normal\">Example </span><span dir=\"ltr\" style=\"text-decoration:underline;font-size:10.0pt;text-align:left;color:#000000;font-weight:normal;font-style:normal\">note</span><span dir=\"ltr\" style=\"font-size:10.0pt;text-align:left;color:#000000;font-weight:normal;font-style:normal\">&#13;</span><span dir=\"ltr\" style=\"font-size:10.0pt;text-align:left;color:#000000;font-weight:bold;font-style:normal\">line </span><span dir=\"ltr\" style=\"font-size:10.0pt;text-align:left;color:#000000;font-weight:normal;font-style:normal\">2</span></p>";
    private final static String TEST_FILE =
            "D:\\Data\\Annotations\\anotationswithzotero\\PdfExtraction\\src\\test\\resources\\lorem.pdf";
    private final static String HIGHLIGHT_TEXT
            = "Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit " +
            "amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit " +
            "cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus " +
            "quam, scelerisque ut, mollis sed, nonummy id, metus.";
    @Test
    public void testGetComments_CountEquals() throws IOException
    {
        PdfExtractor subject = new PdfExtractor(new File(TEST_FILE));

        List<Comment> comments = subject.getComments();

        assertEquals(2, comments.size());
        subject.close();
    }

    @Test
    public void testGetComments_CommentContentEquals() throws IOException, ParserConfigurationException, SAXException {
        PdfExtractor subject = new PdfExtractor(new File(TEST_FILE));

        List<Comment> comments = subject.getComments();

        assertEquals(COMMENT_TEXT,comments.get(1).getText());
        subject.close();
    }

    @Test
    public void testGetComments_HtmlCommentContentEquals() throws IOException, ParserConfigurationException, SAXException {
        PdfExtractor subject = new PdfExtractor(new File(TEST_FILE));

        List<Comment> comments = subject.getComments();

        assertEquals(COMMENT_HTML_TEXT,comments.get(1).getHtmlText());
        subject.close();
    }


    @Test
    public void testGetComments_HighlightContentEquals()
            throws IOException, ParserConfigurationException, SAXException
    {
        PdfExtractor subject = new PdfExtractor(new File(TEST_FILE));

        List<Comment> comments = subject.getComments();

        assertEquals(HIGHLIGHT_TEXT,comments.get(0).getHtmlText());
        subject.close();
    }

    @Test
    public void testExtractAnnotation_OutputExact()
            throws IOException, ParserConfigurationException, SAXException
    {
        PdfExtractor subject = new PdfExtractor(new File(TEST_FILE));
        String template="@type on page @page: @content<br/>";
        String highlight ="Highlight";
        String note = "Note";
        String result = subject.extractAnnotations(template, highlight, note );

        assertEquals("Highlight on page 1: "+ HIGHLIGHT_TEXT +
                "<br/>Note on page 1: " + COMMENT_HTML_TEXT + "<br/>",
                result );
        subject.close();
    }
}
