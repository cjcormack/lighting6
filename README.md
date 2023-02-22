# Chris' Lighting Controller v6

## Building notes

* After rebuilding, kotlin-stdlib Jars need removing, `rm -rf modules/urn.uk.chriscormack.netkernel.lighting.*/build/urn.uk.chriscormack.netkernel.lighting.*/lib/kotlin-stdlib-*`
* Build needs custom build of NetKernel Gradle plugin from https://github.com/cjcormack/gradle-plugin and running a `gradle publishToMavenLocal `


## TODO

* Compile warnings should pass through, even when the compilation was successful

## Example Scripts

### config

```kotlin
log(LogLevel.INFO, "Starting config script")

runScript("config-load-hex-lights")
runScript("config-load-hue-lights")

log(LogLevel.INFO, "Finished config script")
```

### config-load-hex-lights

```kotlin
log(LogLevel.INFO, "Loading Hex lights")

with (fixtureRegister) {
    class HexFixture(key: String, fixtureName: String, firstChannel: Int, position: Int): Fixture(key, fixtureName, position),
            FixtureWithDimmer by DmxFixtureWithDimmer(context, firstChannel),
            FixtureWithColour by DmxFixtureWithColour(context, firstChannel + 1, firstChannel + 2, firstChannel + 3, firstChannel + 4, firstChannel + 5, firstChannel + 6)
    
    register(HexFixture("hex1", "Hex 1", 1, 0))
    register(HexFixture("hex2", "Hex 2", 17, 1))
    register(HexFixture("hex3", "Hex 3", 33, 2))
}
```

### config-load-hue-lights

```kotlin
val hueIp = "192.168.1.115"
val username = "zLeV14QUDZuvFjVEHj1A957q6nFjQzt8nHzMm090"
fixtureRegister.context.setHueConfig(hueIp, username)

log(LogLevel.INFO, "Loading Hue lights")

inner class ColourHueFixture(key: String, fixtureName: String, lightId: Int, position: Int): Fixture(key, fixtureName, position),
        FixtureWithColour by HueFixtureWithColour(fixtureRegister.context, lightId) {

}

fun lookupLightId(groupName: String, lightName: String): Int {
    return source("active:hue-findLight") {
        argumentByValue("hueIp", hueIp)
        argumentByValue("username", username)
        argumentByValue("groupName", groupName)
        argumentByValue("lightName", lightName)
    }
}
    
fixtureRegister.register(ColourHueFixture("hue-living-room-1", "Living Room Front", lookupLightId("Living Room", "Front"), 0))
fixtureRegister.register(ColourHueFixture("hue-living-room-2", "Living Room Back", lookupLightId("Living Room", "Back"), 1))
```

### track-changed

```kotlin
import kotlin.random.Random

log(LogLevel.INFO, "Handling track changed")
 
val colorPairs = listOf(
    Pair(Color.RED, Color.RED),
    Pair(Color.GREEN, Color.GREEN),
    Pair(Color.BLUE, Color.BLUE),
    
    Pair(Color.RED, Color.GREEN),
    Pair(Color.RED, Color.BLUE),
    
    Pair(Color.GREEN, Color.RED),
    Pair(Color.GREEN, Color.BLUE),
    
    Pair(Color.BLUE, Color.RED),
    Pair(Color.BLUE, Color.GREEN),
    
    Pair(Color.GREEN, Color.MAGENTA),
    Pair(Color.MAGENTA, Color.GREEN),
    
    Pair(Color.MAGENTA, Color.CYAN),
    Pair(Color.CYAN, Color.MAGENTA)
)

val (colorOne, colorTwo) = colorPairs.random()

val setUv = Random.nextInt(0,5) == 0

fixtureRegister.fixtureList.forEach { (key, fixture) ->
    if (fixture is FixtureWithDimmer) {
        fixture.level = 255u
    }
    
    if (fixture is FixtureWithColour) {
        if (fixture.position % 2 == 0) {
            fixture.fadeToColour(colorOne, 1000)
        } else {
            fixture.fadeToColour(colorTwo, 1000)
        }
        
        if (fixture.uvSupport) {
            if (setUv) {
                fixture.uvLevel = 255u
            } else {
                fixture.uvLevel = 0u
            }
        }
    }
}
```
