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

import org.easybatch.core.api.Engine;
import org.easybatch.core.api.Report;
import org.easybatch.core.processor.RecordCollector;
import org.easybatch.core.record.GenericRecord;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easybatch.core.impl.EngineBuilder.aNewEngine;

/**
 * Integration test for hibernate support.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public class HibernateRecordReaderTest {

    private HibernateRecordReader<Tweet> hibernateRecordReader;

    @BeforeClass
    public static void init() throws Exception {
        DatabaseUtil.startEmbeddedDatabase();
        DatabaseUtil.populateTweetTable();
        DatabaseUtil.initializeSessionFactory();
    }

    @Before
    public void setUp() {
        SessionFactory sessionFactory = DatabaseUtil.getSessionFactory();
        hibernateRecordReader = new HibernateRecordReader<Tweet>(sessionFactory, "from Tweet");
    }

    @Test
    public void testRecordReading() throws Exception {

        Engine engine = aNewEngine()
                .reader(hibernateRecordReader)
                .processor(new RecordCollector<Tweet>())
                .build();

        Report report = engine.call();
        assertThat(report.getTotalRecords()).isEqualTo(3);

        List<GenericRecord<Tweet>> tweets = (List<GenericRecord<Tweet>>) report.getBatchResult();

        assertThat(tweets).isNotEmpty().hasSize(3);

        Tweet tweet = tweets.get(0).getPayload();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getId()).isEqualTo(1);
        assertThat(tweet.getUser()).isEqualTo("foo");
        assertThat(tweet.getMessage()).isEqualTo("easy batch rocks! #EasyBatch");

        tweet = tweets.get(1).getPayload();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getId()).isEqualTo(2);
        assertThat(tweet.getUser()).isEqualTo("bar");
        assertThat(tweet.getMessage()).isEqualTo("@foo I do confirm :-)");

        tweet = tweets.get(2).getPayload();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getId()).isEqualTo(3);
        assertThat(tweet.getUser()).isEqualTo("baz");
        assertThat(tweet.getMessage()).isEqualTo("yep");
    }

    @AfterClass
    public static void tearDown() {
        DatabaseUtil.closeSessionFactory();
        DatabaseUtil.cleanUpWorkingDirectory();
    }
}
