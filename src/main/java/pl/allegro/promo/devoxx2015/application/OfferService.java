package pl.allegro.promo.devoxx2015.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.allegro.promo.devoxx2015.domain.Offer;
import pl.allegro.promo.devoxx2015.domain.OfferRepository;
import pl.allegro.promo.devoxx2015.domain.PhotoScoreSource;

@Component
public class OfferService {

    private static final double MINIMAL_ACCEPTANCE_SCORE = 0.7;

    private final OfferRepository offerRepository;
    private final PhotoScoreSource photoScoreSource;

    @Autowired
    public OfferService(OfferRepository offerRepository, PhotoScoreSource photoScoreSource) {
        this.offerRepository = offerRepository;
        this.photoScoreSource = photoScoreSource;
    }

    public void processOffers(List<OfferPublishedEvent> events) {

        Iterable<Offer> offers = events.stream()
                .map(this::findOffer)
                .filter(this::isPretty)
                .collect(Collectors.toList());

        offerRepository.save(offers);
    }

    private Offer findOffer(OfferPublishedEvent e) {
        double score = getScore(e.getPhotoUrl());
        return new Offer(e.getId(), e.getTitle(), e.getPhotoUrl(), score);
    }

    private boolean isPretty(Offer offer) {
        return  offer.getPhotoScore() >= MINIMAL_ACCEPTANCE_SCORE;
    }

    private double getScore(String photoUrl) {
        try {
            return  photoScoreSource.getScore(photoUrl);
        } catch (Exception ex) {
            return MINIMAL_ACCEPTANCE_SCORE;
        }
    }

    public List<Offer> getOffers() {
        return offerRepository.findAll()
                .stream()
                .sorted(OfferService::byPhotoScoreDescending)
                .collect(Collectors.toList());
    }

    public static int byPhotoScoreDescending(Offer o1, Offer o2) {
        return Double.compare(o2.getPhotoScore(), o1.getPhotoScore());
    }


}
