package wuliu_j.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class Simplemeta {
    String id;
    String filename;
    String checksum;
    Long size;
    String type;
    Integer like;
    String label;
    String notes;
    String ctime;
    String utime;

    public void readFromJsonFile(Path jsonPath) throws IOException {
        Map<String,Object> data = MyUtil.readJsonFileToMap(jsonPath);
        this.readFromMap(data);
    }

    public void readFromMap(Map<String,Object> data) {
        this.id = (String) data.get("id");
        this.filename = (String) data.get("filename");
        this.checksum = (String) data.get("checksum");
        this.size = MyUtil.getLongFromMap(data, "size");
        this.type = (String) data.get("type");
        this.like = MyUtil.getIntFromMap(data, "like");
        this.label = (String) data.get("label");
        this.notes = (String) data.get("notes");
        this.ctime = (String) data.get("ctime");
        this.utime = (String) data.get("utime");
    }

    public LinkedHashMap<String,Object> toMap() {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.putLast("id", this.id);
        map.putLast("filename", this.filename);
        map.putLast("checksum", this.checksum);
        map.putLast("size", this.size);
        map.putLast("type", this.type);
        map.putLast("like", this.like);
        map.putLast("label", this.label);
        map.putLast("notes", this.notes);
        map.putLast("ctime", this.ctime);
        map.putLast("utime", this.utime);
        return map;
    }
}
