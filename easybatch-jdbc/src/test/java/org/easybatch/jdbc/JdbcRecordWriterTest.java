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

package org.easybatch.jdbc;

import org.easybatch.core.api.Report;
import org.easybatch.core.mapper.GenericRecordMapper;
import org.easybatch.core.reader.IterableRecordReader;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.easybatch.core.impl.EngineBuilder.aNewEngine;

/**
 * Test class for {@link JdbcRecordWriter}.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public class JdbcRecordWriterTest {

    private static final String DATABASE_URL = "jdbc:hsqldb:mem";

    private static Connection connection;

    private JdbcRecordWriter jdbcRecordWriter;

    @BeforeClass
    public static void initDatabase() throws Exception {
        System.setProperty("hsqldb.reconfig_logging", "false");
        connection = DriverManager.getConnection(DATABASE_URL, "sa", "pwd");
        connection.setAutoCommit(false);
        createTweetTable(connection);
    }

    @Before
    public void setUp() throws Exception {
        String query = "INSERT INTO tweet VALUES (?,?,?);";
        PreparedStatementProvider preparedStatementProvider = new PreparedStatementProvider() {
            @Override
            public void prepareStatement(PreparedStatement statement, Object record) throws SQLException {
                Tweet tweet = (Tweet) record;
                statement.setInt(1, tweet.getId());
                statement.setString(2, tweet.getUser());
                statement.setString(3, tweet.getMessage());
            }
        };
        jdbcRecordWriter = new JdbcRecordWriter(connection, query, preparedStatementProvider);
    }

    @Test
    public void testProcessRecord() throws Exception {

        Integer nbTweetsToInsert = 13;
        Integer commitInterval = 5;
        
        /*
         * The first chunk of 5 records will be committed by the JdbcTransactionStepListener
         * The second chunk of 5 records will be committed by the JdbcTransactionStepListener
         * The last chunk of 3 records will be committed by the JdbcTransactionJobListener
         */

        List<Tweet> tweets = createTweets(nbTweetsToInsert);

        Report report = aNewEngine()
                .reader(new IterableRecordReader<Tweet>(tweets))
                .mapper(new GenericRecordMapper())
                .writer(jdbcRecordWriter)
                .recordProcessorEventListener(new JdbcTransactionStepListener(connection, commitInterval))
                .jobEventListener(new JdbcTransactionJobListener(connection))
                .build().call();

        assertThat(report).isNotNull();
        assertThat(report.getTotalRecords()).isEqualTo(valueOf(nbTweetsToInsert));
        assertThat(report.getSuccessRecordsCount()).isEqualTo(valueOf(nbTweetsToInsert));

        int nbTweetsInDatabase = countTweetsInDatabase();

        assertThat(nbTweetsInDatabase).isEqualTo(nbTweetsToInsert);
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

    private List<Tweet> createTweets(Integer nbTweetsToInsert) {
        List<Tweet> tweets = new ArrayList<Tweet>();
        for (int i = 1; i <= nbTweetsToInsert; i++) {
            tweets.add(new Tweet(i, "user " + i, "hello " + i));
        }
        return tweets;
    }

    @AfterClass
    public static void shutdownDatabase() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        //delete hsqldb tmp files
        new File("mem.log").delete();
        new File("mem.properties").delete();
        new File("mem.script").delete();
        new File("mem.tmp").delete();
        new File("mem.lck").delete();
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
        connection.commit();
    }

}
