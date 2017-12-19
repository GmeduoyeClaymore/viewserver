import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.images.ImageController;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Scanner;


public class ImageControllerTest {

    private ImageController sut;


    @Before
    public void createSut(){
        sut = new ImageController(new BasicAWSCredentials("AKIAJ5IKVCUUR6JC7NCQ", "UYB3e20Jr5jmU7Yk57PzAMyezYyLEQZ5o3lOOrDu"));
    }

    @Test
    public void canUploadImage() throws FileNotFoundException {
        URL url = this.getClass().getResource("image_1.txt");
        String entireFileText = new Scanner(new File(url.getFile())).next();
        System.out.println(sut.saveToS3("shotgunclientimages", "testimages/test1.jpg", entireFileText));
    }
}