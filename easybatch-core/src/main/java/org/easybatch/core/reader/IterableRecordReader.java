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

package org.easybatch.core.reader;

import org.easybatch.core.api.Header;
import org.easybatch.core.api.RecordReader;
import org.easybatch.core.api.RecordReaderOpeningException;
import org.easybatch.core.api.RecordReadingException;
import org.easybatch.core.record.GenericRecord;

import java.util.Date;
import java.util.Iterator;

import static org.easybatch.core.util.Utils.checkNotNull;

/**
 * Reads record from an {@link Iterable} data source.
 * <p/>
 * This reader produces {@link GenericRecord} instances containing original objects from the datasource.
 * You can use a {@link org.easybatch.core.mapper.GenericRecordMapper} to get the payload of a generic record
 * which is the original object from the datasource
 *
 * @param <T> the type of records in the iterable data source.
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public class IterableRecordReader<T> implements RecordReader {

    /**
     * The current record number.
     */
    private long currentRecordNumber;

    /**
     * The data source iterator.
     */
    private Iterator<T> iterator;

    /**
     * Reads record from an {@link Iterable} data source.
     * <p/>
     * This reader produces {@link GenericRecord} instances containing original objects from the datasource.
     * You can use a {@link org.easybatch.core.mapper.GenericRecordMapper} to get the payload of a generic record
     * which is the original object from the datasource
     *
     * @param dataSource the data source to read records from.
     */
    public IterableRecordReader(final Iterable<T> dataSource) {
        checkNotNull(dataSource, "data source");
        this.iterator = dataSource.iterator();
    }

    @Override
    public void open() throws RecordReaderOpeningException {
        currentRecordNumber = 0;
    }

    @Override
    public boolean hasNextRecord() {
        return iterator.hasNext();
    }

    @Override
    public GenericRecord<T> readNextRecord() throws RecordReadingException {
        Header header = new Header(++currentRecordNumber, getDataSourceName(), new Date());
        return new GenericRecord<T>(header, iterator.next());
    }

    @Override
    public Long getTotalRecords() {
        return null;
    }

    @Override
    public String getDataSourceName() {
        return "In-Memory Iterable";
    }

    @Override
    public void close() {
        // no op
    }
}
