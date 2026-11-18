package in.flatmate.match;

import in.flatmate.config.AppProperties;
import in.flatmate.profile.Profile;
import in.flatmate.profile.ProfileEnums.Cooking;
import in.flatmate.profile.ProfileEnums.Drink;
import in.flatmate.profile.ProfileEnums.DrinkTolerance;
import in.flatmate.profile.ProfileEnums.FlatmateGenderPref;
import in.flatmate.profile.ProfileEnums.Food;
import in.flatmate.profile.ProfileEnums.Guests;
import in.flatmate.profile.ProfileEnums.LookingFor;
import in.flatmate.profile.ProfileEnums.MoveIn;
import in.flatmate.profile.ProfileEnums.PetTolerance;
import in.flatmate.profile.ProfileEnums.Pets;
import in.flatmate.profile.ProfileEnums.SleepTime;
import in.flatmate.profile.ProfileEnums.Smoke;
import in.flatmate.profile.ProfileEnums.SmokeTolerance;
import in.flatmate.profile.ProfileEnums.Social;
import in.flatmate.profile.ProfileEnums.WakeTime;
import in.flatmate.profile.ProfileEnums.WorkFromHome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit tests — no Spring, no database, no Docker.
 * Expected numbers are derived from the weights, so these double as a guard
 * against someone editing application.yml without re-checking the maths.
 */
class MatchScorerTest {

    private static final AppProperties.Weights WEIGHTS =
            new AppProperties.Weights(13, 13, 12, 11, 10, 8, 8, 8, 6, 6, 5);

    private static Profile base(long id) {
        Profile p = new Profile(id);
        p.setLookingFor(LookingFor.BOTH);
        p.setCityId(1);
        p.setAreaId(1);
        p.setBudgetBand(2);
        p.setMoveIn(MoveIn.TWO_WEEKS);
        p.setFlatmateGenderPref(FlatmateGenderPref.NO_PREF);
        p.setSleepTime(SleepTime.H2200_2300);
        p.setWakeTime(WakeTime.H0600_0800);
        p.setCleanliness(4);
        p.setGuests(Guests.SOMETIMES);
        p.setSocial(Social.BALANCED);
        p.setFood(Food.NONVEG);
        p.setCooking(Cooking.SOMETIMES);
        p.setWorkFromHome(WorkFromHome.NEVER);
        p.setSmoke(Smoke.I_DONT);
        p.setSmokeTolerance(SmokeTolerance.DONT_MIND);
        p.setDrink(Drink.NEVER);
        p.setDrinkTolerance(DrinkTolerance.DONT_MIND);
        p.setPets(Pets.NONE);
        p.setPetTolerance(PetTolerance.DONT_MIND);
        return p;
    }

    private MatchScorer scorer() {
        return new MatchScorer(WEIGHTS, (a, b) -> false);
    }

    @Test
    void identicalProfilesScore100() {
        Profile a = base(1);
        Profile b = base(2);

        MatchScorer.Result result = scorer().score(a, b);

        assertEquals(100, result.score());
        assertTrue(result.frictions().isEmpty(), "a perfect pair should have no frictions");
    }

    @Test
    void oneStepOfSleepCostsWeightTimesPointFour() {
        Profile a = base(1);
        Profile b = base(2);
        b.setSleepTime(SleepTime.H2300_0000);

        // sleep weight 13, value 0.60 -> loses 5.2 -> 94.8 -> 95
        assertEquals(95, scorer().score(a, b).score());
    }

    @Test
    void veganAndNonVegIsTheWeakestFoodPairing() {
        Profile a = base(1);
        Profile b = base(2);
        b.setFood(Food.VEGAN);

        // food weight 8, matrix value 0.30 -> loses 5.6 -> 94.4 -> 94
        assertEquals(94, scorer().score(a, b).score());
    }

    @Test
    void oneBudgetBandApartIsNotAFreePass() {
        Profile a = base(1);
        Profile b = base(2);
        b.setBudgetBand(3);

        // budget weight 11, value 0.60 -> loses 4.4 -> 95.6 -> 96
        assertEquals(96, scorer().score(a, b).score());
    }

    @Test
    void adjacentAreaScoresLowerThanSameArea() {
        Profile a = base(1);
        Profile b = base(2);
        b.setAreaId(2);

        MatchScorer sameOnly = new MatchScorer(WEIGHTS, (x, y) -> false);
        MatchScorer withAdjacency = new MatchScorer(WEIGHTS, (x, y) -> true);

        // area weight 12: same city, different area 0.45 -> 93.4 -> 93
        //                 adjacent 0.70 -> 96.4 -> 96
        assertEquals(93, sameOnly.score(a, b).score());
        assertEquals(96, withAdjacency.score(a, b).score());
    }

    @Test
    void theConstrainedSideDecidesTheScore() {
        Profile tolerant = base(1);          // doesn't mind a smoker
        Profile smoker = base(2);
        smoker.setSmoke(Smoke.I_DO);

        Profile intolerant = base(3);
        intolerant.setSmokeTolerance(SmokeTolerance.CANT);

        // The smoker is fine with a non-smoker: 100
        assertEquals(100.0, scorer().directionalScore(smoker, tolerant));

        // The intolerant non-smoker is not: smoking weight 6, value 0.00 -> 94
        assertEquals(94.0, scorer().directionalScore(intolerant, smoker));

        // Pair score takes the lower of the two
        assertEquals(94, scorer().score(intolerant, smoker).score());
    }

    @Test
    void badPairStillExplainsItself() {
        Profile a = base(1);
        Profile b = base(2);
        b.setCleanliness(1);
        b.setGuests(Guests.VERY_OFTEN);
        b.setSocial(Social.SOCIAL);

        MatchScorer.Result result = scorer().score(a, b);

        assertTrue(result.score() < 80, "expected a weak match, got " + result.score());
        assertFalse(result.frictions().isEmpty(), "weak matches must surface frictions");
        assertTrue(result.frictions().size() <= 2);
        assertTrue(result.reasons().size() <= 3);
    }

    @Test
    void weightsMustSumTo100() {
        AppProperties.Weights broken = new AppProperties.Weights(10, 10, 10, 10, 10, 10, 10, 10, 10, 9, 5);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new MatchScorer(broken, (a, b) -> false));
        assertTrue(ex.getMessage().contains("must sum to 100"));
    }
}
