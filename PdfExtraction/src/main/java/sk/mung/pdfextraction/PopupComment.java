package sk.mung.pdfextraction;

import org.apache.pdfbox.pdmodel.common.PDTextStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class PopupComment implements Comment {

    private final static Pattern pattern = Pattern.compile("<body[^>]*>(.*)</body>");
    private final PDAnnotationText annotation;
    private final String pageLabel;



    PopupComment(PDAnnotationText annotation, String pageLabel)
    {
        this.annotation = annotation;
        this.pageLabel = pageLabel;
    }

    @Override
    public String getText() throws IOException, ParserConfigurationException, SAXException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(annotation.getRichContents().getAsStream());
        return doc.getDocumentElement().getTextContent();
    }

    @Override
    public String getHtmlText() throws ParserConfigurationException, IOException, SAXException
    {
        PDTextStream rich = annotation.getRichContents();
        String text = "";
        if(rich !=null )
        {
            text = rich.getAsString();
            Matcher matcher = pattern.matcher(text);
            if(matcher.find())
            {
                text =  matcher.group(1);
            }
        }
        else
        {
            text = annotation.getContents();
        }
        return text;
    }

    @Override
    public String getPageLabel() {
        return pageLabel;
    }

    @Override
    public String format(String template, String highlight, String note)
            throws IOException, SAXException, ParserConfigurationException {
        return template
                .replace(PdfExtractor.PLACEHOLDER_TYPE, note)
                .replace(PdfExtractor.PLACEHOLDER_PAGE, getPageLabel())
                .replace(PdfExtractor.PLACEHOLDER_CONTENT, getHtmlText());
    }
}
