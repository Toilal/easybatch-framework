/*
 *  The MIT License
 *
 *   Copyright (c) 2015, Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */

package org.easybatch.xml;

import org.easybatch.core.api.RecordMarshallingException;
import org.easybatch.core.processor.AbstractRecordMarshaller;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.StringWriter;

/**
 * Marshals an object to XML using JAXB.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public class XmlRecordMarshaller extends AbstractRecordMarshaller {

    private Marshaller marshaller;

    /**
     * Create a record marshaller.
     *
     * @param type the type of object to marshal
     * @throws JAXBException if an exception occurs during JAXB context setup
     */
    public XmlRecordMarshaller(final Class type) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(type);
        marshaller = jaxbContext.createMarshaller();
        disableXmlDeclaration();
    }

    /**
     * Create a record marshaller.
     *
     * @param jaxbContext a pre-configured JAXB context
     * @throws JAXBException if an exception occurs during Jaxb context setup
     */
    public XmlRecordMarshaller(final JAXBContext jaxbContext) throws JAXBException {
        marshaller = jaxbContext.createMarshaller();
        disableXmlDeclaration();
    }

    private void disableXmlDeclaration() throws PropertyException {
        marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
    }
    
    @Override
    protected String marshal(final Object record) throws RecordMarshallingException {
        StringWriter stringWriter = new StringWriter();
        try {
            marshaller.marshal(record, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            throw new RecordMarshallingException(e);
        }
    }
}
