package nz.ac.squash.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class Utility {
    public static final DateFormat FILE_SAFE_FORMATTER = new SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss");

    public static final DateFormat SPREADSHEET_FORMATTER = new SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss");

    public static final DateFormat DATE_ONLY_FORMATTER = new SimpleDateFormat(
            "dd/MM/yyyy");

    @SafeVarargs
    public static <T> T firstNonNull(T... objects) {
        for (T t : objects) {
            if (t != null) return t;
        }
        return null;
    }

    public static <T> StackTraceElement getOuterTrace(Class<T> clazz) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        boolean hasEnteredInternal = false;
        for (StackTraceElement stackFrame : trace) {
            boolean isInternal = stackFrame.getClassName().startsWith(
                    clazz.getName());
            if (!isInternal && hasEnteredInternal) {
                return stackFrame;
            } else {
                hasEnteredInternal |= isInternal;
            }
        }

        throw new IllegalArgumentException(clazz.getName() +
                                           " does not exist in the stack trace");
    }

    public static <T> T first(Iterable<T> collection) {
        final Iterator<T> it = collection.iterator();

        if (it.hasNext()) return it.next();
        else return null;
    }

    public static void safeClose(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                if (closeable != null) closeable.close();
            } catch (IOException e) {
                // Ignore.
            }
        }
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

    public static boolean eqOrNull(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        else if (o1 == null) return o2.equals(o1);
        else return o1.equals(o2);
    }

    public static <T> T returnIfDifferent(T a, T b) {
        return eqOrNull(a, b) ? null : b;
    }

    public static int deltaSkill(String a, String b) {
        return Math.abs(compareSkill(a, b));
    }

    public static int compareSkill(String a, String b) {
        int aSkill = Integer.parseInt(a);
        int bSkill = Integer.parseInt(b);

        return aSkill - bSkill;
    }

    public static void trySleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}