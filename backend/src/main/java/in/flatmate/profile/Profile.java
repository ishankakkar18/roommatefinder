package in.flatmate.profile;

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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** One onboarding profile per user. Shares the users.id as its primary key. */
@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "looking_for", nullable = false, length = 24)
    private LookingFor lookingFor;

    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    @Column(name = "area_id", nullable = false)
    private Integer areaId;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "budget_band", nullable = false)
    private int budgetBand;

    @Enumerated(EnumType.STRING)
    @Column(name = "move_in", nullable = false, length = 16)
    private MoveIn moveIn;

    @Enumerated(EnumType.STRING)
    @Column(name = "flatmate_gender_pref", nullable = false, length = 16)
    private FlatmateGenderPref flatmateGenderPref;

    @Enumerated(EnumType.STRING)
    @Column(name = "sleep_time", nullable = false, length = 16)
    private SleepTime sleepTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "wake_time", nullable = false, length = 16)
    private WakeTime wakeTime;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private int cleanliness;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Guests guests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Social social;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Food food;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Cooking cooking;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_from_home", nullable = false, length = 16)
    private WorkFromHome workFromHome = WorkFromHome.NEVER;

    @Column(name = "about_me", length = 200)
    private String aboutMe;

    @Column(length = 80)
    private String education;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column
    private Integer age;

    @Column(length = 80)
    private String birthplace;

    @Column(length = 80)
    private String occupation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Smoke smoke = Smoke.I_DONT;

    @Enumerated(EnumType.STRING)
    @Column(name = "smoke_tolerance", nullable = false, length = 16)
    private SmokeTolerance smokeTolerance = SmokeTolerance.DONT_MIND;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Drink drink = Drink.NEVER;

    @Enumerated(EnumType.STRING)
    @Column(name = "drink_tolerance", nullable = false, length = 16)
    private DrinkTolerance drinkTolerance = DrinkTolerance.DONT_MIND;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Pets pets = Pets.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_tolerance", nullable = false, length = 16)
    private PetTolerance petTolerance = PetTolerance.DONT_MIND;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Profile() {
    }

    public Profile(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public LookingFor getLookingFor() {
        return lookingFor;
    }

    public void setLookingFor(LookingFor lookingFor) {
        this.lookingFor = lookingFor;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

    public int getBudgetBand() {
        return budgetBand;
    }

    public void setBudgetBand(int budgetBand) {
        this.budgetBand = budgetBand;
    }

    public MoveIn getMoveIn() {
        return moveIn;
    }

    public void setMoveIn(MoveIn moveIn) {
        this.moveIn = moveIn;
    }

    public FlatmateGenderPref getFlatmateGenderPref() {
        return flatmateGenderPref;
    }

    public void setFlatmateGenderPref(FlatmateGenderPref flatmateGenderPref) {
        this.flatmateGenderPref = flatmateGenderPref;
    }

    public SleepTime getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(SleepTime sleepTime) {
        this.sleepTime = sleepTime;
    }

    public WakeTime getWakeTime() {
        return wakeTime;
    }

    public void setWakeTime(WakeTime wakeTime) {
        this.wakeTime = wakeTime;
    }

    public int getCleanliness() {
        return cleanliness;
    }

    public void setCleanliness(int cleanliness) {
        this.cleanliness = cleanliness;
    }

    public Guests getGuests() {
        return guests;
    }

    public void setGuests(Guests guests) {
        this.guests = guests;
    }

    public Social getSocial() {
        return social;
    }

    public void setSocial(Social social) {
        this.social = social;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public Cooking getCooking() {
        return cooking;
    }

    public void setCooking(Cooking cooking) {
        this.cooking = cooking;
    }

    public WorkFromHome getWorkFromHome() {
        return workFromHome;
    }

    public void setWorkFromHome(WorkFromHome workFromHome) {
        this.workFromHome = workFromHome;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getBirthplace() {
        return birthplace;
    }

    public void setBirthplace(String birthplace) {
        this.birthplace = birthplace;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public Smoke getSmoke() {
        return smoke;
    }

    public void setSmoke(Smoke smoke) {
        this.smoke = smoke;
    }

    public SmokeTolerance getSmokeTolerance() {
        return smokeTolerance;
    }

    public void setSmokeTolerance(SmokeTolerance smokeTolerance) {
        this.smokeTolerance = smokeTolerance;
    }

    public Drink getDrink() {
        return drink;
    }

    public void setDrink(Drink drink) {
        this.drink = drink;
    }

    public DrinkTolerance getDrinkTolerance() {
        return drinkTolerance;
    }

    public void setDrinkTolerance(DrinkTolerance drinkTolerance) {
        this.drinkTolerance = drinkTolerance;
    }

    public Pets getPets() {
        return pets;
    }

    public void setPets(Pets pets) {
        this.pets = pets;
    }

    public PetTolerance getPetTolerance() {
        return petTolerance;
    }

    public void setPetTolerance(PetTolerance petTolerance) {
        this.petTolerance = petTolerance;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isComplete() {
        return lookingFor != null
                && cityId != null
                && areaId != null
                && moveIn != null
                && flatmateGenderPref != null
                && sleepTime != null
                && wakeTime != null
                && guests != null
                && social != null
                && food != null
                && cooking != null
                && cleanliness >= 1;
    }
}
