package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.maps.DirectionRequest;
import com.shotgun.viewserver.maps.LatLng;
import com.shotgun.viewserver.maps.MapsController;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.payments.PaymentController;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Controller(name = "journeyEmulatorController")
public class JourneyEmulatorController {
    private static final Logger log = LoggerFactory.getLogger(DriverController.class);
    private PaymentController paymentController;
    private MessagingController messagingController;
    private MapsController mapsController;

    public JourneyEmulatorController(MapsController mapsController) {
        this.mapsController = mapsController;
    }

    @ControllerAction(path = "emulateJourneyForOrder", isSynchronous = false)
    public void emulateJourneyForOrder(@ActionParam(name = "orderId") String orderId,
                                       @ActionParam(name = "emulator") String emulator,
                                       @ActionParam(name = "userId") String userId) {
        log.debug("Emulator journey for order: " + orderId);

        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        KeyedTable deliveryAddressTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_ADDRESS_TABLE_NAME);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(2000);

                    int currentOrderRow = orderTable.getRow(new TableKey(orderId));
                    String deliveryId = ControllerUtils.getColumnValue(orderTable, "deliveryId", currentOrderRow).toString();

                    Double driverLatitude = (Double) ControllerUtils.getColumnValue(userTable, "latitude", userId);
                    Double driverLongitude = (Double) ControllerUtils.getColumnValue(userTable, "longitude", userId);
                    String originDeliveryAddressId = (String) ControllerUtils.getColumnValue(deliveryTable, "originDeliveryAddressId", deliveryId);
                    String destinationDeliveryAddressId = (String) ControllerUtils.getColumnValue(deliveryTable, "destinationDeliveryAddressId", deliveryId);

                    Double originLat = (Double) ControllerUtils.getColumnValue(deliveryAddressTable, "latitude", originDeliveryAddressId);
                    Double originLng = (Double) ControllerUtils.getColumnValue(deliveryAddressTable, "longitude", originDeliveryAddressId);

                    ArrayList<LatLng> locations = new ArrayList<>();
                    locations.add(new LatLng(driverLatitude, driverLongitude));
                    locations.add(new LatLng(originLat, originLng));

                    if (destinationDeliveryAddressId != null) {
                        Double destLat = (Double) ControllerUtils.getColumnValue(deliveryAddressTable, "latitude", destinationDeliveryAddressId);
                        Double destLng = (Double) ControllerUtils.getColumnValue(deliveryAddressTable, "longitude", destinationDeliveryAddressId);
                        locations.add(new LatLng(destLat, destLng));
                    }

                    emulateJourney(emulator, new DirectionRequest(locations, "driving"));
                } catch (Exception ex) {
                    log.error("There was a problem emulating this journey", ex);
                }
            }
        });

        thread.start();
    }

    @ControllerAction(path = "emulateJourney", isSynchronous = false)
    public void emulateJourney(@ActionParam(name = "emulator") String emulator,
                               @ActionParam(name = "directionsRequest") DirectionRequest directionsRequest) {

        HashMap<String, Object> directions = mapsController.mapDirectionRequest(directionsRequest);

        try {
            ArrayList<LinkedHashMap> steps = (ArrayList) ((LinkedHashMap) ((ArrayList) ((LinkedHashMap) ((ArrayList) directions.get("routes")).get(0)).get("legs")).get(0)).get("steps");

            for (LinkedHashMap step : steps) {
                LinkedHashMap start = (LinkedHashMap) step.get("start_location");
                Double lat = (Double) start.get("lat");
                Double lng = (Double) start.get("lng");

                Runtime.getRuntime().exec(String.format("adb -s %s emu geo fix %s %s", emulator, lng, lat));
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
