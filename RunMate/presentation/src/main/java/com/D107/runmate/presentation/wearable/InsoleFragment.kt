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
import com.D107.runmate.domain.model.Insole.CombinedInsoleData
import com.D107.runmate.domain.model.Insole.InsoleConnectionState
import com.D107.runmate.domain.model.Insole.SmartInsole
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentInsoleBinding
import com.D107.runmate.presentation.wearable.viewmodel.InsoleViewModel
import com.google.android.material.snackbar.Snackbar
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class InsoleFragment : BaseFragment<FragmentInsoleBinding>(
    FragmentInsoleBinding::bind,
    R.layout.fragment_insole
) {

    private val viewModel: InsoleViewModel by viewModels()

    private var deviceListDialog: AlertDialog? = null


    // --- 권한 요청 관련 ---
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12 (API 31) 이상
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
            // Manifest.permission.ACCESS_FINE_LOCATION // 스캔 옵션에 따라 필요 없을 수 있음
        )
    } else {
        // Android 11 (API 30) 이하
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION // 필수
        )
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = true
            permissions.entries.forEach {
                if (!it.value) {
                    allGranted = false
                    Timber.w("Permission denied: ${it.key}")
                    // 사용자에게 권한 필요성 설명 또는 앱 종료 등 처리
                    showSnackbar("BLE 기능을 사용하려면 권한 승인이 필요합니다.")
                }
            }
            if (allGranted) {
                Timber.d("All permissions granted")
                // 권한 승인 후 스캔 시작 시도
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
                showSnackbar("블루투스를 활성화해야 인솔을 찾을 수 있습니다.")
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtonClickListeners()
        observeViewModel()
    }

    private fun setupButtonClickListeners() {
        binding.buttonPair.setOnClickListener {
            Timber.d("click")
            // 페어링 버튼 클릭 시
            // 1. 현재 연결 상태 확인 -> 연결되어 있으면 연결 해제? 아니면 무시? -> 여기선 해제 후 재시도
            if (viewModel.connectionState.value != InsoleConnectionState.DISCONNECTED &&
                viewModel.connectionState.value != InsoleConnectionState.FAILED) {
                Timber.d("showDisconnectDialog")
                showDisconnectDialog()
            } else {
                // 2. 권한 확인 및 스캔 시작
                Timber.d("권한 확인")
                checkPermissionsAndStartScan()
            }
        }
    }

    private fun checkPermissionsAndStartScan() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            // 모든 권한이 있으면 블루투스 상태 확인 및 스캔 시작
            checkBluetoothAndStartScan()
        } else {
            // 권한 요청
            Timber.d("Requesting permissions: ${missingPermissions.joinToString()}")
            requestMultiplePermissions.launch(missingPermissions.toTypedArray())
        }
    }

    private fun checkBluetoothAndStartScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            showSnackbar("이 기기는 블루투스를 지원하지 않습니다.")
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

    private fun startActualScan() {
        Timber.d("권한 및 블루투스 확인 완료, 스캔 시작")
        // 스캔 전 이전 선택 초기화
        viewModel.clearSelection()
        // 스캔 시작
        viewModel.startScan()
        // 스캔 상태를 관찰하여 Dialog 표시 (observeViewModel 에서 처리)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 스캔 상태 관찰 (Dialog 표시/숨김, ProgressBar)
                launch {
                    viewModel.scanState.collect { isScanning ->
                        binding.progressBar.visibility = if (isScanning) View.VISIBLE else View.GONE
                        binding.buttonPair.isEnabled = !isScanning // 스캔 중 버튼 비활성화
                        if (isScanning && (deviceListDialog == null || !deviceListDialog!!.isShowing)) {
                            showDeviceSelectionDialog() // 스캔 시작 시 Dialog 표시
                        } else if (!isScanning && deviceListDialog != null && deviceListDialog!!.isShowing) {
                            // 스캔이 중지되었는데 Dialog 가 열려있으면 닫기 (예: 타임아웃)
                            deviceListDialog?.dismiss()
                        }
                    }
                }

                // 스캔된 기기 목록 관찰 (Dialog 업데이트)
                launch {
                    viewModel.scannedDevices.collect { devices ->
                        // Dialog 가 열려있을 때만 목록 업데이트
                        if (deviceListDialog != null && deviceListDialog!!.isShowing) {
                            updateDeviceListDialog(devices)
                        }
                    }
                }

                // 선택된 인솔 관찰 (UI 표시)
                launch {
                    viewModel.selectedLeftInsole.collect { device ->
                        binding.textViewSelectedLeft.text = if (device != null) {
                            "선택된 왼쪽: ${device.name ?: "Unknown"} (${device.address})"
                        } else {
                            "선택된 왼쪽: 없음"
                        }
                    }
                }
                launch {
                    viewModel.selectedRightInsole.collect { device ->
                        binding.textViewSelectedRight.text = if (device != null) {
                            "선택된 오른쪽: ${device.name ?: "Unknown"} (${device.address})"
                        } else {
                            "선택된 오른쪽: 없음"
                        }
                    }
                }

                // 연결 상태 관찰
                launch {
                    viewModel.connectionState.collect { state ->
                        updateConnectionStatusUI(state)
                        // 연결 완료/실패/끊김 시 Dialog 닫기
                        if (state == InsoleConnectionState.FULLY_CONNECTED ||
                            state == InsoleConnectionState.FAILED ||
                            state == InsoleConnectionState.DISCONNECTED) {
                            deviceListDialog?.dismiss()
                        }
                    }
                }

                // 결합된 데이터 관찰
                launch {
                    viewModel.combinedData.collect { data ->
                        updateInsoleDataUI(data)
                    }
                }

                // 오류 이벤트 관찰
                launch {
                    viewModel.errorEvent.collect { message ->
                        showSnackbar(message)
                    }
                }
            }
        }
    }

    private fun showDeviceSelectionDialog() {
        if (deviceListDialog != null && deviceListDialog!!.isShowing) {
            // 이미 열려있으면 업데이트만 시도 (선택 사항)
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

        // --- LiveData/StateFlow 구독 (Dialog 내에서만 유효하도록) ---
        // Dialog 보여지는 동안 ViewModel 상태 변경 시 Dialog UI 업데이트
        val job = viewLifecycleOwner.lifecycleScope.launch {
            // Dialog 가 떠있는 동안에만 collect
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
                // 선택 후 Dialog UI 업데이트 (위 collect 에서 자동으로 됨)
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
                // Dialog 닫힐 때 스캔 중지 및 선택 초기화 (취소 버튼과 동일 효과)
                // 사용자가 페어링 눌러도 dismiss되므로, 스캔 중지는 필요 시 ViewModel 내부에서 처리
                if (viewModel.scanState.value) { // 아직 스캔 중이었다면 중지
                    viewModel.stopScan()
                }
                viewModel.clearSelection() // Dialog 닫히면 선택 초기화
                // Dialog 내에서 실행되던 collect job 취소
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
                // 기존 dismiss 리스너 호출 (중요)
                builder.create().dismiss() // 이렇게 하면 안됨, 기존 리스너 참조 필요
                // --> setOnDismissListener는 하나만 가능하므로 로직 통합 필요
                // 통합된 Dismiss 리스너에서 jobPairing.cancel() 호출
            }

            // --- Dismiss 리스너 통합 ---
            dialogInterface.setOnDismissListener {
                Timber.d("Device selection dialog dismissed (Integrated Listener)")
                if (viewModel.scanState.value) {
                    viewModel.stopScan()
                }
                viewModel.clearSelection()
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

    // 기존 Dialog 업데이트 (내용만)
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
            .setTitle("연결 해제")
            .setMessage("이미 인솔이 연결되어 있습니다. 연결을 해제하시겠습니까?")
            .setPositiveButton("해제") { _, _ ->
                viewModel.disconnect()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateConnectionStatusUI(state: InsoleConnectionState) {
        binding.textViewConnectionStatus.text = when (state) {
            InsoleConnectionState.DISCONNECTED -> "연결 상태: 연결 안됨"
            InsoleConnectionState.CONNECTING -> "연결 상태: 연결 중..."
            InsoleConnectionState.PARTIALLY_CONNECTED -> "연결 상태: 한쪽만 연결됨"
            InsoleConnectionState.FULLY_CONNECTED -> "연결 상태: 양쪽 인솔 연결됨"
            InsoleConnectionState.FAILED -> "연결 상태: 연결 실패"
        }
        // 연결 상태에 따라 페어링 버튼 텍스트 변경 등 추가 UI 로직 가능
        binding.buttonPair.text = if (state == InsoleConnectionState.FULLY_CONNECTED || state == InsoleConnectionState.PARTIALLY_CONNECTED) {
            "연결 해제 / 재시도"
        } else {
            "스마트 인솔 페어링"
        }
    }

    private fun updateInsoleDataUI(data: CombinedInsoleData?) {
        if (data == null) {
            // 데이터 없을 때 기본값 표시
            binding.textViewLeftFsr.text = "FSR: N/A"
            binding.textViewLeftYpr.text = "YPR: N/A"
            binding.textViewRightFsr.text = "FSR: N/A"
            binding.textViewRightYpr.text = "YPR: N/A"
            return
        }

        // 왼쪽 데이터 업데이트
        data.left?.let {
            binding.textViewLeftFsr.text = "FSR: ${it.bigToe}, ${it.smallToe}, ${it.heel}, ${it.archLeft}, ${it.archRight}"
            binding.textViewLeftYpr.text = String.format(Locale.US, "YPR: %.1f, %.1f, %.1f", it.yaw, it.pitch, it.roll)
        } ?: run {
            binding.textViewLeftFsr.text = "FSR: N/A"
            binding.textViewLeftYpr.text = "YPR: N/A"
        }

        // 오른쪽 데이터 업데이트
        data.right?.let {
            binding.textViewRightFsr.text = "FSR: ${it.bigToe}, ${it.smallToe}, ${it.heel}, ${it.archLeft}, ${it.archRight}"
            binding.textViewRightYpr.text = String.format(Locale.US, "YPR: %.1f, %.1f, %.1f", it.yaw, it.pitch, it.roll)
        } ?: run {
            binding.textViewRightFsr.text = "FSR: N/A"
            binding.textViewRightYpr.text = "YPR: N/A"
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deviceListDialog?.dismiss() // Fragment 소멸 시 Dialog 닫기
        deviceListDialog = null
    }


}