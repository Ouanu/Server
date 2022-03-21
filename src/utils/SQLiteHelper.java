package utils;

import data.ResData;
import java.sql.*;
import java.util.HashMap;

public class SQLiteHelper {
    private Connection conn = null;
    private ResultSet rs = null;
    private Statement statement = null;
    private static volatile SQLiteHelper INSTANCE = null;
    private static String databaseUrl = null;
    private HashMap<Long, ResData> idList = new HashMap<>();
    private boolean isOpen = false;

    public static SQLiteHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (SQLiteHelper.class) {
                if (INSTANCE == null) {
                    return new SQLiteHelper(databaseUrl);
                }
            }
        }
        return INSTANCE;
    }

    public SQLiteHelper(String url) {
        databaseUrl = url;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + url);
            statement = conn.createStatement();
            rs = statement.executeQuery("SELECT * FROM resdata");
            initResData();
            isOpen = true;
        } catch (Exception e) {
            System.out.println("Fail to open database __" + e);
            isOpen = false;
        }
    }

    public void initResData() throws SQLException {
        while (rs.next()) {
            idList.put(rs.getLong("uid"), new ResData(
                    rs.getLong("uid"),
                    rs.getString("title"),
                    rs.getString("desc"),
                    rs.getString("uri"),
                    rs.getLong("updateDate"),
                    rs.getString("dirPath")
            ));

        }
    }

    public boolean getDatabaseStatement() {
        return isOpen;
    }

    public String getDesc(Long id) {
        return idList.get(id).getDesc();
    }

    public long getUpdateDate(Long id) {
        return idList.get(id).getUpdateDate();
    }

    public boolean isExist(Long uid) {
        if (idList.getOrDefault(uid, null) == null) {
            return false;
        }
        return true;
    }

    public HashMap<Long, ResData> getIdList() {
        return idList;
    }

    public boolean updateSQL(Long uid, Long updateDate, String desc) {
        String query = "UPDATE resdata SET desc=?, SET updateDate=? WHERE uid=?";
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(query);
            statement.setString(1, desc);
            statement.setLong(2, updateDate);
            statement.setLong(3, uid);
            statement.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }

    }

    public void shutdownSQL() {
        try {
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
