package com.omfgdevelop.verificaiton.data.resolver.controller;

import com.omfgdevelop.verificaiton.data.resolver.dto.CompleteModel;
import com.omfgdevelop.verificaiton.data.resolver.service.CompleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CompleteController {

    private final CompleteService completeService;

    @PostMapping("/sender/complete")
    public String sendComplete(Model model, CompleteModel completeModel) {

        if (!completeService.getIsRunning().get()) {
            completeService.complete(completeModel.getIdsString(), completeModel.getUrl(), completeModel.getPort(), completeModel.getToken());
        }
        model.addAttribute("running", completeService.getIsRunning().get());
        model.addAttribute("success_count", completeService.getSuccess().get().size());
        model.addAttribute("failed_count", completeService.getSuccess().get().size());
        model.addAttribute("completed",completeService.getIsRunning().get());
        model.addAttribute("completeModel", completeModel);
        return "sender";
    }

    @PostMapping("/sender/check")
    public String check(Model model, CompleteModel completeModel) {
        List<String> success = new ArrayList<>();
        if (completeService.getSuccess() != null) {
            success = completeService.getSuccess().get().stream().map(String::valueOf).toList();
        }
        List<String> failed = new ArrayList<>();

        if (completeService.getFailed().get() != null) {
            failed = completeService.getFailed().get().stream().map(String::valueOf).toList();
        }
        completeModel.setSuccess(String.join(",", success));
        completeModel.setFailed(String.join(",", failed));
        model.addAttribute("running", completeService.getIsRunning().get());
        model.addAttribute("success_count", success.size());
        model.addAttribute("failed_count", failed.size());
        model.addAttribute("completed",completeService.getIsRunning().get());
        model.addAttribute("completeModel", completeModel);
        return "sender";
    }
}
