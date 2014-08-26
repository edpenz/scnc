package nz.ac.squash.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

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

        private String mNewName = null;
        private Boolean mNewActive = null;

        public static ImportActionUpdate tryCreate(Member oldInfo,
                Member newInfo) {
            ImportActionUpdate action = new ImportActionUpdate();
            action.mMember = oldInfo;

            boolean hasAnEffect = false;

            if (oldInfo.isActive() != newInfo.isActive()) {
                action.mNewActive = newInfo.isActive();
                hasAnEffect = true;
            }

            if (!oldInfo.getName().equals(newInfo.getName())) {
                action.mNewName = newInfo.getName();
                hasAnEffect = true;
            }

            return hasAnEffect ? action : null;
        }

        @Override
        public void apply() {
            DB.queueTransaction(new Transaction<Void>() {
                @Override
                public void run() {
                    if (mNewName != null) mMember.setName(mNewName);
                    if (mNewActive != null) mMember.setActive(mNewActive);

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

            if (mNewName != null) builder.append("Name changed to \"")
                    .append(mNewName).append("\", ");

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

                imported.setName(lineParts[1]);
                // imported.setSignupCause(lineParts[2]);
                imported.setStudentStatus(lineParts[3]);
                imported.setStudentIdAndUpi(lineParts[4]);
                imported.setEmail(lineParts[5]);
                imported.setSkillLevel(lineParts[6]);
                imported.setPaymentMethod(lineParts[7]);

                // Optional fields.
                if (lineParts.length > 8) {
                    imported.setActive(StringUtils.isEmpty(lineParts[8]));
                    imported.setSignupMethod(lineParts[9]);
                }

                if (lineParts.length >= 12) {
                    imported.setHasPaid(lineParts[11]);
                }

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
}
