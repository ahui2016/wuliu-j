package wuliu_j.model;

import wuliu_j.util.MyUtil;

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
        Number size = (Number) data.get("size");
        Number like = (Number) data.get("like");

        meta.id = (String) data.get("id");
        meta.filename = (String) data.get("filename");
        meta.checksum = (String) data.get("checksum");
        meta.size = size.longValue();
        meta.type = (String) data.get("type");
        meta.like = like.intValue();
        meta.label = (String) data.get("label");
        meta.notes = (String) data.get("notes");
        meta.keywords = objToList(data, "keywords");
        meta.collections = objToList(data, "collections");
        meta.albums = objToList(data, "albums");
        meta.ctime = (String) data.get("ctime");
        meta.utime = (String) data.get("utime");

        return meta;
    }

    static List<String> objToList(Map<String,Object> data, String key) {
        Object obj = data.get(key);
        if (obj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            var list = (List<String>) obj;
            return list;
        }
        throw new RuntimeException(String.format("%s is not a string list", key));
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
