package com.personal.project.controller;

import com.personal.project.model.Card;
import com.personal.project.service.CardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public List<Card> getCards(@RequestParam(required = false) String userId) {
        return cardService.getCards(userId);
    }

    @PostMapping
    public Card createCard(@RequestBody Card card) {
        return cardService.addCard(card);
    }

    @PutMapping("/{id}")
    public Card updateCard(@PathVariable String id,
                           @RequestBody Card card,
                           @RequestParam(required = false) String userId) {
        return cardService.updateCard(id, card, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable String id, @RequestParam(required = false) String userId) {
        cardService.deleteCard(id, userId);
    }

    @PostMapping("/{id}/default")
    public Card setDefaultCard(@PathVariable String id, @RequestParam(required = false) String userId) {
        return cardService.setDefaultCard(id, userId);
    }

    @PostMapping("/{id}/toggle-active")
    public Card toggleActive(@PathVariable String id, @RequestParam(required = false) String userId) {
        return cardService.toggleActive(id, userId);
    }
}
