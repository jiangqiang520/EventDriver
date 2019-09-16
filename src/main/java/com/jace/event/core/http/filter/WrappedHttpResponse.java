package com.jace.event.core.http.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FastByteArrayOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class WrappedHttpResponse extends HttpServletResponseWrapper {
    private static final Logger logger = LoggerFactory.getLogger(WrappedHttpResponse.class);

    private final FastByteArrayOutputStream content = new FastByteArrayOutputStream(1024);
    private final ResponseServletOutputStream outputStream = new ResponseServletOutputStream();
    private PrintWriter writer;

    public static WrappedHttpResponse wrap(HttpServletResponse response) {
        if (response instanceof WrappedHttpResponse) {
            return (WrappedHttpResponse) response;
        } else {
            return new WrappedHttpResponse(response);
        }
    }

    private WrappedHttpResponse(HttpServletResponse response) {
        super(response);
        writer = new PrintWriter(outputStream);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.writer;
    }

    @Override
    public void reset() {
        super.reset();
        this.content.reset();
    }

    public void finalize() throws Throwable {
        super.finalize();
        outputStream.close();
    }

    private ServletOutputStream getRealOutputStream() throws IOException {
        return super.getOutputStream();
    }
    public String getResponseContent() {
        try {
            writer.flush();
            if (StringUtils.containsIgnoreCase(this.getContentType(), "application/json") ||
            		StringUtils.containsIgnoreCase(this.getContentType(), "text/html")) {
                return new String(content.toByteArray(), this.getCharacterEncoding());
            } else {
                return "content:" + this.getContentType();
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Exception when print response: UnsupportedEncoding");
        }
        return "";
    }

    private class ResponseServletOutputStream extends ServletOutputStream {

        @Override
        public void write(int b) throws IOException {
            content.write(b);
            getRealOutputStream().write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            content.write(b, off, len);
            getRealOutputStream().write(b, off, len);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }
}
