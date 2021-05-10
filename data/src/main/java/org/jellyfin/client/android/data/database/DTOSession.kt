package org.jellyfin.client.android.data.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = "Session", foreignKeys = [ForeignKey(entity = DTOServer::class,
    parentColumns = arrayOf("ServerId"),
    childColumns = arrayOf("ServerId"),
    onDelete = ForeignKey.CASCADE)]
)
data class DTOSession(
    @PrimaryKey @ColumnInfo(name = "SessionId") val sessionId: Int,
    @ColumnInfo(name = "ServerId") val serverId: Int,
    @ColumnInfo(name = "UserName") val userName: String,
    @ColumnInfo(name = "UserUUID") val userUUID: String,
    @ColumnInfo(name = "APIKey") val apiKey: String
) : Parcelable