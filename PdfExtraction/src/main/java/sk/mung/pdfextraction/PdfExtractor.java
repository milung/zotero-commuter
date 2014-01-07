package sk.mung.pdfextraction;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.xml.sax.SAXException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;


public class PdfExtractor implements Closeable
{

    public static final String PLACEHOLDER_TYPE = "@type";
    public static final String PLACEHOLDER_PAGE = "@page";
    public static final String PLACEHOLDER_CONTENT = "@content";


    private final PDDocument pddDocument;

    public PdfExtractor(File file) throws IOException {
        this.pddDocument = PDDocument.load(file);
    }

    List<Comment> getComments() throws IOException {
        @SuppressWarnings("unchecked")
        List<PDPage> allPages = pddDocument.getDocumentCatalog().getAllPages();
        String[] pageLabels = null;

        PDPageLabels labels = pddDocument.getDocumentCatalog().getPageLabels();
        if(labels != null)
        {
            pageLabels = labels.getLabelsByPageIndices();
        }
        List<Comment> comments = new ArrayList<Comment>();
        int pageNumber = 0;
        for (PDPage page: allPages)
        {
            String pageLabel
                    = pageLabels == null
                    ? Integer.toString(pageNumber+1)
                    : pageLabels[pageNumber];

            pageNumber++;
            List<PDAnnotation> pageAnnotations = page.getAnnotations();

            for(PDAnnotation annotation: pageAnnotations)
            {
                if(annotation instanceof PDAnnotationText)
                {
                    comments.add(new PopupComment((PDAnnotationText)annotation, pageLabel));
                }

                if(annotation instanceof PDAnnotationTextMarkup)
                {
                    comments.add(
                            new HighlightComment((PDAnnotationTextMarkup)annotation,page, pageLabel));
                }
            }

        }
        return comments;
    }

    public void close() throws IOException {

        pddDocument.close();
    }

    public String extractAnnotations(String template, String highlight, String note) throws IOException, ParserConfigurationException, SAXException {
        StringBuilder builder = new StringBuilder();
        for(Comment comment : getComments())
        {
            builder.append(comment.format(template, highlight, note));
        }
        return builder.toString();
    }
}
