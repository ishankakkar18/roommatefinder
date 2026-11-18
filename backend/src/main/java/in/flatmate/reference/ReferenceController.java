package in.flatmate.reference;

import in.flatmate.reference.ReferenceRepository.AreaRef;
import in.flatmate.reference.ReferenceRepository.CityRef;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    private final ReferenceRepository reference;

    public ReferenceController(ReferenceRepository reference) {
        this.reference = reference;
    }

    @GetMapping("/cities")
    public List<CityRef> cities() {
        return reference.cities();
    }

    @GetMapping("/areas")
    public List<AreaRef> areas(@RequestParam Integer cityId) {
        return reference.areas(cityId);
    }
}
