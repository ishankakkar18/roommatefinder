package in.flatmate.profile;

/**
 * Every matching input. All are fixed enums, which is what makes scoring a
 * table lookup instead of fuzzy text matching. Names match the DB codes
 * exactly, so @Enumerated(STRING) needs no converter.
 *
 * Ordering matters: several are ordinal and scored by index distance,
 * so do not reorder the constants.
 */
public final class ProfileEnums {

    private ProfileEnums() {
    }

    public enum LookingFor {
        ROOM_SHARE, FLATMATE_FOR_MY_FLAT, BOTH
    }

    /** Ordinal — move-in proximity is scored by index distance. */
    public enum MoveIn {
        IMMEDIATE, TWO_WEEKS, ONE_MONTH, TWO_MONTHS, EXPLORING
    }

    public enum FlatmateGenderPref {
        SAME_AS_MINE, NO_PREF, MALE_ONLY, FEMALE_ONLY
    }

    /** Ordinal — sleep proximity scored by index distance. */
    public enum SleepTime {
        BEFORE_2200, H2200_2300, H2300_0000, AFTER_MIDNIGHT
    }

    /** Ordinal. */
    public enum WakeTime {
        BEFORE_0600, H0600_0800, H0800_1000, AFTER_1000
    }

    /** Ordinal. */
    public enum Guests {
        RARELY, SOMETIMES, OFTEN, VERY_OFTEN
    }

    /** Ordinal. */
    public enum Social {
        QUIET, BALANCED, SOCIAL
    }

    public enum Food {
        VEG, EGG, NONVEG, VEGAN
    }

    /** Ordinal. */
    public enum Cooking {
        NEVER, RARELY, SOMETIMES, DAILY
    }

    /** Ordinal. */
    public enum WorkFromHome {
        NEVER, ONE_TWO_DAYS, THREE_FOUR_DAYS, ALWAYS
    }

    public enum Smoke {
        I_DONT, I_DO, QUIT
    }

    public enum SmokeTolerance {
        CANT, OUTDOORS_ONLY, DONT_MIND
    }

    public enum Drink {
        NEVER, SOCIALLY, REGULARLY
    }

    public enum DrinkTolerance {
        DONT_MIND, RATHER_NOT
    }

    public enum Pets {
        NONE, HAVE, WANT
    }

    public enum PetTolerance {
        DONT_MIND, NO_PETS
    }
}
