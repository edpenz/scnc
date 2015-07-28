package nz.ac.squash.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

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

    private static final String[] NAME_PREPOSITIONS = new String[] { "van",
            "von", "de", "der" };

    public static String formatNameLong(String fullname, String nickname) {
        String cleanedFullname = cleanName(fullname);
        String cleanedNickname = cleanName(nickname);

        final List<String> nameParts = new ArrayList<>();
        Collections.addAll(nameParts, StringUtils.split(cleanedFullname));

        capitaliseNameParts(nameParts);
        insertNickname(nameParts, capitaliseNamePart(cleanedNickname));

        return StringUtils.join(nameParts, " ");
    }

    private static void insertNickname(List<String> nameParts, String nickname) {
        if (StringUtils.isEmpty(nickname)) return;

        String quotedNickname = String.format("'%s'", nickname);

        int nicknameIndex = nameParts.indexOf(nickname);
        if (nicknameIndex > 0) {
            nameParts.set(nicknameIndex, quotedNickname);
        } else if (nicknameIndex < 0) {
            nameParts.add(1, quotedNickname);
        }
    }

    public static String formatName(String fullname, String nickname) {
        String cleanedFullname = cleanName(fullname);
        String cleanedNickname = cleanName(nickname);

        final List<String> nameParts = new ArrayList<>();
        Collections.addAll(nameParts, StringUtils.split(cleanedFullname));

        capitaliseNameParts(nameParts);
        replaceNickname(nameParts, capitaliseNamePart(cleanedNickname));
        removeMiddleNames(nameParts);

        return StringUtils.join(nameParts, " ");
    }

    public static String cleanName(String name) {
        return name != null ? name.trim().toLowerCase() : null;
    }

    private static void capitaliseNameParts(List<String> nameParts) {
        for (int i = 0; i < nameParts.size(); ++i) {
            String namePart = nameParts.get(i);
            nameParts.set(i, capitaliseNamePart(namePart));
        }
    }

    private static String capitaliseNamePart(String namePart) {
        if (namePart == null) return null;

        boolean isPreposition = ArrayUtils
                .contains(NAME_PREPOSITIONS, namePart);
        boolean isAbbreviation = namePart.endsWith(".");

        if (isPreposition) {
            return namePart;
        } else if (isAbbreviation) {
            return namePart.toUpperCase();
        } else {
            return WordUtils.capitalizeFully(namePart);
        }
    }

    private static void replaceNickname(List<String> nameParts, String nickname) {
        boolean hasNickname = StringUtils.isNotBlank(nickname);
        if (!hasNickname) return;

        boolean nicknameIsDifferent = !nickname.equals(nameParts.get(0));
        if (!nicknameIsDifferent) return;

        nameParts.remove(nickname);
        nameParts.add(0, String.format("'%s'", nickname));
    }

    private static void removeMiddleNames(List<String> nameParts) {
        for (int i = 1; i < nameParts.size();) {
            String namePart = nameParts.get(i);

            boolean hasMoreParts = i < nameParts.size() - 1;
            boolean isPreposition = ArrayUtils.contains(NAME_PREPOSITIONS,
                    namePart);
            boolean isShort = namePart.length() <= 3;

            if (hasMoreParts && !isPreposition && !isShort) {
                nameParts.remove(i);
            } else {
                ++i;
            }
        }
    }

    private static Class<?> sEntryClass = null;
    private static String[] sArgs = null;

    public static void seedRestart(Class<?> entryClass, String[] args) {
        sEntryClass = entryClass;
        sArgs = args;
    }

    public static void restart() {
        StringBuilder cmd = new StringBuilder();
        cmd.append(System.getProperty("java.home") + File.separator + "bin" +
                   File.separator + "java ");
        for (String jvmArg : ManagementFactory.getRuntimeMXBean()
                .getInputArguments()) {
            cmd.append(jvmArg + " ");
        }
        cmd.append("-cp \"")
                .append(ManagementFactory.getRuntimeMXBean().getClassPath())
                .append("\" ");
        cmd.append(sEntryClass.getName()).append(" ");
        for (String arg : sArgs) {
            cmd.append(arg).append(" ");
        }
        try {
            Runtime.getRuntime().exec(cmd.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
