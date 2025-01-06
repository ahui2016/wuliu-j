package wuliu_j.common;

import java.util.LinkedHashMap;

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
