package in.flatmate.profile.dto;

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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ProfileDtos {

    private ProfileDtos() {
    }

    /**
     * Fields 4-16 of the spec are required. The tolerance answers (19-21) and
     * work-from-home are optional and default to the most permissive value,
     * so a user can finish onboarding without answering all 19 questions.
     */
    public record ProfileRequest(
            @NotNull LookingFor lookingFor,
            @NotNull Integer cityId,
            @NotNull Integer areaId,
            @NotNull @Min(0) @Max(7) Integer budgetBand,
            @NotNull MoveIn moveIn,
            @NotNull FlatmateGenderPref flatmateGenderPref,
            @NotNull SleepTime sleepTime,
            @NotNull WakeTime wakeTime,
            @NotNull @Min(1) @Max(5) Integer cleanliness,
            @NotNull Guests guests,
            @NotNull Social social,
            @NotNull Food food,
            @NotNull Cooking cooking,

            WorkFromHome workFromHome,
            @Size(max = 200) String aboutMe,
            @Size(max = 80) String education,
            @Min(16) @Max(99) Integer age,
            @Size(max = 80) String birthplace,
            @Size(max = 80) String occupation,
            Smoke smoke,
            SmokeTolerance smokeTolerance,
            Drink drink,
            DrinkTolerance drinkTolerance,
            Pets pets,
            PetTolerance petTolerance
    ) {
    }

    public record ProfileResponse(
            Long userId,
            String displayName,
            LookingFor lookingFor,
            Integer cityId,
            String cityName,
            Integer areaId,
            String areaName,
            int budgetBand,
            String budgetLabel,
            MoveIn moveIn,
            FlatmateGenderPref flatmateGenderPref,
            SleepTime sleepTime,
            WakeTime wakeTime,
            int cleanliness,
            Guests guests,
            Social social,
            Food food,
            Cooking cooking,
            WorkFromHome workFromHome,
            String aboutMe,
            String education,
            Integer age,
            String birthplace,
            String occupation,
            Smoke smoke,
            SmokeTolerance smokeTolerance,
            Drink drink,
            DrinkTolerance drinkTolerance,
            Pets pets,
            PetTolerance petTolerance,
            boolean complete
    ) {
    }
}
