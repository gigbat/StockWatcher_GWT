package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockWatcher implements EntryPoint {
    private static final int REFRESH_INTERVAL = 5000;

    private FlexTable flexTable;
    private VerticalPanel verticalPanel;
    private HorizontalPanel horizontalPanel;
    private Button button;
    private TextBox textBox;
    private Label label;
    private List<String> stocks;

    public StockWatcher() {
        this.flexTable = new FlexTable();
        this.verticalPanel = new VerticalPanel();
        this.horizontalPanel = new HorizontalPanel();
        this.button = new Button("Add");
        this.textBox = new TextBox();
        this.label = new Label();
        this.stocks = new ArrayList<>();
    }

    public void onModuleLoad() {
        flexTable.setText(0, 0, "Symbol");
        flexTable.setText(0, 1, "Price");
        flexTable.setText(0, 2, "Change");
        flexTable.setText(0, 3, "Remove");

        flexTable.getRowFormatter().addStyleName(0, "watchListHeader");
        flexTable.addStyleName("watchList");
        flexTable.getCellFormatter().addStyleName(0,1,"watchListNumericColumn");
        flexTable.getCellFormatter().addStyleName(0,2,"watchListNumericColumn");
        flexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");
        horizontalPanel.addStyleName("addPanel");

        joinTable();

        RootPanel.get("block").add(verticalPanel);

        textBox.setFocus(true);

        addHandler();
        addKey();

        Timer refreshTimer = new Timer() {
            @Override
            public void run() {
                refreshWatchList();
            }
        };
        refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
        flexTable.setCellPadding(6);
    }

    private void joinTable() {
        horizontalPanel.add(textBox);
        horizontalPanel.add(button);

        verticalPanel.add(flexTable);
        verticalPanel.add(horizontalPanel);
        verticalPanel.add(label);
    }

    private void addHandler() {
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addStock();
            }
        });
    }

    private void addKey() {
        textBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent keyDownEvent) {
                if (keyDownEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER) addStock();
            }
        });
    }

    private void addStock() {
        final String symbol = textBox.getText().toUpperCase().trim();
        textBox.setFocus(true);

        if (checkInput(symbol)) return;
        if (sameElement(symbol)) return;
        else {
            int row = flexTable.getRowCount();
            stocks.add(symbol);
            flexTable.setText(row, 0, symbol);
            addDeleteButton(symbol, row);

            flexTable.getCellFormatter().addStyleName(row,1,"watchListNumericColumn");
            flexTable.getCellFormatter().addStyleName(row,2,"watchListNumericColumn");
            flexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");
        }

        textBox.setText("");
        refreshWatchList();
    }

    private boolean checkInput(String symbol) {
        if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
            Window.alert("'" + symbol + "' is not valid symbol.");
            textBox.selectAll();
            return true;
        } else return false;
    }

    private boolean sameElement(String symbol) {
        if (stocks.contains(symbol)) {
            Window.alert("'" + symbol + "' has already exist.");
            textBox.selectAll();
            return true;
        } else return false;
    }

    private void addDeleteButton(String symbol, int row) {
        Button removeButton = new Button("x");
        final String sym = symbol;
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                int removeIndex = stocks.indexOf(sym);
                stocks.remove(removeIndex);
                flexTable.removeRow(removeIndex + 1);
            }
        });
        flexTable.setWidget(row, 3, removeButton);
    }

    private void refreshWatchList() {
        final double MAX_PRICE = 100.0; // 100.0$
        final double MAX_PRICE_CHANGE = 0.02; // +/- 2%

        StockPrice[] stockPrices = new StockPrice[stocks.size()];
        for (int i = 0; i < stocks.size(); i++) {
            double price = Random.nextDouble() * MAX_PRICE;
            double change = price * MAX_PRICE_CHANGE * (Random.nextDouble() * 2.0 - 1.0);

            stockPrices[i] = new StockPrice(stocks.get(i), price, change);
        }

        updateTable(stockPrices);
    }

    public void updateTable(StockPrice[] stockPrices) {
        for (int i = 0; i < stockPrices.length; i++) {
            updateTable(stockPrices[i]);
        }

        DateTimeFormat dateFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
        label.setText("Last update: " + dateFormat.format(new Date()));
    }

    public void updateTable(StockPrice stockPrices) {
      if (!stocks.contains(stockPrices.getSymbol())) return;

      int row = stocks.indexOf(stockPrices.getSymbol()) + 1;

      String priceText = NumberFormat.getFormat("#,##0.00").format(stockPrices.getPrice());
      NumberFormat numberFormat = NumberFormat.getFormat("+#,##0.00; -#,##0.00");
      String changeText = numberFormat.format(stockPrices.getPrice());
      String changeTextPercent = numberFormat.format(stockPrices.getChangePercent());

      flexTable.setText(row, 1, priceText);
      flexTable.setText(row, 2, changeText + "(" + changeTextPercent + "%)");
    }
}
