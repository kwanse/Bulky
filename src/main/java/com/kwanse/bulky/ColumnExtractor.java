package com.kwanse.bulky_dummy;

import jakarta.persistence.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ColumnExtractor {

    private static final Set<Class<? extends Annotation>> EXCLUDED_ANNOTATIONS = Set.of(
            ElementCollection.class,
            Embedded.class,
            OneToMany.class,
            ManyToOne.class,
            OneToOne.class,
            ManyToMany.class,
            GeneratedValue.class,
            EmbeddedId.class
    );


    private ColumnExtractor() {
    }

    public static List<Field> extract(Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();

        List<Field> result = extractEmbeddedIds(declaredFields);
        result.addAll(extractBasicFields(clazz));
        result.addAll(extractMappedFields(declaredFields));

        return result;
    }


    public static List<Field> extractBasicFields(Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();

        List<Field> result = extractPrimitiveFields(declaredFields);
        for (Field field : extractEmbeddedFields(declaredFields)) {
            result.addAll(extractBasicFields(field.getType()));
        }
        return result;
    }


    private static List<Field> extractPrimitiveFields(Field[] declaredFields) {
        return Arrays.stream(declaredFields)
                .filter(ColumnExtractor::isPrimitiveFields)
                .collect(Collectors.toList());
    }

    private static List<Field> extractEmbeddedFields(Field[] declaredFields) {
        return Arrays.stream(declaredFields)
                .filter(field -> field.isAnnotationPresent(Embedded.class))
                .toList();
    }

    private static List<Field> extractEmbeddedIds(Field[] declaredFields) {
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(EmbeddedId.class)) {
                return Arrays.stream(field.getType().getDeclaredFields())
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }


    private static List<Field> extractMappedFields(Field[] declaredFields) {
        List<Field> result = new ArrayList<>();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(JoinColumn.class)) {
                for (Field subField : field.getType().getDeclaredFields()) {
                    if (subField.isAnnotationPresent(Id.class)) {
                        result.add(subField);
                    }
                }
            }
        }
        return result;
    }


    private static boolean isPrimitiveFields(Field field) {
        return EXCLUDED_ANNOTATIONS.stream()
                .noneMatch(field::isAnnotationPresent);
    }

}