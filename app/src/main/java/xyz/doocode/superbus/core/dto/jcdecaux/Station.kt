package xyz.doocode.superbus.core.dto.jcdecaux

data class Station(
    val number: Int,
    val name: String,
    val address: String,
    val position: Position,
    val banking: Boolean,
    val bonus: Boolean,
    val status: String,
    val connected: Boolean,
    var overflow: Boolean,
    val contractName: String,
    val lastUpdate: Long,
    val totalStands: Stands,
    val mainStands: Stands
)

data class Position(val latitude: Double, val longitude: Double)

data class Stands(val availabilities: Availabilities, val capacity: Int)

data class Availabilities(
    val bikes: Int,
    val stands: Int,
    val mechanicalBikes: Int,
    val electricalBikes: Int,
    val electricalInternalBatteryBikes: Int,
    val electricalRemovableBatteryBikes: Int
)
