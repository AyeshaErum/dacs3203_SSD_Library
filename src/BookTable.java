import javafx.beans.property.SimpleStringProperty;

public class BookTable {
    private SimpleStringProperty title;
    private SimpleStringProperty author;
    private SimpleStringProperty isbn;
    private SimpleStringProperty available;

    public BookTable(String title, String author, String isbn, String available) {
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.isbn = new SimpleStringProperty(isbn);
        this.available = new SimpleStringProperty(available);
    }

    public SimpleStringProperty titleProperty() { return title; }
    public SimpleStringProperty authorProperty() { return author; }
    public SimpleStringProperty isbnProperty() { return isbn; }
    public SimpleStringProperty availableProperty() { return available; }
}

