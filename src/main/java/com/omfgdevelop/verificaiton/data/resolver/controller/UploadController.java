package com.omfgdevelop.verificaiton.data.resolver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omfgdevelop.verificaiton.data.resolver.dto.*;
import com.omfgdevelop.verificaiton.data.resolver.service.FileParseService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final String UPLOAD_DIR = "/tmp";

    private final FileParseService fileParseService;

    @GetMapping("/")
    public String homepage(Model model) {
        UploadPageModel uploadPageModel = new UploadPageModel();
        uploadPageModel.setIsOutput(false);
        model.addAttribute("uploadPageModel", uploadPageModel);
        model.addAttribute("resultModel", new ResultModel());
        return "index";

    }

    private List<Inout> inputs = new ArrayList<>();

    private List<Inout> outputs = new ArrayList<>();

    @PostMapping("/upload")
    public String uploadImage(UploadPageModel uploadPageModel, Model model, @RequestParam("image") MultipartFile uploadFile) throws IOException {
        if (uploadPageModel.getIsOutput() != null && Objects.equals(true, uploadPageModel.getIsOutput())) {
            outputs = fileParseService.processFile(outputs, uploadFile.getBytes(), uploadFile.getOriginalFilename());
        } else {
            inputs = fileParseService.processFile(inputs, uploadFile.getBytes(), uploadFile.getOriginalFilename());
        }

        updateModel(model);

        uploadPageModel.setIsOutput(!uploadPageModel.getIsOutput());
        uploadPageModel.setIsOutput(uploadPageModel.getIsOutputMulti() != null ? !uploadPageModel.getIsOutputMulti() : false);
        model.addAttribute("uploadPageModel", uploadPageModel);
        model.addAttribute("resultModel", new ResultModel());

        return "index";
    }

    @GetMapping("/user-update")
    public String userUpdater(Model model) {
        UserUpdaterModel userUpdaterModel = new UserUpdaterModel();
        model.addAttribute("userUpdaterModel", userUpdaterModel);
        return "user-update";
    }

    @PostMapping("/upload_many")
    public String uploadFiles(UploadPageModel uploadPageModel, Model model, @RequestParam("files") MultipartFile[] files) throws IOException {
        for (MultipartFile file : files) {

            if (uploadPageModel.getIsOutputMulti() != null && Objects.equals(true, uploadPageModel.getIsOutputMulti())) {
                outputs = fileParseService.processFile(outputs, file.getBytes(), file.getOriginalFilename());
            } else {
                inputs = fileParseService.processFile(inputs, file.getBytes(), file.getOriginalFilename());
            }
        }

        updateModel(model);

        uploadPageModel.setIsOutput(uploadPageModel.getIsOutput() != null ? !uploadPageModel.getIsOutput() : false);
        uploadPageModel.setIsOutputMulti(uploadPageModel.getIsOutputMulti() != null ? !uploadPageModel.getIsOutputMulti() : false);
        model.addAttribute("uploadPageModel", uploadPageModel);
        model.addAttribute("resultModel", new ResultModel());

        return "index";
    }

    @PostMapping("/process")
    public String processFiles(UploadPageModel uploadPageModel, Model model) throws JsonProcessingException {
        List<VerificationData> verificationDataList = new ArrayList<>();
        inputs.forEach(it -> {
            var data = new VerificationData();
            data.setInput(it.getData());
            verificationDataList.add(data);
        });
        for (int i = 0; i < outputs.size(); i++) {
            String data = outputs.get(i).getData();
            verificationDataList.get(i).setOutput(data);
        }

        ResultModel resultModel = new ResultModel();
        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(verificationDataList);
        resultModel.setResult(result);
        model.addAttribute("resultModel", resultModel);
        model.addAttribute("uploadPageModel", uploadPageModel);
        return "index";
    }


    @PostMapping("/reset")
    public String resetAll(Model model) {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        UploadPageModel uploadPageModel = new UploadPageModel();
        uploadPageModel.setIsOutput(false);
        model.addAttribute("uploadPageModel", uploadPageModel);
        model.addAttribute("resultModel", new ResultModel());
        return "index";
    }

    @PostMapping("/reset_last_input")
    public String resetLastInput(Model model) {
        if (inputs.size() > 0) {
            inputs.remove(inputs.size() - 1);
        }

        UploadPageModel uploadPageModel = new UploadPageModel();
        uploadPageModel.setIsOutput(false);

        updateModel(model);

        model.addAttribute("uploadPageModel", uploadPageModel);
        model.addAttribute("resultModel", new ResultModel());
        return "index";
    }

    @PostMapping("/reset_last_output")
    public String resetLastOutPut(Model model) {
        if (outputs.size() > 0) {
            outputs.remove(outputs.size() - 1);
        }

        UploadPageModel uploadPageModel = new UploadPageModel();
        uploadPageModel.setIsOutput(false);

        updateModel(model);

        model.addAttribute("uploadPageModel", uploadPageModel);
        model.addAttribute("resultModel", new ResultModel());
        return "index";
    }

    private Model updateModel(Model model) {
        StringBuilder fileNamesInputs = new StringBuilder();
        StringBuilder fileNamesOutputs = new StringBuilder();

        outputs.stream().forEach(it -> {
            fileNamesOutputs.append(it.getFilename());
            fileNamesOutputs.append("\n");
        });
        model.addAttribute("msg2", "Uploaded outputs " + fileNamesOutputs.toString());
        inputs.stream().forEach(it -> {
            fileNamesInputs.append(it.getFilename());
            fileNamesInputs.append("\n");
        });
        model.addAttribute("msg", "Uploaded inputs " + fileNamesInputs.toString());
        model.addAttribute("inputsSize", "Inputs size " + inputs.size());
        model.addAttribute("outputsSize", "Outputs size " + outputs.size());
        return model;
    }

    @PostMapping("/to_file")
    private void saveToFile(Model model, UploadPageModel uploadPageModel, HttpServletResponse response) throws IOException {

        List<VerificationData> verificationDataList = new ArrayList<>();
        inputs.forEach(it -> {
            var data = new VerificationData();
            data.setInput(it.getData());
            verificationDataList.add(data);
        });
        for (int i = 0; i < outputs.size(); i++) {
            String data = outputs.get(i).getData();
            verificationDataList.get(i).setOutput(data);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(verificationDataList);

        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename = data.json";
        response.setHeader(headerKey, headerValue);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(result.getBytes());
        outputStream.close();

    }
}
