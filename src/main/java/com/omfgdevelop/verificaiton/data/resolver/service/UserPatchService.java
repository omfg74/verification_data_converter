package com.omfgdevelop.verificaiton.data.resolver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omfgdevelop.verificaiton.data.resolver.dto.UpdateStatsDto;
import com.omfgdevelop.verificaiton.data.resolver.dto.UserPatchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPatchService {


    public UpdateStatsDto patch(List<UserPatchDto> dto, String url, String token, String port) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();


        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + token);

        String payLoad = objectMapper.writeValueAsString(dto);
        HttpEntity<?> entity = new HttpEntity<>(payLoad, headers);
        var uri = UriComponentsBuilder.fromUri(URI.create(url)).port(port).pathSegment("").build().toUri();

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);
        UpdateStatsDto updateStatsDto = switch (response.getStatusCode().value()) {
            case 200, 201 -> objectMapper.readValue(response.getBody(), UpdateStatsDto.class);
            default -> throw new Exception(response.getStatusCode() + "\n " + response.getBody());
        };

        return updateStatsDto;
    }
}
