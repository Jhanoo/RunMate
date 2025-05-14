package com.D107.runmate.presentation.group.viewmodel

sealed class GroupUiEvent {
    object CreationSuccess : GroupUiEvent()
    object GoToGroupInfo : GroupUiEvent()
    object GoToGroup: GroupUiEvent()
    object GoToGroupRunning: GroupUiEvent()
    data class ToggleGroupFragmentVisible(val visible: Boolean):GroupUiEvent()
    data class ShowToast(val message: String) : GroupUiEvent()
}