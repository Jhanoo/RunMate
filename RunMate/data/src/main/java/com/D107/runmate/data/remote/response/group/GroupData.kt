import com.D107.runmate.data.remote.response.group.GroupMemberData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime

@JsonClass(generateAdapter = true)
data class GroupData(
    @Json(name = "groupId")
    val groupId: String,
    @Json(name = "groupName")
    val groupName: String,
    @Json(name = "leaderId")
    val leaderId: String,
    @Json(name = "courseId")
    val courseId: String,
    @Json(name = "startTime")
    val startTime: OffsetDateTime,
    @Json(name = "startLocation")
    val startLocation: String,
    @Json(name = "latitude")
    val latitude: Double,
    @Json(name = "longitude")
    val longitude: Double,
    @Json(name = "inviteCode")
    val inviteCode: String,
    @Json(name = "members")
    val members: List<GroupMemberData>
)