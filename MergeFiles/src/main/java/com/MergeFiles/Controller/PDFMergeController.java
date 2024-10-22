package com.MergeFiles.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.MergeFiles.Service.PdfMergeService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class PDFMergeController {

    @Autowired
    private PdfMergeService pdfMergeService;

    @PostMapping("/merge")
    public ResponseEntity<Resource> mergeFiles(@RequestParam Map<String, MultipartFile> files) {
        try {
            // Call the service to merge files
            ByteArrayOutputStream mergedPdfStream = pdfMergeService.mergeFilesIntoPdf(files);

            // Create headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged.pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

            // Create a ByteArrayResource from the PDF stream
            ByteArrayResource resource = new ByteArrayResource(mergedPdfStream.toByteArray());

            // Return the PDF file as a response
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(mergedPdfStream.size())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
