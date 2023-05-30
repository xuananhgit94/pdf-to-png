package com.example.demo.controller;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DemoController {
    @Autowired
    private MinioClient minioClient;

    @PostMapping("/a")
    public List<String> getUrl(@RequestParam("file")MultipartFile file) throws Exception {
        PDDocument document = PDDocument.load(file.getInputStream());
        List<String> list = new ArrayList<>();
        List<String> list1 = new ArrayList<>();
        PDFRenderer renderer = new PDFRenderer(document);
        for (int pageNumber = 0; pageNumber < document.getNumberOfPages(); pageNumber++) {
            BufferedImage image = renderer.renderImageWithDPI(pageNumber, 300);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageData = baos.toByteArray();

            String objectName = "page_" + pageNumber + ".png";
            list.add(objectName);

            try (InputStream inputStream = new ByteArrayInputStream(imageData)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket("images")
                                .object(objectName)
                                .stream(inputStream, imageData.length, -1)
                                .contentType("image/png")
                                .build()
                );
            }
        }
        for (String s : list) {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket("images")
                    .object(s)
                    .build();
            list1.add(minioClient.getPresignedObjectUrl(args));
        }
        return  list1;
    }
}
