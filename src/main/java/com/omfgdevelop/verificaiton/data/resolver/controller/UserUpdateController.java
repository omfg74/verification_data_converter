package com.omfgdevelop.verificaiton.data.resolver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omfgdevelop.verificaiton.data.resolver.dto.*;
import com.omfgdevelop.verificaiton.data.resolver.service.UserPatchService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserUpdateController {

    private final ObjectMapper objectMapper;

    private final UserPatchService userPatchService;

    @PostMapping("/upload-csv")
    public String uploadImage(UserUpdaterModel userUpdaterModel, Model model, @RequestParam("image") MultipartFile uploadFile) throws IOException {
        String filestring = new String(uploadFile.getBytes());
        List<String> mshList = Arrays.stream(filestring.split("\n")).toList();
        List<UserPatchDto> userPatchDtoList = new ArrayList<>();
        mshList.stream().forEach(it -> {
            UserPatchDto userPatchDto = new UserPatchDto();
            List<String> strl = Arrays.stream(it.split(";")).toList();
            userPatchDto.setPersonId(strl.get(0));
            userPatchDto.setExternalId(Long.parseLong(strl.get(1)));
            userPatchDtoList.add(userPatchDto);
        });

        String userString = objectMapper.writeValueAsString(userPatchDtoList);
        userUpdaterModel = new UserUpdaterModel();
        userUpdaterModel.setResult(userString);
        model.addAttribute("userUpdaterModel", userUpdaterModel);
        return "user-update";
    }

    @GetMapping("/verification-data")
    public String verificationDataController(Model model) {
        UploadPageModel uploadPageModel = new UploadPageModel();
        ResultModel resultModel = new ResultModel();
        model.addAttribute("uploadPageModel", uploadPageModel);
        model.addAttribute("resultModel", resultModel);
        return "index";
    }

    @GetMapping("/sender")
    public String completeController(Model model) {
        CompleteModel completeModel = new CompleteModel();
        model.addAttribute("success_count", 0);
        model.addAttribute("failed_count", 0);
        model.addAttribute("success", new ArrayList<>());
        model.addAttribute("error","");
        model.addAttribute("failed", new ArrayList<>());
        model.addAttribute("completeModel", completeModel);
        return "sender";
    }

    @PostMapping("/patch")
    private String patchUserPersonId(UserUpdaterModel userUpdaterModel, Model model) throws JsonProcessingException {

        try {

            UpdateStatsDto updateStatsDto = userPatchService.patch(objectMapper.readValue(userUpdaterModel.getResult(), new TypeReference<List<UserPatchDto>>() {
            }), userUpdaterModel.getUrl(), userUpdaterModel.getToken(), userUpdaterModel.getPort());

            if (updateStatsDto != null) {
                model.addAttribute("success", updateStatsDto.getUpdatedCount());
                model.addAttribute("failed", updateStatsDto.getFailedCount());
                model.addAttribute("error", "");
                model.addAttribute("stack_trace", "");
            }

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("stack_trace", ExceptionUtils.getStackTrace(e));
        }

        return "user-update";
    }


}
