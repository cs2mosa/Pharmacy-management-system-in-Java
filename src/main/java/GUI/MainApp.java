package GUI;

import Class_model.Item;
import Service_Interfaces.Inventory_service;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.HashSet;

public class MainApp extends Application {

    private BorderPane rootLayout;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Pharmacy Management System");

        // Initialize the root layout
        rootLayout = new BorderPane();
        Scene scene = new Scene(rootLayout, 800, 600);
        primaryStage.setScene(scene);

        // Load the entry page
        loadPage("Entry" , null);

        primaryStage.show();
    }

    void loadPage(String pageName, String PatientName) {
        switch (pageName) {
            case "Entry":
                rootLayout.setCenter(new EntryView(this));
                break;
            case "PatientLogin":
                rootLayout.setCenter(new PatientLoginView(this));
                break;
            case "PharmacistLogin":
                rootLayout.setCenter(new PharmacistLoginView(this));
                break;
            case "PatientSignup":
                rootLayout.setCenter(new PatientSignupView(this));
                break;
            case "PharmacistSignup":
                rootLayout.setCenter(new PharmacistSignupView(this));
                break;
            case "PatientProfile":
                // Assuming we have methods to get the current patient's ID and name
                int patientId = Patient_Service.getInstance().GetPatient(PatientName).getID();
                rootLayout.setCenter(new PatientProfileView(this, patientId, PatientName));
                break;
            case "Order":
                rootLayout.setCenter(new OrderView(this , PatientName));
                break;
            case "Payment":
                // Assuming we have a method to get the current patient's ID and name
                int PatientId = Patient_Service.getInstance().GetPatient(PatientName).getID();
                rootLayout.setCenter(new PaymentView(this, PatientId, PatientName));
                break;
            case "PharmacistProfile":
                rootLayout.setCenter(new PharmacistProfileView(this,PatientName));
                break;
            case "ReceivedOrders":
                rootLayout.setCenter(new ReceivedOrdersView(this,PatientName));
                break;
            case "Inventory":
                rootLayout.setCenter(new InventoryView(this , PatientName));
                break;
            default:
                rootLayout.setCenter(new EntryView(this));
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        try {
            Item item1 = new Item.builder()
                    .setMedicName("Medic1")
                    .setPrice(10.0)
                    .setExpireDate("31/12/2026")
                    .setQuantity(10)
                    .setUsage("Take 1 tablet every 6 hours")
                    .setSideEffects(new HashSet<>())
                    .setHealingEffects(new HashSet<>())
                    .setCategory("cats")
                    .build();

            Item item2 = new Item.builder()
                    .setMedicName("Medic2")
                    .setPrice(10.0)
                    .setExpireDate("31/12/2026")
                    .setQuantity(10)
                    .setUsage("Take 1 tablet every 12 hours")
                    .setSideEffects(new HashSet<>())
                    .setHealingEffects(new HashSet<>())
                    .setCategory("cats")
                    .build();

            Item item3 = new Item.builder()
                    .setMedicName("Medic3")
                    .setPrice(10.0)
                    .setExpireDate("31/12/2026")
                    .setQuantity(10)
                    .setUsage("Take 1 tablet every 6 hours")
                    .setSideEffects(new HashSet<>())
                    .setHealingEffects(new HashSet<>())
                    .setCategory("cats")
                    .build();

            Inventory_service.getInstance().AddNewItem(item1);
            Inventory_service.getInstance().AddNewItem(item2);
            Inventory_service.getInstance().AddNewItem(item3);
        } catch (Exception ex) {
            System.err.println("Optional demo seed skipped (is the Pharmacy API running and the database migrated?). " + ex.getMessage());
        }

        launch(args);
    }
}