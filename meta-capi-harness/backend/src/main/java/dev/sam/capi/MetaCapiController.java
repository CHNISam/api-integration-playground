package dev.sam.capi;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
public class MetaCapiController {
    private final MetaCapiService service;

    public MetaCapiController(MetaCapiService service) { this.service = service; }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("configured", service.configured(), "mode", service.configured() ? "META_TEST_EVENTS" : "PAYLOAD_PREVIEW");
    }

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResult purchase(@Valid @RequestBody PurchaseRequest request) { return service.send(request); }

    @PostMapping("/purchases/{id}/resend")
    public DeliveryResult resend(@PathVariable long id) { return service.resend(id); }

    @GetMapping("/events")
    public List<DeliveryResult> events() { return service.recent(); }
}
