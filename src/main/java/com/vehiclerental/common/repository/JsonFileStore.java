package com.vehiclerental.common.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * FILE implementation of {@link DataStore} (10 marks of the project grade).
 * ------------------------------------------------------------------------
 * All records of one entity are stored as a pretty-printed JSON array inside
 * a text file, e.g. data/users.json, data/vehicles.json ...
 *
 * Why a single class?
 *  - ABSTRACTION: services/repositories never touch java.io / java.nio.
 *  - No duplicated file code in six repositories.
 *  - Easy to explain in the viva: "all file persistence goes through
 *    JsonFileStore; all SQL persistence goes through MySqlStore; both
 *    implement DataStore".
 */
public class JsonFileStore<T> implements DataStore<T> {

    private static final Logger log = LoggerFactory.getLogger(JsonFileStore.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())                       // LocalDate support
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)  // ISO dates in the file
            .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT);              // human readable file

    private final Path file;
    private final Class<T> elementType;

    public JsonFileStore(Path file, Class<T> elementType) {
        this.file = file;
        this.elementType = elementType;
    }

    public Path getFile() {
        return file;
    }

    /** READ operation - deserialise the whole file into a list of entities. */
    @Override
    public synchronized List<T> readAll() {
        if (!Files.exists(file)) {
            log.info("Data file {} does not exist yet - starting with an empty list", file);
            return new ArrayList<>();
        }
        try {
            List<T> loaded = MAPPER.readValue(
                    Files.readAllBytes(file),
                    MAPPER.getTypeFactory().constructCollectionType(List.class, elementType));
            log.info("Loaded {} record(s) from {}", loaded.size(), file.getFileName());
            return new ArrayList<>(loaded);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }

    /** WRITE operation - overwrite the file with the current list of entities. */
    @Override
    public synchronized void writeAll(List<T> items) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            // IMPORTANT: write with the DECLARED collection type, otherwise
            // Jackson forgets the polymorphic type ids ("type"/"vehicleType")
            // of subclasses such as AdminUser or Car.
            JavaType listType = MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, elementType);
            MAPPER.writerFor(listType)
                    .withDefaultPrettyPrinter()
                    .writeValue(file.toFile(), items);
            log.debug("Wrote {} record(s) to {}", items.size(), file.getFileName());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
    }

    @Override
    public synchronized boolean isEmpty() {
        return !Files.exists(file) || readAll().isEmpty();
    }

    @Override
    public String describe() {
        return "file:" + file.getFileName();
    }
}
