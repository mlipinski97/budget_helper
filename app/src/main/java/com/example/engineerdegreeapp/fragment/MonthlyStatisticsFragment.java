package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.BudgetListApi;
import com.example.engineerdegreeapp.retrofit.ExchangeRatesApi;
import com.example.engineerdegreeapp.retrofit.ExpenseApi;
import com.example.engineerdegreeapp.retrofit.entity.EuropeanCentralBankCurrencyRates;
import com.example.engineerdegreeapp.retrofit.entity.Expense;
import com.example.engineerdegreeapp.retrofit.entity.StringResponse;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.engineerdegreeapp.util.CurrencyUtils.getAllCurrencyCodesSortedByPopular;
import static com.example.engineerdegreeapp.util.CurrencyUtils.getLocalCurrencyCode;
import static com.example.engineerdegreeapp.util.CurrencyUtils.round;
import static com.example.engineerdegreeapp.util.DateUtils.getNumberOfDaysInMonth;
import static com.example.engineerdegreeapp.util.DateUtils.parseMonthNameToMonthNumber;

public class MonthlyStatisticsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private final String BUDGET_LIST_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/budgetlist/";
    private final String EXPENSES_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/expenses/";
    private final String CURRENCY_EXCHANGE_BASE_URL = "https://api.exchangeratesapi.io/";


    private Account mAccount;
    private AccountManager mAccountManager;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private String chosenMonth;
    private String chosenYear;
    private static boolean isSpinnersLoadingCompleteFlag = false;
    private PieChart statisticsPieChart;
    private EuropeanCentralBankCurrencyRates fetchedRates;
    private String chosenCurrencyCode;
    private Spinner currencySpinner;
    private ArrayList<Expense> currentExpensesData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_monthly_statistics, container, false);

        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }
        getCurrencyRates(chosenCurrencyCode);
        monthSpinner = rootView.findViewById(R.id.monthly_statistics_month_spinner);
        yearSpinner = rootView.findViewById(R.id.monthly_statistics_year_spinner);
        monthSpinner.setOnItemSelectedListener(this);
        yearSpinner.setOnItemSelectedListener(this);
        populateYearSpinner();
        populateMonthSpinner();
        statisticsPieChart = rootView.findViewById(R.id.monthly_statistics_pie_chart);
        chosenCurrencyCode = getLocalCurrencyCode();
        currencySpinner = rootView.findViewById(R.id.monthly_statistics_currency_spinner);
        currencySpinner.setOnItemSelectedListener(this);
        populateCurrencyCodeSpinner();
        return rootView;
    }

    private void populateMonthSpinner() {
        chosenMonth = String.valueOf(Calendar.getInstance().get(Calendar.MONTH));
        ArrayList<String> currencyList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.months_array)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, currencyList);
        monthSpinner.setAdapter(adapter);
    }

    private void populateCurrencyCodeSpinner() {
        ArrayList<String> currencyList = getAllCurrencyCodesSortedByPopular();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, currencyList);
        currencySpinner.setAdapter(adapter);
    }

    private void populateYearSpinner() {
        chosenYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BUDGET_LIST_BASE_URL)
                .build();
        BudgetListApi budgetListApi = retrofit.create(BudgetListApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<StringResponse> call = budgetListApi.getEarliestDateForUser(auth, loginCredential);
        call.enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("populateYearSpinner()", response.errorBody().string());
                        Toast.makeText(getContext(), getString(R.string.statistics_year_loading_error), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.d("populateYearSpinner()", e.getMessage());
                        Toast.makeText(getContext(), getString(R.string.statistics_year_loading_error), Toast.LENGTH_LONG).show();
                    }
                    ArrayList<String> currencyList = new ArrayList<>();
                    currencyList.add(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, currencyList);
                    yearSpinner.setAdapter(adapter);
                } else {
                    ArrayList<String> yearList = getYearFromFetchedDataAndCrateYearArray(response.body().getResponseContent());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, yearList);
                    yearSpinner.setAdapter(adapter);
                }
                isSpinnersLoadingCompleteFlag = true;
            }

            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                Log.d("populateYearSpinner()", "onFailure while loading spinner data");
                t.printStackTrace();
            }
        });

    }

    private ArrayList<String> getYearFromFetchedDataAndCrateYearArray(String startingDate) {
        String startingYear = startingDate.substring(6);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<String> yearList = new ArrayList<>();
        for (int startingYearIterator = Integer.parseInt(startingYear); startingYearIterator <= currentYear; startingYearIterator++) {
            yearList.add(String.valueOf(startingYearIterator));
        }
        return yearList;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.monthly_statistics_month_spinner:
                chosenMonth = parent.getItemAtPosition(position).toString();
                if (isSpinnersLoadingCompleteFlag) {
                    fetchMonthStatistics(chosenMonth, chosenYear);
                }
                break;
            case R.id.monthly_statistics_year_spinner:
                chosenYear = parent.getItemAtPosition(position).toString();
                if (isSpinnersLoadingCompleteFlag) {
                    fetchMonthStatistics(chosenMonth, chosenYear);
                }
                break;
            case R.id.monthly_statistics_currency_spinner:
                chosenCurrencyCode = parent.getItemAtPosition(position).toString();
                getCurrencyRates(chosenCurrencyCode);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    private void drawPieChart(Map<String, Double> dataMap) {
        statisticsPieChart.setVisibility(View.VISIBLE);
        List<PieEntry> chartEntries = new ArrayList<>();
        AtomicReference<Double> totalAmountSpent = new AtomicReference<>(0.0);
        dataMap.entrySet().forEach(e -> {
            chartEntries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
            totalAmountSpent.updateAndGet(v -> v + e.getValue());
        });
        PieDataSet chartDataSet = new PieDataSet(chartEntries, "");
        chartDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        chartDataSet.setValueTextColor(Color.BLACK);
        chartDataSet.setValueTextSize(16f);
        PieData chartData = new PieData(chartDataSet);

        Legend legend = statisticsPieChart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setTextSize(18f);
        legend.setFormSize(18f);
        legend.setYOffset(25f);

        statisticsPieChart.setCenterText(getResources().getString(R.string.statistics_pie_chart_center_text) + round(totalAmountSpent.get(), 2));
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            statisticsPieChart.setHoleColor(getContext().getResources().getColor(R.color.darkCardSelectedBackgroundColor, null));
        } else {
            statisticsPieChart.setHoleColor(getContext().getResources().getColor(R.color.lightCardSelectedBackgroundColor, null));
        }

        statisticsPieChart.setData(chartData);
        statisticsPieChart.animateY(1000);
        statisticsPieChart.getDescription().setEnabled(false);
        statisticsPieChart.setHoleRadius(50f);
        statisticsPieChart.setTransparentCircleRadius(55f);
        statisticsPieChart.setCenterTextSize(30);
        statisticsPieChart.setDrawEntryLabels(false);
        statisticsPieChart.setExtraTopOffset(-30f);
        statisticsPieChart.setRotationEnabled(false);
        statisticsPieChart.invalidate();
    }

    private void fetchMonthStatistics(String chosenMonth, String chosenYear) {
        int daysInChosenMonth = getNumberOfDaysInMonth(parseMonthNameToMonthNumber(chosenMonth), Integer.parseInt(chosenYear));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EXPENSES_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ExpenseApi expenseApi = retrofit.create(ExpenseApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);


        String startDate = "01-" + String.format("%02d", parseMonthNameToMonthNumber(chosenMonth)) + "-" + chosenYear;
        String endDate = daysInChosenMonth + "-" + String.format("%02d", parseMonthNameToMonthNumber(chosenMonth)) + "-" + chosenYear;
        Call<List<Expense>> call = expenseApi.getAllByDateAndExpenseOwner(auth, startDate, endDate, loginCredential);
        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("fetchMonthStatistics()", response.errorBody().string());
                        Toast.makeText(getContext(), getString(R.string.statistics_year_loading_error), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.d("fetchMonthStatistics()", e.getMessage());
                        Toast.makeText(getContext(), getString(R.string.statistics_year_loading_error), Toast.LENGTH_LONG).show();
                    }
                } else {
                    currentExpensesData = new ArrayList<>(response.body());
                    Map<String, Double> currentDataMap = prepareDataForPieChart(currentExpensesData);
                    drawPieChart(currentDataMap);
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                Log.d("fetchMonthStatistics()", "onFailure while loading spinner data");
                t.printStackTrace();
            }
        });
    }

    private Map<String, Double> prepareDataForPieChart(ArrayList<Expense> expenseArrayList) {
        ArrayList<Expense> expenses = new ArrayList<>();
        expenseArrayList.forEach(e -> {
            Expense expense = new Expense();
            expense.setAmount(e.getAmount());
            expense.setBudgetList(e.getBudgetList());
            expense.setCategory(e.getCategory());
            expenses.add(expense);
        });
        Map<String, Double> dataMap = new LinkedHashMap<>();
        if (fetchedRates == null) {
            Toast.makeText(getContext(), R.string.statistics_loading_currency_rates_error, Toast.LENGTH_LONG).show();
            ArrayList<String> populatedCategoryNames = expenses
                    .stream().map(expense -> expense.getCategory().getCategoryName()).distinct().collect(Collectors.toCollection(ArrayList::new));
            populatedCategoryNames.forEach(categoryName -> {
                ArrayList<Expense> categoryExpenses = expenses
                        .stream().filter(e -> e.getCategory().getCategoryName().equals(categoryName))
                        .collect(Collectors.toCollection(ArrayList::new));
                double summarizedCategoryValue = categoryExpenses.stream().mapToDouble(Expense::getAmount).sum();
                dataMap.put(categoryName, summarizedCategoryValue);
            });
        } else {
            ArrayList<String> populatedCategoryNames = expenses
                    .stream().map(expense -> expense.getCategory().getCategoryName()).distinct().collect(Collectors.toCollection(ArrayList::new));
            System.out.println(populatedCategoryNames);
            populatedCategoryNames.forEach(categoryName -> {
                ArrayList<Expense> categoryExpenses = expenses
                        .stream().filter(e -> e.getCategory().getCategoryName().equals(categoryName))
                        .collect(Collectors.toCollection(ArrayList::new));
                categoryExpenses.forEach(expense -> {
                    Double currencyRate = fetchedRates.getRates().get(expense.getBudgetList().getCurrencyCode());
                    if (currencyRate == null) {
                        currencyRate = 1.0;
                    }
                    Double currencyPrice = 1 / currencyRate;

                    Log.d("prepareDataForPieChart()", "currency price: " + chosenCurrencyCode + "/" + expense.getBudgetList().getCurrencyCode() + " = " + currencyPrice);
                    Log.d("prepareDataForPieChart()", "value before rating: " + expense.getAmount());
                    expense.setAmount(round(expense.getAmount() * currencyPrice, 2));
                    Log.d("prepareDataForPieChart()", "value after rating: " + expense.getAmount());
                });
                double summarizedCategoryValue = categoryExpenses.stream().mapToDouble(Expense::getAmount).sum();
                summarizedCategoryValue = round(summarizedCategoryValue, 2);
                dataMap.put(categoryName, summarizedCategoryValue);
            });
        }
        return dataMap;
    }


    private void getCurrencyRates(String currencyCode) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CURRENCY_EXCHANGE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ExchangeRatesApi exchangeRatesApi = retrofit.create(ExchangeRatesApi.class);


        Call<EuropeanCentralBankCurrencyRates> call = exchangeRatesApi.getRatesForCurrency(currencyCode);
        call.enqueue(new Callback<EuropeanCentralBankCurrencyRates>() {
            @Override
            public void onResponse(Call<EuropeanCentralBankCurrencyRates> call, Response<EuropeanCentralBankCurrencyRates> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("getCurrencyRates()", response.errorBody().string());
                    } catch (Exception e) {
                        Log.d("getCurrencyRates()", e.getMessage());
                    }
                } else {
                    fetchedRates = response.body();
                    if (currentExpensesData != null) {
                        Map<String, Double> currentDataMap = prepareDataForPieChart(currentExpensesData);
                        drawPieChart(currentDataMap);
                    }
                }
            }

            @Override
            public void onFailure(Call<EuropeanCentralBankCurrencyRates> call, Throwable t) {
                Log.d("getCurrencyRates()", "onFailure while loading spinner data");
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isSpinnersLoadingCompleteFlag = false;
    }
}
