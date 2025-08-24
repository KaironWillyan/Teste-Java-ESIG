package application;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.com.esig.application.service.RelatorioService;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

class RelatorioServiceTest {

    private RelatorioService service;

    @BeforeEach
    void setup() {
        service = new RelatorioService();
        try {
            var f = RelatorioService.class.getDeclaredField("CACHE");
            f.setAccessible(true);
            ((Map<?, ?>) f.get(null)).clear();
        } catch (Exception ignore) {}
    }

    @Test
    void deveLancarExcecaoQuandoRelatorioNaoExiste() {
        assertThrows(JRException.class, () ->
            service.gerarRelatorioPDF("nao_existe", Collections.emptyList())
        );
    }

    @Test
    void deveCompilarPreencherEExportarQuandoJRXMLExiste() throws Exception {
        String nome = "relatorio_teste";

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader fake = new ClassLoader(original) {
                @Override
                public InputStream getResourceAsStream(String name) {
                    if (name.equals("reports/jrxml/" + nome + ".jrxml")) {
                        return new ByteArrayInputStream(new byte[]{ /* conteúdo fictício */ });
                    }
                    return super.getResourceAsStream(name);
                }
            };
            Thread.currentThread().setContextClassLoader(fake);

            JasperReport mockReport = mock(JasperReport.class);
            JasperPrint  mockPrint  = mock(JasperPrint.class);
            byte[] pdfBytes = "PDF".getBytes();

            try (MockedStatic<JasperCompileManager> m1 = mockStatic(JasperCompileManager.class);
                 MockedStatic<JasperFillManager>    m2 = mockStatic(JasperFillManager.class);
                 MockedStatic<JasperExportManager>  m3 = mockStatic(JasperExportManager.class)) {

                m1.when(() -> JasperCompileManager.compileReport(any(InputStream.class)))
                  .thenReturn(mockReport);
                m2.when(() -> JasperFillManager.fillReport(eq(mockReport), anyMap(), any(JRDataSource.class)))
                .thenReturn(mockPrint);
                m3.when(() -> JasperExportManager.exportReportToPdf(mockPrint))
                  .thenReturn(pdfBytes);

                byte[] out = service.gerarRelatorioPDF(nome, Collections.emptyList());

                assertArrayEquals(pdfBytes, out);

                m1.verify(() -> JasperCompileManager.compileReport(any(InputStream.class)), times(1));
                m2.when(() -> JasperFillManager.fillReport(eq(mockReport), anyMap(), any(JRDataSource.class)))
                .thenReturn(mockPrint);
                m3.verify(() -> JasperExportManager.exportReportToPdf(mockPrint), times(1));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Test
    void deveUsarCacheENaoRecompilarNaSegundaChamada() throws Exception {
        String nome = "relatorio_cache";

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader fake = new ClassLoader(original) {
                @Override
                public InputStream getResourceAsStream(String name) {
                    if (name.equals("reports/jrxml/" + nome + ".jrxml")) {
                        return new ByteArrayInputStream(new byte[]{});
                    }
                    return null;
                }
            };
            Thread.currentThread().setContextClassLoader(fake);

            JasperReport mockReport = mock(JasperReport.class);
            JasperPrint  mockPrint  = mock(JasperPrint.class);
            byte[] pdfBytes = "PDF".getBytes();

            try (MockedStatic<JasperCompileManager> m1 = mockStatic(JasperCompileManager.class);
                 MockedStatic<JasperFillManager>    m2 = mockStatic(JasperFillManager.class);
                 MockedStatic<JasperExportManager>  m3 = mockStatic(JasperExportManager.class)) {

                m1.when(() -> JasperCompileManager.compileReport(any(InputStream.class)))
                  .thenReturn(mockReport);
                m2.when(() -> JasperFillManager.fillReport(eq(mockReport), anyMap(), any(JRDataSource.class)))
                .thenReturn(mockPrint);
                m3.when(() -> JasperExportManager.exportReportToPdf(mockPrint))
                  .thenReturn(pdfBytes);

                byte[] out1 = service.gerarRelatorioPDF(nome, Collections.emptyList());
                byte[] out2 = service.gerarRelatorioPDF(nome, Collections.emptyList());

                assertArrayEquals(pdfBytes, out1);
                assertArrayEquals(pdfBytes, out2);

                m1.verify(() -> JasperCompileManager.compileReport(any(InputStream.class)), times(1));
                m2.when(() -> JasperFillManager.fillReport(eq(mockReport), anyMap(), any(JRDataSource.class)))
                .thenReturn(mockPrint);
                m3.verify(() -> JasperExportManager.exportReportToPdf(mockPrint), times(2));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }
}
