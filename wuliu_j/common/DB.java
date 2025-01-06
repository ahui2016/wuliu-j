package wuliu_j.common;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

public class DB {
    private final String path;
    private final Jdbi jdbi;

    public String getPath() {
        return this.path;
    }

    public DB(String dbPath) {
        this.path = dbPath;
        this.jdbi = Jdbi.create("jdbc:sqlite:" + dbPath);
        this.jdbi.registerRowMapper(ConstructorMapper.factory(Simplemeta.class));
    }

    public void createTables() {
        this.jdbi.useHandle(
    handle -> handle.createScript(Stmt.CREATE_TABLES).execute());
    }

    public void insertSimplemeta(Simplemeta meta) {
        jdbi.withHandle(handle ->
            handle.createUpdate(Stmt.INSERT_SIMPLEMETA)
                .bindMap(meta.toMap()).execute());
    }

    public void updateSimplemeta(Simplemeta meta) {
        jdbi.withHandle(handle ->
                handle.createUpdate(Stmt.UPDATE_SIMPLEMETA)
                        .bindMap(meta.toMap()).execute());
    }

    public void deleteSimplemeta(String id) {
        jdbi.withHandle(handle ->
                handle.createUpdate(Stmt.DELETE_SIMPLEMETA)
                        .bind("id", id).execute());
    }
}
