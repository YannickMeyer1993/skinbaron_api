package com.company;

import junit.framework.TestCase;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.sql.SQLException;

import static com.company.Deployment.crawlItemInformations;
import static com.company.Deployment.crawlWearValues;

public class DeploymentTest extends TestCase {

    public void testDeployment() throws IOException, InterruptedException, SQLException {
        crawlItemInformations();
    }

    public void testCrawlWearValues() throws SQLException, DocumentException, IOException, InterruptedException {
        crawlWearValues();
    }
}