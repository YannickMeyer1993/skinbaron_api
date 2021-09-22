package com.company;

import junit.framework.TestCase;

import java.io.IOException;
import java.sql.SQLException;

import static com.company.Deployment.crawItemInformations;

public class DeploymentTest extends TestCase {

    public void testDeployment() throws IOException, InterruptedException, SQLException {
        crawItemInformations();
    }

}