package nz.ac.squash.util;

import com.google.gson.Gson;
import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Member;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Importer {
    private static final Logger sLogger = Logger.getLogger(Importer.class);

    private static class Config {
        public String DownloadUrl = "";

        public int TimestampColumn = -1;
        public int FirstNameColumn = -1;
        public int LastNameColumn = -1;
        public int NicknameColumn = -1;
        public int StudentIdColumn = -1;
        public int EmailColumn = -1;
        public int SkillColumn = -1;
        public int PaymentColumn = -1;

        public static Config load(String path) {
            try (FileReader reader = new FileReader(path)) {
                return new Gson().fromJson(reader, Config.class);
            } catch (FileNotFoundException e) {
                sLogger.warn("No importer config found");
            } catch (IOException e) {
                sLogger.error("Failed to read config", e);
            }

            Config defaultConfig = new Config();
            defaultConfig.save(path);
            return defaultConfig;
        }

        public void save(String path) {
            try (FileWriter writer = new FileWriter(path)) {
                new Gson().toJson(this, writer);
            } catch (IOException e) {
                sLogger.error("Failed to write config", e);
            }
        }

    }

    public interface ImportAction {
        Member getMember();

        String getDescription();

        void apply();
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
                    sLogger.info("Imported new member " + mMember.getNameFormatted());
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
        private Member mCopyFrom;

        public static ImportActionUpdate tryCreate(Member member, Member newInfo) {
            ImportActionUpdate action = new ImportActionUpdate();
            action.mMember = member;
            action.mCopyFrom = newInfo;

            if (!areSame(member, newInfo, Member::getNameRaw)) return action;
            if (!areSame(member, newInfo, Member::getNickname)) return action;
            if (!areSame(member, newInfo, Member::getAmountPaid)) return action;

            if (!areSame(member, newInfo, Member::getSignupTime)) return action;
            if (!areSame(member, newInfo, Member::getEmail)) return action;
            if (!areSame(member, newInfo, Member::getStudentId)) return action;

            return null;
        }

        @Override
        public void apply() {
            DB.queueTransaction(new Transaction<Void>() {
                @Override
                public void run() {
                    mMember.setFirstName(mCopyFrom.getFirstName());
                    mMember.setLastName(mCopyFrom.getLastName());
                    mMember.setNickname(mCopyFrom.getNickname());
                    mMember.setAmountPaid(mCopyFrom.getAmountPaid());

                    mMember.setSignupTime(mCopyFrom.getSignupTime());
                    mMember.setEmail(mCopyFrom.getEmail());
                    mMember.setStudentId(mCopyFrom.getStudentId());

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
            return String.format("Updated %s", mMember.getNameFormattedLong());
        }
    }

    public static List<ImportAction> generateImport(File file) {
        final List<ImportAction> actions = new ArrayList<>();
        final Config config = Config.load(new File(file.getParent(), "config.json").getAbsolutePath());

        final List<Member> existingMembers = DB.executeTransaction(new Transaction<List<Member>>() {
            @Override
            public void run() {
                setResult(listAll(Member.class));
            }
        });

        Supplier<Stream<Member>> otherMembers = () -> {
            Stream<Member> existing = existingMembers.stream();
            Stream<Member> added = actions.stream()
                    .filter(ImportActionNewMember.class::isInstance)
                    .map(ImportAction::getMember);

            return Stream.concat(existing, added);
        };

        try (Scanner reader = new Scanner(file)) {
            // TODO Validate configuration against column headers.
            reader.nextLine();

            // Parse each line.
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] lineParts = line.split("\t");
                final Member imported;
                try {
                    imported = parseMember(lineParts, config);
                } catch (ParseException e) {
                    sLogger.warn(String.format("Skipping unparseable line \"%s\"", line));
                    continue;
                }

                Optional<Member> duplicateOf = otherMembers.get()
                        .filter(member -> {
                            int similarity = 0;
                            similarity += areSimilar(member, imported, Member::getSignupTime) ? 1 : 0;
                            similarity += areSimilar(member, imported, Member::getNameRaw) ? 1 : 0;
                            similarity += areSimilar(member, imported, Member::getEmail) ? 1 : 0;
                            similarity += areSimilar(member, imported, Member::getStudentId) ? 1 : 0;
                            return similarity >= 2;
                        })
                        .findFirst();

                if (duplicateOf.isPresent()) {
                    ImportActionUpdate action = ImportActionUpdate.tryCreate(duplicateOf.get(), imported);
                    if (action != null) actions.add(action);
                } else {
                    actions.add(new ImportActionNewMember(imported));
                }
            }
        } catch (IOException ex) {
            sLogger.error("Import failed", ex);
            return null;
        } catch (NoSuchElementException e) {
            // Finished.
        }

        return actions;
    }

    private static <T, K> boolean areSame(T a, T b, Function<T, K> keyAccessor) {
        K aKey = keyAccessor.apply(a);
        K bKey = keyAccessor.apply(b);
        return Objects.equals(aKey, bKey);
    }

    private static <T, K> boolean areSimilar(T a, T b, Function<T, K> keyAccessor) {
        K aKey = keyAccessor.apply(a);
        K bKey = keyAccessor.apply(b);
        return aKey != null && aKey.equals(bKey);
    }

    private static Member parseMember(String[] lineParts, Config config) throws ParseException {
        Member member = new Member();
        member.setSignupTime(tryParseColumn(lineParts, config.TimestampColumn, s -> Timestamp.from(LocalDateTime.from(Utility.SPREADSHEET_FORMATTER.parse(s)).atZone(ZoneId.systemDefault()).toInstant())));

        member.setFirstName(tryParseColumn(lineParts, config.FirstNameColumn));
        member.setLastName(tryParseColumn(lineParts, config.LastNameColumn));
        member.setNickname(tryParseColumn(lineParts, config.NicknameColumn));

        member.setAmountPaid(tryParseColumn(lineParts, config.PaymentColumn, Float::parseFloat));
        member.setSkillLevel(tryParseColumn(lineParts, config.SkillColumn, Float::parseFloat));

        member.setEmail(tryParseColumn(lineParts, config.EmailColumn));
        member.setStudentId(tryParseColumn(lineParts, config.StudentIdColumn));

        return member;
    }

    private static String tryParseColumn(String[] columns, int column) {
        return tryParseColumn(columns, column, s -> s);
    }

    private static <T> T tryParseColumn(String[] columns, int column, Function<String, T> parser) {
        try {
            return parser.apply(columns[column]);
        } catch (Exception e) {
            return null;
        }
    }
}
