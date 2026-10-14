package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.dtos.CurrencyResponse;
import br.edu.atitus.currencyapi.repositories.CurrencyRepository;
import br.edu.atitus.currencyapi.services.CurrencyService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrencyServiceJpa implements CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyServiceJpa.class);

    private final CurrencyRepository repository;
    private final BcbClientService bcbClientService;

    public CurrencyServiceJpa(CurrencyRepository repository, BcbClientService bcbClientService) {
        this.repository = repository;
        this.bcbClientService = bcbClientService;
    }

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.bcb.data-cotacao:10-09-2026}")
    private String dataCotacao;

    @Override
    public CurrencyResponse findBySourceCurrencyAndTargetCurrency(
            String sourceCurrency,
            String targetCurrency
    ) throws Exception {
        String source = sourceCurrency.trim().toUpperCase();
        String target = targetCurrency.trim().toUpperCase();
        String environment = "Currency API running in Port: " + serverPort;

        // 1) Tenta a API do Banco Central
        Double conversionRate = null;
        try {
            conversionRate = getRateFromBcb(source, target);
        } catch (Exception e) {
            log.warn("Falha ao consultar o BCB ({} -> {}): {}", source, target, e.getMessage());
        }

        if (conversionRate != null) {
            log.info("Cotação {} -> {} obtida no BCB: {}", source, target, conversionRate);
            return new CurrencyResponse(source, target, conversionRate, environment);
        }

        // 2) Fallback: banco de dados local (404 se também não existir)
        log.info("Usando fallback do banco local para {} -> {}", source, target);
        var currency = repository.findBySourceCurrencyAndTargetCurrency(source, target)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cotação não encontrada para " + source + " -> " + target));

        return CurrencyResponse.fromEntity(currency, environment);
    }

    /**
     * O PTAX informa quanto 1 unidade da moeda vale em BRL.
     * Taxa source -> target = (source em BRL) / (target em BRL).
     * Retorna null se o BCB não tiver cotação para alguma das moedas.
     */
    private Double getRateFromBcb(String source, String target) {
        Double sourceInBrl = toBrl(source);
        Double targetInBrl = toBrl(target);
        if (sourceInBrl == null || targetInBrl == null) {
            return null;
        }
        return sourceInBrl / targetInBrl;
    }

    private Double toBrl(String currency) {
        if ("BRL".equals(currency)) {
            return 1.0;
        }
        return bcbClientService.getCotacao(currency, dataCotacao);
    }
}
