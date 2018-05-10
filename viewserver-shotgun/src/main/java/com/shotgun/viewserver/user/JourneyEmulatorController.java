package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.maps.DirectionRequest;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.maps.LatLng;
import com.shotgun.viewserver.order.controllers.contracts.OrderCreationController;
import com.shotgun.viewserver.order.controllers.contracts.OrderTransformationController;
import com.shotgun.viewserver.order.domain.JourneyOrder;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.controller.Controller;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Controller(name = "journeyEmulatorController")
public class JourneyEmulatorController implements OrderTransformationController{
    private static final Logger log = LoggerFactory.getLogger(PartnerController.class);
    private IMapsController IMapsController;
    private IDatabaseUpdater databaseUpdater;

    public JourneyEmulatorController(IMapsController IMapsController, IDatabaseUpdater databaseUpdater) {
        this.IMapsController = IMapsController;
        this.databaseUpdater = databaseUpdater;
    }

    public void emulateJourneyForOrder(String orderId, String emulator, String userId) {
        log.debug("Emulator journey for order: " + orderId);

        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        KeyedTable deliveryAddressTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_ADDRESS_TABLE_NAME);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    JourneyOrder order = getOrderForId(orderId, JourneyOrder.class);
                    ArrayList<LatLng> locations = new ArrayList<>();
                    locations.add(order.getOrigin().getLatLong());
                    if(order.getOrigin() == null){
                        throw new RuntimeException("Cannot simulate journey as no origin found");
                    }
                    if(order.getDestination() != null){
                        locations.add(order.getDestination().getLatLong());
                    }
                    emulateJourney(emulator, new DirectionRequest((LatLng[]) locations.toArray(new LatLng[0]), "driving"));
                } catch (Exception ex) {
                    log.error("There was a problem emulating this journey", ex);
                }
            }
        });

        thread.start();
    }

    public void emulateJourney(String emulator, DirectionRequest directionsRequest) {
        HashMap<String, Object> directions = IMapsController.mapDirectionRequest(directionsRequest);

        try {
            System.out.println("Path is - " + System.getenv("PATH"));
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

    @Override
    public IDatabaseUpdater getDatabaseUpdater() {
        return databaseUpdater;
    }
}
