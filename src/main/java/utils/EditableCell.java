package utils;

import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class EditableCell<S, T> extends TextFieldTableCell<S, T> {
    private TextField textField;
    private boolean escapePressed = false;
    private TablePosition<S, ?> tablePos = null;

    public EditableCell(final StringConverter<T> converter) {
        super(converter);
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>>
    forTableColumn(final StringConverter<T> converter) {
        return list -> new EditableCell<>(converter);
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();
        if (isEditing()) {
            if (textField == null) {
                textField = getTextField();
            }
            escapePressed = false;
            startEdit(textField);
            tablePos = getTableView().getEditingCell();
        }
    }

    @Override
    public void commitEdit(T newValue) {
        if (!isEditing())
            return;

        if (newValue == null) {
            super.cancelEdit();
            return;
        }

        final TableView<S> table = getTableView();
        if (table != null) {
            // Inform the TableView of the edit being ready to be committed.
            @SuppressWarnings("unchecked")
            TableColumn.CellEditEvent editEvent = new TableColumn.CellEditEvent(
                    table,
                    tablePos,
                    TableColumn.editCommitEvent(),
                    newValue
            );
            if (getTableColumn() != null) {
                Event.fireEvent(getTableColumn(), editEvent);
            }
        }
        // we need to setEditing(false):
        super.cancelEdit(); // this fires an invalid EditCancelEvent.
        // update the item within this cell, so that it represents the new value
        updateItem(newValue, false);
        if (table != null) {
            // reset the editing cell on the TableView
            table.edit(-1, null);
        }
    }

    @Override
    public void cancelEdit() {
        if (escapePressed) {
            // this is a cancel event after escape key
            super.cancelEdit();
            setText(getConverter().toString(getItem())); // restore the original text in the view
        } else {
            // this is not a cancel event after escape key
            // we interpret it as commit.
            String newText = textField.getText();
            this.commitEdit(getConverter().fromString(newText));
        }
        setGraphic(null); // stop editing with TextField
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        updateItem();
    }

    private TextField getTextField() {
        final TextField textField = new TextField(getConverter().toString(getItem()));

        // Use onAction here rather than onKeyReleased (with check for Enter),
        textField.setOnAction(event -> {
            this.commitEdit(getConverter().fromString(textField.getText()));
            event.consume();
        });
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                commitEdit(getConverter().fromString(textField.getText()));
            }
        });
        textField.setOnKeyPressed(t -> escapePressed = t.getCode() == KeyCode.ESCAPE);
        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                textField.setText(getConverter().toString(getItem()));
                cancelEdit();
                event.consume();
            } else if (event.getCode() == KeyCode.TAB) {
                getTableView().getSelectionModel().selectBelowCell();
                event.consume();
            }
        });
        return textField;
    }

    private void updateItem() {
        if (isEmpty()) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getConverter().toString(getItem()));
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getConverter().toString(getItem()));
                setGraphic(null);
            }
        }
    }

    private void startEdit(final TextField textField) {
        if (textField != null) {
            textField.setText(getConverter().toString(getItem()));
        }
        setText(null);
        setGraphic(textField);
        if (textField != null) {
            textField.selectAll();
            textField.requestFocus();
        }
    }
}
