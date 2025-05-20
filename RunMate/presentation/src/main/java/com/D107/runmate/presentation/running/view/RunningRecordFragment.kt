package com.D107.runmate.presentation.running.view

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.D107.runmate.domain.model.running.CadenceRecordState
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.presentation.MainActivity
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.RunningTrackingService
import com.D107.runmate.presentation.databinding.FragmentRunningRecordBinding
import com.D107.runmate.presentation.utils.CommonUtils.getActivityContext
import com.D107.runmate.presentation.utils.LocationUtils
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

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
                    binding.tvAvgPace.text =
                        LocationUtils.getPaceFromSpeed(state.runningRecords.last().avgSpeed)
                    binding.tvCurrentPace.text =
                        LocationUtils.getPaceFromSpeed(state.runningRecords.last().currentSpeed)
                    binding.tvCadence.text = state.runningRecords.last().cadence.toString()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.trackingStatus.collect { status ->
                    when (mainViewModel.trackingStatus.value) {
                        TrackingStatus.STOPPED -> {
                            findNavController().navigate(R.id.action_runningRecordFragment_to_runningEndFragment)
                        }
                        TrackingStatus.RUNNING -> {
                            Timber.d("onResume: TrackingStatus.RUNNING")
                            binding.groupBtnPause.visibility = View.GONE
                            binding.groupBtnRunning.visibility = View.VISIBLE
                            mContext?.let {
                                (getActivityContext(it) as MainActivity).hideHamburgerBtn()
                            }
                        }

                        TrackingStatus.INITIAL -> {
                            Timber.d("onResume: TrackingStatus.INITIAL")
                            binding.groupBtnPause.visibility = View.GONE
                            binding.groupBtnRunning.visibility = View.GONE
                            mContext?.let {
                                (getActivityContext(it) as MainActivity).showHamburgerBtn()
                            }
                        }

                        TrackingStatus.PAUSED -> {
                            Timber.d("onResume: TrackingStatus.PAUSED")
                            binding.groupBtnPause.visibility = View.VISIBLE
                            binding.groupBtnRunning.visibility = View.GONE
                            mContext?.let {
                                (getActivityContext(it) as MainActivity).hideHamburgerBtn()
                            }
                        }
                    }
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
//                findNavController().navigate(R.id.action_runningRecordFragment_to_runningEndFragment)
            }
        }

        binding.btnVibrate.setOnClickListener {
            if(mainViewModel.isVibration.value) {
                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_off)
            } else {
                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_on)
            }
//            mainViewModel.toggleVibrationEnabled()
        }

        binding.btnSound.setOnClickListener {
            if(mainViewModel.isSound.value) {
                binding.btnSound.setImageResource(R.drawable.ic_running_btn_sound_off)
            } else {
                binding.btnSound.setImageResource(R.drawable.ic_running_btn_sound_on)
            }
//            mainViewModel.toggleSoundEnabled()
        }
    }
}