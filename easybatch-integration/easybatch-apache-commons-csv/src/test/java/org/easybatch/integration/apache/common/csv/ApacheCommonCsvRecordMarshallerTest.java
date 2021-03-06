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

package org.easybatch.integration.apache.common.csv;

import org.apache.commons.csv.CSVFormat;
import org.easybatch.core.api.RecordMarshallingException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link ApacheCommonCsvRecordMarshaller}.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public class ApacheCommonCsvRecordMarshallerTest {

    private ApacheCommonCsvRecordMarshaller marshaller;

    @Before
    public void setUp() throws Exception {
        CSVFormat csvFormat = CSVFormat.DEFAULT;
        marshaller = new ApacheCommonCsvRecordMarshaller(Foo.class, new String[]{"firstName", "lastName", "married"}, csvFormat);
    }

    @Test
    public void marshal() throws RecordMarshallingException {
        Foo foo = new Foo();
        foo.setFirstName("foo");
        foo.setLastName("bar");

        String expected = "foo,bar,false";
        String actual = marshaller.marshal(foo);

        System.out.println(actual);

        assertThat(actual).isEqualTo(expected);
    }
}