package com.shotgun.viewserver.images;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.order.controllers.contracts.OrderUpdateController;
import com.shotgun.viewserver.setup.datasource.ImageDataSource;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.datasource.IRecord;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller(name = "imageController")
public class FileSystemImageController implements IImageController, OrderUpdateController {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemImageController.class);
    private IDatabaseUpdater databaseUpdater;
    private ICatalog systemCatalog;
    private int imageControllerPort;
    private String uploadedFileLocation;

    public FileSystemImageController(IDatabaseUpdater databaseUpdater, ICatalog systemCatalog, int imageControllerPort, String uploadedFileLocation) {
        this.databaseUpdater = databaseUpdater;
        this.systemCatalog = systemCatalog;
        this.imageControllerPort = imageControllerPort;
        this.uploadedFileLocation = uploadedFileLocation;
        createHttpServer(this.imageControllerPort);
    }

    private void createHttpServer(int httpPort) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(httpPort), 0);
            logger.debug("Image controller endpoint starting at " + httpPort);
            server.createContext("/image", new ImageRequestHandler());
            server.createContext("/upload", new ImageUploadHandler(uploadedFileLocation));
            server.setExecutor(ControllerUtils.BackgroundExecutor);
            server.start();
            logger.debug("Image controller endpoint started at " + httpPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ControllerAction(path = "saveImage", isSynchronous = false)
    public ListenableFuture saveImage(@ActionParam(name = "bucketName") String bucketName, @ActionParam(name = "fileName") String fileName, @ActionParam(name = "imageData") String imageData){
        String imageId = ControllerUtils.generateGuid();
        Record rec = new Record().
                addValue("imageId",imageId).
                addValue("imageData",imageData).
                addValue("created",new Date());

        SettableFuture result = SettableFuture.create();
        databaseUpdater.addOrUpdateRow(
                TableNames.IMAGES_TABLE_NAME,
                ImageDataSource.getDataSource().getSchema(),
                rec,
                IRecord.UPDATE_LATEST_VERSION
        ).take(1).subscribe(
                success -> {
                    result.set(new SystemUri("image",imageId).toString());
                },
                err -> {
                    result.setException(err);
                }
        );
        return result;
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

        private String uploadedFileLocation;

        public ImageUploadHandler(String uploadedFileLocation) {
            this.uploadedFileLocation = uploadedFileLocation;
            File directory = new File(uploadedFileLocation);
            if(!directory.exists()){
                directory.mkdirs();
            }
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
                t.sendResponseHeaders(200, 0);
                OutputStream os = t.getResponseBody();

                String imageId = ControllerUtils.generateGuid();
                for(FileItem fi : result) {
                    if(!fi.isFormField()){
                        File uploadedFile = new File(String.format("%s/%s",uploadedFileLocation,imageId));
                        fi.write(uploadedFile);
                    }
                    os.write(new SystemUri("images",imageId).toString().getBytes());
                }
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ImageRequestHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) {
            logger.info("Handling image request {}",he);

            SystemUri imageUrl = new SystemUri(he.getRequestURI());
            File uploadedFile = new File(String.format("%s/%s",uploadedFileLocation,imageUrl.getImageId()));

            try {
                if (!uploadedFile.exists()) {
                    logger.info("Unable to find image for uri {}", he.getRequestURI());
                    he.sendResponseHeaders(404, 0);
                } else {
                    logger.info("Found image for uri {}",he.getRequestURI());
                    he.sendResponseHeaders(200, 0);
                    he.getResponseHeaders().add("Content-Type", " image/jpeg");
                    OutputStream os = he.getResponseBody();
                    IOUtils.copy(new FileInputStream(uploadedFile),os);
                    os.close();
                }
            }catch (Exception ex){
                logger.error("Problem retrieving file",ex);
                try {
                    he.sendResponseHeaders(500, 0);
                } catch (IOException e) {
                    logger.error("Servlet issue",e);
                }
            }

        }
    }


}
