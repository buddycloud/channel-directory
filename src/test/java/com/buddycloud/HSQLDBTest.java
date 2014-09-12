package com.buddycloud;

import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;

public class HSQLDBTest {

	private ChannelDirectoryDataSource dataSource;

	@Before
	public void setUpDB() throws Exception {
		Properties p = new Properties();
		p.setProperty("mahout.jdbc.url", "jdbc:hsqldb:mem:channels;create=true");
		p.setProperty("mahout.jdbc.driver", "org.hsqldb.jdbcDriver");
		this.dataSource = new ChannelDirectoryDataSource(p);
		
		runBatch("resources/schema/create-schema.sql");
		runBatch("resources/schema/update-schema-0.sql");
	}

	@After
	public void tearDownDB() throws Exception {
		runBatch("resources/schema/drop-schema.sql");
	}
	
	private void runBatch(String batchFile) throws PropertyVetoException,
	SQLException, IOException, FileNotFoundException {
		Statement st = dataSource.createStatement();
		String schemaStr = IOUtils.toString(new FileInputStream(batchFile));
		st.addBatch("SET DATABASE SQL SYNTAX PGS TRUE");
		for (String sqlStr : schemaStr.split(";")) {
			if (sqlStr.trim().length() > 0) {
				st.addBatch(sqlStr);
			}
		}
		st.executeBatch();
		ChannelDirectoryDataSource.close(st);
	}
	
	public ChannelDirectoryDataSource getDataSource() {
		return dataSource;
	}
}
