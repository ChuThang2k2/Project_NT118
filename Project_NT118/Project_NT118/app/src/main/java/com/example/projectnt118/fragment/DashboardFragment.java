package com.example.projectnt118.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.example.projectnt118.R;
import com.example.projectnt118.api.ApiService;
import com.example.projectnt118.api.RetrofitClient;
import com.example.projectnt118.modle.PotholeResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;

public class DashboardFragment extends Fragment {

    private final ArrayList<PotholeResponse> potholeList = new ArrayList<>();
    private AnyChartView anyChartView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        anyChartView = view.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(view.findViewById(R.id.progress_bar));

        return view;
    }

    private void drawChart() {
        Pie pie = AnyChart.pie();
        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(requireContext(), event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });

        List<DataEntry> data = new ArrayList<>();
        int countSmall = 0;
        int countMedium = 0;
        int countLarge = 0;
        for (PotholeResponse response : potholeList) {
            if (response.getSeverity() == 1) {
                countSmall++;
            } else if (response.getSeverity() == 2) {
                countMedium++;
            } else {
                countLarge++;
            }
        }

        data.add(new ValueDataEntry("Severity small", countSmall));
        data.add(new ValueDataEntry("Severity Medium", countMedium));
        data.add(new ValueDataEntry("Severity Large", countLarge));

        pie.data(data);

        pie.title("Pothole Chart");


        pie.labels().position("outside");

        pie.legend().title().enabled(true);
        pie.legend().title()
                .text("Chart")
                .padding(0d, 0d, 10d, 0d);

        anyChartView.setChart(pie);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Retrofit retrofit = RetrofitClient.getLocalClient();
        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getPotholes().enqueue(new retrofit2.Callback<List<PotholeResponse>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<List<PotholeResponse>> call, @NonNull retrofit2.Response<List<PotholeResponse>> response) {
                if (response.body() != null) {
                    potholeList.addAll(response.body());
                    Log.d("GT63_x", "potholeList = " + potholeList.size());
                }
                drawChart();
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<List<PotholeResponse>> call, @NonNull Throwable t) {
                Log.d("GT63_x", "onFailure: " + t.getMessage());
            }
        });

    }
}
