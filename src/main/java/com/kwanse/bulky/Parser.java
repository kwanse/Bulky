package com.kwanse.bulky_dummy;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Parser {

    private Parser() {
    }

    public static String parseTable(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null) {
            return table.name();
        }
        String entityName = clazz.getSimpleName();
        return entityName.toLowerCase();
    }


    public static List<String> parseColumns(List<Field> fields) {
        return fields.stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .map(field -> {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null) {
                        return column.name();
                    }
                    return camelToSnake(field.getName());
                })
                .collect(Collectors.toList());
    }

    public static List<String> parseMappedFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> field.getAnnotation(JoinColumn.class).name())
                .toList();
    }

    private static String camelToSnake(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        StringBuilder result = new StringBuilder();
        char[] chars = name.toCharArray();

        result.append(Character.toLowerCase(chars[0]));

        for (int i = 1; i < chars.length; i++) {
            char current = chars[i];
            char previous = chars[i - 1];

            if (Character.isLowerCase(previous) && Character.isUpperCase(current)) {
                result.append('_');
            }
            result.append(Character.toLowerCase(current));
        }
        return result.toString();
    }
}