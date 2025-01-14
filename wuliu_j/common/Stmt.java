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
        CREATE INDEX IF NOT EXISTS idx_simplemeta_ctime    ON simplemeta(ctime);
        CREATE INDEX IF NOT EXISTS idx_simplemeta_utime    ON simplemeta(utime);
        """;

    public static final String INSERT_SIMPLEMETA = """
        INSERT INTO simplemeta
               ( id,  filename,  checksum,  size,  type,  like,  label,  notes,  ctime,  utime)
        VALUES (:id, :filename, :checksum, :size, :type, :like, :label, :notes, :ctime, :utime);
        """;

    public static final String UPDATE_SIMPLEMETA = """
        UPDATE simplemeta SET
            filename=:filename, checksum=:checksum, size=:size, type=:type,
            like=:like, label=:label, notes=:notes, ctime=:ctime, utime=:utime
        WHERE id=:id;
        """;

    public static final String DELETE_SIMPLEMETA = """
        DELETE FROM simplemeta WHERE id=:id;
        """;

    public static final String GET_RECENT_LABELS = """
        SELECT DISTINCT label FROM simplemeta
        ORDER BY utime DESC LIMIT :limit;
        """;

    public static final String GET_META_BY_CHECKSUM = """
        SELECT * FROM simplemeta WHERE checksum=:checksum;
        """;
}
