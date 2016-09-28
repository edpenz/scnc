package nz.ac.squash.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

public class Utility {
    public static final DateFormat FILE_SAFE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    public static final DateFormat SPREADSHEET_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static final DateFormat DATE_ONLY_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

    public static <T> StackTraceElement getOuterTrace(Class<T> clazz) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        boolean hasEnteredInternal = false;
        for (StackTraceElement stackFrame : trace) {
            boolean isInternal = stackFrame.getClassName().startsWith(clazz.getName());
            if (!isInternal && hasEnteredInternal) {
                return stackFrame;
            } else {
                hasEnteredInternal |= isInternal;
            }
        }

        throw new IllegalArgumentException(clazz.getName() + " does not exist in the stack trace");
    }

    public static <T> T first(Iterable<T> collection) {
        final Iterator<T> it = collection.iterator();

        if (it.hasNext()) return it.next();
        else return null;
    }

    public static Date today() {
        return stripTime(new Date());
    }

    @SuppressWarnings("deprecation")
    public static Date stripTime(Date date) {
        Date newDate = new Date(date.getTime());
        newDate.setHours(0);
        newDate.setMinutes(0);
        newDate.setSeconds(0);
        newDate.setTime(newDate.getTime() - newDate.getTime() % 1000);
        return newDate;
    }

    public static Date stripMillis(Date date) {
        return new Date(date.getTime() - date.getTime() % 1000);
    }

    public static <T> T returnIfDifferent(T a, T b) {
        return Objects.equals(a, b) ? null : b;
    }
}
