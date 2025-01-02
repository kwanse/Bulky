package com.kwanse.bulky_dummy;

import java.util.List;
import java.util.stream.Collectors;

public final class QueryGenerator {

    private static final String INSERT = "INSERT INTO %s (%s) VALUES (%s)";

    private QueryGenerator() {
    }

    public static String generate(String tableName, List<String> columnNames) {
        String name = String.join(", ", columnNames);

        String placeholders = columnNames.stream()
                .map(col -> "?")
                .collect(Collectors.joining(", "));

        return String.format(INSERT, tableName, name, placeholders);
    }

}