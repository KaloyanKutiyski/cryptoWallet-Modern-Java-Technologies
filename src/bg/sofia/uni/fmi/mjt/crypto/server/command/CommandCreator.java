package bg.sofia.uni.fmi.mjt.crypto.server.command;

import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CommandCreatorException;

public class CommandCreator {
    public Command commandOf(final String commandString) throws CommandCreatorException {
        String[] tokens = commandString.strip().split(" ");

        if (commandString.strip().length() == 0 || tokens.length == 0) {
            return null;
        }
        if (tokens.length > 3) {
            throw new CommandCreatorException("too many parameters");
        }

        String keyWord = tokens[0];
        String currencyOrUsername = null;
        String password = null;
        Double quantity = null;

        if (tokens.length > 1) {
            try {
                quantity = Double.valueOf(tokens[1]);
            } catch (NumberFormatException e) {
                currencyOrUsername = tokens[1];
            }
        }

        if (tokens.length > 2) {
            if (currencyOrUsername == null) {
                throw new CommandCreatorException(
                        "wrong parameter order or two numeric parameters");
            }
            try {
                quantity = Double.valueOf(tokens[2]);
            } catch (NumberFormatException e) {
                password = tokens[2];
            }
        }

        return new Command(keyWord, currencyOrUsername, password, quantity);
    }
}
