import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.data.remote.response.group.GroupMemberResponse
import com.D107.runmate.data.remote.response.group.GroupMemberResponse.Companion.toDomainModel
import com.D107.runmate.domain.model.group.GroupData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime

@JsonClass(generateAdapter = true)
data class GroupResponse(
    @Json(name = "groupId")
    val groupId: String,
    @Json(name = "groupName")
    val groupName: String,
    @Json(name = "leaderId")
    val leaderId: String,
    @Json(name = "courseName")
    val courseName: String?,
    @Json(name = "courseId")
    val courseId: String?,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "startLocation")
    val startLocation: String,
    @Json(name = "latitude")
    val latitude: Double,
    @Json(name = "longitude")
    val longitude: Double,
    @Json(name = "inviteCode")
    val inviteCode: String,
    @Json(name = "status")
    val status: Int,
    @Json(name = "members")
    val members: List<GroupMemberResponse>?
):BaseResponse{
    companion object : DataMapper<GroupResponse, GroupData> {
            override fun GroupResponse.toDomainModel(): GroupData {
            return GroupData(
                groupId = groupId,
                groupName = groupName,
                leaderId = leaderId,
                courseId = courseId,
                courseName = courseName,
                startTime = startTime,
                startLocation = startLocation,
                latitude = latitude,
                longitude = longitude,
                inviteCode = inviteCode,
                status = status,
                members = members?.map { it.toDomainModel() }?: emptyList()
            )

        }

    }
}