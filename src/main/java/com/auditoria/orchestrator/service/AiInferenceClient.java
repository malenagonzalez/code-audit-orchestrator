package com.auditoria.orchestrator.service;

import com.auditoria.orchestrator.dto.request.AuditRequest;
import com.auditoria.orchestrator.exception.AiServiceBadResponseException;
import com.auditoria.orchestrator.exception.AiServiceUnavailableException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiInferenceClient {

    private final WebClient aiServiceWebClient;

    public AiResponse analyze(AuditRequest request) {
        try {
            return aiServiceWebClient.post()
                    .uri("/analyze")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                        status -> status.value() == 502,
                        response -> response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(
                                new AiServiceBadResponseException("La IA devolvió una respuesta inválida: " + body)))
                    )
                    .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(
                                new AiServiceUnavailableException("El servicio de IA no está disponible: " + body)))
                    )
                    .bodyToMono(AiResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
        } catch (AiServiceBadResponseException | AiServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceUnavailableException(
                "No se pudo conectar al servicio de IA: " + e.getMessage());
        }
    }

    public record AiResponse(
            @JsonProperty("pedagogical_explanation") String pedagogicalExplanation,
            List<AiIssue> issues
    ) {}

    public record AiIssue(
            String severity,
            String category,
            String title,
            String description,
            @JsonProperty("line_start") Integer lineStart,
            @JsonProperty("line_end") Integer lineEnd,
            @JsonProperty("refactored_code") String refactoredCode
    ) {}
}
