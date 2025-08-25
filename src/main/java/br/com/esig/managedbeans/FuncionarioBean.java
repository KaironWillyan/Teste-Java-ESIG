package br.com.esig.managedbeans;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.esig.application.service.PessoaService;
import br.com.esig.application.service.RelatorioService;
import br.com.esig.domain.models.Cargo;
import br.com.esig.domain.models.Pessoa;
import br.com.esig.util.FacesUtils;

@Named("funcionarioBean")
@ViewScoped
public class FuncionarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PessoaService pessoaService;
    @Inject
    private RelatorioService relatorioService;

    private List<Pessoa> funcionarios;
    private Pessoa selectedFuncionario;

    private String filtroNome;
    private Long filtroCargoId;
    private boolean incluirInativos = true;

    private List<Cargo> todosOsCargos;

    @PostConstruct
    public void init() {
        todosOsCargos = pessoaService.findAllCargos();
        buscar();
    }

    public void buscar() {
        try {
            List<Pessoa> lista = pessoaService.findPessoas(filtroNome, filtroCargoId, incluirInativos);
            this.funcionarios = (lista != null ? lista : Collections.emptyList());
        } catch (Exception e) {
            this.funcionarios = Collections.emptyList();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha ao buscar pessoas."));
        }
    }

    public void novoFuncionario() {
        this.selectedFuncionario = new Pessoa();
        this.selectedFuncionario.setAtivo(true);
    }

    public void salvar() {
        try {
            pessoaService.savePessoa(selectedFuncionario);
            buscar();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Sucesso", "Funcionário salvo com sucesso!"));
            org.primefaces.PrimeFaces.current().executeScript("PF('funcionarioDialog').hide()");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível salvar o funcionário."));
        }
    }

    public void toggleStatus(Pessoa funcionario) {
        try {
            boolean eraAtivo = funcionario.isAtivo();
            pessoaService.toggleStatusPessoa(funcionario);
            buscar();
            String acao = eraAtivo ? "desativado" : "ativado";
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Sucesso", "Funcionário " + acao + " com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível alterar o status do funcionário."));
        }
    }

    public void gerarRelatorioPDF() {
        try {
            List<Pessoa> dados = this.funcionarios;
            byte[] pdf = relatorioService.gerarRelatorioPDF("relatorio_pessoas", dados);
            FacesUtils.streamInline(pdf, "relatorio_funcionarios.pdf");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível gerar o relatório."));
            e.printStackTrace();
        }
    }

    public List<Pessoa> getFuncionarios() { return funcionarios; }
    public int getFuncionariosCount() { return funcionarios != null ? funcionarios.size() : 0; }

    public Pessoa getSelectedFuncionario() { return selectedFuncionario; }
    public void setSelectedFuncionario(Pessoa selectedFuncionario) { this.selectedFuncionario = selectedFuncionario; }

    public String getFiltroNome() { return filtroNome; }
    public void setFiltroNome(String filtroNome) { this.filtroNome = filtroNome; }

    public Long getFiltroCargoId() { return filtroCargoId; }
    public void setFiltroCargoId(Long filtroCargoId) { this.filtroCargoId = filtroCargoId; }

    public boolean isIncluirInativos() { return incluirInativos; }
    public void setIncluirInativos(boolean incluirInativos) { this.incluirInativos = incluirInativos; }

    public List<Cargo> getTodosOsCargos() { return todosOsCargos; }
}
