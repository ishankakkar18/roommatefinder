package in.flatmate.match;

import java.util.List;

/** One card in the match list. The score is always explainable. */
public record MatchCard(
        Long userId,
        String displayName,
        int score,
        Integer areaId,
        String areaName,
        Integer age,
        Integer budgetBand,
        String budgetLabel,
        String aboutMe,
        List<String> reasons,
        List<String> frictions
) {
}
