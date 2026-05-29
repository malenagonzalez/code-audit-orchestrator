package com.codeaudit.orchestrator.service;

import com.codeaudit.orchestrator.dto.request.AuditRequest;
import com.codeaudit.orchestrator.exception.AiServiceBadResponseException;
import com.codeaudit.orchestrator.exception.AiServiceUnavailableException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiInferenceClient {

    private final WebClient aiServiceWebClient;

    public AiResponse analyze(AuditRequest request) {
        try {
            AiResponse response = aiServiceWebClient.post()
                    .uri("/analyze")
                    .bodyValue(new AnalyzeRequest(request.code(), request.language()))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            res -> Mono.error(new AiServiceBadResponseException(
                                    "El servicio de análisis devolvió un error: " + res.statusCode())))
                    .bodyToMono(AiResponse.class)
                    .block();

            if (response == null) {
                throw new AiServiceBadResponseException("El servicio de análisis no devolvió datos");
            }
            return response;

        } catch (WebClientRequestException ex) {
            log.error("No se pudo conectar al servicio de análisis: {}", ex.getMessage());
            throw new AiServiceUnavailableException("El servicio de análisis no está disponible");
        }
    }

    private record AnalyzeRequest(String code, String language) {}

    public record AiResponse(
            List<AiIssue> issues,
            @JsonProperty("pedagogical_explanation") String pedagogicalExplanation
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
