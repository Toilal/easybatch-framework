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

package org.easybatch.jpa;

import org.easybatch.core.api.Report;
import org.easybatch.core.mapper.GenericRecordMapper;
import org.easybatch.core.reader.IterableRecordReader;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.easybatch.core.impl.EngineBuilder.aNewEngine;

/**
 * Test class for {@link JpaRecordWriter}.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public class JpaRecordWriterTest {

    private static final String DATABASE_URL = "jdbc:hsqldb:mem";

    private static Connection connection;

    private static EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private JpaRecordWriter<Tweet> jpaRecordWriter;

    @BeforeClass
    public static void initDatabase() throws Exception {
        connection = DriverManager.getConnection(DATABASE_URL, "sa", "pwd");
        createTweetTable(connection);
        entityManagerFactory = Persistence.createEntityManagerFactory("tweet");
    }

    @Before
    public void setUp() throws Exception {
        entityManager = entityManagerFactory.createEntityManager();
        jpaRecordWriter = new JpaRecordWriter<Tweet>(entityManager);
    }

    @Test
    public void testRecordWritingInChunks() throws Exception {

        Integer nbTweetsToInsert = 13;
        Integer commitInterval = 5;

        /*
         * The first chunk of 5 records will be committed by the JpaTransactionStepListener
         * The second chunk of 5 records will be committed by the JpaTransactionStepListener
         * The last chunk of 3 records will be committed by the JpaTransactionJobListener
         */

        List<Tweet> tweets = createTweets(nbTweetsToInsert);

        JpaTransactionStepListener jpaTransactionStepListener = new JpaTransactionStepListener(entityManager, commitInterval);
        JpaTransactionJobListener jpaTransactionJobListener = new JpaTransactionJobListener(entityManager, true);

        Report report = aNewEngine()
                .reader(new IterableRecordReader<Tweet>(tweets))
                .mapper(new GenericRecordMapper())
                .writer(jpaRecordWriter)
                .recordProcessorEventListener(jpaTransactionStepListener)
                .jobEventListener(jpaTransactionJobListener)
                .build().call();

        assertThat(report).isNotNull();
        assertThat(report.getTotalRecords()).isEqualTo(valueOf(nbTweetsToInsert));
        assertThat(report.getSuccessRecordsCount()).isEqualTo(valueOf(nbTweetsToInsert));

        int nbTweetsInDatabase = countTweetsInDatabase();

        assertThat(nbTweetsInDatabase).isEqualTo(nbTweetsToInsert);
    }

    private List<Tweet> createTweets(Integer nbTweetsToInsert) {
        List<Tweet> tweets = new ArrayList<Tweet>();
        for (int i = 1; i <= nbTweetsToInsert; i++) {
            tweets.add(new Tweet(i, "user " + i, "hello " + i));
        }
        return tweets;
    }

    private int countTweetsInDatabase() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from tweet");
        int nbTweets = 0;
        while (resultSet.next()) {
            nbTweets++;
        }
        resultSet.close();
        statement.close();
        return nbTweets;
    }

    @AfterClass
    public static void shutdownDatabase() throws Exception {
        if (connection != null) {
            connection.close();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
        //delete hsqldb tmp files
        new File("mem.log").delete();
        new File("mem.properties").delete();
        new File("mem.script").delete();
        new File("mem.tmp").delete();
    }

    private static void createTweetTable(Connection connection) throws Exception {
        Statement statement = connection.createStatement();
        String query = "DROP TABLE IF EXISTS tweet";
        statement.executeUpdate(query);
        query = "CREATE TABLE tweet (\n" +
                "  id integer NOT NULL PRIMARY KEY,\n" +
                "  user varchar(32) NOT NULL,\n" +
                "  message varchar(140) NOT NULL,\n" +
                ");";
        statement.executeUpdate(query);
        statement.close();
    }

}
