package wuliu_j.common;

public class Stmt {
    public static final String CREATE_TABLES = """
        CREATE TABLE IF NOT EXISTS simplemeta
        (
          id         TEXT   PRIMARY KEY COLLATE NOCASE,
          filename   TEXT   NOT NULL UNIQUE,
          checksum   TEXT   NOT NULL UNIQUE,
          size       INT    NOT NULL,
          type       TEXT   NOT NULL,
          like       INT    NOT NULL,
          label      TEXT   NOT NULL,
          notes      TEXT   NOT NULL,
          ctime      TEXT   NOT NULL,
          utime      TEXT   NOT NULL
        );
        
        CREATE INDEX IF NOT EXISTS idx_simplemeta_size     ON simplemeta(size);
        CREATE INDEX IF NOT EXISTS idx_simplemeta_label    ON simplemeta(label);
        CREATE INDEX IF NOT EXISTS idx_simplemeta_notes    ON simplemeta(notes);
        CREATE INDEX IF NOT EXISTS idx_simplemeta_ctime    ON simplemeta(ctime);
        CREATE INDEX IF NOT EXISTS idx_simplemeta_utime    ON simplemeta(utime);
        
        CREATE TABLE IF NOT EXISTS file_checked
        (
          id         TEXT   PRIMARY KEY COLLATE NOCASE,
          checked    TEXT   NOT NULL,
          damaged    INT    NOT NULL
        );

        CREATE INDEX IF NOT EXISTS idx_file_checked_checked ON file_checked(checked);
        """;

    public static final String COUNT_SIMPLEMETA = """
        SELECT count(id) FROM simplemeta;
        """;

    public static final String GET_ALL_METAS = """
        SELECT * FROM simplemeta;
        """;

    public static final String INSERT_SIMPLEMETA = """
        INSERT INTO simplemeta
               ( id,  filename,  checksum,  size,  type,  like,  label,  notes,  ctime,  utime)
        VALUES (:id, :filename, :checksum, :size, :type, :like, :label, :notes, :ctime, :utime);
        """;

    public static final String UPDATE_SIMPLEMETA = """
        UPDATE simplemeta SET
            checksum=:checksum, size=:size, type=:type, like=:like,
            label=:label, notes=:notes, ctime=:ctime, utime=:utime
        WHERE id=:id;
        """;

    public static final String DELETE_SIMPLEMETA = """
        DELETE FROM simplemeta WHERE id=:id;
        """;

    public static final String GET_RECENT_LABELS = """
        SELECT DISTINCT label FROM simplemeta
        ORDER BY utime DESC LIMIT :limit;
        """;

    public static final String GET_RECENT_NOTES = """
        SELECT DISTINCT notes FROM simplemeta
        ORDER BY utime DESC LIMIT :limit;
        """;

    public static final String GET_META_BY_CHECKSUM = """
        SELECT * FROM simplemeta WHERE checksum=:checksum;
        """;

    public static final String GET_IDS_NEED_CHECK = """
        SELECT id FROM file_checked WHERE checked<:checked;
        """;

    public static final String GET_DAMAGED_IDS = """
        SELECT id FROM file_checked WHERE damaged>0;
        """;

    public static final String COUNT_DAMAGED = """
        SELECT count(id) FROM file_checked WHERE damaged>0;
        """;

    public static final String GET_DAMAGED_BY_ID = """
        SELECT damaged FROM file_checked WHERE id=:id;
        """;

    public static final String GET_META_BY_ID = """
        SELECT * FROM simplemeta WHERE id=:id;
        """;

    public static final String SEARCH_FILENAME_LIMIT = """
        SELECT * FROM simplemeta WHERE filename LIKE :filename
        ORDER BY utime DESC LIMIT :limit;
        """;

    public static final String SEARCH_LABEL_LIMIT = """
        SELECT * FROM simplemeta WHERE label LIKE :label
        ORDER BY utime DESC LIMIT :limit;
        """;

    public static final String SEARCH_NOTES_LIMIT = """
        SELECT * FROM simplemeta WHERE notes LIKE :notes
        ORDER BY utime DESC LIMIT :limit;
        """;

    public static final String SEARCH_FILENAME_LABEL_NOTES_LIMIT = """
        SELECT * FROM simplemeta
        WHERE filename LIKE :filename OR label LIKE :label OR notes LIKE :notes
        ORDER BY utime DESC LIMIT :limit;
        """;

    public static final String GET_LIKE_LIMIT = """
        SELECT * FROM simplemeta WHERE like > 0
        ORDER BY like DESC LIMIT :limit;
        """;

    public static final String GET_ORDER_BY_SIZE = """
        SELECT * FROM simplemeta ORDER BY size DESC LIMIT :limit;
        """;

    public static final String GET_RECENT_META_LIMIT = """
        SELECT * FROM simplemeta ORDER BY utime DESC
        LIMIT :limit;
        """;

    public static final String UPDATE_META_PART = """
        UPDATE simplemeta SET
            like=:like, label=:label, notes=:notes, ctime=:ctime, utime=:utime
        WHERE id=:id;
        """;

    public static final String SUM_META_SIZE = """
        SELECT sum(size) FROM simplemeta;
        """;

    public static final String UPDATE_OVERWRITE_FILE = """
        UPDATE simplemeta SET
            checksum=:checksum, size=:size, utime=:utime
        WHERE id=:id;
        """;

    public static final String INSERT_FILE_CHECKED = """
        INSERT INTO file_checked (id,  checked,  damaged)
                         VALUES (:id, :checked, :damaged);
        """;

    public static final String UPDATE_CHECKED_DAMAGED = """
        UPDATE file_checked SET checked=:checked, damaged=:damaged
        WHERE id=:id;
        """;

    public static final String DELETE_FILE_CHECKED = """
        DELETE FROM file_checked WHERE id=:id;
        """;

    public static final String DELETE_ALL_FILE_CHECKED = """
        DELETE FROM file_checked;
        """;
}
