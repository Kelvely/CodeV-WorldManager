package net.codevmc.util;

public class ExceptionUtils {

    @SuppressWarnings("unchecked") // I fucking checked!!!! RUNTIME LEVEL!!
    public static <E extends Exception> void rethrowException(Exception ex, Class<E> exceptionClass) throws E {
        if(exceptionClass.isInstance(ex)) {
            throw (E) ex;
        }
    }

    public static void rethrowIfIsRuntimeException(Exception ex) {
        if(ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
    }

    public static Error errorize(Exception ex) {
        return new Error("Caught unhandled exception at code level.", ex);
    }

}
