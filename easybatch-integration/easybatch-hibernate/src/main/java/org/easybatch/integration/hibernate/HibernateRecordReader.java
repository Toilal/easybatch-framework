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

package org.easybatch.integration.hibernate;

import org.easybatch.core.api.Header;
import org.easybatch.core.api.RecordReader;
import org.easybatch.core.record.GenericRecord;
import org.hibernate.*;

import java.util.Date;

import static org.easybatch.core.util.Utils.checkArgument;

/**
 * Reader that reads data using Hibernate API.
 * <p/>
 * This reader produces {@link GenericRecord} instances that can be mapped
 * with {@link org.easybatch.core.mapper.GenericRecordMapper} in order to get the raw objects.
 *
 * @param <T> the type of objects this reader will read.
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */

@SuppressWarnings(value = "unchecked") // argh hibernate APIs that return raw types ..
public class HibernateRecordReader<T> implements RecordReader {

    private Session session;

    private String query;

    private ScrollableResults scrollableResults;

    private int maxResults;

    private int fetchSize;

    private long currentRecordNumber;

    /**
     * Create a hibernate record reader.
     *
     * @param sessionFactory a pre-configured hibernate session factory
     * @param query          the HQL query to use to fetch data
     */
    public HibernateRecordReader(final SessionFactory sessionFactory, final String query) {
        this.session = sessionFactory.openSession();
        this.query = query;
    }

    @Override
    public void open() {
        currentRecordNumber = 0;
        Query hibernateQuery = session.createQuery(query);
        hibernateQuery.setReadOnly(true);
        if (maxResults >= 1) {
            hibernateQuery.setMaxResults(maxResults);
        }
        if (fetchSize >= 1) {
            hibernateQuery.setFetchSize(fetchSize);
        }
        scrollableResults = hibernateQuery.scroll(ScrollMode.FORWARD_ONLY);
    }

    @Override
    public boolean hasNextRecord() {
        return scrollableResults.next();
    }

    @Override
    public GenericRecord<T> readNextRecord() {
        Header header = new Header(++currentRecordNumber, getDataSourceName(), new Date());
        return new GenericRecord<T>(header, (T) scrollableResults.get()[0]);
    }

    @Override
    public Long getTotalRecords() {
        return null; // Not possible with FORWARD_ONLY scrollable results
    }

    @Override
    public String getDataSourceName() {
        return "Result of HQL query: " + query;
    }

    @Override
    public void close() {
        session.close();
    }

    public void setMaxResults(final int maxResults) {
        checkArgument(maxResults >= 1, "max result parameter must be greater than or equal to 1");
        this.maxResults = maxResults;
    }

    public void setFetchSize(final int fetchSize) {
        checkArgument(fetchSize >= 1, "fetch size parameter must be greater than or equal to 1");
        this.fetchSize = fetchSize;
    }

}
