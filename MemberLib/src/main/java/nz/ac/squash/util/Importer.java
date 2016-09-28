package nz.ac.squash.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Member;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class Importer {
    private static final Logger sLogger = Logger.getLogger(Importer.class);

    public static interface ImportAction {
        public Member getMember();

        public String getDescription();

        public void apply();
    }

    private static class ImportActionNewMember implements ImportAction {
        private final Member mMember;

        public ImportActionNewMember(Member member) {
            mMember = member;
        }

        @Override
        public void apply() {
            DB.executeTransaction(new Transaction<Void>() {
                @Override
                public void run() {
                    update(mMember);
                    sLogger.info("Imported new member " +
                                 mMember.getNameFormatted());
                }
            });
        }

        @Override
        public Member getMember() {
            return mMember;
        }

        @Override
        public String getDescription() {
            return "New member";
        }
    }

    private static class ImportActionUpdate implements ImportAction {
        private Member mMember;

        private Boolean mNewActive = null;
        private String mNewName = null;
        private String mNewNickname = null;
        private String mNewPaymentStatus = null;

        public static ImportActionUpdate tryCreate(Member oldInfo,
                Member newInfo) {
            ImportActionUpdate action = new ImportActionUpdate();
            action.mMember = oldInfo;

            action.mNewActive = Utility.returnIfDifferent(oldInfo.isActive(),
                    newInfo.isActive());
            action.mNewName = Utility.returnIfDifferent(oldInfo.getName(),
                    newInfo.getName());
            if (!Objects.equals(oldInfo.getNickname(), newInfo.getNickname())) {
                boolean oldBlank = StringUtils.isBlank(oldInfo.getNickname());
                boolean newBlank = StringUtils.isBlank(newInfo.getNickname());

                if (!newBlank) {
                    action.mNewNickname = newInfo.getNickname();
                } else if (newBlank && !oldBlank) {
                    action.mNewNickname = "";
                }
            }
            action.mNewPaymentStatus = Utility.returnIfDifferent(
                    oldInfo.getPaymentStatus(), newInfo.getPaymentStatus());

            if (action.mNewActive != null || action.mNewName != null ||
                action.mNewNickname != null || action.mNewPaymentStatus != null) {
                return action;
            } else {
                return null;
            }
        }

        @Override
        public void apply() {
            DB.queueTransaction(new Transaction<Void>() {
                @Override
                public void run() {
                    if (mNewActive != null) mMember.setActive(mNewActive);
                    if (mNewName != null) mMember.setName(mNewName);
                    if (mNewNickname != null) mMember.setNickname(mNewNickname);
                    if (mNewPaymentStatus != null) mMember
                            .setPaymentStatus(mNewPaymentStatus);

                    update(mMember);
                    sLogger.info("Updated member " + mMember.getNameFormatted());
                }
            });
        }

        @Override
        public Member getMember() {
            return mMember;
        }

        @Override
        public String getDescription() {
            StringBuilder builder = new StringBuilder();

            if (mNewActive != null) builder.append("Disabled, ");
            if (mNewName != null) builder.append("Name changed, ");
            if (mNewNickname != null) builder.append("Nickname changed, ");
            if (mNewPaymentStatus != null) builder.append("Paid, ");

            return builder.toString();
        }
    }

    public static List<ImportAction> generateImport(File file) {
        final List<ImportAction> actions = new ArrayList<>();

        final Map<Date, Member> existingMembers = new HashMap<>();
        DB.executeTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                for (Member member : listAll(Member.class)) {
                    existingMembers.put(member.getSignupTime(), member);
                }
            }
        });

        Scanner reader = null;
        try {
            reader = new Scanner(file);

            // Parse each line.
            while (true) {
                String[] lineParts = reader.nextLine().split(",");
                final Member imported = new Member();
                try {
                    imported.setSignupTime(Utility.SPREADSHEET_FORMATTER
                            .parse(lineParts[0]));
                } catch (ParseException e) {
                    sLogger.warn("Invalid date/time: \"" + lineParts[0] +
                                 "\", probably the CSV header");
                    continue;
                }

                imported.setName(getOrNull(lineParts, 1));
                imported.setNickname(getOrNull(lineParts, 2));
                imported.setEmail(getOrNull(lineParts, 3));

                imported.setStudentStatus(getOrNull(lineParts, 4));
                imported.setStudentId(getOrNull(lineParts, 5));

                imported.setSkillLevel(parseSkillLevel(getOrNull(lineParts, 6),
                        getOrNull(lineParts, 7)));

                imported.setActive(StringUtils.isEmpty(getOrNull(lineParts, 9)));

                // Optional fields.
                if (lineParts.length > 11) imported
                        .setPaymentStatus(lineParts[11]);

                final Member existingMember = existingMembers.get(imported
                        .getSignupTime());

                if (existingMember != null) {
                    ImportActionUpdate action = ImportActionUpdate.tryCreate(
                            existingMember, imported);
                    if (action != null) actions.add(action);
                } else if (imported.isActive()) {
                    actions.add(new ImportActionNewMember(imported));
                }
            }
        } catch (IOException ex) {
            sLogger.error("Import failed", ex);
            return null;
        } catch (NoSuchElementException e) {
            IOUtils.closeQuietly(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }

        return actions;
    }

    private static <T> T getOrNull(T[] array, int index) {
        return index < array.length ? array[index] : null;
    }

    private static float parseSkillLevel(String level, String grade) {
        if (StringUtils.isNotEmpty(grade)) {
            char g = grade.toLowerCase().charAt(0);

            switch (g) {
            case 'a':
            case 'b':
                return 1.f;

            case 'c':
                return 1.f + 1.f / 3.f;
            case 'd':
                return 1.f + 2.f / 3.f;
            case 'e':
                return 2.f;
            case 'f':
                return 2.f + 1.f / 3.f;
            case 'j':
                return 2.f + 2.f / 3.f;
            }
        } else if (StringUtils.isNotEmpty(level)) {
            return Float.parseFloat(level);
        }

        return 2.5f;
    }
}
