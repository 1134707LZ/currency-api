package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.dtos.CurrencyResponse;
import br.edu.atitus.currencyapi.repositories.CurrencyRepository;
import br.edu.atitus.currencyapi.services.CurrencyService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrencyServiceJpa implements CurrencyService {

    private final CurrencyRepository repository;

    public CurrencyServiceJpa(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public CurrencyResponse findBySourceCurrencyAndTargetCurrency(
            String sourceCurrency,
            String targetCurrency
    ) throws Exception {
        String source = sourceCurrency.trim().toUpperCase();
        String target = targetCurrency.trim().toUpperCase();

        var currency = repository.findBySourceCurrencyAndTargetCurrency(source, target)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cotação não encontrada para " + source + " -> " + target));

        String environment = "Currency API running in Port: " + serverPort;
        return CurrencyResponse.fromEntity(currency, environment);
    }
}
