package in.flatmate.profile;

import in.flatmate.common.ApiException;
import in.flatmate.profile.dto.ProfileDtos.ProfileRequest;
import in.flatmate.profile.dto.ProfileDtos.ProfileResponse;
import in.flatmate.reference.ReferenceRepository;
import in.flatmate.user.User;
import in.flatmate.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ProfileService {

    public static final String[] BUDGET_LABELS = {
            "₹5-8k", "₹8-12k", "₹12-15k", "₹15-20k",
            "₹20-25k", "₹25-30k", "₹30-40k", "₹40k+"
    };

    private final ProfileRepository profiles;
    private final UserRepository users;
    private final ReferenceRepository reference;

    public ProfileService(ProfileRepository profiles, UserRepository users, ReferenceRepository reference) {
        this.profiles = profiles;
        this.users = users;
        this.reference = reference;
    }

    @Transactional
    public ProfileResponse upsert(Long userId, ProfileRequest r) {
        if (!reference.areaBelongsToCity(r.areaId(), r.cityId())) {
            throw ApiException.badRequest("AREA_CITY_MISMATCH",
                    "areaId " + r.areaId() + " does not belong to cityId " + r.cityId());
        }

        Profile p = profiles.findById(userId).orElseGet(() -> new Profile(userId));

        p.setLookingFor(r.lookingFor());
        p.setCityId(r.cityId());
        p.setAreaId(r.areaId());
        p.setBudgetBand(r.budgetBand());
        p.setMoveIn(r.moveIn());
        p.setFlatmateGenderPref(r.flatmateGenderPref());
        p.setSleepTime(r.sleepTime());
        p.setWakeTime(r.wakeTime());
        p.setCleanliness(r.cleanliness());
        p.setGuests(r.guests());
        p.setSocial(r.social());
        p.setFood(r.food());
        p.setCooking(r.cooking());

        p.setWorkFromHome(r.workFromHome() != null ? r.workFromHome() : ProfileEnums.WorkFromHome.NEVER);
        p.setAboutMe(normalise(r.aboutMe()));
        p.setEducation(normalise(r.education()));
        p.setAge(r.age());
        p.setBirthplace(normalise(r.birthplace()));
        p.setOccupation(normalise(r.occupation()));
        p.setSmoke(r.smoke() != null ? r.smoke() : ProfileEnums.Smoke.I_DONT);
        p.setSmokeTolerance(r.smokeTolerance() != null ? r.smokeTolerance() : ProfileEnums.SmokeTolerance.DONT_MIND);
        p.setDrink(r.drink() != null ? r.drink() : ProfileEnums.Drink.NEVER);
        p.setDrinkTolerance(r.drinkTolerance() != null ? r.drinkTolerance() : ProfileEnums.DrinkTolerance.DONT_MIND);
        p.setPets(r.pets() != null ? r.pets() : ProfileEnums.Pets.NONE);
        p.setPetTolerance(r.petTolerance() != null ? r.petTolerance() : ProfileEnums.PetTolerance.DONT_MIND);

        if (p.isComplete() && p.getCompletedAt() == null) {
            p.setCompletedAt(LocalDateTime.now());
        }

        return toResponse(profiles.save(p));
    }

    @Transactional(readOnly = true)
    public Optional<ProfileResponse> find(Long userId) {
        return profiles.findById(userId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProfileResponse require(Long userId) {
        return find(userId).orElseThrow(() ->
                ApiException.notFound("PROFILE_NOT_FOUND", "Complete your profile first"));
    }

    private ProfileResponse toResponse(Profile p) {
        String displayName = users.findById(p.getUserId()).map(User::getName).orElse(null);
        return new ProfileResponse(
                p.getUserId(),
                displayName,
                p.getLookingFor(),
                p.getCityId(),
                reference.cityName(p.getCityId()).orElse(null),
                p.getAreaId(),
                reference.areaName(p.getAreaId()).orElse(null),
                p.getBudgetBand(),
                BUDGET_LABELS[p.getBudgetBand()],
                p.getMoveIn(),
                p.getFlatmateGenderPref(),
                p.getSleepTime(),
                p.getWakeTime(),
                p.getCleanliness(),
                p.getGuests(),
                p.getSocial(),
                p.getFood(),
                p.getCooking(),
                p.getWorkFromHome(),
                p.getAboutMe(),
                p.getEducation(),
                p.getAge(),
                p.getBirthplace(),
                p.getOccupation(),
                p.getSmoke(),
                p.getSmokeTolerance(),
                p.getDrink(),
                p.getDrinkTolerance(),
                p.getPets(),
                p.getPetTolerance(),
                p.isComplete());
    }

    private String normalise(String aboutMe) {
        if (aboutMe == null) {
            return null;
        }
        String trimmed = aboutMe.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
