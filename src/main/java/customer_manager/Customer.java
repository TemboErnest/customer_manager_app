package customer_manager;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Customer {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty province = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty notes = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> createdDate = new SimpleObjectProperty<>(LocalDate.now());

    public Customer(String name, String province, String phone, String email, String notes) {
        this.name.set(name);
        this.province.set(province);
        this.phone.set(phone == null ? "" : phone);
        this.email.set(email == null ? "" : email);
        this.notes.set(notes == null ? "" : notes);
    }

    public Customer(String name, String province) {
        this(name, province, "", "", "");
    }

    public Customer() {
        this("", "", "", "", "");
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getProvince() {
        return province.get();
    }

    public void setProvince(String value) {
        province.set(value);
    }

    public StringProperty provinceProperty() {
        return province;
    }

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String value) {
        phone.set(value);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String value) {
        email.set(value);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getNotes() {
        return notes.get();
    }

    public void setNotes(String value) {
        notes.set(value);
    }

    public StringProperty notesProperty() {
        return notes;
    }

    public LocalDate getCreatedDate() {
        return createdDate.get();
    }

    public void setCreatedDate(LocalDate value) {
        createdDate.set(value);
    }

    public ObjectProperty<LocalDate> createdDateProperty() {
        return createdDate;
    }

    public String toStorageString() {
        return String.join("|",
                escape(getName()),
                escape(getProvince()),
                escape(getPhone()),
                escape(getEmail()),
                escape(getNotes()),
                getCreatedDate().toString());
    }

    public static Customer fromStorageString(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) {
            return null;
        }

        Customer customer = new Customer(unescape(parts[0]), unescape(parts[1]), unescape(parts[2]), unescape(parts[3]), unescape(parts[4]));
        customer.setCreatedDate(LocalDate.parse(parts[5]));
        return customer;
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("|", "\\|");
    }

    private static String unescape(String value) {
        return value == null ? "" : value.replace("\\|", "|");
    }
}