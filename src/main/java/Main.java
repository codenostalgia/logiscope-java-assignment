import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException {

//        SQLParsingUtility.parse("SELECT * FROM users where uid =5");
//        SQLParsingUtility.parse("SELECT * FROM users where age between 2 AND 3");
//        SQLParsingUtility.parse("select uid, name from users where name like '%H%'");
//        SQLParsingUtility.parse("SELECT * FROM users where (gender='M' and age=23) AND (name IN ('Harry','John','Lily') OR uid between 2 AND 3)");
//        SQLParsingUtility.parse("select client_ip, status from apache_log where url like '%/abc/def%'");
//        SQLParsingUtility.parse("select count(*) from apache_log where status=404 and msg_ts between 100 and 200");

        SQLParsingUtility.translate("SELECT * FROM users where uid <=5");
//        SQLParsingUtility.translate("SELECT * FROM users where age between 2 AND 3");
//        SQLParsingUtility.translate("SELECT * FROM users where name LIKE '%a%'");
//        SQLParsingUtility.translate("SELECT * FROM users where uid in (1,2,3,4,5)");
//        SQLParsingUtility.translate("SELECT * FROM users where uid =2 AND name='harry'");
//        SQLParsingUtility.translate("SELECT * FROM users where (gender='M' and age=23) AND (name IN ('Harry','John','Lily') OR uid between 2 AND 3)");
//        SQLParsingUtility.translate("select client_ip, status from apache_log where url like '%/abc/def%'");
//        SQLParsingUtility.translate("select count(*) from apache_log where status=404 and msg_ts between 100 and 200");

//        DatabaseConnector.executeQuery("SELECT * FROM users where uid =2");
    }
}
