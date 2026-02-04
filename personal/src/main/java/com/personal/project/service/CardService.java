package com.personal.project.service;

import com.personal.project.model.Card;
import com.personal.project.repository.CardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {
    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> getCards(String userId) {
        return userId == null ? cardRepository.findAll() : cardRepository.findByUserId(userId);
    }

    @Transactional
    public Card addCard(Card card) {
        return cardRepository.save(card);
    }

    @Transactional
    public Card updateCard(String id, Card update, String userId) {
        Card existing = findCardOrThrow(id, userId);

        if (update.getBrand() != null) existing.setBrand(update.getBrand());
        if (update.getLast4() != null) existing.setLast4(update.getLast4());
        if (update.getHolderName() != null) existing.setHolderName(update.getHolderName());
        if (update.getExpiry() != null) existing.setExpiry(update.getExpiry());
        if (update.getType() != null) existing.setType(update.getType());
        if (update.getNickname() != null) existing.setNickname(update.getNickname());
        if (update.getColor() != null) existing.setColor(update.getColor());
        existing.setDefault(update.isDefault());
        existing.setActive(update.isActive());
        if (update.getUserId() != null) existing.setUserId(update.getUserId());

        return cardRepository.save(existing);
    }

    @Transactional
    public void deleteCard(String id, String userId) {
        Card existing = findCardOrThrow(id, userId);
        cardRepository.delete(existing);
    }

    @Transactional
    public Card setDefaultCard(String id, String userId) {
        List<Card> cards = getCards(userId);
        Card selected = null;
        for (Card card : cards) {
            boolean makeDefault = card.getId() != null && card.getId().equals(id);
            card.setDefault(makeDefault);
            if (makeDefault) {
                selected = card;
            }
        }
        cardRepository.saveAll(cards);
        if (selected == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found");
        }
        return selected;
    }

    @Transactional
    public Card toggleActive(String id, String userId) {
        Card existing = findCardOrThrow(id, userId);
        existing.setActive(!existing.isActive());
        return cardRepository.save(existing);
    }

    private Card findCardOrThrow(String id, String userId) {
        Optional<Card> existing = userId == null
                ? cardRepository.findById(id)
                : cardRepository.findByIdAndUserId(id, userId);
        return existing.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
    }
}
