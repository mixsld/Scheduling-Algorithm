public final class InputCheck {
    static final class InputException extends Exception {
        InputException(String message) {
            super(message);
        }
    }

    static int parseInt(String raw, String label, int min, int max) throws InputException {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            throw new InputException(label + " is empty. Enter a whole number from " + min + " to " + max + ".");
        }
        if (!text.matches("[+-]?\\d+")) {
            throw new InputException(label + " must be a whole number (you entered \"" + text + "\").");
        }
        long value;
        try {
            value = Long.parseLong(text);
        } catch (NumberFormatException tooLong) {
            value = text.startsWith("-") ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
        if (value < min || value > max) {
            throw new InputException(label + " must be between " + min + " and " + max
                    + " (you entered " + text + ").");
        }
        return (int) value;
    }
}
