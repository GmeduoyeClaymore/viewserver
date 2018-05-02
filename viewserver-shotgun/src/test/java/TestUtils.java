import org.h2.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class TestUtils{
    public static String getJsonStringFromFile(String dataFile) {
        InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(dataFile);
        if (inputStream == null) {
            throw new RuntimeException("Unable to find resource at at " + dataFile);
        }
        Reader reader = IOUtils.getBufferedReader(inputStream);
        try {
            return  IOUtils.readStringAndClose(reader, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
