package br.com.esig.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@ApplicationScoped
public class RelatorioService implements Serializable {

    private static final long serialVersionUID = 1L;
 
    private static final Map<String, JasperReport> CACHE = new ConcurrentHashMap<>();

    public byte[] gerarRelatorioPDF(String nomeRelatorio, Collection<?> dados) throws JRException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        JasperReport report = CACHE.get(nomeRelatorio);

        if (report == null) {
            try (InputStream jasper = cl.getResourceAsStream("reports/" + nomeRelatorio + ".jasper")) {
                if (jasper != null) {
                    report = (JasperReport) JRLoader.loadObject(jasper);
                }
            } catch (Exception e) {
            }

            if (report == null) {
                try (InputStream jrxml = cl.getResourceAsStream("reports/jrxml/" + nomeRelatorio + ".jrxml")) {
                    if (jrxml == null) {
                        throw new JRException("Relatório não encontrado: /reports/" + nomeRelatorio + ".jrxml");
                    }
                    report = JasperCompileManager.compileReport(jrxml);
                } catch (IOException e) {
					e.printStackTrace();
				}
            }

            CACHE.put(nomeRelatorio, report);
        }

        Map<String, Object> params = new HashMap<>();
        params.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dados, false);

        JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);
        return JasperExportManager.exportReportToPdf(print);
    }
}