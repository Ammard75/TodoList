package com.ondruska.todolist;

import datamodel.TodoData;
import datamodel.TodoItem;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.Iterator;

public class DialogController {
    //premenne prejednotlive data dialogu, ktore chceme ziskat
    @FXML
    private TextField shortDescription;
    @FXML
    private TextArea detailsArea;
    @FXML
    private DatePicker deadlinePicker;

    //metoda, ktora vyberie data z dialogu, vytovrime pomocou nich novy item a prida ho do listu
    //taktiez vracia hodnotu noveho itemu, aby sme mohli ziskat poziciu posledneho pridaneho itemu
    public TodoItem processResults() {
        String shortDes = shortDescription.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();

        TodoItem newItem = new TodoItem(shortDes, details, deadlineValue);
        TodoData.getInstance().addTodoItem(newItem);
        return newItem;
    }

    public TodoItem updatedResults(TodoItem selectedItem) {
        String shortDes = shortDescription.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();
        ObservableList<TodoItem> list = TodoData.getInstance().getTodoItems();
        TodoItem updatedItem = new TodoItem(shortDes, details, deadlineValue);

        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(selectedItem)) {
                TodoData.getInstance().updateTodoItem(list.get(i), updatedItem);
            }
        }
        return updatedItem;
    }
}
