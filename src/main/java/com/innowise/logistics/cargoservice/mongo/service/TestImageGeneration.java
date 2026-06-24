package com.innowise.logistics.cargoservice.mongo.service;

//import com.innowise.logistics.cargoservice.dto.request.ImageCargoUploadRequest;
//import com.innowise.logistics.cargoservice.mongo.service.ImageCargoService;
//import com.innowise.logistics.cargoservice.util.testdata.ImageGenerator;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.web.multipart.MultipartFile;

//@SpringBootTest
public class TestImageGeneration {

//    @Autowired
//    private ImageCargoService imageCargoService;
//
//    @Test
//    public void generateAndUploadTestImage() {
//        MultipartFile file = ImageGenerator.generateImage(
//                800, 600,
//                "Тестовое изображение",
//                "test_cargo_image.png"
//        );
//
//        ImageCargoUploadRequest metadata = new ImageCargoUploadRequest();
//        metadata.setCargoId(1L);
//        metadata.setDescription("Тестовое изображение груза");
//        metadata.setSortOrder(0);
//        metadata.setIsPrimary(true);
//
//        var response = imageCargoService.uploadCargoImage(file, metadata);
//        System.out.println("✅ Изображение загружено! ID: " + response.fileId());
//    }
}
