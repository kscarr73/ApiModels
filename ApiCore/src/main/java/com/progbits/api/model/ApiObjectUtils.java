package com.progbits.api.model;

import static com.progbits.api.model.ApiObject.TYPE_ARRAYLIST;
import static com.progbits.api.model.ApiObject.TYPE_BOOLEAN;
import static com.progbits.api.model.ApiObject.TYPE_DATETIME;
import static com.progbits.api.model.ApiObject.TYPE_DECIMAL;
import static com.progbits.api.model.ApiObject.TYPE_DOUBLE;
import static com.progbits.api.model.ApiObject.TYPE_DOUBLEARRAY;
import static com.progbits.api.model.ApiObject.TYPE_INTEGER;
import static com.progbits.api.model.ApiObject.TYPE_INTEGERARRAY;
import static com.progbits.api.model.ApiObject.TYPE_LONG;
import static com.progbits.api.model.ApiObject.TYPE_OBJECT;
import static com.progbits.api.model.ApiObject.TYPE_STRING;
import static com.progbits.api.model.ApiObject.TYPE_STRINGARRAY;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Set of utilities to Work with ApiObjects.
 *
 * @author scarr
 */
public class ApiObjectUtils {

    /**
     * Null Safe copy of all fields with optional rename
     *
     * @param subject The ApiObject to copy the fields from
     * @param rename Optional Map of the fields to rename to.
     * @return A new ApiObject with the fields from the original, or optionally
     * renamed fields.
     */
    @SuppressWarnings("UnnecessaryBoxing")
    public static ApiObject cloneApiObject(ApiObject subject, Map<String, String> rename) {
        final Map<String, String> lclRename;

        if (rename == null) {
            lclRename = new HashMap<>();
        } else {
            lclRename = rename;
        }

        return cloneApiObject(subject, "", false, lclRename);
    }

    /**
     * Null Safe deep copy of all fields and sub objects with Optional Rename.
     *
     * @param subject The ApiObject to copy from
     * @param fieldPrefix Should start with an empty string ""
     * @param limitToMap Should ONLY the fields in the map be returned.
     * @param lclRename Map with fields. Dot notation can be used for sub
     * objects.
     * @return
     */
    public static ApiObject cloneApiObject(ApiObject subject, String fieldPrefix, boolean limitToMap, final Map<String, String> lclRename) {
        final ApiObject retObj;

        if (subject == null) {
            return null;
        }

        if (subject.getApiClass() != null) {
            retObj = subject.getApiClass().createInstance();
        } else {
            retObj = new ApiObject();
        }

        retObj.setName(subject.getName());

        for (var fldEntry : subject.getFields().entrySet()) {
            var k = fldEntry.getKey();
            var v = fldEntry.getValue();

            int iFieldType = subject.getType(k);

            switch (iFieldType) {
                case TYPE_ARRAYLIST:
                    List<ApiObject> newList = new ArrayList<>();

                    ((List<ApiObject>) v).forEach((entry) -> {
                        newList.add(cloneApiObject(entry, k + ".", limitToMap, lclRename));
                    });

                    putField(retObj, k,
                            newList,
                            fieldPrefix + k, limitToMap, lclRename);

                    break;

                case TYPE_OBJECT:
                    putField(retObj, k,
                            cloneApiObject((ApiObject) v, k + ".", limitToMap, lclRename),
                            fieldPrefix + k, limitToMap, lclRename);

                    break;

                case TYPE_STRINGARRAY:
                    List<String> newStrArray = new ArrayList<>();

                    ((List<String>) v).forEach((entry) -> {
                        newStrArray.add(new String(entry.getBytes()));
                    });

                    putField(retObj, k, newStrArray, fieldPrefix + k, limitToMap, lclRename);

                    break;

                case TYPE_INTEGERARRAY:
                    List<Integer> newIntArray = new ArrayList<>();

                    ((List<Integer>) v).forEach((entry) -> {
                        newIntArray.add(Integer.valueOf(entry.intValue()));
                    });

                    putField(retObj, k, newIntArray, fieldPrefix + k, limitToMap, lclRename);

                    break;

                case TYPE_DOUBLEARRAY:
                    List<Double> newDblArray = new ArrayList<>();

                    ((List<Integer>) v).forEach((entry) -> {
                        newDblArray.add(Double.valueOf(entry.doubleValue()));
                    });

                    putField(retObj, k, newDblArray, fieldPrefix + k, limitToMap, lclRename);

                    break;

                case TYPE_BOOLEAN:
                    putField(retObj, k, Boolean.valueOf((Boolean) v), fieldPrefix + k, limitToMap, lclRename);
                    break;

                case TYPE_STRING:
                    putField(retObj, k, String.valueOf((String) v), fieldPrefix + k, limitToMap, lclRename);
                    break;

                case TYPE_INTEGER:
                    putField(retObj, k, Integer.valueOf((Integer) v), fieldPrefix + k, limitToMap, lclRename);
                    break;

                case TYPE_DOUBLE:
                    putField(retObj, k, Double.valueOf((Double) v), fieldPrefix + k, limitToMap, lclRename);

                    break;

                case TYPE_DATETIME:
                    putField(retObj, k, ((OffsetDateTime) v).plusNanos(0), fieldPrefix + k, limitToMap, lclRename);
                    break;

                case TYPE_DECIMAL:
                    putField(retObj, k, new BigDecimal(((BigDecimal) v).toString()), fieldPrefix + k, limitToMap, lclRename);
                    break;

                case TYPE_LONG:
                    putField(retObj, k, Long.valueOf((Long) v), fieldPrefix + k, limitToMap, lclRename);

                    break;

                default:
                    putField(retObj, k, v, fieldPrefix + k, limitToMap, lclRename);
                    break;
            }
        }

        return retObj;
    }

    public static void putField(ApiObject subject, String field, Object value, String searchField, boolean limitToMap, Map<String, String> rename) {
        if (subject != null) {
            if (limitToMap) {
                if (rename.containsKey(searchField)) {
                    subject.put(rename.get(searchField), value);
                } else if (rename.containsKey(searchFieldAll(searchField))) {
                    subject.put(field, value);
                }
            } else {
                if (rename.containsKey(searchField)) {
                    subject.put(rename.get(searchField), value);
                } else {
                    subject.put(field, value);
                }
            }
        }
    }

    private static String searchFieldAll(String subject) {
        if (subject.contains(".")) {
            return subject.substring(0, subject.lastIndexOf(".") + 1) + "*";
        } else {
            return "*";
        }
    }

    public static boolean isNull(ApiObject subject, String field) {
        if (subject == null) {
            return true;
        } else {
            return subject.isNull(field);
        }
    }

    public static boolean isSet(ApiObject subject, String field) {
        if (subject == null) {
            return false;
        } else {
            return subject.isSet(field);
        }
    }

    public static int getType(ApiObject subject, String field) {
        if (subject == null) {
            return ApiObject.TYPE_NULL;
        } else {
            return subject.getType(field);
        }
    }
}
