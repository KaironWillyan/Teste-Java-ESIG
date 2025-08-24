package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.esig.application.service.PessoaService;
import br.com.esig.domain.enums.TipoVencimento;
import br.com.esig.domain.models.Cargo;
import br.com.esig.domain.models.CargoVencimento;
import br.com.esig.domain.models.Pessoa;
import br.com.esig.domain.models.PessoaSalarioConsolidado;
import br.com.esig.domain.models.Vencimento;
import br.com.esig.domain.repositories.CargoVencimentoRepository;
import br.com.esig.domain.repositories.PessoaRepository;
import br.com.esig.domain.repositories.PessoaSalarioConsolidadoRepository;

@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock private CargoVencimentoRepository cargoVencimentoRepository;
    @Mock private PessoaRepository pessoaRepository;
    @Mock private PessoaSalarioConsolidadoRepository consolidadoRepository;

    @Mock private EntityManager manager;
    @Mock private EntityTransaction transaction;

    @InjectMocks
    private PessoaService pessoaService;

    private static Vencimento venc(TipoVencimento tipo, String valor) {
        Vencimento v = new Vencimento();
        v.setTipo(tipo);
        v.setValor(new BigDecimal(valor));
        return v;
    }

    private static Cargo cargo(Long id, String nome) {
        Cargo c = new Cargo();
        c.setId(id);
        c.setNome(nome);
        return c;
    }

    private static Pessoa pessoa(Long id, String nome, Cargo cargo) {
        Pessoa p = new Pessoa();
        p.setId(id);
        p.setNome(nome);
        p.setCargo(cargo);
        return p;
    }

    @Test
    void deveCalcularSalarioSomandoCreditosESubtraindoDebitos() {
        Cargo dev = cargo(1L, "Desenvolvedor");
        Pessoa p = pessoa(10L, "João", dev);

        List<CargoVencimento> v = List.of(
            new CargoVencimento(dev, venc(TipoVencimento.CREDITO, "2000.00")),
            new CargoVencimento(dev, venc(TipoVencimento.CREDITO, "500.50")),
            new CargoVencimento(dev, venc(TipoVencimento.DEBITO,  "150.25"))
        );

        when(cargoVencimentoRepository.findByCargo(dev)).thenReturn(v);

        BigDecimal salario = pessoaService.calcularSalario(p);

        assertEquals(0, new BigDecimal("2350.25").compareTo(salario));
    }

    @Test
    void deveRetornarSalarioZeroQuandoPessoaNaoTemCargo() {
        Pessoa semCargo = pessoa(20L, "Maria", null);

        BigDecimal salario = pessoaService.calcularSalario(semCargo);

        assertEquals(BigDecimal.ZERO, salario);
    }

    @Test
    void deveConsolidarSalarioDeNovaPessoa() {
        when(manager.getTransaction()).thenReturn(transaction);

        Cargo analista = cargo(2L, "Analista");
        Pessoa p = pessoa(30L, "Carlos", analista);

        when(pessoaRepository.findAll()).thenReturn(List.of(p));
        when(consolidadoRepository.find(p.getId())).thenReturn(null);
        when(cargoVencimentoRepository.findByCargo(any(Cargo.class))).thenReturn(Collections.emptyList());

        pessoaService.calcularEConsolidarTodos();

        verify(consolidadoRepository, times(1)).save(any(PessoaSalarioConsolidado.class));
        verify(consolidadoRepository, never()).saveMerge(any(PessoaSalarioConsolidado.class));
        verify(transaction, times(1)).begin();
        verify(transaction, times(1)).commit();
    }

    @Test
    void deveAtualizarSalarioConsolidadoExistente() {
        when(manager.getTransaction()).thenReturn(transaction);

        Cargo gerente = cargo(3L, "Gerente");
        Pessoa p = pessoa(40L, "Ana", gerente);

        PessoaSalarioConsolidado existente = new PessoaSalarioConsolidado();
        existente.setPessoaId(p.getId());

        when(pessoaRepository.findAll()).thenReturn(List.of(p));
        when(consolidadoRepository.find(p.getId())).thenReturn(existente);
        when(cargoVencimentoRepository.findByCargo(any(Cargo.class))).thenReturn(Collections.emptyList());

        pessoaService.calcularEConsolidarTodos();

        verify(consolidadoRepository, never()).save(any(PessoaSalarioConsolidado.class));
        verify(consolidadoRepository, times(1)).saveMerge(any(PessoaSalarioConsolidado.class));
        verify(transaction, times(1)).begin();
        verify(transaction, times(1)).commit();
    }
}
