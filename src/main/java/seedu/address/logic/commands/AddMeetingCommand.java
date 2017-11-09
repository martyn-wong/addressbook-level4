package seedu.address.logic.commands;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Exceptions.DuplicateMeetingException;
import seedu.address.model.Exceptions.IllegalIdException;
import seedu.address.model.Meeting;
import seedu.address.model.ReadOnlyMeeting;
import seedu.address.model.UniqueMeetingList;

import java.time.format.DateTimeParseException;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.*;
//@@author Sri-vatsa
/**
 * Adds a new meeting to the address book.
 */
public class AddMeetingCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "addMeeting";
    public static final String COMMAND_ALIAS = "am";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a meeting to the address book. "
            + "Parameters: "
            + PREFIX_DATE + "DATE "
            + PREFIX_TIME + "TIME "
            + PREFIX_LOCATION + "LOCATION "
            + PREFIX_NOTES + "NOTES "
            + PREFIX_PERSON + "PERSON 1 "
            + PREFIX_PERSON + "PERSON 2 ...\n"
            + "Example: "
            + COMMAND_WORD + " "
            + PREFIX_DATE + "20/11/2017 "
            + PREFIX_TIME + "1800 "
            + PREFIX_LOCATION + "UTown Starbucks "
            + PREFIX_NOTES + "Project Meeting "
            + PREFIX_PERSON + "1";


    public static final String MESSAGE_SUCCESS = "New meeting added!";
    public static final String MESSAGE_DUPLICATE_MEETING = "This meeting already exists in the address book";
    public static final String MESSAGE_INVALID_ID = "Please input a valid person id!";
    public static final String MESSAGE_TEMPLATE = COMMAND_WORD
            + PREFIX_DATE + "DATE "
            + PREFIX_TIME + "TIME "
            + PREFIX_LOCATION + "LOCATION "
            + PREFIX_NOTES + "NOTES "
            + PREFIX_PERSON + "PERSON ...";


    private final Meeting toAdd;

    public AddMeetingCommand(ReadOnlyMeeting meeting) {toAdd = new Meeting(meeting);}


    @Override
    protected CommandResult executeUndoableCommand() throws CommandException {
        requireNonNull(model);
        try {
            model.addMeeting(toAdd);
        } catch (DuplicateMeetingException e) {
            throw new CommandException(MESSAGE_DUPLICATE_MEETING);
        } catch (IllegalIdException ive) {
            throw new CommandException(MESSAGE_INVALID_ID);
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));

    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddMeetingCommand // instanceof handles nulls
                && toAdd.equals(((AddMeetingCommand) other).toAdd));
    }
}

