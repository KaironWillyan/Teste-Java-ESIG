package br.com.esig.util;

import java.io.IOException;
import java.io.OutputStream;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

public final class FacesUtils {

    private FacesUtils() {
    }

    public static void streamDownload(byte[] data, String contentType, String nomeArquivo) throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        if (facesContext == null) {
            return;
        }

        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.reset();
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment;filename=" + nomeArquivo);
        response.setContentLength(data.length);
        
        try (OutputStream out = response.getOutputStream()) {
            out.write(data);
            out.flush();
        }
        
        facesContext.responseComplete();
    }
    
    public static void streamInline(byte[] data, String filename) throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) return;

        HttpServletResponse resp = (HttpServletResponse) fc.getExternalContext().getResponse();
        resp.reset();
        resp.setContentType("application/pdf");
        String safeName = filename.replace("\"", "");
        resp.setHeader("Content-Disposition", "inline; filename=\"" + safeName + "\"");
        resp.setContentLength(data.length);

        try (OutputStream out = resp.getOutputStream()) {
            out.write(data);
        }
        fc.responseComplete();
    }

}