package br.com.lhmatos.testetecnico.repository;

import br.com.lhmatos.testetecnico.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {}