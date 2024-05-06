package com.vmware.springai.controller;

import com.vmware.springai.dto.PromptDTO;
import com.vmware.springai.model.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Controller
@RequestMapping("/ai")
public class AIController {

    private OllamaChatClient client;

    public AIController(OllamaChatClient client) {
        this.client = client;
    }

    @GetMapping("/index")
    public String index(Model model) {
        PromptDTO promptDTO = new PromptDTO();
        model.addAttribute("promptReq", promptDTO);
        return "index";
    }

    // Reference kept for direct access and pass prompt in the request
    // Example : http://localhost:8086/ai/generate?prompt=hi
    @GetMapping("/generate")
    public Flux<String> promptResponse(@RequestParam("prompt") String prompt) {
        return client.stream(prompt);
    }

    @PostMapping("/generate")
    public String displayPromptResponse(@ModelAttribute("promptReq") PromptDTO promptDTO,
                                        BindingResult bindingResult,
                                        Model model) {
        Flux<String> response =  client.stream(promptDTO.getPrompt());
        if (bindingResult.hasErrors()) {
            model.addAttribute("promptReq", promptDTO.getPrompt());
            return "/index";
        }

        List<String> resList = response.collectList().block();
        if (resList == null || resList.isEmpty()) {
            model.addAttribute("promptReq", promptDTO.getPrompt());
            return "/index";
        }

        String responseMessage = String.join(" ", resList);
        model.addAttribute("promptResponse", responseMessage);

        return "/index";
    }
}
