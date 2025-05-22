package com.D107.runmate.presentation.running.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentChartBinding
import com.D107.runmate.presentation.history.HistoryViewModel
import com.D107.runmate.presentation.utils.GpxParser.getGpxInputStream
import com.D107.runmate.presentation.utils.GpxParser.parseGpx
import com.D107.runmate.presentation.utils.KakaoMapUtil.addCourseLine
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream

@AndroidEntryPoint
class ChartFragment : BaseFragment<FragmentChartBinding>(
    FragmentChartBinding::bind,
    R.layout.fragment_chart
) {
    private var mContext: Context? = null
    private val historyViewModel: HistoryViewModel by viewModels()
    private val args: ChartFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                parseGpxFile(getGpxInputStream(args.gpxFileUrl))
            }
        }
    }

    private fun parseGpxFile(inputStream: InputStream) {
        CoroutineScope(Dispatchers.IO).launch {
            mContext?.let {
                val trackPoints = parseGpx(inputStream)
                var paceEntries = mutableListOf<Entry>()
                val paceDataSets = mutableListOf<ILineDataSet>()
                withContext(Dispatchers.Main) {
                    for ((index, point) in trackPoints.withIndex()) {
                        if (point.pace != 0) {
                            paceEntries.add(Entry((index*5).toFloat(), point.pace!!.toFloat()))
                        } else {
                            if (paceEntries.isNotEmpty()) {
                                val dataSet = LineDataSet(paceEntries, "페이스 변화 그래프").apply {
                                    color = getColor(R.color.primary)
                                    lineWidth = 2f
                                    mode = LineDataSet.Mode.CUBIC_BEZIER
                                    cubicIntensity = 0.2f
                                    setDrawCircles(false)
                                    setDrawValues(false)
                                    isHighlightEnabled = false
                                }
                                dataSet.setDrawValues(false)
                                paceDataSets.add(dataSet)
                                paceEntries = mutableListOf()
                            }
                        }
                    }
                    if(paceEntries.isNotEmpty()){
                        val dataSet = LineDataSet(paceEntries, "페이스 변화 그래프").apply {
                            color = resources.getColor(R.color.primary)
                            lineWidth = 2f
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            cubicIntensity = 0.2f
                            setDrawCircles(false)
                            setDrawValues(false)
                            isHighlightEnabled = false
                        }
                        dataSet.setDrawValues(false)
                        paceDataSets.add(dataSet)
                    }

                    val cadenceEntries = trackPoints.mapIndexed { index, tp ->
                        Entry((index*5).toFloat(), tp.cadence!!.toFloat())
                    }

                    val eleEntries = trackPoints.mapIndexed { index, tp ->
                        Entry((index*5).toFloat(), tp.ele!!.toFloat())
                    }

                    val bpmEntries = trackPoints.mapIndexed { index, tp ->
                        Entry((index*5).toFloat(), tp.hr!!.toFloat())
                    }

                    val paceFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${(value).toInt()/60}' ${(value).toInt()%60}\""
                        }
                    }

                    val cadenceFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()} spm"
                        }
                    }

                    val eleFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${"%.2f".format(value)} km"
                        }
                    }

                    val bpmFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()} bpm"
                        }
                    }

                    setupPaceChart(binding.chartPace, paceDataSets, paceFormatter)

                    setupLineChart(
                        chart = binding.chartCadence,
                        entries = cadenceEntries,
                        label = "케이던스 변화 그래프",
                        lineColor = it.resources.getColor(R.color.primary),
                        yValueFormatter = cadenceFormatter
                    )

                    setupLineChart(
                        chart = binding.chartAltitude,
                        entries = eleEntries,
                        label = "고도 변화 그래프",
                        lineColor = it.resources.getColor(R.color.primary),
                        yValueFormatter = eleFormatter
                    )

                    setupLineChart(
                        chart = binding.chartBpm,
                        entries = bpmEntries,
                        label = "심박수 변화 그래프",
                        lineColor = it.resources.getColor(R.color.primary),
                        yValueFormatter = bpmFormatter
                    )
                }
            }
        }
    }

    private fun setupLineChart(
        chart: LineChart,
        entries: List<Entry>,
        label: String,
        lineColor: Int,
        yValueFormatter: ValueFormatter
    ) {
        val dataSet = LineDataSet(entries, label).apply {
            color = lineColor
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawCircles(false)
            setDrawValues(false)
            isHighlightEnabled = false
        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)
            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
                granularity = 0.5f
                valueFormatter = yValueFormatter
            }
            animateY(1000)
            invalidate()
        }
    }

    private fun setupPaceChart(
        chart: LineChart,
        dataSet: MutableList<ILineDataSet>,
        yValueFormatter: ValueFormatter
    ) {
//        val dataSet = LineDataSet(entries, label).apply {
//            color = lineColor
//            lineWidth = 2f
//            mode = LineDataSet.Mode.CUBIC_BEZIER
//            cubicIntensity = 0.2f
//            setDrawCircles(false)
//            setDrawValues(false)
//            isHighlightEnabled = false
//        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)
            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
                granularity = 0.5f
                valueFormatter = yValueFormatter
            }
            animateY(1000)
            invalidate()
        }
    }

}