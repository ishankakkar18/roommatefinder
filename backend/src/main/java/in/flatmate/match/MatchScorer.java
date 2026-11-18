package in.flatmate.match;

import in.flatmate.config.AppProperties;
import in.flatmate.profile.Profile;
import in.flatmate.profile.ProfileEnums;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Pure scoring. No Spring, no database, no clock — unit-testable in isolation.
 *
 * Weighted 0-100 score over 11 enum factors. Two directions are computed and
 * the LOWER is taken, because one person being comfortable does not make the
 * pairing comfortable: if you don't mind a smoker but they can't live with one,
 * the pairing is constrained by them, not you.
 */
public final class MatchScorer {

    // index = distance between two ordered answers
    private static final double[] D4 = {1.00, 0.60, 0.20, 0.00};
    private static final double[] D5 = {1.00, 0.75, 0.40, 0.15, 0.00};
    private static final double[] D3 = {1.00, 0.60, 0.20};
    private static final double[] D4_SOFT = {1.00, 0.70, 0.30, 0.10};
    private static final double[] D5_BUDGET = {1.00, 0.60, 0.20, 0.00, 0.00};

    /** Rows/cols: VEG, EGG, NONVEG, VEGAN — same order as the enum. */
    private static final double[][] FOOD = {
            {1.00, 0.80, 0.20, 0.90},
            {0.80, 1.00, 0.80, 0.70},
            {0.20, 0.80, 1.00, 0.30},
            {0.90, 0.70, 0.30, 1.00}
    };

    private final AppProperties.Weights weights;
    private final BiPredicate<Integer, Integer> adjacency;

    public MatchScorer(AppProperties.Weights weights, BiPredicate<Integer, Integer> adjacency) {
        if (weights.total() != 100) {
            throw new IllegalStateException(
                    "app.matching.weights must sum to 100, got " + weights.total());
        }
        this.weights = weights;
        this.adjacency = adjacency;
    }

    public record Factor(String key, String label, int weight, double value) {
        double contribution() {
            return weight * value;
        }
    }

    public record Result(int score, List<Factor> factors) {

        /** Top 3 strengths, i.e. the factors that pushed the score up. */
        public List<String> reasons() {
            return factors.stream()
                    .filter(f -> f.value() >= 0.70)
                    .sorted(Comparator.comparingDouble(Factor::contribution).reversed())
                    .limit(3)
                    .map(f -> describe(f, true))
                    .toList();
        }

        /** Up to 2 weakest factors that actually cost points. */
        public List<String> frictions() {
            return factors.stream()
                    .filter(f -> f.value() < 0.80)
                    .sorted(Comparator.comparingDouble(Factor::contribution))
                    .limit(2)
                    .map(f -> describe(f, false))
                    .toList();
        }

        private static String describe(Factor f, boolean positive) {
            return (positive ? "" : "") + f.label();
        }
    }

    public Result score(Profile a, Profile b) {
        List<Factor> fromA = factors(a, b);
        List<Factor> fromB = factors(b, a);

        double scoreA = weighted(fromA);
        double scoreB = weighted(fromB);

        return scoreA <= scoreB
                ? new Result((int) Math.round(scoreA), fromA)
                : new Result((int) Math.round(scoreB), fromB);
    }

    /** "How comfortable is `subject` with `other`." */
    public double directionalScore(Profile subject, Profile other) {
        return weighted(factors(subject, other));
    }

    private double weighted(List<Factor> factors) {
        return factors.stream().mapToDouble(Factor::contribution).sum();
    }

    private List<Factor> factors(Profile subject, Profile other) {
        List<Factor> out = new ArrayList<>(11);

        out.add(new Factor("sleep", "Sleep schedule", weights.sleep(),
                d(D4, subject.getSleepTime().ordinal(), other.getSleepTime().ordinal())));
        out.add(new Factor("cleanliness", "Cleanliness", weights.cleanliness(),
                d(D5, subject.getCleanliness(), other.getCleanliness())));
        out.add(new Factor("budget", "Budget", weights.budget(),
                d(D5_BUDGET, subject.getBudgetBand(), other.getBudgetBand())));
        out.add(new Factor("guests", "Guests at home", weights.guests(),
                d(D4, subject.getGuests().ordinal(), other.getGuests().ordinal())));
        out.add(new Factor("moveIn", "Move-in date", weights.moveIn(),
                d(D4_SOFT, subject.getMoveIn().ordinal(), other.getMoveIn().ordinal())));
        out.add(new Factor("social", "Social style", weights.social(),
                d(D3, subject.getSocial().ordinal(), other.getSocial().ordinal())));
        out.add(new Factor("food", "Food habits", weights.food(),
                FOOD[subject.getFood().ordinal()][other.getFood().ordinal()]));
        out.add(new Factor("wfh", "Work from home", weights.workFromHome(),
                d(D4_SOFT, subject.getWorkFromHome().ordinal(), other.getWorkFromHome().ordinal())));
        out.add(new Factor("area", "Area", weights.area(), areaValue(subject, other)));
        out.add(new Factor("smoking", "Smoking", weights.smoking(),
                smokeValue(subject, other)));
        out.add(new Factor("pets", "Pets", weights.pets(), petsValue(subject, other)));

        return out;
    }

    private double areaValue(Profile subject, Profile other) {
        Integer a = subject.getAreaId();
        Integer b = other.getAreaId();
        if (a == null || b == null) {
            return 0.0;
        }
        if (a.equals(b)) {
            return 1.0;
        }
        return adjacency != null && adjacency.test(a, b) ? 0.70 : 0.45;
    }

    /** Your tolerance against their habit — this is what makes the score directional. */
    private double smokeValue(Profile subject, Profile other) {
        return switch (other.getSmoke()) {
            case I_DONT -> 1.00;
            case QUIT -> switch (subject.getSmokeTolerance()) {
                case CANT -> 0.20;
                case OUTDOORS_ONLY -> 0.60;
                case DONT_MIND -> 0.90;
            };
            case I_DO -> switch (subject.getSmokeTolerance()) {
                case CANT -> 0.00;
                case OUTDOORS_ONLY -> 0.50;
                case DONT_MIND -> 0.80;
            };
        };
    }

    private double petsValue(Profile subject, Profile other) {
        return switch (other.getPets()) {
            case NONE -> 1.00;
            case WANT -> switch (subject.getPetTolerance()) {
                case NO_PETS -> 0.50;
                case DONT_MIND -> 0.90;
            };
            case HAVE -> switch (subject.getPetTolerance()) {
                case NO_PETS -> 0.00;
                case DONT_MIND -> 0.70;
            };
        };
    }

    private static double d(double[] table, int a, int b) {
        int distance = Math.min(Math.abs(a - b), table.length - 1);
        return table[distance];
    }

    /** Exposed for tests and for the hard-filter explanation in docs. */
    public static boolean isFoodPair(ProfileEnums.Food a, ProfileEnums.Food b) {
        return a != null && b != null;
    }
}
