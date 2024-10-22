package com.MergeFiles.Service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;


import java.io.*;
import java.util.Map;

@Service
public class PdfMergeService {

    public ByteArrayOutputStream mergeFilesIntoPdf(Map<String, MultipartFile> files) throws IOException {
        PDDocument document = new PDDocument();

        int fileCounter = 1;
        for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
            MultipartFile file = entry.getValue();
            String originalFilename = file.getOriginalFilename();
            String fileType = file.getContentType();

            // Check for null or empty filename to avoid NullPointerException
            if (originalFilename != null && !originalFilename.isEmpty()) {
                try {
                    // Get the file extension
                    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

                    
                    if (fileType != null) {
                        switch (fileType) {
                            case "application/pdf":
                            case "application/x-pdf":
                            case "application/acrobat":
                            case "application/vnd.pdf":
                            case "application/x-bzpdf":
                            case "application/x-gzpdf":
                            case "application/octet-stream":
                                mergePdf(document, file);
                                break;

                            case "image/jpeg":
                            case "image/png":
                            case "image/jpg":
                            case "image/gif": // added support for GIF images
                                addImageToPdf(document, file);
                                break;

                            case "application/msword": // .doc
                            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": // .docx
                                convertDocxToPdfAndMerge(document, file);
                                break;

                            default:
                                // Log the unsupported file type
                                System.out.println("Unsupported file type: " + fileType + " for file: " + originalFilename);
                                break;
                        }
                    } else {
                        // Log and handle the case where fileType is null
                        System.out.println("File type is null for file: " + originalFilename);
                    }
                } catch (Exception e) {
                    // Catch any exception for the current file and log it, but continue processing other files
                    System.out.println("Error processing file: " + originalFilename + " due to: " + e.getMessage());
                }
            } else {
                // Log and handle cases where the filename is null or empty
                System.out.println("Filename is null or empty for the provided MultipartFile.");
            }
            fileCounter++;
        }

        // Save the merged document to ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();
        return outputStream;
    }

    private void mergePdf(PDDocument targetDocument, MultipartFile pdfFile) throws IOException {
        File tempFile = File.createTempFile("tempPdfFile", ".pdf");
        pdfFile.transferTo(tempFile);
        PDDocument sourceDocument = Loader.loadPDF(tempFile);
        for (PDPage page : sourceDocument.getPages()) {
            PDPage importedPage = targetDocument.importPage(page);
            targetDocument.addPage(importedPage);
        }
        sourceDocument.close();
        tempFile.delete();
    }

    private void addImageToPdf(PDDocument document, MultipartFile imageFile) throws IOException {
        // Create a new A4 page
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
    
        // Load the image from the MultipartFile
        PDImageXObject image = PDImageXObject.createFromByteArray(document, imageFile.getBytes(), imageFile.getOriginalFilename());
    
        // Get the dimensions of the page (A4 size)
        float pageWidth = PDRectangle.A4.getWidth();
        float pageHeight = PDRectangle.A4.getHeight();
    
        // Start a new content stream to draw the image
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
    
        // Draw the image to fit the entire page
        contentStream.drawImage(image, 0, 0, pageWidth, pageHeight);
    
        contentStream.close();
    }
    

    // private void addImageToPdf(PDDocument document, MultipartFile imageFile) throws IOException {
    //     PDPage page = new PDPage();
    //     document.addPage(page);
    //     PDImageXObject image = PDImageXObject.createFromByteArray(document, imageFile.getBytes(), imageFile.getOriginalFilename());
    //     PDPageContentStream contentStream = new PDPageContentStream(document, page);
    //     contentStream.drawImage(image, 20, 400, 400, 400); 
    //     contentStream.close();
    // }
    


    private void convertDocxToPdfAndMerge(PDDocument targetDocument, MultipartFile docxFile) throws IOException {
        File tempFile = File.createTempFile("tempDocxFile", ".docx");
        docxFile.transferTo(tempFile);

        try {
            // Use Aspose.Words to convert DOCX to PDF
            Document docx = new Document(tempFile.getAbsolutePath());
            File pdfFile = File.createTempFile("convertedPdf", ".pdf");
            docx.save(pdfFile.getAbsolutePath(), SaveFormat.PDF);

            // Merge the converted PDF
            mergePdf(targetDocument, new MockMultipartFile("convertedPdf", new FileInputStream(pdfFile)));
            pdfFile.delete();
        } catch (Exception e) {
            System.out.println("Error converting DOCX to PDF: " + e.getMessage());
        }
        tempFile.delete();
    }
     
    
    

}
