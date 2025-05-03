package com.github.ttt374.healthcaretracer.data.repository

import android.content.Context
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.item.TargetVitals
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.DayPeriodConfig
import com.github.ttt374.healthcaretracer.ui.settings.TargetVitalsType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Singleton

@Serializable
data class Config (
    val bloodPressureGuideline: BloodPressureGuideline = BloodPressureGuideline.Default,
    val dayPeriodConfig: DayPeriodConfig = DayPeriodConfig(),
    val targetVitals: TargetVitals = TargetVitals(),
    @Serializable(with = ZoneIdSerializer::class)
    val zoneId: ZoneId = ZoneId.systemDefault(),
){
    fun updateBloodPressureGuidelineByName(name: String): Config {
        return copy(bloodPressureGuideline = BloodPressureGuideline.entries.find { it.name == name } ?: BloodPressureGuideline.Default)
    }
    fun updateTargetVital(type: TargetVitalsType, input: String): Config {
        val updatedVitals = type.updateTargetVitals(this.targetVitals, input)
        return copy(targetVitals = updatedVitals)
    }
    fun updateDayPeriod(dayPeriod: DayPeriod, time: LocalTime): Config {
        return copy(dayPeriodConfig = dayPeriodConfig.update(dayPeriod, time))
    }
    fun updateTimeZone(input: String): Config {
        val zoneId = runCatching { ZoneId.of(input) }.getOrNull()
        return if (zoneId != null) copy(zoneId = zoneId) else this
    }
}

@Serializable
object LocalTimeSerializer : KSerializer<LocalTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME // 例: "08:30:00"

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.format(formatter))
    }
    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString(), formatter)
    }
}

@Serializable
object ZoneIdSerializer : KSerializer<ZoneId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ZoneId", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ZoneId) {
        encoder.encodeString(value.id)
    }
    override fun deserialize(decoder: Decoder): ZoneId {
        return ZoneId.of(decoder.decodeString())
    }
}
//////////////////////////////////////////////////////////////
/**
 *  [ConfigRepository]
 *
 *  Config - 設定を管理する。データを Config にまとめて serialize して　DataStore に入れている。
 *  datastore の interface に移譲しているが、ConfigHandler でラップしたのを使った方がよい。
 */
@Singleton
class ConfigRepository(context: Context) : DataStoreRepository<Config> by DataStoreRepositoryImpl (
    context = context,
    fileName = "config5", // AppConst.DataStoreFilename.CONFIG.filename,
    serializer = GenericSerializer(serializer = Config.serializer(), default = Config())
)

//data class LocalTimeRange(
//    val start: LocalTime = LocalTime.of(0, 0),
//    val endInclusive: LocalTime = LocalTime.of(23, 59)
//) {
//    operator fun contains(time: LocalTime): Boolean {
//        return if (start <= endInclusive) {
//            time in start..endInclusive  // 普通の範囲
//        } else {
//            time >= start || time <= endInclusive  // 0時をまたぐ場合（例: 22:00〜02:00）
//        }
//    }
//    override fun toString(): String = "$start..$endInclusive"
////    fun toClosedRange(): ClosedRange<LocalTime> = object : ClosedRange<LocalTime> {
////        override val start = this@LocalTimeRange.start
////        override val endInclusive = this@LocalTimeRange.endInclusive
////    }
//}