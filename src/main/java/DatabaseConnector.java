import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

import java.io.StringReader;
import java.sql.*;

public class DatabaseConnector {

    static CCJSqlParserManager parserManager;
    static Connection connection;

    static {
        String url = "jdbc:mysql://localhost:port/databasename";
        String username = "";
        String password = "";

        parserManager = new CCJSqlParserManager();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void executeQuery(String query) throws SQLException {

        try {
            Statement statement = parserManager.parse(new StringReader(query));
            Select select = (Select) statement;

            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect) selectBody;

            java.sql.Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ResultSetMetaData rsMetaData = rs.getMetaData();

            int columnCount = rsMetaData.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsMetaData.getColumnLabel(i);
                    Object columnValue = rs.getObject(i);
                    System.out.println(columnName + ": " + columnValue);
                }
                System.out.println("--------");
            }
        } catch (JSQLParserException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}
