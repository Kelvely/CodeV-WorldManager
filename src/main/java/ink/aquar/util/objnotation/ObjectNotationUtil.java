package ink.aquar.util.objnotation;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class ObjectNotationUtil {

    @SuppressWarnings("unchecked")
    public static Map putIntoJsonObject(Map obj, String key, Object value) {
        obj.put(key, value);
        return obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFromJsonObject(Map obj, String key) {
        return (T) obj.get(key);
    }

    public static void requireElements(Object obj, ExpectedElement... elements) {
        for(ExpectedElement element : elements) {
            Object currentObject = obj;
            Iterator<String> iterator = element.iterator();
            StringBuilder pathBuilder = new StringBuilder();
            if(iterator.hasNext()) while(true) {
                if(currentObject instanceof Map) {
                    Map jsonObject = (Map) obj;
                    String f = iterator.next();
                    pathBuilder.append(f);
                    if(jsonObject.containsKey(f)) {
                        currentObject = jsonObject.get(f);
                        if(iterator.hasNext()) pathBuilder.append('.');
                        else break;
                    } else {
                        throw new MissingElementException("An object is expected for path " + pathBuilder.toString() + ".");
                    }
                } else {
                    throw new MissingElementException("A non-null JSON object is expected for path " + (pathBuilder.length() > 0 ? pathBuilder.toString() : "root") + ".");
                }
            }
            if(obj == null) {
                if(!element.acceptNull) {
                    throw new MissingElementException("A non-null object is expected for path " + (pathBuilder.length() > 0 ? pathBuilder.toString() : "root") + ".");
                }
            } else {
                if(!element.expectedClass.isInstance(obj)) {
                    throw new MissingElementException("An instance of "+ element.expectedClass.getName() +" is expected for path "+ (pathBuilder.length() > 0 ? pathBuilder.toString() : "root") + ".");
                }
            }
        }
    }

    public static class ExpectedElement implements Iterable<String> {

        private static final String[] EMPTY_STRING_ARRAY = { };

        /** READ ONLY */
        final String[] fieldNames;

        final Class<?> expectedClass;

        final boolean acceptNull;

        /**
         * Create a path to a field with a path expression. <br/>
         * @param path the path expression, like <code>gary.ability.1.name</code>
         */
        public ExpectedElement(Class<?> expectedClass, boolean accceptNull, String path) {
            this(expectedClass, accceptNull, path.split("."));
        }

        /**
         * Create a path to field with an array of field names. <br/>
         * The next element of the array is the field of the previous one,
         * as in <code>String[] fieldNames = {"gary", "ability", "1", "name"}</code>,
         * <code>ability</code> is a field of <code>gary</code>. <br/>
         * @param fieldNames An array of field names in member's-member order.
         */
        public ExpectedElement(Class<?> expectedClass, boolean acceptNull, String... fieldNames) {
            ArrayList<String> listOfFieldNames = new ArrayList<>(fieldNames.length);
            for (String fieldName : fieldNames) {
                if (!fieldName.isEmpty()) listOfFieldNames.add(fieldName);
            }
            this.expectedClass = expectedClass;
            this.acceptNull = acceptNull;
            this.fieldNames = listOfFieldNames.toArray(EMPTY_STRING_ARRAY);
        }

        /**
         * Get the depth of this path, as <code>cat.name</code> has depth of 2. <br/>
         * Depth of 0 means <code>this</code> in Java. Just notice if you are doing some implementations like
         * <code>get(new FieldPath({ }))</code>, it should return the object itself. <br/>
         * @return The depth of this path
         */
        public int depth() {
            return fieldNames.length;
        }

        /**
         * The next element of the array is the field of the previous one.
         * @return An iterator to access each field of this in member's-member order
         */
        @Override
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                private int pointer = 0;

                @Override
                public boolean hasNext() {
                    return pointer < fieldNames.length;
                }

                @Override
                public String next() {
                    return fieldNames[pointer++];
                }
            };
        }
    }

    public static class MissingElementException extends RuntimeException {
        
        public MissingElementException() {
            super();
        }

        public MissingElementException(String message) {
            super(message);
        }

        public MissingElementException(String message, Throwable cause) {
            super(message, cause);
        }

        public MissingElementException(Throwable cause) {
            super(cause);
        }
        
    }

}
