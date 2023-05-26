package com.omfgdevelop.verificaiton.data.resolver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omfgdevelop.verificaiton.data.resolver.dto.UserPatchDto;
import com.omfgdevelop.verificaiton.data.resolver.dto.UserUpdaterModel;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.TypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserUpdateController {

    private final ObjectMapper objectMapper;

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


}
