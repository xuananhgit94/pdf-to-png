package com.example.demo.controller;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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

    @GetMapping("/b")
    public String demo2() throws IOException {
        String result = "";

        final WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

            HtmlPage page = webClient.getPage("https://tpb.vn/cong-cu-tinh-toan/lai-suat");
            webClient.waitForBackgroundJavaScript(4000);

            DomElement element = page.getElementById("tab-1");
            DomNodeList<HtmlElement> paragraphs = element.getElementsByTagName("p");
            for (HtmlElement paragraph : paragraphs) {
                DomNodeList<HtmlElement> links = paragraph.getElementsByTagName("a");

                for (HtmlElement link : links) {
                    String hrefValue = link.getAttribute("href");
                    result += hrefValue + "\n";
                }
            }

        return result;
    }

    @GetMapping("c")
    public List<String> demo3() throws Exception {
        String url = "https://tpb.vn/wps/wcm/connect/8b2c4f61-b4ea-4c31-8cb4-660d9c263fdb/BieuLaiSuat_up+web_25.05.2023.pdf?MOD=AJPERES&CVID=oxdkWXd&CVID=oxdkWXd&CVID=ox16sOB&CVID=ox16sOB&CVID=ox16sOB&CVID=ow94pkZ&CVID=ouSsR-u&CVID=ot4OzXL&CVID=or-vI-T&CVID=oqGRSpU&CVID=oqGRSpU&CVID=omV-n-t&CVID=ohhI4O1&CVID=ohhI4O1&CVID=ohhI4O1&CVID=oghYCPf&CVID=oghYCPf&CVID=of4FDDS&CVID=of4FDDS&CVID=of4FDDS&CVID=of4FDDS&CVID=of4FDDS&CVID=of4FDDS&CVID=of4FDDS&CVID=of4FDDS&CVID=of4FDDS&CVID=odFZ-3S&CVID=odFZ-3S&CVID=oaaxBq-&CVID=oaaxBq-&CVID=oaaxBq-&CVID=o8f4PQG&CVID=o4Zc-6D&CVID=o4T7Mu5";
        URL url1 = new URL(url);
        PDDocument document = PDDocument.load(url1.openStream());
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
