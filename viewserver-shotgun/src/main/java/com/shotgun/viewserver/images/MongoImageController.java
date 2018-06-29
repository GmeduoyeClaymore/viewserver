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
import io.viewserver.operators.IOperator;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.schema.column.ColumnHolderUtils;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Date;

@Controller(name = "imageController")
public class MongoImageController implements IImageController, OrderUpdateController {

    private static final Logger logger = LoggerFactory.getLogger(MongoImageController.class);
    private IDatabaseUpdater databaseUpdater;
    private ICatalog systemCatalog;
    private int imageControllerPort;

    public MongoImageController(IDatabaseUpdater databaseUpdater, ICatalog systemCatalog, int imageControllerPort) {
        this.databaseUpdater = databaseUpdater;
        this.systemCatalog = systemCatalog;
        this.imageControllerPort = imageControllerPort;
        createHttpServer(this.imageControllerPort);
    }

    private void createHttpServer(int httpPort) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(httpPort), 0);
            logger.debug("Image controller endpoint starting at " + httpPort);
            server.createContext("/image", new ImageRequestHandler(this.systemCatalog));
            server.setExecutor(null);
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
                    result.set(new MongoUri("image",imageId).toString());
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

    private class ImageRequestHandler implements HttpHandler {

        private final Observable<IOperator> imageTableObservable;
        private ICatalog catalog;

        public ImageRequestHandler(ICatalog catalog) {
            this.catalog = catalog;
            this.imageTableObservable = this.catalog.waitForOperatorAtThisPath(
                    TableNames.IMAGES_TABLE_NAME
            );
        }

        @Override
        public void handle(HttpExchange he) {
            logger.info("Handling image request {}",he);
            imageTableObservable.take(1).subscribe(
                    res -> {
                        try {
                            KeyedTable table  = (KeyedTable)res;
                            logger.info("Got table trying to get request from uri {}",he.getRequestURI());
                            MongoUri imageUrl = new MongoUri(he.getRequestURI());
                            int row = table.getRow(new TableKey(imageUrl.getImageId()));
                            if(row == -1){
                                logger.info("Unable to find image for uri {}",he.getRequestURI());
                                he.sendResponseHeaders(404, 0);
                            }else{
                                logger.info("Found image for uri {}",he.getRequestURI());
                                String imageData = (String) ColumnHolderUtils.getColumnValue(table,"imageData",row);
                                byte[] bytes = Base64.decodeBase64(imageData);
                                he.sendResponseHeaders(200, bytes.length);
                                he.getResponseHeaders().add("Content-Type", " image/jpeg");
                                OutputStream os = he.getResponseBody();
                                os.write(bytes);
                                os.close();
                            }
                        } catch (IOException e) {
                            logger.error("Problem handling image request",e);
                        }
                    },
                    err -> {
                      logger.error("Issue getting image table table {}",err);
                    }
            );

        }
    }


}
