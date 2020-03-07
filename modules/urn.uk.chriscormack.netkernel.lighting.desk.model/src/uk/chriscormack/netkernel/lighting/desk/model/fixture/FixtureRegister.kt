package uk.chriscormack.netkernel.lighting.desk.model.fixture

class FixtureRegister(val context: FixtureContext) {
    val fixtureList = HashMap<String, Fixture>()

    fun register(fixture: Fixture) {
        fixtureList[fixture.key] = fixture
    }
}
