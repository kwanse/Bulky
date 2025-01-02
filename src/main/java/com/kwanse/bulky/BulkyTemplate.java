package com.kwanse.bulky_dummy;

import jakarta.persistence.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public final class BulkyTemplate {

    public static final Set<Class<? extends Annotation>> INVALID_ANNOTATIONS = Set.of(
            ElementCollection.class,
            Embedded.class,
            OneToMany.class,
            ManyToOne.class,
            OneToOne.class,
            ManyToMany.class,
            GeneratedValue.class,
            EmbeddedId.class
    );

    private final JdbcTemplate jdbcTemplate;

    public BulkyTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public <T> void batchInsert(List<T> entities) throws IllegalAccessException {
        if (entities.isEmpty()) {
            return;
        }
        Class<?> clazz = entities.get(0).getClass();

        List<Field> columns = ColumnExtractor.extract(clazz);
        List<String> columnNames = Parser.parseColumns(columns);

        columnNames.addAll(Parser.parseMappedFields(clazz));
        String tableName = Parser.parseTable(clazz);
        String query = QueryGenerator.generate(tableName, columnNames);

        List<Object[]> batchParams = new ArrayList<>();
        for (T entity : entities) {
            List<Object> values = new ArrayList<>();
            buildValues(entity, values);
            batchParams.add(values.toArray());
        }

        jdbcTemplate.batchUpdate(query, batchParams);
    }

    private static void buildValues(Object obj, List<Object> values) throws IllegalAccessException {
        if (obj == null) {
            return;
        }
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Embedded.class)) {
                buildValues(field.get(obj), values);
            }
            if (field.isAnnotationPresent(EmbeddedId.class)) {
                processEmbeddedIdColumns(obj, values, field);
            }
            if (field.isAnnotationPresent(JoinColumn.class)) {
                processJoinColumns(obj, values, field);
            }
            if (isBasicFields(field)) {
                processBasicColumns(obj, values, field);
            }
        }
    }

    private static void processBasicColumns(Object obj, List<Object> values, Field field) throws IllegalAccessException {
        if (field.isAnnotationPresent(Enumerated.class)) {
            values.add(field.get(obj).toString());
        }
        else {
            values.add(field.get(obj));
        }
    }

    private static void processEmbeddedIdColumns(Object obj, List<Object> values, Field field) throws IllegalAccessException {
        Field[] declaredFields = field.getType().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            values.add(declaredField.get(field.get(obj)));
        }
    }

    private static void processJoinColumns(Object obj, List<Object> values, Field field) throws IllegalAccessException {
        Field[] declaredFields = field.getType().getDeclaredFields();
        Field idField = Arrays.stream(declaredFields)
                .filter(declaredField -> declaredField.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow();
        idField.setAccessible(true);
        values.add(idField.get(field.get(obj)));
    }

    private static boolean isBasicFields(Field field) {
        return INVALID_ANNOTATIONS.stream()
                .noneMatch(field::isAnnotationPresent);
    }
}