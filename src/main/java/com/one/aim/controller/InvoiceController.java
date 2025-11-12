package com.one.aim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.one.aim.service.InvoiceService;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

	@Autowired
	InvoiceService invoiceService;

//    @Value("${invoice.template.path}")
//    private String templatePath;
//
//    private final ResourceLoader resourceLoader;

//    public InvoiceController(ResourceLoader resourceLoader) {
//        this.resourceLoader = resourceLoader;
//    }

	@GetMapping("/download/{orderId}")
	public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable("orderId") long orderId) throws Exception {
		// 1. Load HTML template
		String htmlContent = invoiceService.downloadInvoice(orderId);
		// 3. Convert HTML to PDF using iText html2pdf
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdfDocument = new PdfDocument(writer);
		ConverterProperties converterProperties = new ConverterProperties();
		HtmlConverter.convertToPdf(htmlContent, pdfDocument, converterProperties);

		// 4. Prepare response
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.attachment().filename("invoice.pdf").build());

		return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
	}
}
