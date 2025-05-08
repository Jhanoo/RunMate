package com.D107.runmate.presentation.running

import CadenceTracker
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.presentation.MainActivity
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.RunningTrackingService
import com.D107.runmate.presentation.databinding.FragmentRunningRecordBinding
import com.D107.runmate.presentation.utils.CommonUtils.getActivityContext
import com.D107.runmate.presentation.utils.LocationUtils.getPaceFromSpeed
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "RunningRecordFragment"
@AndroidEntryPoint
class RunningRecordFragment : BaseFragment<FragmentRunningRecordBinding>(
    FragmentRunningRecordBinding::bind,
    R.layout.fragment_running_record
) {
    private val mainViewModel: MainViewModel by activityViewModels()
    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.runningRecord.collectLatest { state ->
                if(state is RunningRecordState.Exist) {
                    binding.tvDistance.text = getString(R.string.running_distance, state.runningRecords.last().distance)
                    binding.tvAvgPace.text = getPaceFromSpeed(state.runningRecords.last().avgSpeed)
                    binding.tvCurrentPace.text = getPaceFromSpeed(state.runningRecords.last().currentSpeed)
                    binding.tvCadence.text = CadenceTracker.cadence.toString()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.time.collectLatest { it ->
                binding.tvTime.text = getString(R.string.running_time, it / 60, it % 60)
            }
        }

        binding.btnPause.setOnClickListener {
            binding.groupBtnPause.visibility = View.VISIBLE
            binding.groupBtnRunning.visibility = View.GONE
            mContext?.let {
                RunningTrackingService.pauseService(it)
            }
        }

        binding.btnRestart.setOnClickListener {
            binding.groupBtnPause.visibility = View.GONE
            binding.groupBtnRunning.visibility = View.VISIBLE
            mContext?.let {
                RunningTrackingService.startService(it)
            }
        }

        binding.btnEnd.setOnClickListener {
            mContext?.let {
                RunningTrackingService.stopService(it)
                findNavController().navigate(R.id.action_runningRecordFragment_to_runningEndFragment)
            }
        }

        binding.btnVibrate.setOnClickListener {
            if(mainViewModel.isVibrationEnabled.value) {
                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_off)
            } else {
                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_on)
            }
//            mainViewModel.toggleVibrationEnabled()
        }

        binding.btnSound.setOnClickListener {
            if(mainViewModel.isSoundEnabled.value) {
                binding.btnSound.setImageResource(R.drawable.ic_running_btn_sound_off)
            } else {
                binding.btnSound.setImageResource(R.drawable.ic_running_btn_sound_on)
            }
//            mainViewModel.toggleSoundEnabled()
        }
    }

    override fun onResume() {
        super.onResume()
        when (mainViewModel.trackingStatus.value) {
            TrackingStatus.STOPPED -> {
                // 종료
            }

            TrackingStatus.RUNNING -> {
                binding.groupBtnPause.visibility = View.GONE
                binding.groupBtnRunning.visibility = View.VISIBLE
            }

            TrackingStatus.INITIAL -> {
                binding.groupBtnPause.visibility = View.GONE
                binding.groupBtnRunning.visibility = View.GONE
            }

            TrackingStatus.PAUSED -> {
                binding.groupBtnPause.visibility = View.VISIBLE
                binding.groupBtnRunning.visibility = View.GONE
            }
        }
    }
}