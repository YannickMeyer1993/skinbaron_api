package com.company.common;

import junit.framework.TestCase;

import java.io.IOException;
import java.sql.SQLException;

import static com.company.common.PostgresHelper.executeDDL;
import static com.company.common.PostgresHelper.executeDDLfromPath;

public class PostgresHelperTest extends TestCase {

    public void testExecuteSQLfromPath() throws Exception {
        executeDDLfromPath("src/test/resources/PostgresHelperTest/testExecuteSQLfromPath.sql");
    }

    public void testExecuteSQLfromPathNegative() throws Exception {
        try {
            executeDDLfromPath("src/test/resources/PostgresHelperTest/testExecuteSQLfromPath.negative");
        }
        catch (IllegalStateException e){
            if (!"Only .sql Files are allowed.".equals(e.getMessage())) {
                throw new Exception("test not successfull.");
            }
        }
    }

    public void testExecute2Statements() throws Exception {
        executeDDLfromPath("src/test/resources/PostgresHelperTest/testExecute2Statements.sql");
    }

    public void testExecuteEmptyStatements() throws Exception {
        executeDDLfromPath("src/test/resources/PostgresHelperTest/emptyStatement.sql");
    }

    public void testExecuteDDL() throws SQLException {
        executeDDL("select CURRENT_TIMESTAMP;");
    }

    public void testExecuteDDLwithMultipleLines() throws SQLException, IOException {
        executeDDLfromPath("src/test/resources/PostgresHelperTest/multipleLines.sql");
    }
}