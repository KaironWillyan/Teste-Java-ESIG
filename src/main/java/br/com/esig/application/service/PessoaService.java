package br.com.esig.application.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.RequestContextController;
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
    @Inject 
    private RequestContextController requestContextController;

    private ExecutorService executor;
    private final AtomicBoolean busy = new AtomicBoolean(false);
    private final Map<String, JobStatus> jobs = new ConcurrentHashMap<>();
    private volatile String currentJobId;

    @PostConstruct
    void init() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "calc-salarios");
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    void destroy() {
        if (executor != null) executor.shutdownNow();
    }

    public boolean isBusy() { return busy.get(); }
    public String getCurrentJobId() { return currentJobId; }
    public JobStatus getJobStatus(String jobId) { return jobs.get(jobId); }

    public String calcularEConsolidarTodos() {
        if (!busy.compareAndSet(false, true)) {
            throw new IllegalStateException("Já existe um cálculo em execução.");
        }
        final String jobId = UUID.randomUUID().toString();
        currentJobId = jobId;

        JobStatus status = new JobStatus(jobId);
        status.setState(JobState.PENDING);
        jobs.put(jobId, status);

        executor.submit(() -> {
            status.setState(JobState.RUNNING);
            status.setStartedAt(Instant.now());
            requestContextController.activate();
            try {
                executarCalculoConsolidacao(status);
                if (status.isCancelRequested()) {
                    status.setState(JobState.CANCELLED);
                    status.setMessage("Processo cancelado pelo usuário.");
                } else {
                    status.setState(JobState.SUCCESS);
                    status.setMessage("Consolidação concluída com sucesso.");
                }
            } catch (Exception e) {
                status.setState(JobState.ERROR);
                status.setMessage("Falha no cálculo: " + e.getMessage());
                status.setError(e);
            } finally {
                status.setEndedAt(Instant.now());
                busy.set(false);
                currentJobId = null;
                requestContextController.deactivate();
            }
        });

        return jobId;
    }

    public void cancelar(String jobId) {
        JobStatus st = jobs.get(jobId);
        if (st != null) st.setCancelRequested(true);
    }

    private void executarCalculoConsolidacao(JobStatus status) {
        List<Pessoa> pessoas = pessoaRepository.findAll();
        status.setTotal(pessoas.size());
        status.setProcessed(0);
        status.setProgressPercent(calcPercent(0, status.getTotal()));

        final int BATCH = 100;
        int i = 0;

        manager.getTransaction().begin();
        try {
            for (Pessoa pessoa : pessoas) {
                if (status.isCancelRequested()) {
                    if (manager.getTransaction().isActive()) manager.getTransaction().commit();
                    return;
                }

                Pessoa pessoaRef = manager.contains(pessoa)
                    ? pessoa
                    : manager.getReference(Pessoa.class, pessoa.getId());

                BigDecimal salario = calcularSalario(pessoaRef);

                PessoaSalarioConsolidado cons = consolidadoRepository.find(pessoaRef.getId());
                boolean novo = (cons == null);
                if (novo) {
                    cons = new PessoaSalarioConsolidado();
                    cons.setPessoaId(pessoaRef.getId());
                }

                cons.setPessoa(pessoaRef); 
                cons.setNomePessoa(pessoaRef.getNome());
                cons.setNomeCargo(pessoaRef.getCargo() != null ? pessoaRef.getCargo().getNome() : null);
                cons.setSalario(salario);

                if (novo) {
                    consolidadoRepository.save(cons);
                } else {
                    consolidadoRepository.saveMerge(cons);
                }

                i++;
                status.setProcessed(i);
                status.setProgressPercent(calcPercent(i, status.getTotal()));

                if (i % BATCH == 0) {
                    manager.getTransaction().commit();
                    manager.clear();
                    manager.getTransaction().begin();
                }
            }

            if (manager.getTransaction().isActive()) manager.getTransaction().commit();
            status.setProgressPercent(100);

        } catch (Exception e) {
            if (manager.getTransaction().isActive()) manager.getTransaction().rollback();
            throw e;
        }
    }

    private static int calcPercent(int processed, int total) {
        return (total <= 0) ? 0 : (int) Math.min(100, Math.round(processed * 100.0 / total));
    }

    public BigDecimal calcularSalario(Pessoa pessoa) {
        if (pessoa == null || pessoa.getCargo() == null) return BigDecimal.ZERO;

        List<CargoVencimento> vencs = cargoVencimentoRepository.findByCargo(pessoa.getCargo());
        BigDecimal salario = BigDecimal.ZERO;

        for (CargoVencimento item : vencs) {
            if (item.getVencimento() == null || item.getVencimento().getValor() == null || item.getVencimento().getTipo() == null) {
                continue;
            }
            if (item.getVencimento().getTipo() == TipoVencimento.CREDITO) {
                salario = salario.add(item.getVencimento().getValor());
            } else if (item.getVencimento().getTipo() == TipoVencimento.DEBITO) {
                salario = salario.subtract(item.getVencimento().getValor());
            }
        }
        return salario;
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

    public List<Cargo> findAllCargos() { return cargoRepository.findAll(); }

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
            Pessoa db = pessoaRepository.find(pessoa.getId());
            if (db != null) {
                db.setAtivo(!db.isAtivo());
                pessoaRepository.save(db);
            }
            manager.getTransaction().commit();
        } catch (Exception e) {
            if (manager.getTransaction().isActive()) manager.getTransaction().rollback();
            throw e;
        }
    }

    public List<Pessoa> findPessoas(String nome, Long cargoId, boolean incluirInativos) {
        return pessoaRepository.findWithFilters(nome, cargoId, incluirInativos);
    }

    public enum JobState { PENDING, RUNNING, SUCCESS, ERROR, CANCELLED }

    public static final class JobStatus implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String id;
        private volatile JobState state = JobState.PENDING;
        private volatile int total;
        private volatile int processed;
        private volatile int progressPercent;
        private volatile boolean cancelRequested;
        private volatile String message;
        private volatile Instant startedAt;
        private volatile Instant endedAt;
        private transient Throwable error;

        JobStatus(String id) { this.id = id; }

        public String getId() { return id; }
        public JobState getState() { return state; }
        void setState(JobState s) { this.state = s; }
        public int getTotal() { return total; }
        void setTotal(int t) { this.total = t; }
        public int getProcessed() { return processed; }
        void setProcessed(int p) { this.processed = p; }
        public int getProgressPercent() { return progressPercent; }
        void setProgressPercent(int v) { this.progressPercent = v; }
        public boolean isCancelRequested() { return cancelRequested; }
        void setCancelRequested(boolean v) { this.cancelRequested = v; }
        public String getMessage() { return message; }
        void setMessage(String m) { this.message = m; }
        public Instant getStartedAt() { return startedAt; }
        void setStartedAt(Instant t) { this.startedAt = t; }
        public Instant getEndedAt() { return endedAt; }
        void setEndedAt(Instant t) { this.endedAt = t; }
        public Throwable getError() { return error; }
        void setError(Throwable e) { this.error = e; }
    }
}
