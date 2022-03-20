package data;

public class ResData {
    private long uid;
    private String title;
    private String desc;
    private String uri;
    private long updateDate;
    private String dirPath;

    public ResData(long uid, String title, String desc, String uri, long updateDate, String dirPath) {
        this.uid = uid;
        this.title = title;
        this.desc = desc;
        this.uri = uri;
        this.updateDate = updateDate;
        this.dirPath = dirPath;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }
}
