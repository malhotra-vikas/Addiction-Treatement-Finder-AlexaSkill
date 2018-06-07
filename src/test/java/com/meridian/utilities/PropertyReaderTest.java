package test.java.com.meridian.utilities;

import main.java.com.meridian.utilities.PropertyReader;

public class PropertyReaderTest {
    public static void main(String[] args) {
        PropertyReader propertyReader = PropertyReader.getPropertyReader();

        System.out.println(propertyReader.getGoodBye());
    }

}
