package com.omfgdevelop.verificaiton.data.resolver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omfgdevelop.verificaiton.data.resolver.dto.CompletedTestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteService {

    private AtomicReference<List<Long>> success = new AtomicReference<>(new ArrayList<>());

    private AtomicReference<List<Long>> failed = new AtomicReference<>(new ArrayList<>());

    private CompletteThread thread = null;

    private AtomicReference<String> error = new AtomicReference<>("");

    public AtomicBoolean getIsRunning() {
        return isRunning;
    }

    private final AtomicBoolean isRunning = new AtomicBoolean();

    public Boolean complete(String idsString, String url, String port, String token) {
        List<Long> ids = Arrays.stream(idsString.split(",")).map(String::trim).map(Long::parseLong).toList();
        if (isRunning.get()) return false;
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        success = new AtomicReference<>(new ArrayList<>());
        failed = new AtomicReference<>(new ArrayList<>());
        error = new AtomicReference<>(new String());


        thread = new CompletteThread("complete_thread", ids, url, port, restTemplate, objectMapper, token);

        thread.start();
        return isRunning.get();
    }

    public AtomicReference<List<Long>> getSuccess() {
        return success;
    }

    public AtomicReference<List<Long>> getFailed() {
        return failed;
    }

    private class CompletteThread extends Thread {


        private final List<Long> ids;
        private final String url;
        private final String port;

        private final RestTemplate restTemplate;

        private final ObjectMapper objectMapper;

        private final String token;


        public CompletteThread(String name, List<Long> ids, String url, String port, RestTemplate restTemplate, ObjectMapper objectMapper, String token) {
            super(name);
            this.ids = ids;
            this.url = url;
            this.port = port;
            this.restTemplate = restTemplate;
            this.objectMapper = objectMapper;
            this.token = token;
        }

        @Override
        public void run() {
            super.run();
            isRunning.set(true);
            StringBuilder stringBuilder = new StringBuilder();
            try {

                var headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("Authorization", "Bearer " + token);

                for (int i = 0; i < ids.size(); i++) {
                    String payLoad = null;
                    try {
                        payLoad = objectMapper.writeValueAsString(ids.get(i));
                    } catch (JsonProcessingException e) {
                        log.error("Ошибка парсинга набора идентификаторов");
                    }
                    var entity = new HttpEntity<>(headers);
                    var uri = UriComponentsBuilder.fromUri(URI.create(url)).port(port).path(payLoad + "/complete").build().toUri();

                    try {
                        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
                        switch (response.getStatusCode().value()) {
                            case 200, 201 -> {
                                success.get().add(ids.get(i));
                                log.info("Completed test {}", ids.get(i));
                                stringBuilder.append("Completed test " + ids.get(i) + "\n");
                            }
                            default -> {
                                log.error("Error complete test {}", ids.get(i));
                                log.error("Error text {}", response.getBody());
                                failed.get().add(ids.get(i));
                                stringBuilder.append("Error complete test " + ids.get(i) + "\n");
                            }
                        }

                    } catch (Exception e) {
                        failed.get().add(ids.get(i));
                        stringBuilder.append("Error complete test " + ids.get(i) + "\n");
                        e.printStackTrace();
                    }


                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                log.error("Error on complete thread ", e);
                e.printStackTrace();
            } finally {
                isRunning.set(false);
                try {
                    BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("." + System.currentTimeMillis())));
                    bwr.write(stringBuilder.toString());
                    bwr.flush();
                    bwr.close();
                } catch (IOException e) {
                }
                log.info("Completed finished ");
                log.info("Success {}", String.join(",", success.get().stream().map(String::valueOf).toList()));
                log.info("Failed {}", String.join(",", success.get().stream().map(String::valueOf).toList()));
            }
            isRunning.set(false);

        }


    }

    public void killThread() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals("complete_thread")) {
                t.notify();
                isRunning.set(false);
            }
        }
    }
}
