package nz.ac.squash.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class NameUtils {
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
}
