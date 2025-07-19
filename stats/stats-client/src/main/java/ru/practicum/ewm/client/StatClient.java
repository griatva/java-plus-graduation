package ru.practicum.ewm.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
public class StatClient {

    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId = "stats-server";
    private final WebClient webClient = WebClient.create();

    private ServiceInstance getInstance() {
        return retryTemplate.execute(ctx -> discoveryClient
                .getInstances(statsServiceId)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("stats-server not found via Discovery")));
    }

    private URI makeUri(String path) {
        ServiceInstance instance = getInstance();
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    public void sendHit(EndpointHitDto hitDto) {
        URI uri = makeUri("/hit");

        webClient.post()
                .uri(uri)
                .bodyValue(hitDto)
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("unique", unique);

        if (start != null && !start.isBlank()) {
            uriBuilder.queryParam("start", start);
        }
        if (end != null && !end.isBlank()) {
            uriBuilder.queryParam("end", end);
        }
        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", uris.toArray());
        }

        String pathWithParams = uriBuilder.build().encode().toUriString();
        URI uri = makeUri(pathWithParams);

        ViewStatsDto[] response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ViewStatsDto[].class)
                .block();

        return response != null ? List.of(response) : List.of();
    }
}