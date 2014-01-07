package sk.mung.pdfextraction;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;


interface Comment {
    String getText() throws IOException, ParserConfigurationException, SAXException;

    String getHtmlText() throws ParserConfigurationException, IOException, SAXException;

    String getPageLabel();

    String format(String template, String highlight, String note) throws IOException, SAXException, ParserConfigurationException;
}
