package odk.malicompetences.backend.dev;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/dev", "/dev"})
@Profile("dev")
public class DevOtpController {

    private final DevOtpCache devOtpCache;

    public DevOtpController(DevOtpCache devOtpCache) {
        this.devOtpCache = devOtpCache;
    }

    @GetMapping("/otp")
    public Map<String, String> getOtp(@RequestParam("destinataire") String destinataire) {
        String code = devOtpCache.getDernierOtp(destinataire)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun code OTP trouvé pour " + destinataire));
        return Map.of(
                "destinataire", destinataire,
                "code", code
        );
    }
}

