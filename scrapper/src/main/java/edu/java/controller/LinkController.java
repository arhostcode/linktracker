package edu.java.controller;

import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.service.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/links", consumes = "application/json", produces = "application/json")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @Operation(summary = "Получить все отслеживаемые ссылки")
    @GetMapping
    public ListLinksResponse listLinks(@RequestHeader(name = "Tg-Chat-Id") Long tgChatId) {
        return linkService.listLinks(tgChatId);
    }

    @Operation(summary = "Добавить отслеживание ссылки")
    @PostMapping
    public LinkResponse addLink(
        @RequestHeader(name = "Tg-Chat-Id") Long tgChatId,
        @RequestBody @Valid AddLinkRequest addLinkRequest
    ) {
        return linkService.addLink(addLinkRequest.link(), tgChatId);
    }

    @Operation(summary = "Убрать отслеживание ссылки")
    @DeleteMapping
    public LinkResponse removeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long tgChatId,
        @RequestBody @Valid RemoveLinkRequest addLinkRequest
    ) {
        return linkService.removeLink(addLinkRequest.id(), tgChatId);
    }
}
