import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.images.ImageController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Scanner;

@Ignore
public class ImageControllerTest {

    private IImageController sut;


    @Before
    public void createSut(){
        sut = new ImageController(new BasicAWSCredentials("AKIAJ5IKVCUUR6JC7NCQ", "UYB3e20Jr5jmU7Yk57PzAMyezYyLEQZ5o3lOOrDu"), null, null);
    }

    @Test
    public void canUploadImage() throws FileNotFoundException {
        URL url = this.getClass().getResource("image_1.txt");
        String entireFileText = new Scanner(new File(url.getFile())).next();
        System.out.println(sut.saveImage("shotgunclientimages", "testimages/test1.jpg", entireFileText));
    }
}
