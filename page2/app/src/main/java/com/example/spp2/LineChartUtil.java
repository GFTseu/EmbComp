package com.example.spp2;


import android.content.Intent;
import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;

public class LineChartUtil {
    private LineChart mLineChart;
    private float lastX = 0;
    // 设置最多显示个点
    int maxVisibleCount = 3000;
    public LineChartUtil(LineChart lineChart){
        mLineChart = lineChart;
    }
    /**
     * 初始化X轴
     */
    public void initXAxis(ArrayList list) {
        XAxis xAxis = mLineChart.getXAxis();
        //  2.设置X轴的位置（默认在上方）：
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//值：BOTTOM,BOTH_SIDED,BOTTOM_INSIDE,TOP,TOP_INSIDE
        //  3.设置X轴坐标之间的最小间隔（因为此图有缩放功能，X轴,Y轴可设置可缩放）
        xAxis.setGranularity(1f);
        //  4.设置X轴的刻度数量
        xAxis.setLabelCount(40, false);
        // 5.设置X轴的值（最小值、最大值、然后会根据设置的刻度数量自动分配刻度显示）
//        xAxis.setAxisMinimum(0f);
//        xAxis.setAxisMaximum(4000f);
        // 6.设置当前图表中最多在x轴坐标线上显示刻度线的总量
        mLineChart.setVisibleXRangeMaximum(999);// 设置当前图表中最多在x轴坐标线上显示的刻度线总量15

        ValueFormatter valueFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (list.isEmpty()) {
                    return super.getFormattedValue(value);
                } else {
                    NumberFormat nf2 = NumberFormat.getNumberInstance();
                    nf2.setMaximumFractionDigits(2);
                    nf2.setRoundingMode(RoundingMode.HALF_UP);

                    String dotXString = super.getFormattedValue(value);
                    Double dotIndex = Double.parseDouble(dotXString);
                    Double unitTime = Double.parseDouble(((String) list.get(0)));
                    dotXString = nf2.format(dotIndex * unitTime);
                    return dotXString;
                }
            }
        };

        xAxis.setValueFormatter(valueFormatter);
    }
    /**
     * 初始化折线图数据集合
     */

    public void initLineChartData() {
        // 创建一个空数据集合并将其设置到图表中
        LineDataSet dataSet = new LineDataSet(null, "Input Signal");
        dataSet.setLineWidth(2.0f);
        // 设置折线的颜色
        dataSet.setColor(Color.RED);
        // 设置曲线为圆滑曲线
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        // 设置折线图填充
        dataSet.setDrawFilled(true);
        // 设置每个点是否以圆圈来显示
        dataSet.setDrawCircles(false);
        // 是否显示每个点的y值
        dataSet.setDrawValues(true);
        // 创建LineData对象并设置其为图表的数据
        LineData data = new LineData(dataSet);
        mLineChart.setData(data);
    }
    /**
     * 初始化折线图
     */
    public void initLineChart(ArrayList list) {
        // 折线图是否可以触摸
        mLineChart.setTouchEnabled(true);
        // 折线图是否可以拖动
        mLineChart.setDragEnabled(true);
        // 折线图是否可以放大
        mLineChart.setScaleEnabled(true);
        // 折线图是否显示网格
        mLineChart.setDrawGridBackground(true);
        // 如果设置为true, x和y轴可以用2个手指同时缩放，如果设置为false, x和y轴可以分别缩放。默认值:假
        mLineChart.setPinchZoom(false);
        // 返回图表的Description对象，该对象负责保存与显示在图表右下角的描述文本相关的所有信息(默认情况下)
        mLineChart.getDescription().setEnabled(false);
        mLineChart.animateX(1500);
        // 设置x轴的属性
        initXAxis(list);
        // 设置y轴的属性
        initYAxis();
        initLegend(list);
    }
    /**
     * 初始化折线图的图标
     */
    public void initLegend(ArrayList list) {
        Legend legend = mLineChart.getLegend();
        // 设置图例位置为底部居中
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        // 显示图例文本
        legend.setEnabled(true);

        // 设置图例文本颜色
        legend.setTextColor(Color.BLACK);

        // 设置图例样式为水平
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        // 设置描述内容
        Description description = new Description();
        if (list.isEmpty()) {
            description.setText("t/us");
        } else {
            description.setText("t/" + list.get(1));
        }
        description.setTextColor(Color.BLACK);
        mLineChart.setDescription(description);

    }
    /**
     * 初始化折线图的y轴
     */
    public void initYAxis() {
        //  1.得到Y轴
        YAxis leftYAxis = mLineChart.getAxisLeft();
        YAxis rightYAxis = mLineChart.getAxisRight();
        // 2.设置某一个Y轴是否显示
        rightYAxis.setEnabled(false); //右侧Y轴不显示
        //4.X轴和Y轴类似，都具有相同的属性方法

        rightYAxis.setAxisMinimum(0f);
        rightYAxis.setAxisMaximum(100f);
        rightYAxis.setGranularity(0.0001f);
//        rightYAxis.setLabelCount(11,false);
        rightYAxis.setTextColor(Color.BLUE); //文字颜色
        rightYAxis.setGridColor(Color.RED); //网格线颜色
        rightYAxis.setAxisLineColor(Color.GREEN); //Y轴颜色
    }

    public void DeleteLimitLine() {
        XAxis XAxis = mLineChart.getXAxis();
        XAxis.removeAllLimitLines();
    }

    public void ShowLimitLine(ArrayList limitLineList) {
        XAxis XAxis = mLineChart.getXAxis();

        if (limitLineList.isEmpty()) {
            limitLineList.add(0f);
            limitLineList.add(4f);
        }
        for (int i = 0; i < limitLineList.size(); i++) {
            System.out.println(limitLineList.get(i) + "fufufufufufufufufufu");
        }

        LimitLine x1 = new LimitLine((Float) limitLineList.get(0),"Cursor 1"); //得到限制线
        x1.setLineWidth(1f); //宽度
        x1.setTextSize(10f);
        x1.setTextColor(Color.BLACK);  //颜色
        x1.setLineColor(Color.BLUE);
        XAxis.addLimitLine(x1); //X轴添加限制线

        LimitLine x2 = new LimitLine((Float) limitLineList.get(1),"Cursor 2"); //得到限制线
        x2.setLineWidth(1f); //宽度
        x2.setTextSize(10f);
        x2.setTextColor(Color.BLACK);  //颜色
        x2.setLineColor(Color.RED);
        XAxis.addLimitLine(x2); //X轴添加限制线
    }

    public void clearLimitLine() {
        XAxis XAxis = mLineChart.getXAxis();
        XAxis.removeAllLimitLines();
    }

    public float getHighVisX() {
        float x = mLineChart.getHighestVisibleX();
        return x;
    }

    public float getLowVisX() {
        float x = mLineChart.getLowestVisibleX();
        return x;
    }


    public boolean addEntry(float value, ArrayList list, ArrayList chartDataArray) {
        LineData data = mLineChart.getData();
        if (data != null) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return ""+value;
                }
            });

            // 添加新点到数据集中
            data.addEntry(new Entry(lastX++, value), 0);
            chartDataArray.add(value);
            initLegend(list);
            // 更新数据
            data.notifyDataChanged();
            mLineChart.notifyDataSetChanged();

            // 平移x轴，确保新添加的点显示在最右侧

            float maxX = data.getXMax();
            if (maxX > maxVisibleCount) {
                mLineChart.getXAxis().setAxisMinimum(data.getXMin() + 1);
                mLineChart.getXAxis().setAxisMaximum(data.getXMax() + maxVisibleCount);
//                maxVisibleCount+=20;
//                return false;
            }

            // 将图表滚动到最新的点处
            mLineChart.moveViewToX(data.getEntryCount()-1);
        }
        return true;
    }
    /**
     * 创建数据集
     */
    public LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "动态折线图");
        set.setLineWidth(2.5f);
        set.setColor(Color.RED);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        return set;
    }

    //LineChartUtil 新增方法
    public void refresh() {
        mLineChart.getXAxis().setAxisMinimum(0);
        mLineChart.getXAxis().setAxisMaximum(maxVisibleCount);
    }
}