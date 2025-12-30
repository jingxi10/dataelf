package com.dataelf.platform.controller;

import com.dataelf.platform.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Slf4j
public class ExportController {
    
    private final ExportService exportService;
    
    /**
     * Export content as JSON-LD
     * GET /api/export/{contentId}/jsonld
     */
    @GetMapping("/{contentId}/jsonld")
    public ResponseEntity<String> exportJsonLd(@PathVariable Long contentId) {
        log.info("Export JSON-LD request for content: {}", contentId);
        
        String jsonLd = exportService.exportAsJsonLd(contentId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "content-" + contentId + ".jsonld");
        
        return new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);
    }
    
    /**
     * Export content as HTML
     * GET /api/export/{contentId}/html
     */
    @GetMapping("/{contentId}/html")
    public ResponseEntity<String> exportHtml(@PathVariable Long contentId) {
        log.info("Export HTML request for content: {}", contentId);
        
        String html = exportService.exportAsHtml(contentId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDispositionFormData("attachment", "content-" + contentId + ".html");
        
        return new ResponseEntity<>(html, headers, HttpStatus.OK);
    }
    
    /**
     * Export content as Markdown
     * GET /api/export/{contentId}/markdown
     */
    @GetMapping("/{contentId}/markdown")
    public ResponseEntity<String> exportMarkdown(@PathVariable Long contentId) {
        log.info("Export Markdown request for content: {}", contentId);
        
        String markdown = exportService.exportAsMarkdown(contentId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "content-" + contentId + ".md");
        
        return new ResponseEntity<>(markdown, headers, HttpStatus.OK);
    }
    
    /**
     * Export content as CSV
     * GET /api/export/{contentId}/csv
     */
    @GetMapping("/{contentId}/csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long contentId) {
        log.info("Export CSV request for content: {}", contentId);
        
        byte[] csv = exportService.exportAsCsv(contentId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "content-" + contentId + ".csv");
        
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
    
    /**
     * Export content as Word document (DOCX)
     * GET /api/export/{contentId}/word
     */
    @GetMapping("/{contentId}/word")
    public ResponseEntity<byte[]> exportWord(@PathVariable Long contentId) {
        log.info("Export Word document request for content: {}", contentId);
        
        byte[] wordDoc = exportService.exportAsWord(contentId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDispositionFormData("attachment", "content-" + contentId + ".docx");
        
        return new ResponseEntity<>(wordDoc, headers, HttpStatus.OK);
    }
}
