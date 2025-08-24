package br.com.esig.application.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.primefaces.model.SortOrder;

import br.com.esig.domain.enums.TipoVencimento;
import br.com.esig.domain.models.Cargo;
import br.com.esig.domain.models.CargoVencimento;
import br.com.esig.domain.models.Pessoa;
import br.com.esig.domain.models.PessoaSalarioConsolidado;
import br.com.esig.domain.repositories.CargoRepository;
import br.com.esig.domain.repositories.CargoVencimentoRepository;
import br.com.esig.domain.repositories.PessoaRepository;
import br.com.esig.domain.repositories.PessoaSalarioConsolidadoRepository;


@ApplicationScoped
public class PessoaService implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	
	@Inject
    private CargoVencimentoRepository cargoVencimentoRepository;

    @Inject
    private PessoaRepository pessoaRepository;

    @Inject
    private PessoaSalarioConsolidadoRepository consolidadoRepository;
    
    @Inject
    private CargoRepository cargoRepository;
    
    @Inject
    private EntityManager manager;
	
    public void calcularEConsolidarTodos() {
        try {
            manager.getTransaction().begin();

            List<Pessoa> todasAsPessoas = pessoaRepository.findAll();

            for (Pessoa pessoa : todasAsPessoas) {
                BigDecimal salarioCalculado = this.calcularSalario(pessoa);

                PessoaSalarioConsolidado consolidado = consolidadoRepository.find(pessoa.getId());

                if (consolidado == null) {
                    consolidado = new PessoaSalarioConsolidado();
                    consolidado.setPessoaId(pessoa.getId()); 
                    consolidado.setPessoa(pessoa);           
                    consolidado.setNomePessoa(pessoa.getNome());
                    consolidado.setNomeCargo(pessoa.getCargo().getNome());
                    consolidado.setSalario(salarioCalculado);

                    consolidadoRepository.save(consolidado); 
                } else {
                    consolidado.setNomePessoa(pessoa.getNome());
                    consolidado.setNomeCargo(pessoa.getCargo().getNome());
                    consolidado.setSalario(salarioCalculado);

                    consolidadoRepository.saveMerge(consolidado);
                }
            }

            manager.getTransaction().commit();

        } catch (Exception e) {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erro ao calcular e consolidar salários.", e);
        }
    }
    
    public BigDecimal calcularSalario(Pessoa pessoa) {
        if (pessoa == null || pessoa.getCargo() == null) {
            return BigDecimal.ZERO;
        }

        List<CargoVencimento> vencimentosDoCargo = cargoVencimentoRepository.findByCargo(pessoa.getCargo());

        BigDecimal salarioCalculado = BigDecimal.ZERO;

        for (CargoVencimento item : vencimentosDoCargo) {
            if (item.getVencimento().getTipo() == TipoVencimento.CREDITO) {
                salarioCalculado = salarioCalculado.add(item.getVencimento().getValor());
            } else if (item.getVencimento().getTipo() == TipoVencimento.DEBITO) {
                salarioCalculado = salarioCalculado.subtract(item.getVencimento().getValor());
            }
        }
        return salarioCalculado;
    }
    
    public List<PessoaSalarioConsolidado> findSalariosPaginado(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        return consolidadoRepository.findAndFilter(first, pageSize, sortField, sortOrder, filters);
    }
    
	 public List<PessoaSalarioConsolidado> findSalariosParaRelatorio(String sortField, SortOrder sortOrder, Map<String, Object> filters) {
	    return consolidadoRepository.findAndFilter(0, Integer.MAX_VALUE, sortField, sortOrder, filters);
	}

    public int countSalariosComFiltro(Map<String, Object> filters) {
        return consolidadoRepository.countWithFilters(filters);
    }
    
    public List<Cargo> findAllCargos() {
        return cargoRepository.findAll();
    }
    
    public void savePessoa(Pessoa pessoa) {
        try {
            manager.getTransaction().begin();            
            pessoaRepository.save(pessoa);
            manager.getTransaction().commit();
        } catch (Exception e) {
            if (manager.getTransaction().isActive()) manager.getTransaction().rollback();
            throw e;
        }
    }
    
    public void toggleStatusPessoa(Pessoa pessoa) {
        try {
            manager.getTransaction().begin();
            Pessoa pessoaBD = pessoaRepository.find(pessoa.getId());
            if (pessoaBD != null) {
                pessoaBD.setAtivo(!pessoaBD.isAtivo()); 
                pessoaRepository.save(pessoaBD);
            }
            manager.getTransaction().commit();
        } catch (Exception e) {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            throw e;
        }
    }
    
    public List<Pessoa> findPessoas(String nome, Long cargoId, boolean incluirInativos) {
        return pessoaRepository.findWithFilters(nome, cargoId, incluirInativos);
    }
}
