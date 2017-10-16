package features;

public enum Vars {
    KEY_URI("URI"),
    KEY_CONFIG_PAYLOAY("PAYLOAD"),
    KEY_PROCESS_ID("PROCESS_ID");

    private final String _value;

    Vars(final String text) {
        _value = text;
    }

    @Override
    public String toString() {
        return _value;
    }
}
