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

import org.easybatch.core.api.RecordProcessingException;
import org.easybatch.core.writer.AbstractRecordWriter;
import org.hibernate.Session;

/**
 * Writes entities to a database using Hibernate.
 *
 * This writer does not commit a transaction after writing entities.
 * You can use a {@link HibernateTransactionStepListener} for this purpose.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public class HibernateRecordWriter<T> extends AbstractRecordWriter<T> {

    private Session session;

    /**
     * Create a Hibernate record writer.
     *
     * @param session the hibernate session to use to write entities.
     */
    public HibernateRecordWriter(final Session session) {
        this.session = session;
    }

    @Override
    public void writeRecord(T record) throws RecordProcessingException {
        session.saveOrUpdate(record);
    }
}
