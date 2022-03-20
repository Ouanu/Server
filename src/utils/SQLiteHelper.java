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
        System.out.println(url);
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
        System.out.println(idList.get(15L).getDesc());
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


}
