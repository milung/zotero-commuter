package sk.mung.pdfextraction;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.util.PDFTextStripperByArea;
import org.xml.sax.SAXException;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

class HighlightComment implements Comment {

    private final PDAnnotationTextMarkup annotation;
    private final PDPage page;
    private final String pageLabel;



    HighlightComment(PDAnnotationTextMarkup annotation, PDPage page, String pageLabel)
    {
        this.annotation = annotation;
        this.page = page;
        this.pageLabel = pageLabel;
    }

    @Override
    public String getText() throws IOException, ParserConfigurationException, SAXException
    {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);

        int rotation = page.findRotation();
        PDRectangle pageSize = page.findMediaBox();

        float[] quadPoints = annotation.getQuadPoints();
        for(int i = 0; i < quadPoints.length/8; ++i)
        {
            float minx = Integer.MAX_VALUE;
            float miny = Integer.MAX_VALUE;
            float maxx = Integer.MIN_VALUE;
            float maxy = Integer.MIN_VALUE;
            for(int j = 0; j < 8; j+=2)
            {
                minx = Math.min(minx,quadPoints[i*8+j]);
                miny = Math.min(miny,quadPoints[i*8+j+1]);
                maxx = Math.max(maxx,quadPoints[i*8+j]);
                maxy = Math.max(maxy,quadPoints[i*8+j+1]);
            }
            float x = quadPoints[i*8] - 1;
            float y = quadPoints[i*8 + 1] - 1;

            float w = maxx-minx + 2;
            float h = maxy - miny + 2;

            if (rotation == 0) {

                y = pageSize.getHeight() - y;
            }

            Rectangle2D.Float awtRect = new Rectangle2D.Float(x, y, w,h);

            stripper.addRegion(Integer.toString(i), awtRect);
        }

        stripper.extractRegions(page);
        StringBuilder textBuilder = new StringBuilder();
        for(int i = 0; i < quadPoints.length/8; ++i)
        {
            String regionText = stripper.getTextForRegion(Integer.toString(i));
            textBuilder.append(regionText);
        }
        return textBuilder.toString().replace("\r\n"," ").replace("\r"," ").replace("\r"," ").trim();
    }

    @Override
    public String getHtmlText() throws ParserConfigurationException, IOException, SAXException
    {
        return getText();
    }

    @Override
    public String getPageLabel() {
        return pageLabel;
    }

    @Override
    public String format(String template, String highlight, String note) throws IOException, SAXException, ParserConfigurationException {
        return template
                .replace(PdfExtractor.PLACEHOLDER_TYPE, highlight)
                .replace(PdfExtractor.PLACEHOLDER_PAGE, getPageLabel())
                .replace(PdfExtractor.PLACEHOLDER_CONTENT, getHtmlText());
    }
}
