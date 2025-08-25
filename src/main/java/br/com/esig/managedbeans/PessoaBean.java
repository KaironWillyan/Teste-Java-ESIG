package br.com.esig.managedbeans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import br.com.esig.application.service.PessoaService;
import br.com.esig.application.service.PessoaService.JobStatus;
import br.com.esig.application.service.RelatorioService;
import br.com.esig.domain.models.Cargo;
import br.com.esig.domain.models.PessoaSalarioConsolidado;
import br.com.esig.util.FacesUtils;

@Named("pessoaBean")
@ViewScoped
public class PessoaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private PessoaService pessoaService;
    @Inject private RelatorioService relatorioService;

    private LazyDataModel<PessoaSalarioConsolidado> lazyModel;
    private SortMeta sortBy;
    private String filtroNome;
    private Long filtroCargoId;
    private List<Cargo> todosOsCargos;

    private String jobId;

    @PostConstruct
    public void init() {
        todosOsCargos = pessoaService.findAllCargos();
        inicializarLazyModel();
    }

    private void inicializarLazyModel() {
        lazyModel = new LazyDataModel<PessoaSalarioConsolidado>() {
            private static final long serialVersionUID = 1L;

            @Override
            public List<PessoaSalarioConsolidado> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, org.primefaces.model.FilterMeta> filterBy) {
                String sortField = "nomePessoa";
                SortOrder sortOrder = SortOrder.ASCENDING;
                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta meta = sortBy.values().iterator().next();
                    sortField = meta.getField();
                    sortOrder = meta.getOrder();
                }

                Map<String, Object> filters = new HashMap<>();
                if (filtroNome != null && !filtroNome.trim().isEmpty()) {
                    filters.put("nomePessoa", filtroNome);
                }
                if (filtroCargoId != null) {
                    todosOsCargos.stream()
                        .filter(c -> c.getId().equals(filtroCargoId))
                        .findFirst()
                        .ifPresent(cargo -> filters.put("nomeCargo", cargo.getNome()));
                }

                setRowCount(pessoaService.countSalariosComFiltro(filters));
                return pessoaService.findSalariosPaginado(first, pageSize, sortField, sortOrder, filters);
            }
        };
    }

    public void limparFiltros() {
        this.filtroNome = null;
        this.filtroCargoId = null;
    }

    public void iniciarRecalculoAsync() {
        try {
            this.jobId = pessoaService.calcularEConsolidarTodos();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cálculo iniciado", "Job: " + jobId));
        } catch (IllegalStateException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Já em execução", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha ao iniciar cálculo."));
        }
    }

    public void cancelarRecalculo() {
        if (jobId != null) {
            pessoaService.cancelar(jobId);
        }
    }

    public void onPollStatus() {
        JobStatus st = getStatus();
        if (isBusy()) {
            return; 
        }
        if (st == null) return;

        switch (st.getState()) {
            case SUCCESS:
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Concluído", "Consolidação finalizada."));
                break;
            case CANCELLED:
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Cancelado", "Processo interrompido pelo usuário."));
                break;
            case ERROR:
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", st.getMessage()));
                break;
            default: break;
        }
        this.jobId = null;
    }

    public JobStatus getStatus() {
        return (jobId == null) ? null : pessoaService.getJobStatus(jobId);
    }

    public boolean isBusy() {
        return pessoaService.isBusy();
    }

    public String getJobId() {
        return jobId;
    }


    public void recalcularSalariosSync() {
        try {
            pessoaService.calcularEConsolidarTodos();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Salários recalculados e atualizados."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Ocorreu um erro ao recalcular os salários."));
        }
    }


    public void gerarRelatorioPDF() {
        try {
            Map<String, Object> backendFilters = new HashMap<>();
            if (filtroNome != null && !filtroNome.trim().isEmpty()) {
                backendFilters.put("nomePessoa", filtroNome);
            }
            if (filtroCargoId != null) {
                todosOsCargos.stream()
                    .filter(c -> c.getId().equals(filtroCargoId))
                    .findFirst()
                    .ifPresent(cargo -> backendFilters.put("nomeCargo", cargo.getNome()));
            }

            String sortField = this.sortBy != null ? this.sortBy.getField() : "nomePessoa";
            SortOrder sortOrder = this.sortBy != null ? this.sortBy.getOrder() : SortOrder.ASCENDING;

            List<PessoaSalarioConsolidado> dados =
                pessoaService.findSalariosParaRelatorio(sortField, sortOrder, backendFilters);

            byte[] pdf = relatorioService.gerarRelatorioPDF("relatorio_salarios", dados);
            FacesUtils.streamInline(pdf, "relatorio_salarios.pdf");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível gerar o relatório."));
            e.printStackTrace();
        }
    }


    public LazyDataModel<PessoaSalarioConsolidado> getLazyModel() { return lazyModel; }

    public String getFiltroNome() { return filtroNome; }
    public void setFiltroNome(String filtroNome) { this.filtroNome = filtroNome; }

    public Long getFiltroCargoId() { return filtroCargoId; }
    public void setFiltroCargoId(Long filtroCargoId) { this.filtroCargoId = filtroCargoId; }

    public List<Cargo> getTodosOsCargos() { return todosOsCargos; }
}
