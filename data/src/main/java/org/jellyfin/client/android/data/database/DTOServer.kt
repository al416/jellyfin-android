package org.jellyfin.client.android.data.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = "Server")
data class DTOServer(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ServerId") val serverId: Int,
    @ColumnInfo(name = "Name") val serverName: String,
    @ColumnInfo(name = "URL") val serverUrl: String,
    @ColumnInfo(name = "DisplayOrder") val displayOrder: Int
) : Parcelable