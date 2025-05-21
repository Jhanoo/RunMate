package com.D107.runmate.presentation.wearable

import android.Manifest
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
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
import com.D107.runmate.presentation.utils.ManagerAnalysisProcessState
import com.D107.runmate.presentation.wearable.state.InsoleCardState
import com.D107.runmate.presentation.wearable.viewmodel.AnalysisProcessState
import com.D107.runmate.presentation.wearable.viewmodel.InsoleViewModel
import com.google.android.material.button.MaterialButton
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

    private var dialogPanningValueAnimator: ValueAnimator? = null

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
                viewModel.initiateTimedDiagnosis(10) // 15초 진단 시작
            } else {
                showToast("스마트 인솔이 완전히 연결되어야 진단할 수 있습니다.")
            }
        }

        binding.btnDiagnoseNoResultsInsole.setOnClickListener {
            if (viewModel.connectionState.value == InsoleConnectionState.FULLY_CONNECTED) {
                Timber.d("click diagnosisInsole")
                viewModel.initiateTimedDiagnosis(10) // 15초 진단 시작
            } else {
                showToast("스마트 인솔이 완전히 연결되어야 진단할 수 있습니다.")
            }
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
            showDeviceSelectionDialog()
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
//                        if (isScanning && (deviceListDialog == null || !deviceListDialog!!.isShowing)) {
//                            showDeviceSelectionDialog()
//                        } else if (!isScanning && deviceListDialog != null && deviceListDialog!!.isShowing) {
//                            deviceListDialog?.dismiss()
//                        }
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
                    viewModel.saveGaitAnalysisResult.collect { savedResult ->
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
                            }

                        }
                    }
                }

                //스캔시 dialog 관리
                launch {
                    viewModel.scanState.collect { isScanning ->
                        binding.btnFindInsole.isEnabled = !isScanning
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
            Timber.d("GaitResult : ${result}")
//            var gaitDiff:Float =result.averageRightYaw!!-result.averageLeftYaw!!
            val gaitResultDescriptionString = getGaitPatternString(result)


            binding.tvAnalysisDescriptionInsole.text = gaitResultDescriptionString
            when(result.overallGaitPattern){
                GaitPatternType.IN_TOEING -> {
                    binding.tvGaitResultInsole.text = "안짱걸음"
                    binding.ivGaitInsole.setImageResource(R.drawable.img_in_toeing)
                    binding.ivFootstrikeInsole.visibility = View.VISIBLE
                }
                GaitPatternType.OUT_TOEING->{
                    binding.tvGaitResultInsole.text = "팔짜걸음"
                    binding.ivGaitInsole.setImageResource(R.drawable.img_out_toeing)
                    binding.ivFootstrikeInsole.visibility = View.VISIBLE
                }GaitPatternType.NEUTRAL->{
                    binding.tvGaitResultInsole.text = "정상걸음"
                    binding.ivGaitInsole.setImageResource(R.drawable.img_neutral_toeing)
                    binding.ivFootstrikeInsole.visibility = View.VISIBLE
                }else->{
                    binding.tvGaitResultInsole.text = "알수 없음"
                    binding.ivFootstrikeInsole.visibility = View.GONE

                }
            }

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


    private fun getGaitPatternString(gaitResultDescription:Float, pattern:GaitPatternType): CharSequence {
        return when (pattern) {
            GaitPatternType.IN_TOEING -> TextUtils.concat(String.format("현제 발의 각도는 %.1f으로 ", gaitResultDescription)+"안짱걸음으로 진단되었습니다.\n",getText(R.string.in_toeing_description))
            GaitPatternType.OUT_TOEING -> TextUtils.concat(String.format("현제 발의 각도는 %.1f으로 ", gaitResultDescription)+"팔자걸음으로 진단되었습니다.\n",getText(R.string.out_toeing_description))
            GaitPatternType.NEUTRAL -> String.format("현제 발의 각도는 %.1f으로 ", gaitResultDescription)+"정상걸음으로 진단되었습니다.\n"
            GaitPatternType.UNKNOWN -> "알 수 없음"
        }
    }

    private fun getGaitPatternString(result: GaitAnalysisResult): CharSequence {
        val dominantPattern = result.overallGaitPattern
        val patternDistribution = result.gaitPatternDistribution

        // GaitPatternType.UNKNOWN을 제외한, 실제 진단된 패턴들의 총 스텝 수
        val totalDiagnosedSteps = patternDistribution
            .filterKeys { it != GaitPatternType.UNKNOWN }
            .values
            .sum()

        // 디버깅용 로그 (필요시 사용)
        // Timber.d("Dominant Pattern: $dominantPattern")
        // Timber.d("Pattern Distribution: $patternDistribution")
        // Timber.d("Total Diagnosed Steps: $totalDiagnosedSteps")

        if (dominantPattern == GaitPatternType.UNKNOWN || totalDiagnosedSteps == 0) {
            // 아직 분석할 데이터가 충분하지 않거나, 모든 스텝이 UNKNOWN으로 판단된 경우
            val totalSteps = result.totalLeftSteps + result.totalRightSteps
            return if (totalSteps < 10) { // 예: 최소 10걸음 미만일 경우
                "걸음걸이 분석을 위해 좀 더 걸어주세요."
            } else {
                "걸음걸이 패턴을 분석 중이거나, 판단 가능한 데이터가 부족합니다."
            }
        }

        val dominantPatternCount = patternDistribution[dominantPattern] ?: 0
        val percentage = if (totalDiagnosedSteps > 0) {
            (dominantPatternCount.toDouble() / totalDiagnosedSteps * 100.0)
        } else {
            0.0 // 이 경우는 위의 if문에서 대부분 걸러지지만, 방어적 코딩
        }

        val patternNameString = when (dominantPattern) {
            GaitPatternType.IN_TOEING -> "안짱걸음"
            GaitPatternType.OUT_TOEING -> "팔자걸음"
            GaitPatternType.NEUTRAL -> "정상걸음"
            GaitPatternType.UNKNOWN -> "알 수 없음" // 이 케이스는 위의 if문에서 처리됨
        }

        // "진단 걸음 중 N.N%로 OOO걸음의 비중이 가장 높은 것으로 진단되었습니다." 형식
        val mainMessage = String.format(
            "진단된 걸음 중 %.1f%%로 %s의 비중이 가장 높습니다.",
            percentage,
            patternNameString
        )

        // 각 패턴에 대한 상세 설명 리소스 ID
        val detailedDescriptionResId = when (dominantPattern) {
            GaitPatternType.IN_TOEING -> R.string.in_toeing_description // string.xml에 정의 필요
            GaitPatternType.OUT_TOEING -> R.string.out_toeing_description // string.xml에 정의 필요
            else -> null // UNKNOWN 또는 기타 경우는 상세 설명 없음
        }

        return if (detailedDescriptionResId != null) {
            // 기본 메시지와 상세 설명을 합쳐서 반환
            TextUtils.concat(mainMessage, "\n\n", getText(detailedDescriptionResId))
        } else {
            // 기본 메시지만 반환
            mainMessage
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
        val fairingButton =  dialogView.findViewById<MaterialButton>(R.id.btn_fairing_dialog)
        val cancelButton =  dialogView.findViewById<MaterialButton>(R.id.btn_cancel_dialog)



        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1)
        listView.adapter = adapter

        // 초기 목록 설정
        updateDialogListAdapter(adapter, viewModel.scannedDevices.value)
        updateDialogSelectionText(selectedLeftTextView, selectedRightTextView)

        val jobScannedDevices = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scannedDevices.collect { devices -> updateDialogListAdapter(adapter, devices) }
        }
        val jobSelectedLeft = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedLeftInsole.collect { updateDialogSelectionText(selectedLeftTextView, selectedRightTextView) }
        }
        val jobSelectedRight = viewLifecycleOwner.lifecycleScope.launch {
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
            .setView(dialogView) // 커스텀 뷰 설정
            // .setTitle("스마트 인솔 선택") // 커스텀 레이아웃에 제목이 있다면 제거
            .setCancelable(true) // 백 버튼 등으로 닫을 수 있게 할지 여부 (선택)

        deviceListDialog = builder.create()

        // 커스텀 레이아웃의 '페어링' 버튼 클릭 리스너 설정
        fairingButton.setOnClickListener {
            viewModel.pairSelectedDevices()
            deviceListDialog?.dismiss() // Dialog 닫기
        }

        // 커스텀 레이아웃의 '취소' 버튼 클릭 리스너 설정
        cancelButton.setOnClickListener {
            viewModel.stopScan()
            viewModel.clearSelection()
            deviceListDialog?.dismiss() // Dialog 닫기
        }

        // '페어링' 버튼 활성화 상태를 ViewModel의 isPairingReady 상태와 동기화
        val jobPairingButtonState = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isPairingReady.collect { isReady ->
                fairingButton.isEnabled = isReady
            }
        }

        // 다이얼로그가 닫힐 때 처리 (리소스 정리 등)
        deviceListDialog?.setOnDismissListener {
            Timber.d("Device selection dialog dismissed")
            if (viewModel.scanState.value) { // 아직 스캔 중이었다면 중지
                viewModel.stopScan()
            }

            jobScannedDevices.cancel()
            jobSelectedLeft.cancel()
            jobSelectedRight.cancel()
            jobPairingButtonState.cancel() // 페어링 버튼 상태 업데이트 job도 취소
            deviceListDialog = null // 참조 제거
        }

        // 다이얼로그 배경을 투명하게 하여 커스텀 레이아웃의 배경만 보이도록 (선택 사항)
        deviceListDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

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
        if (diagnosisProgressDialog != null && diagnosisProgressDialog!!.isShowing) {
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_diagnosis_progress, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_progress_title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_progress_message)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.btn_cancel_progress_dialog)

        val cvLoadingDiagnosis = dialogView.findViewById<CardView>(R.id.cv_loading_diagnosis) // CardView
        val ivManagerLoadingDiagnosis = dialogView.findViewById<ImageView>(R.id.iv_manager_loading_diagnosis) // 배경 이미지
        val ivGifTonieDiagnosis = dialogView.findViewById<ImageView>(R.id.iv_gif_tonie_diagnosis) // Tonie GIF

        messageTextView.text = "10초 동안 데이터를 측정합니다..."

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        diagnosisProgressDialog = builder.create()


        diagnosisProgressDialog?.setOnShowListener {

            dialogPanningValueAnimator?.cancel()
            dialogPanningValueAnimator = setupPanningAnimationForItem(
                ivManagerLoadingDiagnosis,
                R.drawable.loading_forest_bg, // 사용할 배경 이미지 리소스
                cvLoadingDiagnosis,           // 애니메이션 기준이 될 CardView
                requireContext()
            )
            dialogPanningValueAnimator?.start()

            // Tonie 프레임 애니메이션 설정 및 시작
            setupFrameAnimationForItem(
                ivGifTonieDiagnosis,
                R.drawable.tonie_animation // Tonie 애니메이션 리소스 (animation-list drawable)
            )
        }

        cancelButton.setOnClickListener {
            viewModel.stopRealTimeAnalysis("사용자 취소", saveResult = false)
            diagnosisProgressDialog?.dismiss()
        }

        // 다이얼로그가 닫힐 때 애니메이션 리소스 정리
        diagnosisProgressDialog?.setOnDismissListener {
            dialogPanningValueAnimator?.cancel()
            dialogPanningValueAnimator = null // 참조 해제

            val tonieAnimation = ivGifTonieDiagnosis.background as? AnimationDrawable
            tonieAnimation?.stop()

        }


        if (diagnosisProgressDialog?.isShowing == false) {
            diagnosisProgressDialog?.show()
        }
    }

    private fun setupPanningAnimationForItem(
        imageView: ImageView,
        imageResId: Int,
        containerView: View, // 애니메이션의 기준이 될 뷰 (여기서는 CardView)
        context: Context
    ): ValueAnimator {
        val imageDrawable = AppCompatResources.getDrawable(context, imageResId)
            ?: throw IllegalArgumentException("Drawable $imageResId not found")

        imageView.setImageDrawable(imageDrawable)
        imageView.scaleType = ImageView.ScaleType.MATRIX

        val imageIntrinsicWidth = imageDrawable.intrinsicWidth.toFloat()
        val imageIntrinsicHeight = imageDrawable.intrinsicHeight.toFloat()

        // ValueAnimator는 containerView의 크기가 확정된 후에 설정 및 시작되어야 합니다.
        // 이 함수는 ValueAnimator를 생성하여 반환하고, 시작은 외부에서 (ViewTreeObserver나 onShowListener 등)
        val animator = ValueAnimator.ofFloat(0f, 1f).apply { // 0f to 1f 로 한 사이클 표현
            duration = 10000 // 10초 동안 한 방향으로 이동
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART // RESTART 또는 REVERSE 선택 가능
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                if (containerView.width == 0 || containerView.height == 0) {
                    return@addUpdateListener // 아직 뷰 크기 확정 안됨
                }

                val matrix = Matrix()
                val animatedValue = animation.animatedValue as Float // 0.0f ~ 1.0f

                // 이미지 스케일 계산 (이미지의 높이를 컨테이너 뷰의 높이에 맞춤)
                val viewHeight = containerView.height.toFloat()
                val viewWidth = containerView.width.toFloat()

                val scale = viewHeight / imageIntrinsicHeight
                matrix.postScale(scale, scale)

                // 스케일링된 이미지 너비
                val scaledImageWidth = imageIntrinsicWidth * scale

                // 이미지가 뷰보다 작으면 애니메이션 의미가 없을 수 있으므로, 최소 뷰 너비만큼은 이동하도록 처리
                // 또는 이미지가 뷰보다 작으면 패닝을 안 하거나 다른 효과를 줄 수 있습니다.
                // 여기서는 이미지가 뷰보다 크다고 가정하고, 초과분만큼 이동합니다.
                val maxTranslateX = scaledImageWidth - viewWidth
                var currentTranslateX = 0f

                if (maxTranslateX > 0) {
                    currentTranslateX = -animatedValue * maxTranslateX // 왼쪽으로 이동
                }
                // 만약 이미지가 뷰 너비보다 작다면, currentTranslateX는 0 또는 음수가 될 수 있습니다.
                // 이 경우 중앙 정렬 등의 추가 로직이 필요할 수 있습니다.
                // 예: val startX = (viewWidth - scaledImageWidth) / 2f (이미지가 뷰보다 작을 때 중앙 정렬)
                // matrix.postTranslate(startX + currentTranslateX, 0f)

                matrix.postTranslate(currentTranslateX, 0f)
                imageView.imageMatrix = matrix
            }
        }
        return animator
    }

    // 범용 프레임 애니메이션 (AnimationDrawable) 설정 함수
    private fun setupFrameAnimationForItem(imageView: ImageView, animationResId: Int) {
        imageView.setBackgroundResource(animationResId) // XML에서 src 대신 background로 설정하는 것이 일반적
        val animationDrawable = imageView.background as? AnimationDrawable
        animationDrawable?.start()
    }


    private fun showCalibrationInstructionDialog() {
        if (calibrationInstructionDialog != null && calibrationInstructionDialog!!.isShowing) {
            return
        }
        // 이전 다이얼로그가 있다면 먼저 dismiss (혹시 모를 중복 방지)
        calibrationInstructionDialog?.dismiss()


        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_calibration, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_calibration_title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_calibration_message)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.btn_cancel_calibration_dialog)

        val calibrationDurationSeconds = viewModel.calibrationDurationSeconds

        // 필요시 텍스트 설정
        // titleTextView.text = "캘리브레이션"
        messageTextView.text = "정확한 분석을 위해 발을 정면으로 향하고 ${calibrationDurationSeconds}초 동안 잠시 기다려주세요."

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false) // 이 다이얼로그도 코드 또는 특정 조건에서만 닫히도록

        calibrationInstructionDialog = builder.create()

        cancelButton.setOnClickListener {
            viewModel.stopRealTimeAnalysis(saveResult = false)
            showToast("캘리브레이션이 취소되었습니다.")
            calibrationInstructionDialog?.dismiss()
        }

        calibrationInstructionDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (!calibrationInstructionDialog!!.isShowing) {
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