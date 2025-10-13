package br.com.oficinas.gestaooficinas.service;

import br.com.oficinas.gestaooficinas.domain.CategoriaItem;
import br.com.oficinas.gestaooficinas.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class RelatorioService {

    private final AgendamentoRepository agendamentoRepository;

    // Fatores simples (kg CO2eq evitados por item concluído)
    // Ajuste aqui se quiser calibrar valores
    private static final Map<CategoriaItem, Double> FATORES_KG = Map.of(
            CategoriaItem.ELETRODOMESTICOS, 25.0,
            CategoriaItem.BICICLETAS,      15.0,
            CategoriaItem.ROUPAS,           5.0
    );

    public RelatorioService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional(readOnly = true)
    public ImpactoAmbientalRelatorio gerarImpactoAmbiental(Long oficinaId, LocalDate inicio, LocalDate fim) {
        LocalDateTime dtInicio = inicio.atStartOfDay();
        LocalDateTime dtFim = LocalDateTime.of(fim, LocalTime.MAX);

        List<Object[]> linhas = agendamentoRepository.contagemConcluidosPorCategoria(oficinaId, dtInicio, dtFim);

        List<LinhaCategoria> categorias = new ArrayList<>();
        double totalKg = 0.0;
        long totalItens = 0;

        for (Object[] row : linhas) {
            CategoriaItem cat = (CategoriaItem) row[0];
            long qtd = (Long) row[1];
            double fator = FATORES_KG.getOrDefault(cat, 0.0);
            double kg = qtd * fator;

            categorias.add(new LinhaCategoria(cat, qtd, fator, kg));
            totalKg += kg;
            totalItens += qtd;
        }

        categorias.sort(Comparator.comparing(LinhaCategoria::categoria));

        return new ImpactoAmbientalRelatorio(oficinaId, inicio, fim, categorias, totalItens, totalKg);
    }

    // ===== DTOs simples =====

    public record LinhaCategoria(CategoriaItem categoria, long quantidadeConcluidos, double fatorKgPorItem, double kgEvitados) {}

    public record ImpactoAmbientalRelatorio(
            Long oficinaId,
            LocalDate inicio,
            LocalDate fim,
            List<LinhaCategoria> linhas,
            long totalItens,
            double totalKg
    ) {}
}
