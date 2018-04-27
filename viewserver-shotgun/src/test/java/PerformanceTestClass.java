import com.fasterxml.jackson.databind.Module;
import com.shotgun.viewserver.servercomponents.OrderSerializationModule;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.databene.contiperf.PerfTest;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class PerformanceTestClass{
    static OrderSerializationModule orderSerializationModule = new OrderSerializationModule();

    static{
        orderSerializationModule.registerDynamicClass(UtilsTests.TestUnit.class);
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        orderSerializationModule
                });
    }

    public class ConcreteTestUnitHolder implements UtilsTests.TestUnitHolder {

        @Override
        public DynamicJsonBackedObject set(String propertyName, Object propertyValue) {
            return null;
        }

        @Override
        public String serialize(String... excludedFields) {
            return null;
        }

        @Override
        public Map<String,Object> getFields(String... excludedFields) {
            return null;
        }

        @Override
        public String get(String responseField) {
            return null;
        }

        @Override
        public UtilsTests.TestUnit[] getHolders() {
            return new UtilsTests.TestUnit[0];
        }
    }

    String json = "{\"holders\":[{\"name\":\"foo\"},{\"name\":\"bar\"}]}";
    UtilsTests.TestUnitHolder proxiedInstance = JSONBackedObjectFactory.create(json, UtilsTests.TestUnitHolder.class);
    UtilsTests.TestUnitHolder concreteInstance = new ConcreteTestUnitHolder();


    @Test
    @PerfTest(invocations = 1000, warmUp = 1000)
    public void invokeConcrete() {
        getAMillionRandomLongs(concreteInstance);
    }

    @Test
    @PerfTest(invocations = 1000, warmUp = 1000)
    public void invokeProxied() {
        getAMillionRandomLongs(proxiedInstance);
    }

    private void getAMillionRandomLongs(UtilsTests.TestUnitHolder generator) {
        for (int i = 0; i < 1000000; i++) {
            generator.getHolders();
        }
    }
}
