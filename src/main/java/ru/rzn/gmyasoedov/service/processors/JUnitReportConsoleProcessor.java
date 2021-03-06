package ru.rzn.gmyasoedov.service.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public class JUnitReportConsoleProcessor implements FileProcessor {
    private static final String TAG_TEST_CASE = "testcase";
    private static final String ATTRIBUTE_CLASS_NAME = "classname";
    private static final String ATTRIBUTE_NAME = "name";
    private static final ReportType REPORT_TYPE = new ReportType("junit");
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ReportType getReportType() {
        return REPORT_TYPE;
    }

    @Override
    public void process(Path path) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = null;
        try {
            reader = xmlInputFactory.createXMLEventReader(new FileInputStream(path.toFile()));
            parse(reader);
        } catch (XMLStreamException e) {
            logger.error("parse error", e);
        } catch (FileNotFoundException e) {
            logger.error("file not found error", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                logger.error("parse error", e);
            }
        }
    }

    private void parse(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                if (TAG_TEST_CASE.equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    processItem(startElement);
                }
            }
        }
    }

    void processItem(StartElement startElement) {
        Attribute className = startElement.getAttributeByName(new QName(ATTRIBUTE_CLASS_NAME));
        Attribute name = startElement.getAttributeByName(new QName(ATTRIBUTE_NAME));
        System.out.println(className.getValue() + "#" + name);
    }
}
