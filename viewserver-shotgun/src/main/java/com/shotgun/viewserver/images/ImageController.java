package com.shotgun.viewserver.images;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;


@Controller(name = "imageController")
public class ImageController {

    private BasicAWSCredentials awsCredentials;
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    public ImageController(BasicAWSCredentials awsCredentials) {
        this.awsCredentials = awsCredentials;
    }

    @ControllerAction(path = "saveToS3", isSynchronous = false)
    public String saveToS3(@ActionParam(name = "bucketName")String bucketName, @ActionParam(name = "fileName")String fileName, @ActionParam(name = "imageData")String imageData){

        byte[] data = Base64.decodeBase64(imageData);
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(data.length);

        try {
            logger.debug(String.format("Uploading image %s to s3 bucket", fileName, bucketName));
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion("eu-west-2").withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, input, meta).withCannedAcl(CannedAccessControlList.PublicRead));
            URL url = s3Client.getUrl(bucketName, fileName);
            logger.debug(String.format("Uploaded image to s3 with url %s", url.toString()));
            return url.toString();

        }catch (Exception ex){
            logger.error(String.format("Unable to upload image %s to s3", fileName),ex);
            throw new RuntimeException("Unable to upload image to s3");
        }
    }
}
