import bg.sofia.uni.fmi.mjt.crypto.server.command.Command;
import bg.sofia.uni.fmi.mjt.crypto.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CommandCreatorException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CommandCreatorTest {
    private CommandCreator commandCreator = new CommandCreator();
    private static final String KEY_WORD = "key";
    private static final String FIRST_STRING_PARAMETER = "first_string";
    private static final String SECOND_STRING_PARAMETER = "second_string";
    private static final Double NUMERIC_PARAMETER = 12.34;

    @Test
    public void commandOfEmptyIsNullTest() throws CommandCreatorException {
        assertNull(commandCreator.commandOf(""));
    }

    @Test
    public void commandOfBlankIsNullTest() throws CommandCreatorException {
        assertNull(commandCreator.commandOf("       "));
    }

    @Test
    public void commandWithoutParametersTest() throws CommandCreatorException {
        Command command = commandCreator.commandOf(KEY_WORD);
        assertEquals(command.keyWord(), KEY_WORD);
        assertNull(command.currencyOrUsername());
        assertNull(command.password());
        assertNull(command.quantity());
    }

    @Test
    public void commandOfOneStringParameterTest() throws CommandCreatorException {
        Command command = commandCreator.commandOf(
                KEY_WORD + " " + FIRST_STRING_PARAMETER);
        assertEquals(command.keyWord(), KEY_WORD);
        assertEquals(command.currencyOrUsername(), FIRST_STRING_PARAMETER);
        assertNull(command.password());
        assertNull(command.quantity());
    }

    @Test
    public void commandOfTwoStringParametersTest() throws CommandCreatorException {
        Command command = commandCreator.commandOf(
                KEY_WORD + " "
                        + FIRST_STRING_PARAMETER + " "
                        + SECOND_STRING_PARAMETER);
        assertEquals(command.keyWord(), KEY_WORD);
        assertEquals(command.currencyOrUsername(), FIRST_STRING_PARAMETER);
        assertEquals(command.password(), SECOND_STRING_PARAMETER);
        assertNull(command.quantity());
    }

    @Test
    public void commandOfOneNumericParameterTest() throws CommandCreatorException {
        Command command = commandCreator.commandOf(
                KEY_WORD + " " + NUMERIC_PARAMETER);
        assertEquals(command.keyWord(), KEY_WORD);
        assertNull(command.currencyOrUsername());
        assertNull(command.password());
        assertEquals(command.quantity(), NUMERIC_PARAMETER);
    }

    @Test
    public void commandOfOneNumericAndOneStringParameterCorrectOrderTest()
            throws CommandCreatorException {
        Command command = commandCreator.commandOf(
                KEY_WORD + " " + FIRST_STRING_PARAMETER
                        + " " + NUMERIC_PARAMETER);
        assertEquals(command.keyWord(), KEY_WORD);
        assertEquals(command.currencyOrUsername(), FIRST_STRING_PARAMETER);
        assertNull(command.password());
        assertEquals(command.quantity(), NUMERIC_PARAMETER);
    }

    @Test(expected = CommandCreatorException.class)
    public void commandOfOneNumericAndOneStringParameterWrongOrderTest()
            throws CommandCreatorException {
        Command command = commandCreator.commandOf(
                KEY_WORD + " " + NUMERIC_PARAMETER
                        + " " + FIRST_STRING_PARAMETER);
    }

    @Test(expected = CommandCreatorException.class)
    public void tooManyParametersTest() throws CommandCreatorException {
        Command command = commandCreator.commandOf(
                KEY_WORD + " " + FIRST_STRING_PARAMETER
                        + " " + SECOND_STRING_PARAMETER + " " + NUMERIC_PARAMETER);
    }
}
