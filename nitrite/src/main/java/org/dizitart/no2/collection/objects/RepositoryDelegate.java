/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.objects;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.exceptions.*;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.annotations.Indices;
import org.dizitart.no2.index.annotations.InheritIndices;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.dizitart.no2.common.Constants.FIELD_SEPARATOR;
import static org.dizitart.no2.common.util.DocumentUtils.skeletonDocument;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.filters.Filter.eq;

/**
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
class RepositoryDelegate {

    <T> Document toDocument(T object, NitriteMapper nitriteMapper,
                            Field idField, boolean update) {
        Document document = nitriteMapper.asDocument(object);
        if (idField != null) {
            if (idField.getType() == NitriteId.class) {
                try {
                    idField.setAccessible(true);
                    if (idField.get(object) == null) {
                        NitriteId id = document.getId();
                        idField.set(object, id);
                        document.put(idField.getName(), id.getIdValue());
                    } else if (!update) {
                        throw new InvalidIdException(AUTO_ID_ALREADY_SET);
                    }
                } catch (IllegalAccessException iae) {
                    throw new InvalidIdException(CANNOT_ACCESS_AUTO_ID);
                }
            }
            Object idValue = document.get(idField.getName());
            if (idValue == null) {
                throw new InvalidIdException(ID_CAN_NOT_BE_NULL);
            }
            if (idValue instanceof String && isNullOrEmpty((String) idValue)) {
                throw new InvalidIdException(ID_VALUE_CAN_NOT_BE_EMPTY_STRING);
            }
        }
        return document;
    }

    /**
     * Creates unique filter from the object.
     *
     * @param object  the object
     * @param idField the id field
     * @return the equals filter
     */
    Filter createUniqueFilter(Object object, Field idField) {
        idField.setAccessible(true);
        try {
            Object value = idField.get(object);
            if (value == null) {
                throw new InvalidIdException(ID_FILTER_VALUE_CAN_NOT_BE_NULL);
            }
            return eq(idField.getName(), value);
        } catch (IllegalAccessException iae) {
            throw new InvalidIdException(ID_FIELD_IS_NOT_ACCESSIBLE);
        }
    }

    <T> Field getIdField(NitriteMapper nitriteMapper, Class<T> type) {
        List<Field> fields;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            fields = getFieldsUpto(type, Object.class);
        } else {
            fields = Arrays.asList(type.getDeclaredFields());
        }

        boolean alreadyIdFound = false;
        Field idField = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                validateObjectIndexField(nitriteMapper, field.getType(), field.getName());
                if (alreadyIdFound) {
                    throw new NotIdentifiableException(OBJ_MULTIPLE_ID_FOUND);
                } else {
                    alreadyIdFound = true;
                    idField = field;
                }
            }
        }
        return idField;
    }

    <T> Set<Index> extractIndices(NitriteMapper nitriteMapper, Class<T> type) {
        notNull(type, errorMessage("type can not be null", VE_INDEX_ANNOTATION_NULL_TYPE));

        List<Indices> indicesList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indicesList = findAnnotations(Indices.class, type);
        } else {
            indicesList = new ArrayList<>();
            Indices indices = type.getAnnotation(Indices.class);
            if (indices != null) indicesList.add(indices);
        }

        Set<Index> indexSet = new LinkedHashSet<>();
        if (indicesList != null) {
            for (Indices indices : indicesList) {
                Index[] indexList = indices.value();
                populateIndex(nitriteMapper, type, Arrays.asList(indexList), indexSet);
            }
        }

        List<Index> indexList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indexList = findAnnotations(Index.class, type);
        } else {
            indexList = new ArrayList<>();
            Index index = type.getAnnotation(Index.class);
            if (index != null) indexList.add(index);
        }

        if (indexList != null) {
            populateIndex(nitriteMapper, type, indexList, indexSet);
        }
        return indexSet;
    }

    <T> Field getField(Class<T> type, String name, boolean recursive) {
        if (name.contains(FIELD_SEPARATOR)) {
            return getEmbeddedField(type, name);
        } else {
            // first check declared fields (fix for kotlin properties, ref: issue #54)
            // if nothing found and is-recursive then check recursively
            Field[] declaredFields = type.getDeclaredFields();
            Field field = null;
            for (Field declaredField : declaredFields) {
                if (declaredField.getName().equals(name)) {
                    field = declaredField;
                    break;
                }
            }

            if (field == null && recursive) {
                List<Field> fields = getFieldsUpto(type, Object.class);
                for (Field recursiveField : fields) {
                    if (recursiveField.getName().equals(name)) {
                        field = recursiveField;
                        break;
                    }
                }
            }
            if (field == null) {
                throw new ValidationException(errorMessage(
                        "no such field \'" + name + "\' for type " + type.getName(),
                        VE_REFLECT_FIELD_NO_SUCH_FIELD));
            }
            return field;
        }
    }

    private <T> Field getEmbeddedField(Class<T> startingClass, String embeddedField) {
        String regex = "\\" + FIELD_SEPARATOR;
        String[] split = embeddedField.split(regex, 2);
        String key = split[0];
        String remaining = split.length == 2 ? split[1] : "";

        if (isNullOrEmpty(key)) {
            throw new ValidationException(OBJ_INVALID_EMBEDDED_FIELD);
        }

        Field field;
        try {
            field = startingClass.getDeclaredField(key);
        } catch (NoSuchFieldException nsfe) {
            throw new ValidationException(errorMessage(
                    "no such field \'" + key + "\' for type " + startingClass.getName(),
                    VE_OBJ_INVALID_FIELD));
        }

        if (!isNullOrEmpty(remaining) || remaining.contains(FIELD_SEPARATOR)) {
            return getEmbeddedField(field.getType(), remaining);
        } else {
            return field;
        }
    }

    private <T> void populateIndex(NitriteMapper nitriteMapper, Class<T> type,
                                   List<Index> indexList, Set<Index> indexSet) {
        for (Index index : indexList) {
            String name = index.value();
            Field field = getField(type, name, type.isAnnotationPresent(InheritIndices.class));
            if (field != null) {
                validateObjectIndexField(nitriteMapper, field.getType(), field.getName());
                indexSet.add(index);
            }
        }
    }

    private void validateObjectIndexField(NitriteMapper nitriteMapper, Class<?> fieldType, String field) {
        if (!Comparable.class.isAssignableFrom(fieldType) && !fieldType.isPrimitive()) {
            throw new IndexingException(errorMessage("can not index on non comparable field " + field,
                    IE_OBJ_INDEX_ON_NON_COMPARABLE_FIELD));
        }

        if (Iterable.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
            throw new IndexingException(errorMessage("indexing on arrays or collections for field " + field
                    + " are not supported", IE_OBJ_INDEX_ON_ARRAY_NOT_SUPPORTED));
        }

        if (fieldType.isPrimitive()
                || fieldType == NitriteId.class
                || fieldType.isInterface()
                || Modifier.isAbstract(fieldType.getModifiers())) {
            // we will validate the solid class during insertion/update
            return;
        }

        Document document;
        try {
            document = skeletonDocument(nitriteMapper, fieldType);
        } catch (Throwable e) {
            throw new IndexingException(errorMessage(
                    "invalid type specified " + fieldType.getName() + " for indexing",
                    IE_INVALID_TYPE_FOR_INDEX), e);
        }

        if (document == null || document.size() > 0) {
            throw new InvalidOperationException(errorMessage(
                    "compound index on field " + field + " is not supported",
                    IOE_OBJ_COMPOUND_INDEX));
        }
    }

    List<Field> getFieldsUpto(Class<?> startClass, Class<?> exclusiveParent) {
        notNull(startClass, errorMessage("startClass can not be null", VE_REFLECT_FIELD_NULL_START_CLASS));
        List<Field> currentClassFields = new ArrayList<>(Arrays.asList(startClass.getDeclaredFields()));
        filterSynthetics(currentClassFields);
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Field> parentClassFields = getFieldsUpto(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    private static void filterSynthetics(List<Field> fields) {
        if (fields == null || fields.isEmpty()) return;
        Iterator<Field> iterator = fields.iterator();
        while (iterator.hasNext()) {
            Field f = iterator.next();
            if (f.isSynthetic()) iterator.remove();
        }
    }

    private <T extends Annotation> List<T> findAnnotations(Class<T> annotation, Class<?> type) {
        notNull(type, errorMessage("type can not be null", VE_REFLECT_NULL_START_CLASS));
        notNull(annotation, errorMessage("annotationClass can not be null", VE_REFLECT_NULL_ANNOTATION_CLASS));
        List<T> annotations = new ArrayList<>();

        T t = type.getAnnotation(annotation);
        if (t != null) annotations.add(t);

        Class[] interfaces = type.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            T ann = anInterface.getAnnotation(annotation);
            if (ann != null) annotations.add(ann);
        }

        Class<?> parentClass = type.getSuperclass();
        if (parentClass != null && !parentClass.equals(Object.class)) {
            List<T> list = findAnnotations(annotation, parentClass);
            annotations.addAll(list);
        }

        return annotations;
    }
}
