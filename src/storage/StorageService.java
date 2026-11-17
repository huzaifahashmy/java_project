package storage;

import java.lang.reflect.Type;
import java.util.List;

/**
 * ==============================================================================
 * TEAM MEMBER 5: JSON STORAGE & GENERICS LEAD
 * File: StorageService.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Generics: Defines a generic type parameter <T> allowing this interface to
 *    work seamlessly with any model entity (Product, Supplier, Transaction, etc.).
 * 2. Interface Abstraction: Decouples high-level storage operations from the
 *    underlying storage mechanism (Gson JSON, SQL, or binary serialization).
 * ==============================================================================
 *
 * @param <T> The type of objects contained in the list being saved or loaded
 */
public interface StorageService<T> {

    /**
     * Reads and deserializes a list of entities from a storage file.
     *
     * @param filePath The path to the file
     * @param type     The generic Type token representing List<T>
     * @return List of deserialized entities of type T
     */
    List<T> loadFromFile(String filePath, Type type);

    /**
     * Serializes and writes a list of entities to a storage file.
     *
     * @param filePath The destination file path
     * @param data     The list of entities to write
     */
    void saveToFile(String filePath, List<T> data);
}

