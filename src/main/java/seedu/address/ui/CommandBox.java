package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.NewResultAvailableEvent;
import seedu.address.logic.ListElementPointer;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final Logger logger = LogsCenter.getLogger(CommandBox.class);
    private final Logic logic;
    private ListElementPointer historySnapshot;

    private StackPane helperContainer;
    private CommandBoxHelper commandBoxHelper;
    private Boolean helpEnabled = false;

    @FXML
    private TextField commandTextField;

    public CommandBox(Logic logic, StackPane commandBoxHelp) {
        super(FXML);
        this.logic = logic;
        this.commandBoxHelper = new CommandBoxHelper(logic);
        this.helperContainer = commandBoxHelp;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> {
            setStyleToDefault();

            /**old code
            //shows helper if there is text in the command text field
            if (!commandTextField.getText().trim().isEmpty() && !helpEnabled) {
                showHelper();
            } else if (commandTextField.getText().trim().isEmpty()) {
                hideHelper();
            }**/

            if (commandBoxHelper.listHelp(commandTextField) && !helpEnabled) {
                showHelper();
            } else if (!commandBoxHelper.listHelp(commandTextField)) {
                hideHelper();
            }
        });
        commandTextField.setStyle("-fx-font-style: italic;" + " -fx-text-fill: lime");
        historySnapshot = logic.getHistorySnapshot();
    }

    /**
     * Handles the key press event, {@code keyEvent}.
     */
    @FXML
    private void handleKeyPress(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
        case UP:
            //Check if CLI has any text within to trigger commandboxhelper first
            if (!commandBoxHelper.checkEmpty()) {
                commandBoxHelper.selectUpHelperBox();
            } else {
                // As up and down buttons will alter the position of the caret,
                // consuming it causes the caret's position to remain unchanged
                keyEvent.consume();
                navigateToPreviousInput();
            }
            break;
        case DOWN:
            //Check if CLI has any text within to trigger commandboxhelper first
            if (!commandBoxHelper.checkEmpty()) {
                commandBoxHelper.selectDownHelperBox();
            } else {
                keyEvent.consume();
                navigateToNextInput();
            }
            break;
        case TAB:
            if (helperContainer.getChildren().contains(commandBoxHelper.getRoot())
                    && commandBoxHelper.isMainSelected()) {
                try {
                    commandTextField.setText(commandBoxHelper.getHelperText());
                    hideHelper();
                } catch (Exception e) {
                    logger.info(e.getMessage() + "Nothing selected in command helper");
                }
            }
            keyEvent.consume();
            break;
        case BACK_SPACE:
            if (commandTextField.getText().trim().length() <= 0 || !commandBoxHelper.listHelp(commandTextField)) {
                hideHelper();
                logger.info("Hiding command helper");
            }
            break;
        default:
            // let JavaFx handle the keypress
        }
    }

    /**
     * Updates the text field with the previous input in {@code historySnapshot},
     * if there exists a previous input in {@code historySnapshot}
     */
    private void navigateToPreviousInput() {
        assert historySnapshot != null;
        if (!historySnapshot.hasPrevious()) {
            return;
        }

        replaceText(historySnapshot.previous());
    }

    /**
     * Updates the text field with the next input in {@code historySnapshot},
     * if there exists a next input in {@code historySnapshot}
     */
    private void navigateToNextInput() {
        assert historySnapshot != null;
        if (!historySnapshot.hasNext()) {
            return;
        }

        replaceText(historySnapshot.next());
    }

    /**
     * Sets {@code CommandBox}'s text field with {@code text} and
     * positions the caret to the end of the {@code text}.
     */
    private void replaceText(String text) {
        commandTextField.setText(text);
        commandTextField.positionCaret(commandTextField.getText().length());
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandInputChanged() {
        try {
            if(helperContainer.getChildren().contains(commandBoxHelper.getRoot())
                    && commandBoxHelper.isMainSelected()) {
                commandTextField.setText(commandBoxHelper.getHelperText());
                hideHelper();
            } else {
                hideHelper();
                CommandResult commandResult = logic.execute(commandTextField.getText());
                initHistory();
                historySnapshot.next();
                // process result of the command
                commandTextField.setText("");
                logger.info("Result: " + commandResult.feedbackToUser);
                raise(new NewResultAvailableEvent(commandResult.feedbackToUser, false));
            }

        } catch (CommandException | ParseException e) {
            initHistory();
            // handle command failure
            setStyleToIndicateCommandFailure();
            logger.info("Invalid command: " + commandTextField.getText());
            raise(new NewResultAvailableEvent(e.getMessage(), true));
        }
    }

    /**
     * Initializes the history snapshot.
     */
    private void initHistory() {
        historySnapshot = logic.getHistorySnapshot();
        // add an empty string to represent the most-recent end of historySnapshot, to be shown to
        // the user if she tries to navigate past the most-recent end of the historySnapshot.
        historySnapshot.add("");
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }
        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Shows the command helper
     */
    private void showHelper() {
        helperContainer.getChildren().add(commandBoxHelper.getRoot());
        helpEnabled = true;
    }

    /**
     * Hides the command helper
     */
    private void hideHelper() {
        helperContainer.getChildren().remove(commandBoxHelper.getRoot());
        helpEnabled = false;
    }
}
