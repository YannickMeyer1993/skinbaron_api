package com.company;

import junit.framework.TestCase;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.sql.SQLException;

import static com.company.Deployment.crawItemInformations;
import static com.company.Deployment.crawWearValues;

public class DeploymentTest extends TestCase {

    public void testDeployment() throws IOException, InterruptedException, SQLException {
        crawItemInformations();
    }

    public void testCrawWearValues() throws SQLException, DocumentException, IOException, InterruptedException {
        crawWearValues();
    }
}