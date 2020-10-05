package datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

//Singleton classa, neda sa vytvorit jej instancia inou triedou
public class TodoData {
    //vytvorili sme instanciu
    private static final TodoData instance = new TodoData();
    //vytvorenie nazvu suboru, s ktorym budeme pracovat
    private static final String filename = "TodoListItems.txt";

    //list itemov a premenna na formatovanie datumu, pouzijeme ObservableList
    private ObservableList<TodoItem> todoItems;
    private final DateTimeFormatter formatter;

    //getter pre vytvorenu instanciu
    public static TodoData getInstance() {
        return instance;
    }

    //privatny konstruktor, vdaka ktoremu nemozeme vytvorit instanciu tejto classy v inej classe
    private TodoData() {
        //nastavenie formatovania
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    //pridanie itemu do nasho todoListu
    public void addTodoItem(TodoItem item) {
        todoItems.add(item);
    }

    //getter pre nas list itemov
    public ObservableList<TodoItem> getTodoItems() {
        return todoItems;
    }

    //metoda pre nacotanie itemov zo suboru
    public void loadTodoItems() throws IOException {
        //todoItems zmenime na observableArraylist, aby sme mohli pouzit metodu setItems() v Controlleri
        todoItems = FXCollections.observableArrayList();
        //vytvorime cestu k nasmu suboru
        Path path = Paths.get(filename);
        //vytvorime bufferedReader na citanie suboru
        BufferedReader br = Files.newBufferedReader(path);

        String input;

        //pokial subor obsahuje riadky na citanie, vytvorime pole, do ktoreho ulozime rozsekany citany riadok, nastavime datum
        //vytvorime novy item pomocou dát z citaneho suboru a tento item ulozime do nasho listu.
        try {
            while ((input = br.readLine()) != null) {
                String[] itemPieces = input.split("\t");
                String shortDescription = itemPieces[0];
                String details = itemPieces[1];
                String dateString = itemPieces[2];

                LocalDate date = LocalDate.parse(dateString, formatter);

                TodoItem todoItem = new TodoItem(shortDescription, details, date);
                todoItems.add(todoItem);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    //ulozime udaje z listu do suboru, vytvorime iterator a prejdeme vsetky itemy z listu. pomocou bufferedWritera a
    //String.format ulozime udaje kazdeho itemu do suboru
    public void storeTodoItems() throws IOException {
        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);

        try {
            Iterator<TodoItem> i = todoItems.iterator();
            while (i.hasNext()) {
                TodoItem item = i.next();
                bw.write(String.format("%s\t%s\t%s", item.getShortDescription(), item.getDetails(), item.getDeadline().format(formatter)));
                bw.newLine();
            }

        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    //delete item from list
    public void deleteTodoItem(TodoItem item) {
        todoItems.remove(item);
    }

    public void updateTodoItem(TodoItem oldItem, TodoItem updatedItem) {
        for (int i = 0; i < todoItems.size(); i++) {
            if (todoItems.get(i).equals(oldItem)) {
                todoItems.set(i, updatedItem);
            }
        }
    }


}
