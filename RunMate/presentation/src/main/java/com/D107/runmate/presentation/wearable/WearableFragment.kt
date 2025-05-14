package com.D107.runmate.presentation.wearable

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.D107.runmate.domain.model.smartinsole.FootStrikeType
import com.D107.runmate.domain.model.smartinsole.GaitAnalysisResult
import com.D107.runmate.domain.model.smartinsole.GaitPatternType
import com.D107.runmate.domain.model.smartinsole.InsoleConnectionState
import com.D107.runmate.domain.model.smartinsole.SmartInsole
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentWearableBinding
import com.D107.runmate.presentation.wearable.state.InsoleCardState
import com.D107.runmate.presentation.wearable.viewmodel.AnalysisProcessState
import com.D107.runmate.presentation.wearable.viewmodel.InsoleViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

@AndroidEntryPoint
class WearableFragment : BaseFragment<FragmentWearableBinding>(
    FragmentWearableBinding::bind,
    R.layout.fragment_wearable
) {

    private val viewModel: InsoleViewModel by viewModels()

    private var deviceListDialog: AlertDialog? = null

    private var diagnosisProgressDialog: AlertDialog? = null

    private var calibrationInstructionDialog: AlertDialog? = null


    // --- 권한 요청 관련 ---
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = true
            permissions.entries.forEach {
                if (!it.value) {
                    allGranted = false
                    Timber.w("Permission denied: ${it.key}")
                }
            }
            if (allGranted) {
                Timber.d("All permissions granted")
                checkBluetoothAndStartScan()
            }
        }

    // 블루투스 활성화 요청
    private val enableBluetoothRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                Timber.d("Bluetooth enabled by user")
                // 블루투스 활성화 후 스캔 시작
                startActualScan()
            } else {
                Timber.w("Bluetooth not enabled by user")
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtonClickListeners()
        observeViewModel()
    }

    private fun setupButtonClickListeners() {
        binding.btnFindInsole.setOnClickListener {
            Timber.d("권한 확인")
//            if(viewModel.insoleCardState.value==InsoleCardState.DISCONNECTED_SAVED){
//                viewModel.disconnect()
//                viewModel.forgetDevice()
//            }else {
            checkPermissionsAndStartScan()
//            }
        }

        binding.btnDisconnectInsole.setOnClickListener{
            showDisconnectDialog()
        }

        binding.btnDiagnoseInsole.setOnClickListener {
            if (viewModel.connectionState.value == InsoleConnectionState.FULLY_CONNECTED) {
                viewModel.initiateTimedDiagnosis(15) // 15초 진단 시작
            } else {
                showToast("스마트 인솔이 완전히 연결되어야 진단할 수 있습니다.")
            }
        }

        binding.btnDiagnoseNoResultsInsole.setOnClickListener {
            if (viewModel.connectionState.value == InsoleConnectionState.FULLY_CONNECTED) {
                viewModel.initiateTimedDiagnosis(15) // 15초 진단 시작
            } else {
                showToast("스마트 인솔이 완전히 연결되어야 진단할 수 있습니다.")
            }
//            viewModel.startDiagnosis()
        }
    }

    private fun checkPermissionsAndStartScan() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            checkBluetoothAndStartScan()
        } else {
            Timber.d("Requesting permissions: ${missingPermissions.joinToString()}")
            requestMultiplePermissions.launch(missingPermissions.toTypedArray())
        }
    }

    private fun checkBluetoothAndStartScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            // 블루투스 활성화 요청
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothRequest.launch(enableBtIntent)
        } else {
            // 블루투스 켜져 있으면 스캔 시작
            startActualScan()
        }
    }


    //인솔 디바이스 탐색
    private fun startActualScan() {
        Timber.d("권한 및 블루투스 확인 완료, 스캔 시작")
        viewModel.clearSelection()
        viewModel.startScan()
    }


    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // --- 인솔 카드 상태 관찰 ---
                launch {
                    viewModel.insoleCardState.collect { state ->
                        Timber.d("InsoleCardState collected: $state")
                        updateInsoleCardUI(state)
                    }
                }
                // --- 스캔 상태 관찰 (Dialog 관련) ---
                launch {
                    viewModel.scanState.collect { isScanning ->
                        if (isScanning && (deviceListDialog == null || !deviceListDialog!!.isShowing)) {
                            showDeviceSelectionDialog()
                        } else if (!isScanning && deviceListDialog != null && deviceListDialog!!.isShowing) {
                            deviceListDialog?.dismiss()
                        }
                    }
                }
                // --- 연결 상태 (세부 텍스트 업데이트 등에 사용 가능) ---
                launch {
                    viewModel.connectionState.collect { state ->
                        val isConnected = state == InsoleConnectionState.FULLY_CONNECTED
                        binding.btnDiagnoseInsole.isEnabled = isConnected
                        binding.btnDiagnoseNoResultsInsole.isEnabled = isConnected

                        // 연결 상태에 따라 '연결하고 진단받으세요' 메시지 표시/숨김
                        binding.tvConnectInsoleMessageInsole.visibility = if (!isConnected && binding.layoutNoDiagnosisInsole.visibility == View.VISIBLE) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                        // 연결되면 스캔 다이얼로그 닫기 등 (기존 로직 유지)
                        if (state == InsoleConnectionState.FULLY_CONNECTED ||
                            state == InsoleConnectionState.FAILED ||
                            state == InsoleConnectionState.DISCONNECTED) {
                            deviceListDialog?.dismiss()
                        }
                    }
                }
                launch {
                    viewModel.savedGaitAnalysisResult.collect { savedResult ->
                        Timber.d("Saved Gait Analysis Result collected: ${savedResult != null}")
                        updateDiagnosisResultUI(savedResult) // UI 업데이트
                    }
                }
                launch {
                    viewModel.analysisProcessState.collect { state ->
                        Timber.d("AnalysisState collected: $state")
                        when (state) {
                            AnalysisProcessState.ANALYZING -> showDiagnosisProgressDialog()
                            AnalysisProcessState.CALIBRATING->{
                                showCalibrationInstructionDialog()
                            }
                            else -> {
                                dismissCalibrationInstructionDialog()
                                dismissDiagnosisProgressDialog()
                                // 분석 완료(STOPPED) 시 최신 결과로 UI 업데이트
                                if (state == AnalysisProcessState.STOPPED) {
                                    updateDiagnosisResultUI(viewModel.realTimeAnalysisResult.value)
                                }
                            }

                        }
                    }
                }

                //스캔시 dialog 관리
                launch {
                    viewModel.scanState.collect { isScanning ->
                        binding.btnFindInsole.isEnabled = !isScanning
                        if (isScanning && (deviceListDialog == null || !deviceListDialog!!.isShowing)) {
                            showDeviceSelectionDialog()
                        } else if (!isScanning && deviceListDialog != null && deviceListDialog!!.isShowing) {
                            deviceListDialog?.dismiss()
                        }
                    }
                }
                // 스캔된 기기 목록 dialog에 표기
                launch {
                    viewModel.scannedDevices.collect { devices ->
                        if (deviceListDialog != null && deviceListDialog!!.isShowing) {
                            updateDeviceListDialog(devices)
                        }
                    }
                }

            }
        }
    }

    private fun updateInsoleCardUI(state: InsoleCardState) {
        when (state) {
            InsoleCardState.CONNECTED -> {
                binding.layoutConnectedInsole.visibility = View.VISIBLE
                binding.layoutDisconnectedInsole.visibility = View.GONE
                binding.ivBluetoothConnectInsole.visibility = View.VISIBLE
                binding.ivBluetoothDisconnectInsole.visibility = View.GONE
            }
            InsoleCardState.DISCONNECTED_SAVED -> {
                binding.layoutConnectedInsole.visibility = View.VISIBLE
                binding.layoutDisconnectedInsole.visibility = View.GONE
                binding.ivBluetoothConnectInsole.visibility = View.GONE
                binding.ivBluetoothDisconnectInsole.visibility = View.VISIBLE
            }
            InsoleCardState.DISCONNECTED_NO_SAVED -> {
                binding.layoutConnectedInsole.visibility = View.GONE
                binding.layoutDisconnectedInsole.visibility = View.VISIBLE
                binding.tvDisconnectedMessageInsole.text = "연결된 인솔이 없습니다"
                binding.btnFindInsole.text = "인솔 찾기"
            }
        }
    }

    private fun updateDiagnosisResultUI(result: GaitAnalysisResult?) {
        if (result != null && (result.totalLeftSteps > 0 || result.totalRightSteps > 0)) {
            // 결과 있음
            binding.layoutAnalysisResultsInsole.visibility = View.VISIBLE
            binding.layoutNoDiagnosisInsole.visibility = View.GONE
            binding.btnDiagnoseNoResultsInsole.visibility = View.GONE // 결과 없 음 화면의 버튼 숨김

            val totalSteps = result.totalLeftSteps + result.totalRightSteps
            if (totalSteps == 0) return // 걸음 수 없으면 업데이트 안 함

            // --- 착지 분포 계산 (좌우 합산) ---
            val totalStrikeCounts = mutableMapOf<FootStrikeType, Int>()
            result.leftFootStrikeDistribution.forEach { (type, count) ->
                totalStrikeCounts[type] = (totalStrikeCounts[type] ?: 0) + count
            }
            result.rightFootStrikeDistribution.forEach { (type, count) ->
                totalStrikeCounts[type] = (totalStrikeCounts[type] ?: 0) + count
            }

            val midfootPercent = ((totalStrikeCounts[FootStrikeType.MIDFOOT] ?: 0).toFloat() / totalSteps * 100).roundToInt()
            val forefootPercent = ((totalStrikeCounts[FootStrikeType.FOREFOOT] ?: 0).toFloat() / totalSteps * 100).roundToInt()
            val rearfootPercent = ((totalStrikeCounts[FootStrikeType.REARFOOT] ?: 0).toFloat() / totalSteps * 100).roundToInt()

            binding.tvMidfootValueInsole.text = String.format(Locale.getDefault(), "%d%%", midfootPercent)
            binding.tvForefootValueInsole.text = String.format(Locale.getDefault(), "%d%%", forefootPercent)
            binding.tvHeelValueInsole.text = String.format(Locale.getDefault(), "%d%%", rearfootPercent)

//            var analysisText = "걸음걸이 패턴: ${getGaitPatternString(result.overallGaitPattern)}\n" // 수정
//            analysisText += "- 왼쪽 평균 Yaw: ${result.averageLeftYaw?.let { String.format("%.1f°", it) } ?: "N/A"}\n"
//            analysisText += "- 오른쪽 평균 Yaw: ${result.averageRightYaw?.let { String.format("%.1f°", it) } ?: "N/A"}\n"
            var gaitDiff:Float =0f
            if(result.averageLeftYaw!=null&&result.averageRightYaw!=null) {
                gaitDiff = if(result.averageLeftYaw!!>result.averageRightYaw!!) result.averageRightYaw!! - result.averageLeftYaw!! else Math.abs(result.averageRightYaw!! - result.averageLeftYaw!!)
            }
            var gaitResultDescriptionString = getGaitPatternString(gaitDiff,result.overallGaitPattern)


            binding.tvAnalysisDescriptionInsole.text = gaitResultDescriptionString

            var strikeType = FootStrikeType.UNKNOWN
            var maxPercent = -1


            binding.tvDiagnosisDateInsole.text = formatTimestampToKoreanDate(result.timestamp?:0L)
            binding.tvDiagnosisDateInsole.visibility = View.VISIBLE
            if (forefootPercent > maxPercent) {
                maxPercent = forefootPercent
                strikeType = FootStrikeType.FOREFOOT
            }
            // 주의: 동률일 경우, 위에서부터 순서대로 첫 번째 것이 선택됨.
            // 만약 MIDFOOT와 FOREFOOT가 동률이고 MIDFOOT를 우선하고 싶다면, MIDFOOT를 먼저 비교.
            if (midfootPercent > maxPercent) {
                maxPercent = midfootPercent
                strikeType = FootStrikeType.MIDFOOT
            }
            if (rearfootPercent > maxPercent) {
                // maxPercent = rearfootPercent // 마지막이므로 maxPercent 업데이트는 불필요할 수 있음
                strikeType = FootStrikeType.REARFOOT
            }
            // 만약 모든 퍼센트가 0이거나 매우 낮아 유효한 dominant를 찾지 못했다면 (totalStrikeCounts가 비어있는 등),
            // dominantStrikeTypeForImage는 초기값 UNKNOWN 유지

            // 모든 퍼센트가 0인 경우 (즉, totalStrikeCounts에 유효한 데이터가 없었던 경우) 처리
            if (totalStrikeCounts.isEmpty() || maxPercent <= 0) { // maxPercent <= 0 조건 추가
                strikeType = FootStrikeType.UNKNOWN
            }


            val footstrikeImageResource = when (strikeType) {
                FootStrikeType.REARFOOT -> R.drawable.img_rearfoot
                FootStrikeType.MIDFOOT -> R.drawable.img_midfoot
                FootStrikeType.FOREFOOT -> R.drawable.img_forefoot
                FootStrikeType.UNKNOWN -> R.drawable.img_no_insole
            }
            val footStringDescription = when(strikeType){
                FootStrikeType.REARFOOT -> getText(R.string.rearfoot_strike_description)
                FootStrikeType.MIDFOOT -> getText(R.string.midfoot_strike_description)
                FootStrikeType.FOREFOOT -> getText(R.string.forefoot_strike_description)
                FootStrikeType.UNKNOWN -> getText(R.string.unknown_strike_description)
            }


            binding.tvFootstrikeDescriptionInsole.text = footStringDescription
            binding.ivFootstrikeInsole.setImageResource(footstrikeImageResource)
            binding.btnDiagnoseInsole.visibility = View.VISIBLE

        } else {
            // 결과 없음 또는 유효하지 않음
            binding.layoutAnalysisResultsInsole.visibility = View.GONE
            binding.layoutNoDiagnosisInsole.visibility = View.VISIBLE
            binding.btnDiagnoseNoResultsInsole.visibility = View.VISIBLE // 결과 없음 화면의 버튼 표시
            binding.btnDiagnoseInsole.visibility = View.GONE
            binding.tvDiagnosisDateInsole.visibility = View.GONE

            // 인솔 연결 상태에 따라 추가 메시지 표시
            binding.tvConnectInsoleMessageInsole.visibility = if (viewModel.connectionState.value != InsoleConnectionState.FULLY_CONNECTED) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }


    private fun getGaitPatternString(gaitResultDescription:Float, pattern:GaitPatternType): String {
        return when (pattern) {
            GaitPatternType.IN_TOEING -> String.format("현제 발의 각도는 %.1f으로 ", gaitResultDescription)+"안짱걸음으로 진단되었습니다.\n"+getString(R.string.in_toeing_description)
            GaitPatternType.OUT_TOEING -> String.format("현제 발의 각도는 %.1f으로 ", gaitResultDescription)+"팔자걸음으로 진단되었습니다.\n"+getString(R.string.out_toeing_description)
            GaitPatternType.NEUTRAL -> String.format("현제 발의 각도는 %.1f으로 ", gaitResultDescription)+"정상걸음으로 진단되었습니다.\n"
            GaitPatternType.UNKNOWN -> "알 수 없음"
        }
    }
    private fun getFootStrikeString(strike: FootStrikeType): String {
        return when(strike) {
            FootStrikeType.REARFOOT -> "리어풋"
            FootStrikeType.MIDFOOT -> "미드풋"
            FootStrikeType.FOREFOOT -> "포어풋"
            FootStrikeType.UNKNOWN -> "알 수 없음"
        }
    }

    private fun showDeviceSelectionDialog() {
        if (deviceListDialog != null && deviceListDialog!!.isShowing) {
            updateDeviceListDialog(viewModel.scannedDevices.value)
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_device_list, null) // Dialog 레이아웃 필요
        val listView = dialogView.findViewById<android.widget.ListView>(R.id.listViewDevices) // ListView ID 확인 필요
        val selectedLeftTextView = dialogView.findViewById<TextView>(R.id.textViewDialogSelectedLeft) // Dialog 내 TextView ID
        val selectedRightTextView = dialogView.findViewById<TextView>(R.id.textViewDialogSelectedRight) // Dialog 내 TextView ID

        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1)
        listView.adapter = adapter

        // 초기 목록 설정
        updateDialogListAdapter(adapter, viewModel.scannedDevices.value)
        updateDialogSelectionText(selectedLeftTextView, selectedRightTextView)

        val job = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scannedDevices.collect { devices -> updateDialogListAdapter(adapter, devices) }
        }
        val jobLeft = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedLeftInsole.collect { updateDialogSelectionText(selectedLeftTextView, selectedRightTextView) }
        }
        val jobRight = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedRightInsole.collect { updateDialogSelectionText(selectedLeftTextView, selectedRightTextView) }
        }


        listView.setOnItemClickListener { _, _, position, _ ->
            Timber.d("List Clicked $position")
            val selectedDeviceName = adapter.getItem(position)
            val selectedDevice = viewModel.scannedDevices.value[position]
            Timber.d(selectedDeviceName)
            Timber.d(selectedDevice?.name?:"Unknown")
            selectedDevice?.let {
                viewModel.selectDevice(it)
            }
        }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("스마트 인솔 선택")
            .setView(dialogView)
            .setPositiveButton("페어링") { dialog, _ ->
                viewModel.pairSelectedDevices()
                dialog.dismiss() // Dialog 닫기
            }
            .setNegativeButton("취소") { dialog, _ ->
                viewModel.stopScan()
                viewModel.clearSelection()
                dialog.dismiss()
            }
            .setOnDismissListener {
                Timber.d("Device selection dialog dismissed")
                if (viewModel.scanState.value) { // 아직 스캔 중이었다면 중지
                    viewModel.stopScan()
                }
                job.cancel()
                jobLeft.cancel()
                jobRight.cancel()
                deviceListDialog = null // 참조 제거
            }

        deviceListDialog = builder.create()

        // "페어링" 버튼 초기 상태 설정 (양쪽 다 선택 시 활성화)
        deviceListDialog?.setOnShowListener { dialogInterface ->
            val positiveButton = (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            // isPairingReady 상태를 구독하여 버튼 활성화/비활성화
            val jobPairing = viewLifecycleOwner.lifecycleScope.launch {
                viewModel.isPairingReady.collect { isReady ->
                    positiveButton.isEnabled = isReady
                }
            }
            // Dialog 해제 시 이 job도 취소
            dialogInterface.setOnDismissListener {
                jobPairing.cancel()
                builder.create().dismiss() // 이렇게 하면 안됨, 기존 리스너 참조 필요

            }

            // --- Dismiss 리스너 통합 ---
            dialogInterface.setOnDismissListener {
                Timber.d("Device selection dialog dismissed (Integrated Listener)")
                if (viewModel.scanState.value) {
                    viewModel.stopScan()
                }
//                viewModel.clearSelection()
                job.cancel()
                jobLeft.cancel()
                jobRight.cancel()
                jobPairing.cancel() // 페어링 버튼 활성화 job도 취소
                deviceListDialog = null
            }
        }

        deviceListDialog?.show()
    }

    // Dialog의 목록 Adapter 업데이트
    private fun updateDialogListAdapter(adapter: ArrayAdapter<String>, devices: List<SmartInsole>) {
        adapter.clear()
        adapter.addAll(devices.map { "${it.name ?: "Unknown"} (${it.address}) - ${it.side}" }) // Side 정보 표시
        adapter.notifyDataSetChanged()
    }

    // Dialog의 선택 정보 TextView 업데이트
    private fun updateDialogSelectionText(leftTv: TextView?, rightTv: TextView?) {
        leftTv?.text = "왼쪽: ${viewModel.selectedLeftInsole.value?.name ?: "미선택"}"
        rightTv?.text = "오른쪽: ${viewModel.selectedRightInsole.value?.name ?: "미선택"}"
    }

    private fun updateDeviceListDialog(devices: List<SmartInsole>) {
        deviceListDialog?.let { dialog ->
            val listView = dialog.findViewById<android.widget.ListView>(R.id.listViewDevices)
            (listView?.adapter as? ArrayAdapter<String>)?.let { adapter ->
                updateDialogListAdapter(adapter, devices)
            }
        }
    }


    private fun showDisconnectDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("인솔 삭제")
            .setMessage("연결된 인솔을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.forgetDevice()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDiagnosisProgressDialog() {
        if (diagnosisProgressDialog == null) {
            diagnosisProgressDialog = AlertDialog.Builder(requireContext())
                .setTitle("보행 분석 중")
                .setMessage("15초 동안 데이터를 측정합니다...")
                .setCancelable(false) // 사용자가 임의로 닫지 못하게
                // .setNegativeButton("취소") { _, _ -> viewModel.stopRealTimeAnalysis("사용자 취소", saveResult = false)} // 취소 버튼 추가 시
                .create()
        }
        if (!diagnosisProgressDialog!!.isShowing) {
            diagnosisProgressDialog?.show()
        }
    }

    /**
     * "진단 중..." 다이얼로그를 숨깁니다.
     */

    private fun showCalibrationInstructionDialog() {
        if (calibrationInstructionDialog == null || !calibrationInstructionDialog!!.isShowing) {
            // 이전 다이얼로그가 있다면 안전하게 dismiss
            calibrationInstructionDialog?.dismiss()

            val calibrationDurationSeconds = viewModel.calibrationDurationSeconds // 예시로 2초 (GaitAnalyzerUtil의 실제 값과 동기화 필요)

            calibrationInstructionDialog = AlertDialog.Builder(requireContext())
                .setTitle("캘리브레이션")
                .setMessage("정확한 분석을 위해 발을 정면으로 향하고 ${calibrationDurationSeconds}초 동안 잠시 기다려주세요.")
                .setCancelable(false)
                .create()
            calibrationInstructionDialog?.show()
        }
    }

    /**
     * 캘리브레이션 안내 다이얼로그를 숨깁니다.
     */
    private fun dismissCalibrationInstructionDialog() {
        calibrationInstructionDialog?.dismiss()
        calibrationInstructionDialog = null // 참조 해제 (선택적이지만, 메모리 관리에 도움)
    }

    private fun dismissDiagnosisProgressDialog() {
        diagnosisProgressDialog?.dismiss()
    }

    fun formatTimestampToKoreanDate(timestamp: Long): String {
        val koreanTimeZone = TimeZone.getTimeZone("Asia/Seoul")

        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        sdf.timeZone = koreanTimeZone

        return sdf.format(Date(timestamp))
    }



    override fun onDestroyView() {
        super.onDestroyView()
        deviceListDialog?.dismiss()
        deviceListDialog = null
        diagnosisProgressDialog?.dismiss()
        diagnosisProgressDialog = null
        calibrationInstructionDialog?.dismiss()
        calibrationInstructionDialog = null
    }

}