package uk.chriscormack.netkernel.lighting.desk.model.fixture

interface FixtureSettingValue {
    val name: String
}

@FixtureProperty("Setting")
interface FixtureSetting<T : FixtureSettingValue> {
    var setting: T

    fun setValue(valueName: String)
}

@FixtureProperty("Settings")
interface FixtureWithSettings {
    val settings: Map<String, FixtureSetting<*>>

    fun setSetting(settingName: String, valueName: String)
}
