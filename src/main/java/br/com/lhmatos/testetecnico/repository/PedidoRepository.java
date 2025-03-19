package br.com.lhmatos.testetecnico.repository;

import br.com.lhmatos.testetecnico.dto.TicketMedioDTO;
import br.com.lhmatos.testetecnico.dto.TopUsuarioDTO;
import br.com.lhmatos.testetecnico.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    List<Pedido> findByUsuarioEmail(String usuarioId);

    @Query(value = """
    SELECT u.id AS usuarioId, u.email, SUM(p.valor_total) AS totalGasto
    FROM pedido p
    JOIN usuario u ON p.usuario_id = u.id
    WHERE p.status NOT IN ('PENDENTE' OR 'PROCESSANDO')
    GROUP BY u.id, u.email
    ORDER BY totalGasto DESC
    LIMIT 5
    """, nativeQuery = true)
    List<TopUsuarioDTO> findTop5UsuariosByPedidos();

    @Query(value = """
    SELECT u.id AS usuarioId, u.email, AVG(p.valor_total) AS ticketMedio
    FROM pedido p
    JOIN usuario u ON p.usuario_id = u.id
    GROUP BY u.id, u.email
    """, nativeQuery = true)
    List<TicketMedioDTO> findTicketMedioPorUsuario();

    @Query(value = """
    SELECT COALESCE(SUM(p.valor_total), 0) AS valorTotal
    FROM pedido p
    WHERE p.status NOT IN ('PENDENTE' OR 'PROCESSANDO')
    AND YEAR(p.data_criacao) = :ano
    AND MONTH(p.data_criacao) = :mes
    """, nativeQuery = true)
    BigDecimal calcularFaturamentoMensal(@Param("ano") int ano, @Param("mes") int mes);
}
