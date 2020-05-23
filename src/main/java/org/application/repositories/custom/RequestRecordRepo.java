package org.application.repositories.custom;

import org.application.models.custom.RequestRecord;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class RequestRecordRepo {

    private String jdbcUrl = "jdbc:h2:mem:db";
    private String username = "sa";
    private String password = "";

    private String createRecordTable =
            "CREATE TABLE records (" +
                    "id bigint generated by default as identity," +
                    "type varchar(255)," +
                    "out varchar(255)," +
                    "to varchar(255)," +
                    "date date," +
                    "primary key (id)" +
                    ");";

    private String deleteRecordTable = "DROP TABLE records;";
    private String insertStringInit = "INSERT INTO records (fields) VALUES (vals)";

    private Connection connection;

    @PostConstruct
    public void init() throws SQLException {
        connection = DriverManager.getConnection(jdbcUrl, username, password);
        Statement statement = connection.createStatement();

        statement.execute(createRecordTable);
    }

    public void save(RequestRecord record) throws SQLException {
        String insertString = insertStringInit;
        Statement statement = connection.createStatement();
        insertString = insertRecordIntoInsertStatement(record, insertString);
        statement.execute(insertString);
    }

    private String insertRecordIntoInsertStatement(RequestRecord record, String insertString) {

        List<Field> fields = Arrays.asList(record.getClass().getDeclaredFields());

        StringBuilder fieldString = new StringBuilder("(");
        for (Field field : fields) {
            String name = field.getName();
            if (!field.getName().equals("id")) {
                fieldString.append(name).append(",");
            }
        }
        fieldString.append(")");
        fieldString.deleteCharAt(fieldString.length() - 2);


        StringBuilder valString = new StringBuilder("(");
        for (Field field : fields) {
            String val = null;
            try {
                field.setAccessible(true);
                val = String.valueOf(field.get(record));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (!field.getName().equals("id")) {
                valString.append("'").append(val).append("'").append(",");
            }
        }
        valString.append(")");
        valString.deleteCharAt(valString.length() - 2);

        insertString = insertString.replace("(fields)", fieldString.toString());
        insertString = insertString.replace("(vals)", valString.toString());


        return insertString;
    }

    public RequestRecord get(long id) throws SQLException {
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM records WHERE id = " + id);

        resultSet.next();

        long idOut = resultSet.getLong(1);
        String type = resultSet.getString(2);
        String out = resultSet.getString(3);
        String to = resultSet.getString(4);
        Date date = resultSet.getDate(5);
        LocalDate localDate = date.toLocalDate();

        return new RequestRecord(idOut, type, out, to, localDate);
    }

    public List<RequestRecord> getAll() throws SQLException {
        Statement statement = connection.createStatement();

        List<RequestRecord> requestRecords = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM records;");

        while (resultSet.next()) {
            long idOut = resultSet.getLong(1);
            String type = resultSet.getString(2);
            String out = resultSet.getString(3);
            String to = resultSet.getString(4);
            Date date = resultSet.getDate(5);
            LocalDate localDate = date.toLocalDate();

            requestRecords.add(new RequestRecord(idOut, type, out, to, localDate));
        }

        return requestRecords;
    }

    @PreDestroy
    public void destroy() throws SQLException {
        Statement statement = connection.createStatement();

        statement.execute(deleteRecordTable);
    }

}
