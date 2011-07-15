package environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.InputSource;

/**
 * RVOSpace
 *
 * @author Vaisagh
 * Created: Dec 15, 2010
 *
 * Copyright vaisagh
 *
 * Description:
 *
 * This class is defined to get the data from the XML file for our requirements.
 */
public class XMLManager {

    private static XMLManager instance = null;
    private JAXBContext context = null;
    private Marshaller marshaller = null;
    private Unmarshaller unmarshaller = null;

    public static synchronized XMLManager instance() {
        if (instance == null) {
            instance = new XMLManager();
        }
        return instance;
    }

    private XMLManager() {
        try {
            context = JAXBContext.newInstance("environment.geography");

            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            unmarshaller = context.createUnmarshaller();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized String marshal(Object obj, FileOutputStream stringWriter) throws JAXBException {
        //StringWriter stringWriter = new StringWriter();
        marshaller.marshal(obj, stringWriter);
        return stringWriter.toString();
    }

    public synchronized Object unmarshal(String str) throws JAXBException {
        File inputSource = new File(str);
        return unmarshaller.unmarshal(inputSource);
    }

    public synchronized Object unmarshal(File inputFile) throws JAXBException, FileNotFoundException {
        InputSource inputSource = new InputSource(new FileInputStream(inputFile));
        return unmarshaller.unmarshal(inputSource);
    }
}
