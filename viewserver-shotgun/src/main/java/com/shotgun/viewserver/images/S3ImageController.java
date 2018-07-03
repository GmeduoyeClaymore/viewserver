package com.shotgun.viewserver.images;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.order.controllers.contracts.OrderUpdateController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;


@Controller(name = "imageController")
public class S3ImageController implements IImageController, OrderUpdateController {

    private final AmazonS3 s3Client;
    private int imageControllerPort;
    private BasicAWSCredentials awsCredentials;
    private static final Logger logger = LoggerFactory.getLogger(S3ImageController.class);
    private IDatabaseUpdater databaseUpdater;
    private ICatalog systemCatalog;

    public S3ImageController(BasicAWSCredentials awsCredentials, IDatabaseUpdater databaseUpdater, ICatalog systemCatalog, int imageControllerPort) {
        this.awsCredentials = awsCredentials;
        this.databaseUpdater = databaseUpdater;
        this.systemCatalog = systemCatalog;
        s3Client = AmazonS3ClientBuilder.standard().withRegion("eu-west-2").withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
        this.imageControllerPort = imageControllerPort;
        createHttpServer(this.imageControllerPort);
    }

    private void createHttpServer(int httpPort) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(httpPort), 0);
            logger.debug("Image controller endpoint starting at " + httpPort);
            server.createContext("/upload", new ImageUploadHandler(s3Client));
            server.setExecutor(ControllerUtils.BackgroundExecutor);
            server.start();
            logger.debug("Image controller endpoint started at " + httpPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    @ControllerAction(path = "saveImage", isSynchronous = false)
    public ListenableFuture saveImage(@ActionParam(name = "bucketName") String bucketName, @ActionParam(name = "fileName") String fileName, @ActionParam(name = "imageData") String imageData){

        if(imageData == null){
            throw new RuntimeException("No image data sent");
        }
        byte[] data = Base64.decodeBase64(imageData);
        logger.info("Started loading image - " + (data.length/1024) + "kb");

        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(data.length);

        try {
            logger.info(String.format("Uploading image %s to s3 bucket", fileName, bucketName));
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, input, meta).withCannedAcl(CannedAccessControlList.PublicRead));
            URL url = s3Client.getUrl(bucketName, fileName);
            logger.info("Finish loading image - " + (imageData.getBytes().length/1024) + "kb");
            logger.info(String.format("Uploaded image to s3 with url %s", url.toString()));
            return Futures.immediateFuture(url.toString());

        }catch (Exception ex){
            logger.error(String.format("Unable to upload image %s to s3", fileName),ex);
            throw new RuntimeException("Unable to upload image to s3");
        }
    }

    @Override
    public ICatalog getSystemCatalog() {
        return systemCatalog;
    }

    @Override
    public IDatabaseUpdater getDatabaseUpdater() {
        return databaseUpdater;
    }


    static class ImageUploadHandler implements HttpHandler {


        private AmazonS3 s3Client;

        public ImageUploadHandler(AmazonS3 s3Client) {
            this.s3Client = s3Client;
        }

        @Override
        public void handle(final HttpExchange t) {
            for(Map.Entry<String, List<String>> header : t.getRequestHeaders().entrySet()) {
                System.out.println(header.getKey() + ": " + header.getValue().get(0));
            }
            DiskFileItemFactory d = new DiskFileItemFactory();

            try {
                ServletFileUpload up = new ServletFileUpload(d);
                List<FileItem> result = up.parseRequest(new RequestContext() {

                    @Override
                    public String getCharacterEncoding() {
                        return "UTF-8";
                    }

                    @Override
                    public int getContentLength() {
                        return Integer.parseInt(t.getRequestHeaders().getFirst("Content-length"));
                    }

                    @Override
                    public String getContentType() {
                        return t.getRequestHeaders().getFirst("Content-type");
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return t.getRequestBody();
                    }

                });
                t.getResponseHeaders().add("Content-type", "text/plain");

                OutputStream os = t.getResponseBody();

                String imageId = ControllerUtils.generateGuid();
                for(FileItem fi : result) {
                    if(!fi.isFormField()){
                        byte[] buf = fi.get();
                        ByteArrayInputStream input = new ByteArrayInputStream(buf);
                        ObjectMetadata meta = new ObjectMetadata();
                        meta.setContentLength(buf.length);

                        try {
                            logger.info(String.format("Uploading image %s to s3 bucket", imageId, BucketNames.shotgunclientimages.name()));
                            s3Client.putObject(new PutObjectRequest(BucketNames.shotgunclientimages.name(), imageId, input, meta).withCannedAcl(CannedAccessControlList.PublicRead));
                            URL url = s3Client.getUrl(BucketNames.shotgunclientimages.name(), imageId);
                            logger.info("Finish loading image - " + (buf.length/1024) + "kb");
                            logger.info(String.format("Uploaded image to s3 with url %s", url.toString()));
                            t.sendResponseHeaders(200, 0);
                            os.write(url.toString().getBytes());

                        }catch (Exception ex){
                            logger.error(String.format("Unable to upload image %s to s3", imageId),ex);
                            t.sendResponseHeaders(500, 0);
                            os.write(String.format("Unable to upload image %s to s3", imageId).getBytes());
                        }
                    }

                }
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


