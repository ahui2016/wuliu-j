package wuliu_j.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Metadata extends Simplemeta {
    List<String> keywords;
    List<String> collections;
    List<String> albums;

    public static Metadata fromJsonFile(Path jsonPath) throws IOException {
        Map<String,Object> data = MyUtil.readJsonFileToMap(jsonPath);
        return of(data);
    }

    public static Metadata of(Map<String,Object> data) {
        Metadata meta = new Metadata();
        meta.id = (String) data.get("id");
        meta.filename = (String) data.get("filename");
        meta.checksum = (String) data.get("checksum");
        meta.size = MyUtil.getLongFromMap(data, "size");
        meta.type = (String) data.get("type");
        meta.like = MyUtil.getIntFromMap(data, "like");
        meta.label = (String) data.get("label");
        meta.notes = (String) data.get("notes");
        meta.keywords = MyUtil.getStrListFromMap(data, "keywords");
        meta.collections = MyUtil.getStrListFromMap(data, "collections");
        meta.albums = MyUtil.getStrListFromMap(data, "albums");
        meta.ctime = (String) data.get("ctime");
        meta.utime = (String) data.get("utime");
        return meta;
    }

    public Simplemeta toSimple() {
        SortedSet<String> words = new TreeSet<>();
        words.addAll(this.keywords);
        words.addAll(this.collections);
        words.addAll(this.albums);
        List<String> wordList = new ArrayList<>(words);
        String notes = String.join(", ", wordList);
        String label = (this.label + " " + this.notes).strip();

        Simplemeta simple = new Simplemeta();
        simple.id = this.id;
        simple.filename = this.filename;
        simple.checksum = this.checksum;
        simple.size = this.size;
        simple.type = this.type;
        simple.like = this.like;
        simple.label = label;
        simple.notes = notes;
        simple.ctime = this.ctime;
        simple.utime = this.utime;

        return simple;
    }
}
