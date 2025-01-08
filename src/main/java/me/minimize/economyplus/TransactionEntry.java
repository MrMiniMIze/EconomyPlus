package me.minimize.economyplus;

/**
 * (Optional) A simple data object to represent a transaction in code.
 * This is not strictly required since we store directly in YAML,
 * but it can help if you want to work with transactions in memory.
 */
public class TransactionEntry {

    private final String date;
    private final String type;      // e.g., "PAY", "ADMIN_SET", ...
    private final String from;      // who initiated or source
    private final String to;        // target (player/faction)
    private final double amount;    // amount of money/points
    private final String currency;  // "MONEY" or "FACTION_POINTS"

    public TransactionEntry(String date, String type, String from, String to, double amount, String currency) {
        this.date = date;
        this.type = type;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.currency = currency;
    }

    // Standard getters
    public String getDate() {
        return date;
    }
    public String getType() {
        return type;
    }
    public String getFrom() {
        return from;
    }
    public String getTo() {
        return to;
    }
    public double getAmount() {
        return amount;
    }
    public String getCurrency() {
        return currency;
    }
}
