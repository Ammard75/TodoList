package com.ondruska.todolist;

import datamodel.TodoData;
import datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    @FXML
    private ListView<TodoItem> todoListDescription;
    @FXML
    private TextArea todoListDetails;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<TodoItem> filteredList;
    private Predicate<TodoItem> wantAllItems;
    private Predicate<TodoItem> wantTodaysItems;

    //kod s vykona pri spusteni aplikacie
    public void initialize() {
        //vytvori nove context menu(pop up menu)
        listContextMenu = new ContextMenu();
        //vytvori item Delete v nasom context menu
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem updateMenuItem = new MenuItem("Update");

        //vytvorime listener, ktory sa vykona po kliknuti pravym tlacidlom na item v liste pripomienok
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //vyberieme item, ktory mame oznaceni a vymazeme ho pomocou vytvorenej metody deleteItem
                TodoItem item = todoListDescription.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        updateMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TodoItem item = todoListDescription.getSelectionModel().getSelectedItem();
                updateItem(item);
            }
        });

        //pridame vytvorenu delete moznost do nasho context menu
        listContextMenu.getItems().addAll(deleteMenuItem);
        listContextMenu.getItems().addAll(updateMenuItem);

        //cakame na zmenu stavu v todoListDescription, po zmene sa vykona kod a vypisu sa detaily oznaceneho itemu z listu
        todoListDescription.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observableValue, TodoItem todoItem, TodoItem newValue) {
                //ak newValue nie je null tak ziskame oznaceny item a vypiseme jeho detaily v TextArea
                if (newValue != null) {
                    TodoItem item = todoListDescription.getSelectionModel().getSelectedItem();
                    todoListDetails.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MMMM.yyyy");
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });

        wantAllItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return true;
            }
        };

        wantTodaysItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return (todoItem.getDeadline().equals(LocalDate.now()));
            }
        };

        //list na filtrovanie itemov, filtrovanie podla konecneho datumu pripomienky
        filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(), wantAllItems);
        //usporiadany list nasich itemov
        SortedList<TodoItem> soredList = new SortedList<TodoItem>(filteredList, new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });
        //naplnenie ListView komponentu a nastavenie moznosti vyberu: po jednom
        //setItems() funguje ako event, vzdy ked sa zmeni nas list(pridame alebo z neho vymazeme udaje) tak sa updatne a znova vypise cely
        todoListDescription.setItems(soredList);
        todoListDescription.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListDescription.getSelectionModel().select(0);

        //
        todoListDescription.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(TodoItem todoItem, boolean b) {
                        super.updateItem(todoItem, b);
                        if (b) {
                            setText(null);
                        } else {
                            setText(todoItem.getShortDescription());
                            if (todoItem.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.RED);
                            } else if (todoItem.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.ORANGE);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        });
                return cell;
            }
        });


    }

    @FXML
    private void updateItem(TodoItem item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Update selected item");
        dialog.setHeaderText("Use this dialog to updated the selected item.");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e) {
            System.out.println("Could not load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();

            TodoItem newItem = controller.updatedResults(todoListDescription.getSelectionModel().getSelectedItem());
            todoListDescription.getSelectionModel().select(newItem);
        }
    }

    @FXML
    public void showNewItemDialog() {
        //vytvorime instanciu dialogu
        Dialog<ButtonType> dialog = new Dialog<>();
        //nastavime okno, v ktorom sa vytvori dialog, nastavime titulok a text na vrchu dialogu(headerText)
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Todo Item");
        dialog.setHeaderText("Use this dialog to create a new item.");
        //fxmlLoader sluzi na nacitanie ineho fxml suboru, v tomto pripade nacita fxml subor Dialogu
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            //do dialogu sa nacita GUI z todoItemDialog.fxml suboru
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e) {
            System.out.println("Could not load the dialog");
            e.printStackTrace();
            return;
        }
        //pridanie predvytvorenych tlacidiel OK a CANCEL do nasho dialogu
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        //dialog sa ukaze a caka na stlacenie tlacidla OK alebo CANCEL(caka na vysledok)
        Optional<ButtonType> result = dialog.showAndWait();
        //ak je stlacene tlacidlo OK tak sa nacita controller classa dialogu z todoListItem.fxml suboru
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
            //vytvorime novy item a naplnime ho udajmi, ktore sme vpisali do nasich poli v dialogu(pomocou metody processResults)
            TodoItem newItem = controller.processResults();
            //oznacime novo vytvoreny item
            todoListDescription.getSelectionModel().select(newItem);
        }

    }

    //method for deleting item using alerts, to make you they really want to delete selected item
    //then we make Optional variable and make appear and wait for result
    //if user click OK then the selected item will be deleted, if they click cancel, then the item will stay and alert will close
    public void deleteItem(TodoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deleting selected item");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm or Cancel to back out.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoData.getInstance().deleteTodoItem(item);
        }
    }

    //ulozime oznaceny item do premennej a skontrolujeme ci user stlacil tlacidlo delete a ak ho stlacil tak ten item vymazeme
    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        TodoItem selectedItem = todoListDescription.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }

    //metoda na filtrovanie udajov pomocou togglebuttonu
    @FXML
    public void handleFilterButton() {
        //ak je toggleButton zakliknuty, tak vratime len itemy, ktore maju konecny datum dnes
        TodoItem selectedItem = todoListDescription.getSelectionModel().getSelectedItem();
        if (filterToggleButton.isSelected()) {
            filteredList.setPredicate(wantTodaysItems);
            if (filteredList.isEmpty()) {
                todoListDetails.clear();
                deadlineLabel.setText("");
            } else if (filteredList.contains(selectedItem)) {
                todoListDescription.getSelectionModel().select(selectedItem);
            } else {
                todoListDescription.getSelectionModel().select(0);
            }
        } else {
            //ak nie je toggleButton zakliknuty tak chceme vidiet vsetky itemy, co znamena ze predicate vrati true.
            filteredList.setPredicate(wantAllItems);
            todoListDescription.getSelectionModel().select(selectedItem);
        }
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }
}
