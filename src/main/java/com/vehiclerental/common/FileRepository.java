package com.vehiclerental.common;

import com.vehiclerental.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * FILE HANDLING layer. Persists entities in a plain text file, one record per line,
 * fields separated by '|' (the separator, backslash and line breaks are escaped).
 *
 * Every create / update / delete rewrites the file (to a temp file first, then moved
 * into place) so the file is never left half-written. Reads come from an in-memory
 * copy that is loaded from the file the first time it is needed.
 *
 * Subclasses only describe how ONE entity maps to / from its fields (ABSTRACTION,
 * INHERITANCE and POLYMORPHISM via the abstract template methods below).
 */
public abstract class FileRepository<T extends BaseEntity> implements CrudRepository<T> {

    private static final System.Logger LOG = System.getLogger(FileRepository.class.getName());

    private final Path file;
    /** id -> encoded row [id, createdAt, ...entity fields] (kept as text so callers get independent copies). */
    private final Map<Long, String[]> rows = new LinkedHashMap<>();
    private long nextId = 1;
    private boolean loaded;

    protected FileRepository(Path file) {
        this.file = file;
    }

    /** Entity specific fields, in a fixed order. Do not include id / createdAt. */
    protected abstract String[] toFields(T entity);

    /** Rebuild an entity from the fields written by {@link #toFields}. */
    protected abstract T fromFields(String[] fields);

    /** Comment line written at the top of the file describing the columns. */
    protected abstract String headerComment();

    // ------------------------------------------------------------------ CRUD

    @Override
    public synchronized T create(T entity) {
        ensureLoaded();
        entity.setId(nextId++);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        rows.put(entity.getId(), encode(entity));
        flush();
        return entity;
    }

    @Override
    public synchronized Optional<T> findById(Long id) {
        ensureLoaded();
        String[] row = id == null ? null : rows.get(id);
        return row == null ? Optional.empty() : Optional.of(decode(row));
    }

    @Override
    public synchronized List<T> findAll() {
        ensureLoaded();
        List<T> result = new ArrayList<>(rows.size());
        for (String[] row : rows.values()) {
            result.add(decode(row));
        }
        return result;
    }

    @Override
    public synchronized T update(T entity) {
        ensureLoaded();
        if (entity.getId() == null || !rows.containsKey(entity.getId())) {
            throw ResourceNotFoundException.of(getClass().getSimpleName() + " record", entity.getId());
        }
        rows.put(entity.getId(), encode(entity));
        flush();
        return entity;
    }

    @Override
    public synchronized boolean deleteById(Long id) {
        ensureLoaded();
        if (id == null || rows.remove(id) == null) {
            return false;
        }
        flush();
        return true;
    }

    @Override
    public synchronized long count() {
        ensureLoaded();
        return rows.size();
    }

    /** Helper for subclasses: all entities matching a condition. */
    protected List<T> findWhere(Predicate<T> condition) {
        List<T> result = new ArrayList<>();
        for (T entity : findAll()) {
            if (condition.test(entity)) {
                result.add(entity);
            }
        }
        return result;
    }

    protected Optional<T> findFirst(Predicate<T> condition) {
        for (T entity : findAll()) {
            if (condition.test(entity)) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------ file access

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            if (!Files.exists(file)) {
                flush();
                return;
            }
            long maxId = 0;
            int lineNo = 0;
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                lineNo++;
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                try {
                    String[] row = splitLine(line);
                    long id = Long.parseLong(row[0]);
                    decode(row); // validates the row can be turned into an entity
                    rows.put(id, row);
                    maxId = Math.max(maxId, id);
                } catch (RuntimeException ex) {
                    LOG.log(System.Logger.Level.WARNING,
                            "Skipping corrupt line " + lineNo + " in " + file + ": " + ex.getMessage());
                }
            }
            nextId = maxId + 1;
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read " + file, ex);
        }
    }

    private void flush() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            Path temp = Files.createTempFile(parent, file.getFileName().toString(), ".tmp");
            List<String> lines = new ArrayList<>();
            lines.add("# " + headerComment());
            for (String[] row : rows.values()) {
                lines.add(joinLine(row));
            }
            Files.write(temp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not write " + file, ex);
        }
    }

    // ------------------------------------------------------------ encoding

    private String[] encode(T entity) {
        String[] own = toFields(entity);
        String[] row = new String[own.length + 2];
        row[0] = String.valueOf(entity.getId());
        row[1] = entity.getCreatedAt() == null ? "" : entity.getCreatedAt().toString();
        System.arraycopy(own, 0, row, 2, own.length);
        return row;
    }

    private T decode(String[] row) {
        T entity = fromFields(Arrays.copyOfRange(row, 2, row.length));
        entity.setId(Long.parseLong(row[0]));
        entity.setCreatedAt(row[1].isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(row[1]));
        return entity;
    }

    static String joinLine(String[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(escape(row[i]));
        }
        return sb.toString();
    }

    static String[] splitLine(String line) {
        String[] parts = line.split("\\|", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = unescape(parts[i]);
        }
        return parts;
    }

    static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '|' -> sb.append("\\p");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    static String unescape(String value) {
        if (value.indexOf('\\') < 0) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 'p' -> sb.append('|');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------- field parsing helpers

    protected static String str(String[] f, int i) {
        return i < f.length ? f[i] : "";
    }

    protected static String strOrNull(String[] f, int i) {
        String s = str(f, i);
        return s.isEmpty() ? null : s;
    }

    protected static Long lng(String[] f, int i) {
        String s = str(f, i);
        return s.isEmpty() ? null : Long.valueOf(s);
    }

    protected static Integer integer(String[] f, int i) {
        String s = str(f, i);
        return s.isEmpty() ? null : Integer.valueOf(s);
    }

    protected static BigDecimal decimal(String[] f, int i) {
        String s = str(f, i);
        return s.isEmpty() ? BigDecimal.ZERO : new BigDecimal(s);
    }

    protected static LocalDate date(String[] f, int i) {
        String s = str(f, i);
        return s.isEmpty() ? null : LocalDate.parse(s);
    }

    protected static LocalDateTime dateTime(String[] f, int i) {
        String s = str(f, i);
        return s.isEmpty() ? null : LocalDateTime.parse(s);
    }

    protected static boolean bool(String[] f, int i) {
        return Boolean.parseBoolean(str(f, i));
    }

    protected static <E extends Enum<E>> E enumOf(Class<E> type, String[] f, int i, E fallback) {
        String s = str(f, i);
        return s.isEmpty() ? fallback : Enum.valueOf(type, s);
    }

    protected static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
