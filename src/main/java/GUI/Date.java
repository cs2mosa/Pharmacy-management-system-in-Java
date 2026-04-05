package GUI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Date {
    public static String getCurrentDateAsString(){
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return currentDate.format(formatter);
    }
}